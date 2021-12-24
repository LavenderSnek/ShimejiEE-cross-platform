package com.group_finity.mascotnative.macjni.menu;

import java.util.Objects;

// used for menu item that need an onClose and/or onOpen actions
class MacJniMenu {

    static native long createRegularNSMenuAsSubmenuOf(long parentNsMenuPtr, String title);
    static native void addDisabledItemToNSMenuWithTitle(long nsMenuPtr, String title);
    static native void addSeparatorToNSMenu(long nsMenuPtr);

    private final long ptr;
    private final Runnable onOpenAction;
    private final Runnable onCloseAction;

    private native long createMacJniMenu();

    MacJniMenu(Runnable onOpenAction, Runnable onCloseAction) {
        this.onOpenAction = Objects.requireNonNullElse(onOpenAction, () -> {});
        this.onCloseAction = Objects.requireNonNullElse(onCloseAction, () -> {});
        this.ptr = createMacJniMenu();
    }

    long getNsMenuPtr() {
        return ptr;
    }

    private void _onOpen() {
        onOpenAction.run();
    }

    private void _onClose() {
        onCloseAction.run();
    }

}
