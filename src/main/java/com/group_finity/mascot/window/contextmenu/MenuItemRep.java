package com.group_finity.mascot.window.contextmenu;

public class MenuItemRep {

    public static final MenuItemRep SEPARATOR = new MenuItemRep(null, null) {
        @Override
        public boolean isSeparator() {
            return true;
        }
    };

    private final String title;
    private final Runnable action;
    private boolean enabled = true;

    public MenuItemRep(String title, Runnable action) {
        this.title = title;
        this.action = action;
    }

    public MenuItemRep(String title, Runnable action, boolean enabled) {
        this(title, enabled ? action : null);
        this.enabled = enabled;
    }

    public boolean isSeparator() {
        return false;
    }

    public String getTitle() {
        return title;
    }

    public Runnable getAction() {
        return action;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
