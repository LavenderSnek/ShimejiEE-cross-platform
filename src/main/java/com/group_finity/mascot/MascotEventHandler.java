package com.group_finity.mascot;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.ui.contextmenu.MenuItemRep;
import com.group_finity.mascot.ui.contextmenu.MenuRep;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;

import java.util.ArrayList;
import java.util.List;

class MascotEventHandler implements TranslucentWindowEventHandler {

    private final Mascot mascot;

    public MascotEventHandler(Mascot mascot) {
        this.mascot = mascot;
    }

    @Override
    public void onDragBegin(TranslucentWindowEvent event) {
        if (mascot.getBehavior() != null) {
            try {
                mascot.getBehavior().mousePressed(event);
            } catch (final CantBeAliveException e) {
                mascot.dispose();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDragEnd(TranslucentWindowEvent event) {
        if (mascot.getBehavior() != null) {
            try {
                mascot.getBehavior().mouseReleased(event);
            } catch (final CantBeAliveException e) {
                mascot.dispose();
                throw new RuntimeException(e);
            }
        }
    }

    // todo: this isnt really an event handling thing. maybe replace w something like onContextMenuRequest
    @Override
    public TopLevelMenuRep getContextMenuRep() {
        TopLevelMenuRep mainMenu = new TopLevelMenuRep("Shimeji Popup",
                new MenuItemRep(Tr.tr("CallAnother"), () -> {
//                    Main.getInstance().createMascot(mascot.getImageSet())
                }, false),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(Tr.tr("FollowCursor"), () -> {
//                    var config = Main.getInstance().getConfiguration(mascot.getImageSet());
//                    mascot.getManager().setBehaviorAll(config, Main.BEHAVIOR_GATHER, mascot.getImageSet());
                }, false),
                new MenuItemRep(Tr.tr("RestoreWindows"), () -> NativeFactory.getInstance().getEnvironment().restoreIE()),
                new MenuItemRep(Tr.tr("RevealStatistics"), mascot::startDebugUi),
                MenuItemRep.SEPARATOR,
                createBehaviourSubmenu(Tr.tr("SetBehaviour")),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(Tr.tr("Dismiss"), mascot::dispose),
                new MenuItemRep(Tr.tr("DismissOthers"), () -> {
//                    mascot.getManager().remainOne(mascot.getImageSet());
                }, false),
                new MenuItemRep(Tr.tr("DismissAllOthers"), () -> {
//                    mascot.getManager().remainOne(mascot);
                }, false),
                new MenuItemRep(Tr.tr("DismissAll"), () -> System.exit(0))
        );

        mainMenu.setOnOpenAction(() -> mascot.setAnimating(false));
        mainMenu.setOnCloseAction(() -> mascot.setAnimating(true));

        return mainMenu;
    }

    private MenuRep createBehaviourSubmenu(String title) {
        var config = mascot.getOwnImageSet().getConfiguration();
        boolean translateNames = mascot.shouldTranslateBehaviours();

        List<MenuItemRep> behaviorItems = new ArrayList<>();
        Behavior behaviour;
        for (String behaviorName : config.getBehaviorNames()) {
            String lblName = translateNames ? Tr.trBv(behaviorName) : behaviorName;
            try {
                behaviour = config.buildBehavior(behaviorName);
                if (!behaviour.isHidden()) {
                    behaviorItems.add(new MenuItemRep(lblName, () -> {
                        try {
                            mascot.setBehavior(config.buildBehavior(behaviorName));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
            } catch (Exception e) {
                // disable menu item if the behaviour is broken
                behaviorItems.add(new MenuItemRep(lblName, null, false));
            }
        }

        return new MenuRep(title, behaviorItems.toArray(new MenuItemRep[0]));
    }

}
