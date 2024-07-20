package com.group_finity.mascotapp.imageset;

import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.imageset.ImageSetStore;

import java.util.*;

public class ImageSetManager implements ImageSetStore {

    private Map<String, ImageSet> loaded = new HashMap<>(4);
    private Set<String> selected = new HashSet<>(4);

    private ImageSetLoadingDelegate loader;
    private ImageSetSelectionDelegate selectionDelegate;

    public ImageSetManager(ImageSetLoadingDelegate loader, ImageSetSelectionDelegate selectionDelegate) {
        this.loader = loader;
        this.selectionDelegate = selectionDelegate;
    }

    public void setSelected(Collection<String> newSelected) {
        if (newSelected == null) {
            return;
        }

        // remove if the new set doesn't contain set
        // any deps will be removed
        // get list copy first so we're not deleting while iterating
        getLoaded().stream()
                .filter(s -> !newSelected.contains(s))
                .forEach(this::removeImageSet);

        for (String s : newSelected) {
            if (!loaded.containsKey(s)) {
                // new image set added (never used before)
                addSelected(s);
            } else if(!selected.contains(s)) {
                // in case it was previously loaded as a dep, make selected
                selectionDelegate.dependencyWillBecomeSelection(s, loaded.get(s));
                selected.add(s);
                selectionDelegate.dependencyHasBecomeSelection(s, loaded.get(s));
            }
        }
    }

    private void removeImageSet(String name) {
        var ims = loaded.get(name);
        if (ims == null) {
            return;
        }
        selectionDelegate.imageSetWillBeRemoved(name, ims);
        selected.remove(name);
        loaded.remove(name);
        selectionDelegate.imageSetHasBeenRemoved(name, ims);
    }

    private void addSelected(String name) {
        var imageSet = loader.load(name);
        if (imageSet == null) {
            // load failed (the loader should deal w any errors)
            return;
        }
        selectionDelegate.imageSetWillBeAdded(name, imageSet);
        loaded.put(name, imageSet);
        selected.add(name);
        selectionDelegate.imageSetHasBeenAdded(name, imageSet);
    }


    @Override
    public ImageSet get(String name) {
        if (name == null) {
            return null;
        }
        return loaded.getOrDefault(name, null);
    }

    @Override
    public ImageSet getAsDependency(String name, String dependent) {
        if (name == null) {
            return null;
        }
        // not using putIfAbsent here since that wouldn't handle failure properly
        ImageSet ims = null;
        if (!loaded.containsKey(name)) {
            ims = loader.loadAsDependency(name, dependent);
        }

        return ims == null ? null : loaded.put(name, ims);
    }

    public Collection<String> getSelected() {
        return selected.stream().toList();
    }

    public Collection<String> getLoaded() {
        return loaded.keySet().stream().toList();
    }

    public String getRandomSelection() {
        return selected.stream()
                .skip(new Random().nextInt(selected.size()))
                .findFirst()
                .orElse(null);
    }
}
