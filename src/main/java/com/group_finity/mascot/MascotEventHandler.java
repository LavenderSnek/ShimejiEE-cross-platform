package com.group_finity.mascot;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.ui.contextmenu.MenuItemRep;
import com.group_finity.mascot.ui.contextmenu.MenuRep;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

import java.util.ArrayList;
import java.util.List;

class MascotEventHandler {

    private final Mascot mascot;

    public MascotEventHandler(Mascot mascot) {
        this.mascot = mascot;
    }

    public void leftMousePressed() {
        if (mascot.getBehavior() != null) {
            try {
                mascot.getBehavior().mousePressed(null);
            } catch (final CantBeAliveException e) {
                Main.showError(Tr.tr("SevereShimejiErrorErrorMessage")
                        + "\n" + e.getMessage()
                        + "\n" + Tr.tr("SeeLogForDetails"));
                mascot.dispose();
            }
        }
    }

    public void leftMouseReleased() {
        if (mascot.getBehavior() != null) {
            try {
                mascot.getBehavior().mouseReleased(null);
            } catch (final CantBeAliveException e) {
                Main.showError(Tr.tr("SevereShimejiErrorErrorMessage")
                        + "\n" + e.getMessage()
                        + "\n" + Tr.tr("SeeLogForDetails"));
                mascot.dispose();
            }
        }
    }

    public TopLevelMenuRep createPopupMenuRep() {
        TopLevelMenuRep mainMenu = new TopLevelMenuRep("Shimeji Popup",
                new MenuItemRep(Tr.tr("CallAnother"), () -> Main.getInstance().createMascot(mascot.getImageSet())),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(Tr.tr("FollowCursor"), () -> {
                    var config = Main.getInstance().getConfiguration(mascot.getImageSet());
                    mascot.getManager().setBehaviorAll(config, Main.BEHAVIOR_GATHER, mascot.getImageSet());
                }),
                new MenuItemRep(Tr.tr("RestoreWindows"), () -> NativeFactory.getInstance().getEnvironment().restoreIE()),
                new MenuItemRep(Tr.tr("RevealStatistics"), mascot::startDebugUi),
                MenuItemRep.SEPARATOR,
                createBehaviourSubmenu(Tr.tr("SetBehaviour")),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(Tr.tr("Dismiss"), mascot::dispose),
                new MenuItemRep(Tr.tr("DismissOthers"), () -> mascot.getManager().remainOne(mascot.getImageSet())),
                new MenuItemRep(Tr.tr("DismissAllOthers"), () -> mascot.getManager().remainOne(mascot)),
                new MenuItemRep(Tr.tr("DismissAll"), () -> System.exit(0))
        );

        mainMenu.setOnOpenAction(() -> mascot.setAnimating(false));
        mainMenu.setOnCloseAction(() -> mascot.setAnimating(true));

        return mainMenu;
    }

    private MenuRep createBehaviourSubmenu(String title) {
        var config = Main.getInstance().getConfiguration(mascot.getImageSet());
        boolean translateNames = Main.getInstance().shouldTranslateBehaviorNames();

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
                        } catch (Exception err) {
                            Main.showError(Tr.tr("CouldNotSetBehaviourErrorMessage")
                                    + "\n" + err.getMessage()
                                    + "\n" + Tr.tr("SeeLogForDetails"));
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
