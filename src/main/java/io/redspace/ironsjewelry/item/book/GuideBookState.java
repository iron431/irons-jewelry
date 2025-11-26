package io.redspace.ironsjewelry.item.book;

import java.util.List;

public class GuideBookState {
    public GuideBookState(List<BookSection> sections) {
        this.sections = sections;
    }

    public record BookSection(List<GuideBookScreen.Page> pages) {
    }

    final List<BookSection> sections;
    int sectionIndex;
    int localPageIndex;

    public BookSection getCurrentSection() {
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("Cannot have empty book state!");
        }
        return sections.get(sectionIndex);
    }

    public GuideBookScreen.Page getCurrentPage() {
        var section = getCurrentSection();
        if (section.pages.isEmpty()) {
            throw new IllegalArgumentException("Cannot have empty book section!");
        }
        return section.pages.get(localPageIndex);
    }

    public GuideBookScreen.Page getGlobalPage(int globalIndex) {
        int pageIndex = globalIndex;
        int i = 0;
        while (pageIndex >= sections.get(i).pages.size()) {
            pageIndex -= sections.get(i).pages.size();
            i++;
        }
        if (i >= sections.size()) {
            throw new IllegalArgumentException("Accessing invalid page index: " + globalIndex);
        }
        return sections.get(i).pages().get(pageIndex);
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

    public boolean setLocalPage(int page) {
        if (page >= 0 && page < sections.get(sectionIndex).pages.size()) {
            localPageIndex = page;
            return true;
        }
        return false;
    }

    public boolean navigateToPage(GuideBookScreen.Page page) {
        for (int j = 0; j < sections.size(); j++) {
            var section = sections.get(j);
            int i = section.pages.indexOf(page);
            if (i >= 0) {
                localPageIndex = i;
                sectionIndex = j;
                return true;
            }
        }
        return false;
    }

    public boolean returnSection() {
        if (localPageIndex == 0) {
            if (sectionIndex != 0) {
                sectionIndex = 0;
                return true;
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
