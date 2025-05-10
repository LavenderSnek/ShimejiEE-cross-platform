package com.group_finity.mascot;

import com.group_finity.mascot.window.contextmenu.TopLevelMenuRep;

public interface MascotUiFactory {

    DebugUi createDebugUiFor(Mascot mascot);

    TopLevelMenuRep createContextMenuFor(Mascot mascot);
}
