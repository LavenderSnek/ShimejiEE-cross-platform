package com.group_finity.mascotnative.macjni.menu;

import java.util.Objects;

class MacJniMenuItem {

    private final Runnable action;

    private native void createNativeMacJniMenuItemWithParent(String title, long parentNsMenuPtr);

    private MacJniMenuItem(String title, Runnable action, long parentNsMenuPtr) {
        this.action = Objects.requireNonNullElse(action, () -> {});
        createNativeMacJniMenuItemWithParent(title, parentNsMenuPtr);
    }

    static void createMacJniMenuItemWithParent(String title, Runnable action, long parentNsMenuPtr) {
        new MacJniMenuItem(title, action, parentNsMenuPtr);
    }

    private void _onClick() {
        action.run();
    }

}
