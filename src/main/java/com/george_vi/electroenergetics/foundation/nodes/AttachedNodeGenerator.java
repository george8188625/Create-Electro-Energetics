package com.george_vi.electroenergetics.foundation.nodes;

/**
 * Infinite {@link AttachedNode} generator.
 */
public class AttachedNodeGenerator {
    private int id = 0;
    private int poolId = 0;
    private final String name;

    public AttachedNodeGenerator(String name) {
        this.name = name;
    }

    public AttachedNode newNode() {
        if (id == Integer.MAX_VALUE) {
            id = 0;
            poolId++;
        }
        return new AttachedNode(id++, name + poolId);
    }
}
