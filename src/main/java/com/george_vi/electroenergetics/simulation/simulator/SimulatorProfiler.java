package com.george_vi.electroenergetics.simulation.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class SimulatorProfiler {
    List<ResultEntry> results = new ArrayList<>();
    Stack<Long> startTimes = new Stack<>();
    Stack<String> ids = new Stack<>();
    int profilerTime = 0;


    public void push(String id) {
        startTimes.push(System.nanoTime());
        ids.push(id);
    }

    public void pop() {
        long popStart = System.nanoTime();
        long startTime = startTimes.pop();
        int diff = (int) (System.nanoTime() - startTime);
        List<ResultEntry> children = results;
        for (int i = 0; i < ids.size() - 1; i++) {
            String id = ids.get(i);
            Optional<ResultEntry> oc = children.stream().filter(e -> e.id().equals(id)).findFirst();
            ResultEntry re;
            if (oc.isEmpty())
                children.add(re = new ResultEntry(id, new ArrayList<>(), 0));
            else {
                children.remove(oc.get());
                children.add(re = new ResultEntry(oc.get().id, oc.get().children, 0));
            }
            children = re.children();
        }
        Optional<ResultEntry> oc = children.stream().filter(e -> e.id().equals(ids.peek())).findFirst();
        if (oc.isEmpty())
            children.add(new ResultEntry(ids.pop(), new ArrayList<>(), diff));
        else {
            children.remove(oc.get());
            children.add(new ResultEntry(ids.pop(), oc.get().children, oc.get().timeTook + diff));
        }
        profilerTime += (int) (System.nanoTime() - popStart);
    }

    public void popPush(String id) {
        pop();
        push(id);
    }

    public void clear() {
        results.clear();
        startTimes.clear();
        ids.clear();
        profilerTime = 0;
    }

    public List<ResultEntry> getResults() {
        return results;
    }

    public int getProfilerTime() {
        return profilerTime;
    }

    public record ResultEntry(String id, List<ResultEntry> children, Integer timeTook) {

    }
}
