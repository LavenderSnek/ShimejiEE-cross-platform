package com.group_finity.mascotapp;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.MascotManager;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.exception.CantBeAliveException;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

// maybe this can start deal with image sets too??? im basically moving the nicer parts of main here?
// making manager kinda worse to fix main ig
public class Manager implements MascotManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private ConcurrentMap<Integer, Mascot> mascots = new ConcurrentSkipListMap<>();

    public static final int TICK_INTERVAL_MILLIS = 40;

    public ScheduledFuture<?> start() throws ExecutionException, InterruptedException {
        return scheduler.scheduleAtFixedRate(this::tick, 0, TICK_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void tick() {
        // update environment
        NativeFactory.getInstance().getEnvironment().tick();
        // update mascot internal state
        mascots.forEach((id, mascot) -> {
            try {
                mascot.tick();
            } catch (CantBeAliveException e) {
                throw new RuntimeException(e);
            }
        });
        // render new mascot state
        mascots.forEach((id, mascot) -> mascot.apply());
    }

    // i'll deal w any threading issues if/when they happen
    // the original implementation also deleted while iterating so hopefully its fine??

    public void disposeAll() {
        mascots.values().forEach(Mascot::dispose);
    }

    public void disposeIf(Predicate<Mascot> predicate) {
        mascots.values().stream().filter(predicate).forEach(Mascot::dispose);
    }

    public void trySetBehaviorAll(String name) {
        mascots.values().forEach(m -> {
            try {
                var bv = m.getOwnImageSet().getConfiguration().buildBehavior(name);
                m.setBehavior(bv);
            } catch (Exception ignored) {
                // this removes enforcement of ChaseMouse having to exist
                // technically a compatibility break for errors
            }
        });
    }

    // remove these later and replace them w proper breed/spawn
    @Override
    public void add(Mascot mascot) {
        scheduler.submit(() -> {
            mascot.setManager(this);
            mascots.putIfAbsent(mascot.id, mascot);
        });
    }

    @Override
    public void remove(Mascot mascot) {
        scheduler.submit(() -> {
            mascots.remove(mascot.id);
            mascot.setManager(null);
        });
    }

    @Override
    public int getCount(String imageSet) {
        if (imageSet == null) {
            return mascots.size();
        }
        // yeah this is awful
        return (int) mascots.values().stream()
                .filter(m -> m.getImageSet().equals(imageSet))
                .count();
    }

    @Override
    public WeakReference<Mascot> getMascotWithAffordance(String affordance) {
        if (affordance == null) {
            return null;
        }
        // idk how thread safe any of this is btw
        // might be able to refactor the affordance system to be less O(wtf)-ish (but that's for way later)
        return mascots.values().parallelStream()
                .filter(m -> m.getAffordances().contains(affordance))
                .findFirst()
                .map(WeakReference::new)
                .orElse(null);
    }

    @Override
    public boolean hasOverlappingMascotsAtPoint(Point anchor) {
        // there no way to fix this without writing an actual collision system
        // so here's a nice stream to distract from the pain
        return mascots.values().parallelStream()
                .anyMatch(m -> m.getAnchor().equals(anchor));
    }
}
