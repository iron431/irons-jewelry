### Additions
- Added Jewelcrafting and You: A guide to Gems 'n Jewelry
  - Guide book displaying all materials and patterns in the game, as well as a significant amount of information for both
- Added material tag for blacklisting materials from generating as loot: `#irons_jewelry:jewelry_loot_blacklist`
  - Materials in this tag are prevented from rolling when generating random jewelry loot
  - Setting a specific material whitelist in the loot function ignores the blacklist
        
### Changes
- Reworked Rhinestone Amulet textures
- Reworked Lapis Lazuli palette
- Updated Japanese localization, thanks to Mohuzato
- Example jewelry items are now displayed in JEI

### Fixes
- 

### API
- Removed `materialType` from material definitions; material classification is now driven by material registry tags.
- Part material filters now use `allowedMaterials` holder sets instead of `allowedMaterialTypes` string lists.
- `irons_jewelry:generate_jewelry` loot `materials` filter keys now use material tags (for example `#irons_jewelry:metal`) instead of free-form strings.
