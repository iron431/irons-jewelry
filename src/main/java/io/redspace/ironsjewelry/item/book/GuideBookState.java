package io.redspace.ironsjewelry.item.book;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuideBookState {
    public GuideBookState(List<BookSection> sections) {
        this.sections = sections;
    }

    public record BookSection(@Nullable BookSection parent, List<GuideBookScreen.Page> pages) {
    }

    final List<BookSection> sections;
    int sectionIndex;
    int localPageIndex;

    public GuideBookScreen.Page getCurrentPage() {
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("Cannot have empty book state!");
        }
        var section = sections.get(sectionIndex);
        if (section.pages.isEmpty()) {
            throw new IllegalArgumentException("Cannot have empty book section!");
        }
        return section.pages.get(localPageIndex);
    }

    /**
     * @return Whether page successfully turned
     */
    public boolean incrementPage() {
        BookSection currentSection = sections.get(sectionIndex);
        if (localPageIndex + 1 >= currentSection.pages.size()) {
            // finished all current pages, try to advance to next section
            if (sectionIndex < sections.size() - 1) {
                localPageIndex = 0;
                sectionIndex++;
                return true; // we successfully advanced section and reset page counter
            }
            return false; // unable to advance, no sections remaining
        } else {
            localPageIndex++;
            return true; // we have more pages remaining
        }
    }

    /**
     * @return Whether page successfully turned
     */
    public boolean decrementPage() {
        localPageIndex--;
        if (localPageIndex == -1) {
            // finished with  current section, try to go back to previous section
            if (sectionIndex > 0) {
                sectionIndex--;
                localPageIndex = sections.get(sectionIndex).pages.size() - 1;
                return true; // we successfully went to previous section and set page counter to final page
            }
            localPageIndex = 0;
            return false; // we have no more sections to go back to, clamp page index back to 0
        } else {
            return true; // we have pages to fall back to
        }
    }

    public boolean returnSection() {
        if (localPageIndex == 0) {
            BookSection currentSection = sections.get(sectionIndex);
            if (currentSection.parent != null) {
                int i = sections.indexOf(currentSection.parent);
                if (i >= 0) {
                    sectionIndex = i;
                    localPageIndex = 0;
                    return true;
                }
            }
        } else {
            localPageIndex = 0;
            return true;
        }

        return false;
    }

    public int getGlobalPageNumber() {
        int page = 0;
        for (int i = 0; i < sectionIndex; i++) {
            page += sections.get(i).pages.size();
        }
        page += localPageIndex + 1;
        return page;
    }

    public int getMaxPageCount() {
        int pages = 0;
        for (int i = 0; i < sections.size(); i++) {
            pages += sections.get(i).pages.size();
        }
        return pages;
    }

}
