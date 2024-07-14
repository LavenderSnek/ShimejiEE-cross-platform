package com.group_finity.mascotapp;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.MascotManager;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.exception.CantBeAliveException;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

// maybe this can start deal with image sets too??? im basically moving the nicer parts of main here?
// making manager kinda worse to fix main ig
public class Manager implements MascotManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // todo: will have to change the interface to account for transformation (maybe throw the transform/breed off here)
    private ConcurrentMap<String, ConcurrentSkipListSet<Integer>> imageSetMascots = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, Mascot> mascots = new ConcurrentSkipListMap<>();

    public static final int TICK_INTERVAL_MS = 40;

    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
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

    // remove these later and replace them w proper breed/spawn
    @Override
    public void add(Mascot mascot) {
        imageSetMascots.putIfAbsent(mascot.getImageSet(), new ConcurrentSkipListSet<>());
        imageSetMascots.computeIfPresent(mascot.getImageSet(), (s, l) -> {
            mascot.setManager(this);
            l.add(mascot.id);
            return l;
        });
        mascots.putIfAbsent(mascot.id, mascot);
    }

    @Override
    public void remove(Mascot mascot) {
        imageSetMascots.computeIfPresent(mascot.getImageSet(), (s, l) -> {
            mascot.setManager(null);
            l.remove(mascot.id);
            return l;
        });
        mascots.remove(mascot.id);
    }

    @Override
    public int getCount(String imageSet) {
        return imageSet == null
                ? mascots.size()
                : imageSetMascots.get(imageSet).size();
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
