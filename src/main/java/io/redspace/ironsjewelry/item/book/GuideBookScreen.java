package io.redspace.ironsjewelry.item.book;

import com.mojang.blaze3d.platform.NativeImage;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.actions.*;
import io.redspace.ironsjewelry.core.bonuses.BonusType;
import io.redspace.ironsjewelry.core.data.AttributeInstance;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GuideBookScreen extends Screen {
    public static final ResourceLocation BOOK_LOCATION = IronsJewelry.id("textures/gui/double_book.png");

    private PageButton forwardButton;
    private PageButton backButton;
    private PageButton homeButton;

    static final int IMAGE_WIDTH = 256;
    static final int IMAGE_HEIGHT = 180;
    static final int TICKS_PER_ITEM = 40;
    static final int XM = 15;
    static final int YM = 15;
    protected int leftPos;
    protected int topPos;
    protected int ingredientIndex;

    public GuideBookScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(BOOK_LOCATION, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Holder<MaterialDefinition> materialHolder = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("ruby")));
        MaterialDefinition material = materialHolder.value();
        Component name = Component.translatable(material.descriptionId());
        Ingredient ingredient = material.ingredient();
        if (ingredient.isEmpty()) {
            return;
        }
        var items = ingredient.getItems();
        int tickCount = Minecraft.getInstance().player.tickCount;
        if (tickCount % TICKS_PER_ITEM == 0) {
            ingredientIndex = (ingredientIndex + 1) % items.length;
        }
        if (ingredientIndex >= items.length) {
            ingredientIndex = 0;
        }
        var poseStack = guiGraphics.pose();
//        poseStack.pushPose();
//        poseStack.mulPose(Axis.YP.rotationDegrees(tickCount + partialTick));
//        poseStack.translate(leftPos + XM, (topPos + YM), 100);
        ItemStack item = items[ingredientIndex];
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2, 2, 2);
        guiGraphics.renderItem(item, (leftPos + XM - 5) / 2, (topPos + YM / 2) / 2);
        guiGraphics.pose().popPose();
//        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.GUI, LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY, poseStack, guiGraphics.bufferSource(), Minecraft.getInstance().level, 0);
//        poseStack.popPose();
        var font = Minecraft.getInstance().font;
        float textScale = 2;
        int textcolor = getTextColor(material.paletteLocation());
        poseStack.pushPose();
        poseStack.scale(textScale, textScale, textScale);
        guiGraphics.drawString(font, name, (int) ((leftPos + XM + 32 - 5) / textScale), (int) ((topPos + YM) / textScale), textcolor, true);
        poseStack.popPose();

        Component quality = Component.translatable("tooltip.irons_jewelry.quality_multiplier", material.quality());
