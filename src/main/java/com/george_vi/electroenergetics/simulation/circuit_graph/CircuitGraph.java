package com.george_vi.electroenergetics.simulation.circuit_graph;

import com.george_vi.electroenergetics.simulation.WrappedIndexedNode;
import com.george_vi.electroenergetics.simulation.electrical_properties.CoupledProperties;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingElectricalProperties;
import com.george_vi.electroenergetics.simulation.electrical_properties.MicroTickingInvertedElectricalProperties;
import com.george_vi.electroenergetics.simulation.util.CholeskySolver;
import com.george_vi.electroenergetics.simulation.util.DataPacker;
import com.george_vi.electroenergetics.simulation.util.LUSolver;
import com.george_vi.electroenergetics.simulation.util.SparseMatrix;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This object describes a linear electrical graph.
 * <br>
 * Works on {@link WrappedIndexedNode} as nodes and {@link ElectricalProperties} as edges
 */
public class CircuitGraph {
    private int ids = 0;
    private List<WrappedIndexedNode> graphNodes = new ArrayList<>();
    private List<Int2ObjectMap<ElectricalProperties>> graphEdges = new ArrayList<>();
    private Object2IntMap<WrappedIndexedNode> nodeIds = new Object2IntOpenHashMap<>();
    private Long2ObjectMap<ElectricalProperties> microTicked = new Long2ObjectOpenHashMap<>();
    private Long2DoubleMap voltageSources = new Long2DoubleOpenHashMap();
    private Long2DoubleMap currentSources = new Long2DoubleOpenHashMap();
    private Long2ObjectMap<LongDoublePair> coupledProperties = new Long2ObjectOpenHashMap<>();

    public CircuitGraph() {
        nodeIds.defaultReturnValue(-1);
    }

    public int pushNode(WrappedIndexedNode node) {
        int nodeId = this.ids++;
        graphNodes.add(node);
        graphEdges.add(new Int2ObjectArrayMap<>());
        nodeIds.put(node, nodeId);

        return nodeId;
    }

    public int getId(WrappedIndexedNode node) {
        return nodeIds.getInt(node);
    }

    public ElectricalProperties getConnection(int id1, int id2) {
        Int2ObjectMap<ElectricalProperties> adjacency = graphEdges.get(id1);
        if (adjacency == null)
            return ElectricalProperties.ZERO_CONDUCTANCE;
        return adjacency.getOrDefault(id2, ElectricalProperties.ZERO_CONDUCTANCE);
    }

    public void createConnection(int id1, int id2, ElectricalProperties properties) {
        if (properties instanceof CoupledProperties cp)
            throw new IllegalArgumentException("CoupledProperties added through createConnection!");

        Int2ObjectMap<ElectricalProperties> adjacency1 = graphEdges.get(id1);
        Int2ObjectMap<ElectricalProperties> adjacency2 = graphEdges.get(id2);
        if (adjacency1 == null || adjacency2 == null)
            return;
        adjacency1.put(id2, properties);
        adjacency2.put(id1, properties);
        if (properties instanceof MicroTickingElectricalProperties)
            microTicked.put(DataPacker.pack(id1, id2), properties);
        else if (properties instanceof MicroTickingInvertedElectricalProperties || id1 > id2)
            return;

        if (properties.isCurrentSource())
            currentSources.put(DataPacker.pack(id1, id2), properties.currentSource);
        if (properties.isVoltageSource())
            voltageSources.put(DataPacker.pack(id1, id2), properties.voltageSource);
    }

