package com.group_finity.mascotapp;

import java.util.Collection;
import java.util.Locale;

public interface Controller {
    void setLocale(Locale locale);

    void setImageSets(Collection<String> selection);

    /**
     * Clears all image set data and reloads all selected image sets
     */
    void reloadImageSets();

    void runGlobalAction(String name);
}
