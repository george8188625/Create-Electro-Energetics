package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.*;
import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class WireSimulationState {
    public final InfrastructureSavedData sd;
    public final Level level;

    private final Map<InWorldNodeConnection, ConnectionEntry> connections = new HashMap<>();
    private final Long2ObjectMap<Map<InWorldNodeConnection, ConnectionEntry>> connectionsBySection = new Long2ObjectOpenHashMap<>();
    private final List<Map<AttachedNode, CutWireEntry>> cutsByHandle = new ArrayList<>(256);
    private final IntStack freeHandleIDs = new IntArrayList();
    private int nextHandleID = 0;
    private final Map<InWorldNodeConnection, List<CutWireEntry>> cutsByWire = new HashMap<>();
    private final AttachedNodeGenerator cutNodeGenerator = new AttachedNodeGenerator("CEECutNode");
    private final List<ObjectDoublePair<DirectionalNodeConnection>> lazyConnections = new ArrayList<>();
    private boolean reloadLazy = true;

    private List<WrappedIndexedNode> allLazyIndexedNodes = new ArrayList<>();
    private Object2IntOpenHashMap<Node> lazyIndexedNodeIndexes = new Object2IntOpenHashMap<>();
    private int id = 0;

    public boolean rebuild = true;

    public WireSimulationState(InfrastructureSavedData sd, Level level) {
        this.sd = sd;
        this.level = level;
    }

    public void onReloadConfigs() {
        sd.wireCrossContactModule.onReloadConfigs();
        connections.values().forEach(ConnectionEntry::onReloadConfigs);

        reloadLazyConnections();
    }

    public List<ObjectDoublePair<DirectionalNodeConnection>> getLazyConnections() {
        if (reloadLazy) {
            lazyConnections.clear();
            sd.wireAssemblerModule.loadLazyConnections(lazyConnections);
            sd.wireCrossContactModule.loadLazyConnections(lazyConnections);
            reloadLazy = false;
        }
        return lazyConnections;
    }

    public void reloadLazyConnections() {
        reloadLazy = true;
    }

    /**
     * This is used to partially index IW Nodes to not do that every tick
     */
    public void onNodeChange(Collection<InWorldNode> nodes) {
        id = 0;
        allLazyIndexedNodes = new ArrayList<>(nodes.size() * 2);
        lazyIndexedNodeIndexes = new Object2IntOpenHashMap<>(nodes.size() * 2);
        lazyIndexedNodeIndexes.defaultReturnValue(-1);
        for (Node node : nodes) {
            WrappedIndexedNode indexedNode = new WrappedIndexedNode(node, id);
            allLazyIndexedNodes.add(indexedNode);
            lazyIndexedNodeIndexes.put(node, id);
            id++;
        }
    }

    public CircuitBuilder createCircuitBuilder() {
        for (WrappedIndexedNode node : allLazyIndexedNodes)
            node.clear();

        return new CircuitBuilder(id, allLazyIndexedNodes, lazyIndexedNodeIndexes);
    }

    void addConnection(InWorldNodeConnection connection, WireData wireData, boolean load) {
        if (wireData instanceof CatenaryConnectionData)
            throw new IllegalArgumentException("CatenaryConnectionData used as data for normal wire creation!");
        Vec3 pos1 = sd.getNodePosition(connection.node1());
        Vec3 pos2 = sd.getNodePosition(connection.node2());
        double distance = pos1.distanceTo(pos2);
        if (distance > 1000)
            return; // Don't brick worlds / cause unnecessary crashes
        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance), 0.5f);

        double miny = pos1.y;

        if (wireData.length == 0) // New connection
            wireData.length = distance;

        for (Vec3 point : points)
            miny = Math.min(miny, point.y());

        List<CutWireEntry> cuts = new ArrayList<>();
        cutsByWire.put(connection, cuts);
        AABB bb = new AABB(pos1, pos2).setMinY(miny).inflate(0.25);
        ConnectionEntry connectionEntry = new ConnectionEntry(pos1, pos2, points, wireData, bb, cuts);
        connections.put(connection, connectionEntry);

        SectionPos prevSection = null;
        for (Vec3 point : points) {
            SectionPos section = SectionPos.of(point);
            if (!Objects.equals(prevSection, section)) {
                connectionsBySection.computeIfAbsent(section.asLong(), s -> new HashMap<>()).put(connection, connectionEntry);
                prevSection = section;
            }
        }
        if (load)
            return;
        sd.wireCrossContactModule.onWireAdded(connection, connectionEntry);
        reloadLazyConnections();
    }

    @SuppressWarnings("UnusedReturnValue")
    ConnectionEntry removeConnection(InWorldNodeConnection connection) {
        ConnectionEntry v = connections.remove(connection);
        if (v != null) {
            sd.wireCrossContactModule.onWireRemoved(connection, v);
            SectionPos prevSection = null;
            for (Vec3 point : v.points) {
                SectionPos section = SectionPos.of(point);
                if (!Objects.equals(prevSection, section)) {
                    prevSection = section;
                    Map<InWorldNodeConnection, ConnectionEntry> c = connectionsBySection.get(section.asLong());
                    if (c == null)
                        continue;
                    c.remove(connection);
                    if (c.isEmpty())
                        connectionsBySection.remove(section.asLong());
                }
            }
        }

        List<CutWireEntry> cuts = cutsByWire.remove(connection);
        if (cuts != null) {
            for (CutWireEntry entry : cuts) {
//                entry.originalList.remove(entry);
                Map<AttachedNode, CutWireEntry> c = cutsByHandle.get(entry.handle.id);
                c.remove(entry.node);
            }
        }
        reloadLazyConnections();
        return v;
    }

    void addCatenaryConnection(InWorldNodeConnection connection, CatenaryConnectionData catenaryData, boolean load) {
        Vec3 pos1 = connection.node1().sourcePos().getBottomCenter();
        Vec3 pos2 = connection.node2().sourcePos().getBottomCenter();

        List<Vec3> points;
        if (catenaryData.isLow) {
             points = QuadraticWireHelper.cablePoints(pos1, pos2, 0, 0.5f);
        } else {
            List<Vec3> points1 = QuadraticWireHelper.cablePoints(pos1, pos2, 0, 0.5f);
            float distance = (float) pos1.distanceTo(pos2);
            List<Vec3> points2 = QuadraticWireHelper.cablePoints(pos1.add(0, 1.5, 0), pos2.add(0, 1.5, 0), 350f * (0.05f / distance), 0.5f);
            points = new ArrayList<>(points1.size() * 2);
            for (int i = 0; i < points1.size() * 2; i++) {
                points.add(i % 2 == 0 ? points1.get(i / 2) : points2.get((i - 1) / 2));
//                ((ServerLevel)level).sendParticles(ParticleTypes.SCRAPE, points.get(i).x, points.get(i).y, points.get(i).z, 3, 0, 0, 0, 0);
            }
        }

        if (catenaryData.length == 0)
            catenaryData.length = pos1.distanceTo(pos2);

        List<CutWireEntry> cuts = new ArrayList<>();
        cutsByWire.put(connection, cuts);
        AABB bb = new AABB(pos1, catenaryData.isLow ? pos2 : pos2.add(0, 1.5, 0)).inflate(0.5);
        ConnectionEntry connectionEntry = new ConnectionEntry(pos1, pos2, points, catenaryData, bb, true, cuts);
        connections.put(connection, connectionEntry);

        SectionPos prevSection = null;
        for (Vec3 point : points) {
            SectionPos section = SectionPos.of(point);
            if (!Objects.equals(prevSection, section)) {
                connectionsBySection.computeIfAbsent(section.asLong(), s -> new HashMap<>()).put(connection, connectionEntry);
                prevSection = section;
            }
        }
        if (load)
            return;
        sd.wireCrossContactModule.onWireAdded(connection, connectionEntry);
        reloadLazyConnections();
    }

    public AttachedNode createCut(WireCutHandle handle, InWorldNodeConnection connection, float point) {
        if (handle.invalidated)
            throw new IllegalArgumentException("Used an invalidated wire cut handle! " + handle);
        ConnectionEntry connectionEntry = connections.get(connection);
        if (connectionEntry == null)
            throw new IllegalArgumentException("Connection: " + connection.toString() + " doesn't exist!");
        Map<AttachedNode, CutWireEntry> entries = cutsByHandle.get(handle.id);
        AttachedNode node = cutNodeGenerator.newNode();
        CutWireEntry entry = new CutWireEntry(node, point, handle, connectionEntry.cuts);
        entries.put(node, entry);
        // Put it in the right place so its sorted
        for (int i = 0; i < connectionEntry.cuts.size() + 1; i++) {
            if (i == connectionEntry.cuts.size()) {
                // If it's the largest one, this gets called
                connectionEntry.cuts.add(entry);
                break;
            }

            if (connectionEntry.cuts.get(i).point > point) {
                connectionEntry.cuts.add(i, entry);
                break;
            }
        }
        reloadLazyConnections();
        return node;
    }

    public AttachedNode createCut(WireCutHandle handle, ConnectionEntry connectionEntry, float point) {
        if (handle.invalidated)
            throw new IllegalArgumentException("Used an invalidated wire cut handle! " + handle);
        Map<AttachedNode, CutWireEntry> entries = cutsByHandle.get(handle.id);
        AttachedNode node = cutNodeGenerator.newNode();
        CutWireEntry entry = new CutWireEntry(node, point, handle, connectionEntry.cuts);
        entries.put(node, entry);
        // Put it in the right place so its sorted
        for (int i = 0; i < connectionEntry.cuts.size() + 1; i++) {
            if (i == connectionEntry.cuts.size()) {
                // If it's the largest one, this gets called
                connectionEntry.cuts.add(entry);
                break;
            }

            if (i > point) {
                connectionEntry.cuts.add(i, entry);
                break;
            }
        }
        reloadLazyConnections();
        return node;
    }


    public void removeCutsFrom(WireCutHandle handle) {
        if (handle.invalidated)
            throw new IllegalArgumentException("Used an invalidated wire cut handle! " + handle);
        Map<AttachedNode, CutWireEntry> entries = cutsByHandle.get(handle.id);
        entries.forEach(((node, cutWireEntry) ->
                cutWireEntry.originalList.remove(cutWireEntry)));
        entries.clear();
        reloadLazyConnections();
    }

    public void removeCut(WireCutHandle handle, AttachedNode node) {
        if (handle.invalidated)
            throw new IllegalArgumentException("Used an invalidated wire cut handle! " + handle);
        Map<AttachedNode, CutWireEntry> entries = cutsByHandle.get(handle.id);
        CutWireEntry entry = entries.remove(node);
        if (entry != null)
            entry.originalList.remove(entry);
        reloadLazyConnections();
    }

    public void relocateCut(WireCutHandle handle, AttachedNode node, float point) {
        if (handle.invalidated)
            throw new IllegalArgumentException("Used an invalidated wire cut handle! " + handle);
        Map<AttachedNode, CutWireEntry> entries = cutsByHandle.get(handle.id);
        CutWireEntry entry = entries.get(node);
        if (entry != null) {
            List<CutWireEntry> originalList = entry.originalList;
            CutWireEntry newEntry = new CutWireEntry(entry.node, point, entry.handle, originalList);
            originalList.set(originalList.indexOf(entry), newEntry);
            originalList.sort(Comparator.comparing(CutWireEntry::point));
            entries.replace(node, newEntry);
        }
        reloadLazyConnections();
    }

    /**
     * Creates a new handle for managing cuts.
     * Each handle can own some cuts. This reduces the risk of leaks.
     * @param name Display name of the handle
     * @return the new handle
     */
    public WireCutHandle createHandle(String name) {
        int id;
        if (freeHandleIDs.isEmpty())
            id = nextHandleID++;
        else
            id = freeHandleIDs.popInt();

        if (cutsByHandle.size() <= id)
            cutsByHandle.add(new HashMap<>());
        else
            cutsByHandle.set(id, new HashMap<>());
        return new WireCutHandle(id, name);
    }

    /**
     * It is possible your cuts may get removed at some point (wire relocations, etc.)
     * @param handle handle
     * @return If the node exists
     */
    public boolean cutExists(WireCutHandle handle, AttachedNode node) {
        if (handle.invalidated)
            throw new IllegalArgumentException("Used an invalidated wire cut handle! " + handle);
        return cutsByHandle.get(handle.id).containsKey(node);
    }

    /**
     * Removes all cuts owned by this handle and invalidates it.
     * @param handle handle
     */
    public void invalidateHandle(WireCutHandle handle) {
        removeCutsFrom(handle);
        handle.invalidated = true;
        freeHandleIDs.push(handle.id);
    }

    public Map<InWorldNodeConnection, ConnectionEntry> getConnectionsInSection(long section) {
        Map<InWorldNodeConnection, ConnectionEntry> out = connectionsBySection.get(section);
        if (out == null)
            return Collections.emptyMap();
        return out;
    }

    public Set<Map.Entry<InWorldNodeConnection, ConnectionEntry>> getAllConnections() {
        return connections.entrySet();
    }

    @SuppressWarnings("unused")
    public ConnectionEntry getConnection(InWorldNodeConnection connection) {
        return connections.get(connection);
    }

    public void rebuild() {
        sd.wireCrossContactModule.onRebuild();
        rebuild = false;
        reloadLazyConnections();
    }

    public Collection<ConnectionEntry> getAllConnectionEntries() {
        return connections.values();
    }

    public boolean relocateConnection(InWorldNodeConnection connection, WireData wireData) {
        ConnectionEntry v = connections.get(connection);
        if (v == null)
            return false;
        sd.wireCrossContactModule.onWireRemoved(connection, v);
        SectionPos prevSection1 = null;
        for (Vec3 point1 : v.points) {
            SectionPos section1 = SectionPos.of(point1);
            if (!Objects.equals(prevSection1, section1)) {
                prevSection1 = section1;
                Map<InWorldNodeConnection, ConnectionEntry> c = connectionsBySection.get(section1.asLong());
                if (c == null)
                    continue;
                c.remove(connection);
                if (c.isEmpty())
                    connectionsBySection.remove(section1.asLong());
            }
        }

        Vec3 pos1 = sd.getNodePosition(connection.node1());
        Vec3 pos2 = sd.getNodePosition(connection.node2());
        double distance = pos1.distanceTo(pos2);
        List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance), 0.5f);

        double miny = pos1.y;

        double length = wireData.length;
        double lengthDiff = pos1.distanceTo(pos2) - length;
        double lengthRatio = length == 0 ? 1 : pos1.distanceTo(pos2) / length;
        boolean shouldBreakWire = (lengthDiff > 1) && (wireData.getSag() == 0 ? Math.abs(1 - lengthRatio) > 0.1 : lengthRatio > 1.4f);

        for (Vec3 point : points)
            miny = Math.min(miny, point.y());


        List<CutWireEntry> cuts = cutsByWire.get(connection);
        if (cuts == null) // ???
            cutsByWire.put(connection, cuts = new ArrayList<>());

        AABB bb = new AABB(pos1, pos2).setMinY(miny).inflate(0.25);
        ConnectionEntry connectionEntry = new ConnectionEntry(pos1, pos2, points, wireData, bb, cuts);

        long prevSection = 0;
        for (Vec3 point : points) {
            long section = SectionPos.asLong(
                    Mth.floor(point.x) >> 4,
                    Mth.floor(point.y) >> 4,
                    Mth.floor(point.z) >> 4);
            if (section != prevSection) {
                connectionsBySection.computeIfAbsent(section, s -> new HashMap<>()).put(connection, connectionEntry);
                prevSection = section;
            }
        }

        sd.wireCrossContactModule.onWireAdded(connection, connectionEntry);
        reloadLazyConnections();

        return shouldBreakWire;
    }

    public static class WireCutHandle {
        private final int id;
        public final String name;
        private boolean invalidated = false;

        private WireCutHandle(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + "-" + name;
        }

        public boolean valid() {
            return !invalidated;
        }
    }

    @ParametersAreNonnullByDefault
    public record CutWireEntry(AttachedNode node, float point, WireCutHandle handle, List<CutWireEntry> originalList) {
        @Override
        public @NotNull String toString() {
            return node + " at " + point + "@" + handle;
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, point, handle);
        }
    }
}