    public void createCoupledConnection(int ip1, int ip2, int is1, int is2, ElectricalProperties properties) {
        Int2ObjectMap<ElectricalProperties> adjacencyp1 = graphEdges.get(ip1);
        Int2ObjectMap<ElectricalProperties> adjacencyp2 = graphEdges.get(ip2);
        Int2ObjectMap<ElectricalProperties> adjacencys1 = graphEdges.get(is1);
        Int2ObjectMap<ElectricalProperties> adjacencys2 = graphEdges.get(is2);
        if (adjacencyp1 == null || adjacencyp2 == null ||
                adjacencys1 == null || adjacencys2 == null ||
                !(properties instanceof CoupledProperties cp))
            return;
        adjacencyp1.put(ip2, properties);
        adjacencyp2.put(ip1, properties);
        adjacencyp1.put(ip2, properties.invert());
        adjacencyp2.put(ip1, properties.invert());

        long packedPrimary = DataPacker.pack(ip1, ip2);
        long packedSecondary = DataPacker.pack(is1, is2);
        double ratio = cp.ratio();

        coupledProperties.put(packedPrimary, LongDoublePair.of(packedSecondary, ratio));
    }

    public void clearConnection(int id1, int id2) {
        Int2ObjectMap<ElectricalProperties> adjacency1 = graphEdges.get(id1);
        Int2ObjectMap<ElectricalProperties> adjacency2 = graphEdges.get(id2);
        if (adjacency1 == null || adjacency2 == null)
            return;
        ElectricalProperties properties1 = adjacency1.remove(id2);
        ElectricalProperties properties2 = adjacency2.remove(id1);

        long packed = DataPacker.pack(id1, id2);
        microTicked.remove(packed);
        currentSources.remove(packed);
        voltageSources.remove(packed);
        coupledProperties.remove(packed);

        long reversePacked = DataPacker.pack(id2, id1);
        microTicked.remove(reversePacked);
        currentSources.remove(reversePacked);
        voltageSources.remove(reversePacked);
        coupledProperties.remove(reversePacked);
    }

    public int size() {
        return nodeIds.size();
    }

    /**
     * Calculates the Norton equivalent of the circuit between the specified nodes.
     * <br>
     * Returns an {@link ElectricalProperties} object, that contains a current source and a resistor in parallel,
     * that behaves the same way as the full circuit.
     * <br>
     * There must not already exist a connection between the passed nodes.
     */
    public ElectricalProperties calculateNortonEquivalent(int id1, int id2) {
        createConnection(id1, id2, ElectricalProperties.INFINITE_CONDUCTANCE);
        formMatrix(DataPacker.pack(id1, id2), false);
        clearConnection(id1, id2);
        double[] result = LUSolver.solve(conductanceMatrix, rhsVector);

        double sourceCurrent = result[trackedVoltageSourceIndex];

        createConnection(id1, id2, new ElectricalProperties(1e+11d, 0, 1));
        formMatrix(0, true);
        clearConnection(id1, id2);
        // Use Cholesky if possible as it's faster
        result = matrixSPD ?
                CholeskySolver.solve(conductanceMatrix, rhsVector) :
                LUSolver.solve(conductanceMatrix, rhsVector);

        double sourceResistance = Math.abs(result[id1] - result[id2]);

        return new ElectricalProperties(sourceResistance, 0, sourceCurrent);
    }

    private boolean matrixSPD = true;
    private SparseMatrix conductanceMatrix;
    private double[] rhsVector;
    private int trackedVoltageSourceIndex;

