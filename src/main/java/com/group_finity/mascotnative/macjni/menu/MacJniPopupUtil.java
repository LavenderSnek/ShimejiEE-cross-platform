package com.group_finity.mascotnative.macjni.menu;

import com.group_finity.mascot.ui.contextmenu.MenuItemRep;
import com.group_finity.mascot.ui.contextmenu.MenuRep;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

public class MacJniPopupUtil {

    public static long createNSMenuFor(TopLevelMenuRep topLevelMenuRep) {
        if (topLevelMenuRep == null || !topLevelMenuRep.isEnabled() || topLevelMenuRep.getSubItems().length == 0) {
            return 0;
        }

        MacJniMenu mainMenu = new MacJniMenu(
                topLevelMenuRep.getOnOpenAction(),
                topLevelMenuRep.getOnCloseAction()
        );

        for (MenuItemRep subItem : topLevelMenuRep.getSubItems()) {
            addItemToMenu(subItem, mainMenu.getNsMenuPtr());
        }

        return mainMenu.getNsMenuPtr();
    }

    private static void addItemToMenu(MenuItemRep itemRep, long nsMenuPtr) {
        if (itemRep == null) {
            return;
        }

        if (itemRep.isSeparator()) {
            MacJniMenu.addSeparatorToNSMenu(nsMenuPtr);
            return;
        }

        if (itemRep instanceof MenuRep menuRep && itemRep.isEnabled()) {
            addSubmenuToMenu(menuRep, nsMenuPtr);
            return;
        }

        String title = itemRep.getTitle() == null ? "" : itemRep.getTitle();

        if (itemRep.getAction() == null || !itemRep.isEnabled()) {
            MacJniMenu.addDisabledItemToNSMenuWithTitle(nsMenuPtr, title);
            return;
        }

        MacJniMenuItem.createMacJniMenuItemWithParent(
                title,
                itemRep.getAction(),
                nsMenuPtr
        );
    }

    private static void addSubmenuToMenu(MenuRep rep, long nsMenuPtr) {
        long submenuNsMenuPtr = MacJniMenu.createRegularNSMenuAsSubmenuOf(nsMenuPtr, rep.getTitle());

        for (var subItem : rep.getSubItems()) {
            addItemToMenu(subItem, submenuNsMenuPtr);
        }
    }

}
