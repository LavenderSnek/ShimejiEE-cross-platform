package com.group_finity.mascot.window.contextmenu;

public class TopLevelMenuRep extends MenuRep {

    private Runnable onOpenAction = null;
    private Runnable onCloseAction = null;

    public TopLevelMenuRep(String title, MenuItemRep... items) {
        super(title, items);
    }

    public Runnable getOnOpenAction() {
        return onOpenAction;
    }

    public void setOnOpenAction(Runnable action) {
        this.onOpenAction = action;
    }

    public Runnable getOnCloseAction() {
        return onCloseAction;
    }

    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }

}