    public void formMatrix(long keepTrackVoltageSource, boolean deactivateSources) {
        List<LongDoublePair> microVoltageSources = new ArrayList<>();
        List<LongDoublePair> microCurrentSources = new ArrayList<>();
        for (Long2ObjectMap.Entry<ElectricalProperties> entry : microTicked.long2ObjectEntrySet()) {
            if (entry.getValue().isVoltageSource())
                microVoltageSources.add(new LongDoubleImmutablePair(entry.getLongKey(), entry.getValue().voltageSource()));
            if (entry.getValue().isCurrentSource())
                microCurrentSources.add(new LongDoubleImmutablePair(entry.getLongKey(), entry.getValue().currentSource));
        }

        int size = size() + voltageSources.size() + microVoltageSources.size() + (coupledProperties.size() * 2);

        conductanceMatrix = new SparseMatrix(size);


        for (int nodeId = 0; nodeId < size(); nodeId++) {
            double totalConductance = nodeId == 0 ? 1 : 0;

            Int2ObjectMap<ElectricalProperties> adjacency = graphEdges.get(nodeId);
            for (Int2ObjectMap.Entry<ElectricalProperties> e : adjacency.int2ObjectEntrySet()) {
                int adjacentId = e.getIntKey();
                ElectricalProperties properties = e.getValue();

                double conductance = properties.conductance();

                totalConductance += conductance;
                if (conductance == 0)
                    continue;
                conductanceMatrix.set(nodeId, adjacentId, -conductance);
                conductanceMatrix.set(adjacentId, nodeId, -conductance);
            }
            conductanceMatrix.set(nodeId, nodeId, totalConductance);
        }

        rhsVector = new double[size];
        if (!deactivateSources)
            for (Long2DoubleMap.Entry e : currentSources.long2DoubleEntrySet()) {
                long packedConnection = e.getLongKey();
                double v = e.getDoubleValue();
                rhsVector[DataPacker.unpackFirstI(packedConnection)] += -v;
                rhsVector[DataPacker.unpackSecondI(packedConnection)] += v;
            }

        if (!deactivateSources)
            for (LongDoublePair e : microCurrentSources) {
                long packedConnection = e.firstLong();
                double v = e.secondDouble();
                rhsVector[DataPacker.unpackFirstI(packedConnection)] += -v;
                rhsVector[DataPacker.unpackSecondI(packedConnection)] += v;
            }

        int i = size();
        for (Long2DoubleMap.Entry e : voltageSources.long2DoubleEntrySet()) {
            long packedConnection = e.getLongKey();
            double v = deactivateSources ? 0 : e.getDoubleValue();
            int first = DataPacker.unpackFirstI(packedConnection);
            int second = DataPacker.unpackSecondI(packedConnection);
            conductanceMatrix.set(i, first, 1d);
            conductanceMatrix.set(i, second, -1d);
            conductanceMatrix.set(first, i, 1d);
            conductanceMatrix.set(second, i, -1d);
            rhsVector[i] = -v;
            if (packedConnection == keepTrackVoltageSource)
                trackedVoltageSourceIndex = i;
            i++;
        }

        for (LongDoublePair e : microVoltageSources) {
            long packedConnection = e.firstLong();
            double v = deactivateSources ? 0 : e.secondDouble();
            int first = DataPacker.unpackFirstI(packedConnection);
            int second = DataPacker.unpackSecondI(packedConnection);

            conductanceMatrix.set(i, first, 1d);
            conductanceMatrix.set(i, second, -1d);
            conductanceMatrix.set(first, i, 1d);
            conductanceMatrix.set(second, i, -1d);
            rhsVector[i] = -v;
            i++;
        }

        for (Map.Entry<Long, LongDoublePair> e : coupledProperties.entrySet()) {
            double ratio = e.getValue().secondDouble();
            int ip1 = DataPacker.unpackFirstI(e.getKey());
            int ip2 = DataPacker.unpackFirstI(e.getKey());
            int is1 = DataPacker.unpackFirstI(e.getValue().firstLong());
            int is2 = DataPacker.unpackFirstI(e.getValue().firstLong());

            // Primary (ROW I1)
            conductanceMatrix.set(ip1, i, +1);
            conductanceMatrix.set(ip2, i, -1);

            // Vp1 - Vp2 - n*(Vs1 - Vs2) = 0
            conductanceMatrix.set(i, ip1, +1);
            conductanceMatrix.set(i, ip2, -1);
            conductanceMatrix.set(i, is1, -ratio);
            conductanceMatrix.set(i, is2, ratio);
            i++;

            // Secondary (ROW I2)
            conductanceMatrix.set(is1, i, +1);
            conductanceMatrix.set(is2, i, -1);

            conductanceMatrix.set(i, i-1, ratio);
            conductanceMatrix.set(i, i, 1);
            i++;
        }

        if (!voltageSources.isEmpty() || !microVoltageSources.isEmpty() || !coupledProperties.isEmpty())
            matrixSPD = false;
    }

    public void clear() {
        ids = 0;
        graphEdges.clear();
        graphNodes.clear();
        nodeIds.clear();
        microTicked.clear();
        voltageSources.clear();
        currentSources.clear();
        coupledProperties.clear();
    }
}
