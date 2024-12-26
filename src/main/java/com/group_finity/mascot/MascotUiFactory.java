package com.group_finity.mascot;

import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.contextmenu.TopLevelMenuRep;

public interface MascotUiFactory {

    DebugUi createDebugUiFor(Mascot mascot);

    TopLevelMenuRep createContextMenuFor(Mascot mascot);

    TranslucentWindow createWindowFor(Mascot mascot);
}
