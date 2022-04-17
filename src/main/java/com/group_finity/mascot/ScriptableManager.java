package com.group_finity.mascot;

import java.awt.Point;
import java.lang.ref.WeakReference;

public interface ScriptableManager {

    /**
     * The total number of mascots that are being controlled by this manager
     */
    default int getCount() {
        return getCount(null);
    }

    /**
     * The number of mascots from a certain imageSet that are being controlled by this manager
     */
    int getCount(String imageSet);

    /**
     * Returns a weak ref to a Mascot with the given affordance.
     *
     * @param affordance the affordance being searched for
     * @return A reference to a mascot with the required affordance, or null if a match is not found.
     */
    WeakReference<Mascot> getMascotWithAffordance(String affordance);

    /**
     * Whether more than one mascot has an anchor at the specified point.
     */
    boolean hasOverlappingMascotsAtPoint(Point anchor);

}
