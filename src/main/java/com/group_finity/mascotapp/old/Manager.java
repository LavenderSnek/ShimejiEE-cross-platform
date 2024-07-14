package com.group_finity.mascotapp.old;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.MascotManager;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.Tr;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An object that manages a list of mascots and takes timing.
 * <p>
 * This class adjusts the overall timing because each mascot moves asynchronously
 * (such as when throwing a window).
 * The {@link #tick()} method gets the latest environmental information.
 * It then moves all the mascots
 */
public class Manager implements MascotManager {

    private static final Logger log = Logger.getLogger(Manager.class.getName());

    /**
     * Interval of timer in milliseconds. Is at 25fps
     */
    public static final int TICK_INTERVAL = 40;

    private final List<Mascot> mascots = new ArrayList<>();

    /**
     * List of mascots to be added. To prevent {@link ConcurrentModificationException},
     * mascot additions are delayed until the next {@link #tick()}.
     */
    private final Set<Mascot> added = new LinkedHashSet<>();

    /**
     * List of mascots to be added. To prevent {@link ConcurrentModificationException},
     * mascot removals are delayed until the next {@link #tick()}.
     */
    private final Set<Mascot> removed = new LinkedHashSet<>();

    private boolean exitOnLastRemoved = true;

    /**
     * timing thread that controls the ticks
     */
    private Thread thread;


    public Manager() {
        new Thread() {
            {
                this.setDaemon(true);
                this.start();
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Integer.MAX_VALUE);
                    } catch (final InterruptedException ignored) {
                    }
                }
            }

        };
    }

    public void start() {
        if (thread != null && thread.isAlive()) {
            return;
        }

        thread = new Thread(() -> {
            long prev = System.nanoTime() / 1000000;

            try {

                while (true) {
                    //inner
                    while (true) {
                        final long cur = System.nanoTime() / 1000000;
                        if (cur - prev >= TICK_INTERVAL) {

                            if (cur > prev + TICK_INTERVAL * 2) { //checks for a skip
                                prev = cur;
                            } else {
                                prev += TICK_INTERVAL;
                            }
                            break; //breaks inner while
                        }
                        Thread.sleep(1, 0);
                    }

                    tick();
                }

            } catch (final InterruptedException ignored) {
            } catch (CantBeAliveException e) {
                throw new RuntimeException(e);
            }

        });

        thread.setDaemon(false);
        thread.start();
    }

    public void stop() {
        if (thread == null || !thread.isAlive()) {
            return;
        }
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Advance the mascot one frame.
     */
    private void tick() throws CantBeAliveException {

        // Update the environmental information
        NativeFactory.getInstance().getEnvironment().tick();

        synchronized (this.getMascots()) {

            // Add the mascot if it should be added
            for (final Mascot mascot : this.getAdded()) {
                this.getMascots().add(mascot);
            }
            this.getAdded().clear();

            // Remove the mascot if it should be removed
            for (final Mascot mascot : this.getRemoved()) {
                this.getMascots().remove(mascot);
            }
            this.getRemoved().clear();

            // Advance mascot's time
            for (final Mascot mascot : this.getMascots()) {
                mascot.tick();
            }

            // Draw the mascot
            for (final Mascot mascot : this.getMascots()) {
                mascot.apply();
            }

            // exit if needed
            if (isExitOnLastRemoved() && this.getMascots().isEmpty()) {
                System.exit(0); // stop() ?
            }
        }

    }

    @Override
    public void add(final Mascot mascot) {
        synchronized (this.getAdded()) {
            this.getAdded().add(mascot);
            this.getRemoved().remove(mascot);
        }
        mascot.setManager(this);
    }

    @Override
    public void remove(final Mascot mascot) {
        synchronized (this.getAdded()) {
            this.getAdded().remove(mascot);
            this.getRemoved().add(mascot);
        }
        mascot.setManager(null);
    }

    public void setBehaviorAll(final String name) {
        synchronized (this.getMascots()) {
            for (final Mascot mascot : this.getMascots()) {
                try {
                    Configuration conf = mascot.getOwnImageSet().getConfiguration();
                    mascot.setBehavior(conf.buildBehavior(conf.getSchema().getString(name)));
                } catch (final BehaviorInstantiationException e) {
                    log.log(Level.SEVERE, "Failed to initialize the following actions", e);
                    Main.showError(Tr.tr("FailedSetBehaviourErrorMessage") + "\n" + e.getMessage() + "\n" + Tr.tr("SeeLogForDetails"));
                    mascot.dispose();
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "Fatal Error", e);
                    Main.showError(Tr.tr("FailedSetBehaviourErrorMessage") + "\n" + e.getMessage() + "\n" + Tr.tr("SeeLogForDetails"));
                    mascot.dispose();
                }
            }
        }
    }


    public void remainOne() {
        synchronized (this.getMascots()) {
            int totalMascots = this.getMascots().size();
            for (int i = totalMascots - 1; i > 0; --i) {
                this.getMascots().get(i).dispose();
            }
        }
    }

    /**
     * Disposes all mascots made from the specified imageSet.
     */
    public void remainNone(String imageSet) {
        synchronized (this.getMascots()) {
            int totalMascots = this.getMascots().size();
            for (int i = totalMascots - 1; i >= 0; --i) {
                Mascot m = this.getMascots().get(i);
                if (m.getImageSet().equals(imageSet)) {
                    m.dispose();
                }
            }
        }
    }


    @Override
    public int getCount(String imageSet) {
        synchronized (getMascots()) {
            if (imageSet == null) {
                return getMascots().size();
            } else {
                int count = 0;
                for (Mascot m : getMascots()) {
                    if (m.getImageSet().equals(imageSet)) {
                        count++;
                    }
                }
                return count;
            }
        }
    }

    @Override
    public WeakReference<Mascot> getMascotWithAffordance(String affordance) {
        synchronized (this.getMascots()) {
            for (final Mascot mascot : this.getMascots()) {
                if (mascot.getAffordances().contains(affordance)) {
                    return new WeakReference<>(mascot);
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasOverlappingMascotsAtPoint(Point anchor) {
        int count = 0;
        synchronized (this.getMascots()) {
            for (final Mascot mascot : this.getMascots()) {
                if (mascot.getAnchor().equals(anchor)) {
                    count++;
                    if (count > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void disposeAll() {
        synchronized (this.getMascots()) {
            for (int i = this.getMascots().size() - 1; i >= 0; --i) {
                this.getMascots().get(i).dispose();
            }
        }
    }

    /**
     * Whether the program should be terminated when the last mascot is deleted.
     * <p>
     * If the tray icon creation fails, the process will remain forever if the program
     * is not terminated when the mascot disappears.
     */
    public boolean isExitOnLastRemoved() {
        return exitOnLastRemoved;
    }

    public void setExitOnLastRemoved(boolean exitOnLastRemoved) {
        this.exitOnLastRemoved = exitOnLastRemoved;
    }

    private List<Mascot> getMascots() {
        return this.mascots;
    }

    private Set<Mascot> getAdded() {
        return this.added;
    }

    private Set<Mascot> getRemoved() {
        return this.removed;
    }

}