//        List<MutableComponent> bonusTypes = material.bonusParameters().keySet().stream().map(param -> Component.translatable(param.getDescriptionId())).toList();
//        List<String> bonusValues = material.bonusParameters().entrySet().stream().map(entry ->
//                (handleMaterialBonusDescription(entry.getKey(), entry.getValue()))).toList();
        List<MutableComponent> bonusTypes = new ArrayList<>();
        List<String> bonusValues = new ArrayList<>();
        for(BonusType bonus : IronsJewelryRegistries.BONUS_TYPE_REGISTRY){
            var param = bonus.getParameterType();
            if(!material.bonusParameters().containsKey(param)){
                continue;
            }
            bonusTypes.add(Component.translatable(bonus.getDescriptionId()));
            bonusValues.add(handleMaterialBonusDescription(param, material.bonusParameters().get(param)));
        }

        int ypos = (int) (topPos + YM + font.lineHeight * (1 + textScale));
        guiGraphics.drawString(font, quality, leftPos + XM, ypos, 0x0, false);
        ypos += font.lineHeight * 2;
        int maxWidth = 0;
        for (var t : bonusTypes) {
            var w = font.width(t);
            if (w > maxWidth) {
                maxWidth = w;
            }
        }
        for (int i = 0; i < bonusTypes.size(); i++) {
            var type = bonusTypes.get(i);
            var value = bonusValues.get(i);
            guiGraphics.drawString(font, type, leftPos + XM, ypos, 0x0, false);
            guiGraphics.drawString(font, value, leftPos + XM + maxWidth + 4, ypos, 0x0, false);
            ypos += font.lineHeight;
        }
    }

    public static String handleMaterialBonusDescription(IBonusParameterType<?> type, Object value) {
        //ATTRIBUTE_PARAMETER
        //POSITIVE_EFFECT_PARAMETER
        //NEGATIVE_EFFECT_PARAMETER
        //ACTION_PARAMETER
        if (type.equals(ParameterTypeRegistry.ACTION_PARAMETER.get())) {
            ActionParameter.ActionRunnable action = (ActionParameter.ActionRunnable) value;
            var resource = IronsJewelryRegistries.ACTION_REGISTRY.getKey(action.action().codec());
            return Component.translatable(String.format("action.%s.%s.name", resource.getNamespace(), resource.getPath())).getString() + handleExtraActionInformation(action);
        } else if (type.equals(ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get())) {
            return Component.translatable(((Holder<MobEffect>) value).value().getDescriptionId()).getString();
        } else if (type.equals(ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get())) {
            return Component.translatable(((Holder<MobEffect>) value).value().getDescriptionId()).getString();
        } else if (type.equals(ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get())) {
            var attribute = (AttributeInstance) value;
            return createAttributeModifierText(attribute.attribute(), new AttributeModifier(IronsJewelry.id("noop"), attribute.amount(), attribute.operation()));
        }
        return "";
    }

    public static String handleExtraActionInformation(ActionParameter.ActionRunnable action) {
        if (action.action() instanceof ApplyDamageAction damageAction) {
            var location = damageAction.damageType().getKey().location();
            var typeComponent = Component.translatable(String.format("damage_type.%s.%s", location.getNamespace(), location.getPath()));
            return String.format(" (%s %s Damage)", damageAction.getDamage(1), typeComponent.getString());
        } else if (action.action() instanceof ApplyEffectAction effectAction) {
            String[] amp = {"I", "II", "III", "IV", "V"};
            if (effectAction.effect().value().isInstantenous()) {
                return String.format(" (%s %s)", Component.translatable(effectAction.effect().value().getDescriptionId()).getString(), amp[(int) effectAction.amplifier().sample(1)]);
            } else {
                return String.format(" (%s %s, %s)", Component.translatable(effectAction.effect().value().getDescriptionId()).getString(), amp[(int) effectAction.amplifier().sample(1)], Utils.digitalTimeFromTicks((int) effectAction.duration().sample(1), true));
            }
        } else if (action.action() instanceof HealAction healAction) {
            return String.format(" (%s Base Healing)", healAction.amount().sample(1));
        } else if (action.action() instanceof CreateItemsAction itemsAction) {
            return String.format(" (%s)", itemsAction.formatTooltip(new BonusInstance(null, 1, null, null), false).getString());
        } else if (action.action() instanceof ExplodeAction explodeAction) {
            return String.format(" (%s)", explodeAction.formatTooltip(new BonusInstance(null, 1, null, null), false).getString().replace(" (", ", ").replace(")", ""));
        }
        return "";
    }

    /**
     * Adapted {@link ItemStack#addModifierTooltip}
     */
    public static String createAttributeModifierText(Holder<Attribute> attribute, AttributeModifier modifier) {
        double d0 = modifier.amount();
        double d1;
        if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            d1 = d0 * 100.0;
        } else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
            d1 = d0 * 10.0;
        } else {
            d1 = d0;
        }

        if (d0 >= 0.0) {
            return (
                    Component.translatable(
                                    "attribute.modifier.plus." + modifier.operation().id(),
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                                    Component.translatable(attribute.value().getDescriptionId())
                            )
                            .withStyle(attribute.value().getStyle(true))
            ).getString();
        } else {
            return (
                    Component.translatable(
                                    "attribute.modifier.take." + modifier.operation().id(),
                                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d1),
                                    Component.translatable(attribute.value().getDescriptionId())
                            )
                            .withStyle(attribute.value().getStyle(false))
            ).getString();
        }
    }

    private int getTextColor(ResourceLocation palette) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(palette));
            if (resource.isPresent()) {
                int[] aint;
                try (
                        InputStream inputstream = resource.get().open();
                        NativeImage nativeimage = NativeImage.read(inputstream);
                ) {
                    aint = nativeimage.getPixelsRGBA();
                }
                aint = Arrays.copyOf(aint, 5); // exclude brightest two pixels, which often contain pure white
                // for some reason, these ints are AGBR
                int r = Arrays.stream(aint).map(i -> i & 0xFF).sum() / aint.length;
                int g = Arrays.stream(aint).map(i -> i >> 8 & 0xFF).sum() / aint.length;
                int b = Arrays.stream(aint).map(i -> i >> 16 & 0xFF).sum() / aint.length;
                int color = (r << 16) + (g << 8) + b;
                return color;
            }
        } catch (Exception exception) {
        }
        return 0xFFFFFF;
    }

    public boolean isPauseScreen() {
        return false;
    }
}
