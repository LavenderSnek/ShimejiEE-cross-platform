package com.group_finity.mascot.ui.contextmenu;

public class MenuRep extends MenuItemRep {

    private final MenuItemRep[] subItems;

    public MenuRep(String title, MenuItemRep... items) {
        super(title, null);
        this.subItems = items;
    }

    public MenuItemRep[] getSubItems() {
        return subItems;
    }

}
