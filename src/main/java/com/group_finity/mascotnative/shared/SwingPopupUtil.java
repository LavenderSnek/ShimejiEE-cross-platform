package com.group_finity.mascotnative.shared;

import com.group_finity.mascot.ui.contextmenu.MenuItemRep;
import com.group_finity.mascot.ui.contextmenu.MenuRep;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Container;

class SwingPopupUtil {

    static JPopupMenu createJPopupmenuFrom(TopLevelMenuRep topMenuRep) {
        JPopupMenu mainPopup = new JPopupMenu(topMenuRep.getTitle());
        mainPopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (topMenuRep.getOnOpenAction() != null) {
                    topMenuRep.getOnOpenAction().run();
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (topMenuRep.getOnCloseAction() != null) {
                    topMenuRep.getOnCloseAction().run();
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        for (MenuItemRep itemRep : topMenuRep.getSubItems()) {
            addItemToMenu(itemRep, mainPopup);
        }

        return mainPopup;
    }

    private static void addItemToMenu(MenuItemRep itemRep, Container menu) {
        if (itemRep == null) {
            return;
        }
        if (itemRep.isSeparator()) {
            menu.add(new JSeparator());
            return;
        }
        if (itemRep instanceof MenuRep subMenu) {
            addSubmenuItemToMenu(subMenu, menu);
            return;
        }

        var item = new JMenuItem(itemRep.getTitle());

        if (!itemRep.isEnabled()) {
            item.setEnabled(false);
            menu.add(item);
            return;
        }

        if (itemRep.getAction() != null) {
            item.addActionListener(e -> itemRep.getAction().run());
        }

        menu.add(item);
    }

    private static void addSubmenuItemToMenu(MenuRep menuRep, Container parentMenu) {
        JMenu menu = new JMenu(menuRep.getTitle());

        if (menuRep.getSubItems() == null || menuRep.getSubItems().length < 1 || !menuRep.isEnabled()) {
            menu.setEnabled(false);
            parentMenu.add(menu);
            return;
        }

        for (MenuItemRep itemRep : menuRep.getSubItems()) {
            addItemToMenu(itemRep, menu);
        }

        parentMenu.add(menu);
    }

}
