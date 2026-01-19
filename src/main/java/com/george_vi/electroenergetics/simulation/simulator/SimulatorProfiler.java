package com.george_vi.electroenergetics.simulation.simulator;

import net.neoforged.fml.loading.FMLEnvironment;

import java.util.*;


public class SimulatorProfiler {
    private final ResultEntry root = new ResultEntry("r");
    private final Deque<ResultEntry> entryStack = new ArrayDeque<>();
    private final Deque<Long> startTimes = new ArrayDeque<>();
    private long profilerOverheadNanos = 0L;
    private long threadedNanos = 0L;

    public SimulatorProfiler() {

    }

    public void push(String id) {
        long t0 = System.nanoTime();
        startTimes.push(t0);

        ResultEntry parent = entryStack.peek();
        ResultEntry node = (parent == null) ? root.getOrCreateChild(id) : parent.getOrCreateChild(id);
        entryStack.push(node);
        profilerOverheadNanos += System.nanoTime() - t0;
    }

    public void pop() {
        if (startTimes.isEmpty() || entryStack.isEmpty())
            if (FMLEnvironment.production)
                return;
            else
                throw new IllegalStateException("Mismatched push/pop");

        long before = System.nanoTime();
        long start = startTimes.pop();
        long now = System.nanoTime();
        long diff = now - start;
        ResultEntry node = entryStack.pop();
        node.timeTookNanos += diff;

        profilerOverheadNanos += System.nanoTime() - before;
    }

    public void popPush(String id) {
        pop();
        push(id);
    }

    public void clear() {
        root.childrenClear();
        entryStack.clear();
        startTimes.clear();
        profilerOverheadNanos = 0L;
        threadedNanos = 0L;
    }

    public List<ResultEntry> getResults() {
        return root.childrenList();
    }

    public long getThreadedNanos() {
        return threadedNanos;
    }

    public void addThreadedNanos(long n) {
        synchronized (this) { // I don't know if this is required, but I think this is safer.
            threadedNanos += n;
        }
    }

    public long getProfilerOverheadNanos() {
        return profilerOverheadNanos;
    }

    public static final class ResultEntry {
        public final String id;
        public final List<ResultEntry> children = new ArrayList<>();
        private final Map<String, ResultEntry> childrenMap = new HashMap<>();
        public long timeTookNanos;

        ResultEntry(String id) { this.id = id; }

        ResultEntry getOrCreateChild(String id) {
            return childrenMap.computeIfAbsent(id, k -> {
                ResultEntry r = new ResultEntry(k);
                children.add(r);
                return r;
            });
        }

        List<ResultEntry> childrenList() { return children; }
        void childrenClear() {
            children.clear();
            childrenMap.clear();
            timeTookNanos = 0L;
        }
    }
}