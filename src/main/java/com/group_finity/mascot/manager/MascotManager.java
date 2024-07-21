package com.group_finity.mascot.manager;

import com.group_finity.mascot.Mascot;

public interface MascotManager extends ScriptableManager {

    void add(Mascot mascot);

    void remove(Mascot mascot);

}
