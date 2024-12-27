package com.group_finity.mascotapp;

import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import com.group_finity.mascotapp.prefs.MutablePrefs;

import java.util.Collection;

public interface Ui {
    void showError(String message);

    void start(MutablePrefs prefs, Runnable onFinish);

    void reload();

    void requestImageSetChooser(Collection<String> currentSelection, ShimejiProgramFolder pf);

}
