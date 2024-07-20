package com.group_finity.mascotapp;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.MascotManager;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.exception.CantBeAliveException;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class Manager implements MascotManager {

    // this could technically go in mascot since it does draw the mascots (maybe as DefaultManager?)
    // todo: think abt it later

    public static final int TICK_INTERVAL_MILLIS = 40;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();

    private final ConcurrentMap<Integer, Mascot> mascots = new ConcurrentSkipListMap<>();

    public ScheduledFuture<?> start() throws ExecutionException, InterruptedException {
        return scheduler.scheduleAtFixedRate(this::tick, 0, TICK_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        // tasks
        runPendingTasks();

        // update environment
        NativeFactory.getInstance().getEnvironment().tick();

        // update each mascot's internal state
        mascots.forEach((id, mascot) -> {
            try {
                mascot.tick();
            } catch (CantBeAliveException e) {
                throw new RuntimeException(e);
            }
        });

        // render new mascot state
        mascots.forEach((id, mascot) -> mascot.apply());

        // tasks
        runPendingTasks();
    }

    private void runPendingTasks() {
        while (!tasks.isEmpty()) {
            tasks.removeFirst().run();
        }
    }

    // basically just that linkedHashSet thing from the original Manager
    public void queueTask(Runnable r) {
        tasks.add(r);
    }

    public void disposeAll() {
        queueTask(() -> mascots.values().forEach(Mascot::dispose));
    }

    public void disposeIf(Predicate<Mascot> predicate) {
        queueTask(() -> mascots.values().stream().filter(predicate).forEach(Mascot::dispose));
    }

    public void trySetBehaviorAll(String name) {
        queueTask(() -> mascots.values().forEach(m -> {
            try {
                var bv = m.getOwnImageSet().getConfiguration().buildBehavior(name);
                m.setBehavior(bv);
            } catch (Exception ignored) {
                // this removes enforcement of ChaseMouse having to exist
                // technically a compatibility break for errors
            }
        }));
    }

    @Override
    public void add(Mascot mascot) {
        queueTask(() -> {
            mascot.setManager(this);
            mascots.putIfAbsent(mascot.id, mascot);
        });
    }

    @Override
    public void remove(Mascot mascot) {
        queueTask(() -> {
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
        return mascots.values()
                .parallelStream()
                .anyMatch(m -> m.getAnchor().equals(anchor));
    }
}
