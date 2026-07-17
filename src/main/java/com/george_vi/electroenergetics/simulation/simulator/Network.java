package com.george_vi.electroenergetics.simulation.simulator;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.SimulationNode;
import com.george_vi.electroenergetics.simulation.electrical_properties.*;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.optimization.*;
import com.george_vi.electroenergetics.simulation.util.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.*;

import java.util.*;

public class Network {
    private static final int MAX_ITERATIONS = 100;
    final Set<SimulationNode> allNodes;
    final CircuitBuilder builder;
    final InfrastructureSavedData sd;

    private CircuitNodeList optimizedNodeList;
    private int[] originalOptimizedNodes;

    final Long2DoubleMap voltageSources = new Long2DoubleOpenHashMap();
    final Long2ObjectMap<ElectricalProperties> originalMicroTicked = new Long2ObjectOpenHashMap<>();
    final Long2ObjectMap<ElectricalProperties> simulationMicroTicked = new Long2ObjectOpenHashMap<>();
    final LongList solverIterationTickerIDs = new LongArrayList();
    final List<ISolverIterationTicker> solverIterationTickers = new ArrayList<>();

    public SparseMatrix conductanceMatrix;
    public double[] rhsVector;
    public boolean matrixNonSPD = false;
    List<CoupledProperties> coupledProperties;

    Deque<TopologyOptimizationEntry> optimizations = new ArrayDeque<>();
    private final int[] allOriginalNodeIDs;

    /**
     * for the Newton iteration
     */
    public double[] x;

    private static final byte UNGROUNDED = 0;
    private static final byte GROUNDED = 1;
    private static final byte FIXED = 2;

    public Network(Collection<SimulationNode> allNodes, CircuitBuilder builder, InfrastructureSavedData sd) {
        allOriginalNodeIDs = new int[allNodes.size()];
        int i = 0;
        for (SimulationNode node : allNodes)
            allOriginalNodeIDs[i++] = node.ordinal;
        this.allNodes = new HashSet<>(allNodes);
        this.builder = builder;
        this.sd = sd;
    }

    public void mapToSimNodes() {
        optimizedNodeList = new CircuitNodeList(allNodes.size());
        coupledProperties = new ArrayList<>();
        originalOptimizedNodes = new int[allNodes.size()];

        for (SimulationNode node : allNodes) {
            int nodeID = optimizedNodeList.addNode();
            originalOptimizedNodes[nodeID] = node.ordinal;
            node.simNodeID = nodeID;
        }

        byte[] currentRegionGrounded = builder.currentRegionGrounded;
        for (SimulationNode node : allNodes) {
            int nodeID = node.simNodeID;
            Int2ObjectMap<ElectricalProperties> nodeAdjacency = getAdjacency(node);
            for (Int2ObjectMap.Entry<ElectricalProperties> e : nodeAdjacency.int2ObjectEntrySet()) {
                int neighborID = builder.getNode(e.getIntKey()).simNodeID;
                ElectricalProperties connectionProperties = e.getValue();

                if (node.groundConductance != 0) {
                    optimizedNodeList.setGroundConductance(nodeID, node.groundConductance);
                    currentRegionGrounded[node.currentRegionID] = GROUNDED;
                }

                // Only compute the connection once!
                if (neighborID > nodeID)
                    continue;

                optimizedNodeList.connect(nodeID, neighborID, connectionProperties);
                if (connectionProperties instanceof ISolverIterationTicker t) {
                    solverIterationTickers.add(t);
                    solverIterationTickerIDs.add(DataPacker.pack(nodeID, neighborID));
                } else if (connectionProperties.invert() instanceof ISolverIterationTicker t) {
                    solverIterationTickers.add(t);
                    solverIterationTickerIDs.add(DataPacker.pack(neighborID, nodeID));
                } else if (connectionProperties instanceof MicroTickingElectricalProperties) {
                    simulationMicroTicked.put(DataPacker.pack(nodeID, neighborID), connectionProperties);
                    originalMicroTicked.put(DataPacker.pack(originalOptimizedNodes[nodeID], originalOptimizedNodes[neighborID]), connectionProperties);
                } else if (connectionProperties.invert() instanceof MicroTickingElectricalProperties microTicking) {
                    simulationMicroTicked.put(DataPacker.pack(neighborID, nodeID), microTicking);
                    originalMicroTicked.put(DataPacker.pack(originalOptimizedNodes[neighborID], originalOptimizedNodes[nodeID]), microTicking);
                } else if (connectionProperties instanceof CoupledProperties cp && cp.isPrimary()) {
                    coupledProperties.add(cp);
                } else {
                    if ((connectionProperties.isVoltageSource()))
                        voltageSources.put(DataPacker.pack(nodeID, neighborID), connectionProperties.voltageSource());
                }
            }
        }

        for (SimulationNode node : allNodes) {
            if (currentRegionGrounded[node.currentRegionID] == UNGROUNDED) {
                // "ground" it so it can be solved
                int nodeID = node.simNodeID;
                optimizedNodeList.setGroundConductance(nodeID, 1);
                currentRegionGrounded[node.currentRegionID] = FIXED;
            }
        }

    }

    public void optimize() {
        NetworkOptimizer optimizer = new NetworkOptimizer(this);
        while (allNodes.size() > 0) {
            if (optimizer.runOptimizationPass())
                break;
        }
    }

    private Int2ObjectMap<ElectricalProperties> getAdjacency(SimulationNode node) {
        return node.localAdjacencyOverride == null ? node.adjacency : node.localAdjacencyOverride;
    }

    private Int2ObjectMap<ElectricalProperties> overrideAdjacency(SimulationNode node) {
        if (node.localAdjacencyOverride == null) {
            node.localAdjacencyOverride = new Int2ObjectArrayMap<>(node.adjacency.size());
            node.localAdjacencyOverride.putAll(node.adjacency);
        }

        return node.localAdjacencyOverride;
    }

    public void formMatrix() {
        matrixNonSPD = false;
        List<LongDoublePair> microVoltageSources = new ArrayList<>();
        for (Long2ObjectMap.Entry<ElectricalProperties> entry : simulationMicroTicked.long2ObjectEntrySet()) {
            if (entry.getValue().isVoltageSource())
                microVoltageSources.add(new LongDoubleImmutablePair(entry.getLongKey(), entry.getValue().voltageSource()));
        }

        int size = optimizedNodeList.totalNodes() + voltageSources.size() + microVoltageSources.size() + (coupledProperties.size() * 2);

        conductanceMatrix = new SparseMatrix(size);
        rhsVector = new double[size];
        boolean firstIteration = false;
        if (x == null) {
            x = new double[size];
            firstIteration = true;
        }

        for (int i = 0; i < solverIterationTickerIDs.size(); i++) {
            long packedConnection = solverIterationTickerIDs.getLong(i);
            int nodeID = DataPacker.unpackFirstI(packedConnection);
            int neighborID = DataPacker.unpackSecondI(packedConnection);
            ISolverIterationTicker ticker = solverIterationTickers.get(i);
            ticker.tick(x[nodeID], x[neighborID], firstIteration);
        }

        for (int nodeID = 0; nodeID < optimizedNodeList.totalNodes(); nodeID++) {
            double totalConductance = 0;
            for (Int2ObjectMap.Entry<ElectricalProperties> e : optimizedNodeList.getNeighbors(nodeID).int2ObjectEntrySet()) {
                int neighborID = e.getIntKey();
                ElectricalProperties properties = e.getValue();

                double conductance = properties.conductance();
                totalConductance += conductance + properties.gMin();
                if (neighborID > nodeID)
                    continue;

                if (properties instanceof NonlinearProperties nl) {
                    nl.stampNonLinear(x[nodeID], x[neighborID], conductanceMatrix, rhsVector, nodeID, neighborID, firstIteration);
                } else if (properties.invert() instanceof NonlinearProperties nl) {
                    nl.stampNonLinear(x[neighborID], x[nodeID], conductanceMatrix, rhsVector, neighborID, nodeID, firstIteration);
                }


                // apply current source
                if (properties.isCurrentSource()) {
                    double v = properties.currentSource();
                    rhsVector[nodeID] += v;
                    rhsVector[neighborID] -= v;
                }

                if (conductance == 0)
                    continue;

                conductanceMatrix.add(nodeID, neighborID, -conductance);
                conductanceMatrix.add(neighborID, nodeID, -conductance);
            }
            totalConductance += Math.abs(optimizedNodeList.getGroundConductance(nodeID));
            conductanceMatrix.add(nodeID, nodeID, totalConductance);
        }

        int i = optimizedNodeList.totalNodes();
        for (Long2DoubleMap.Entry e : voltageSources.long2DoubleEntrySet()) {
            long packedConnection = e.getLongKey();
            double v = e.getDoubleValue();
            int first = DataPacker.unpackFirstI(packedConnection);
            int second = DataPacker.unpackSecondI(packedConnection);
            conductanceMatrix.set(i, first, 1d);
            conductanceMatrix.set(i, second, -1d);
            conductanceMatrix.set(first, i, 1d);
            conductanceMatrix.set(second, i, -1d);
            rhsVector[i] = -v;
            i++;
            matrixNonSPD = true;
        }

        for (LongDoublePair e : microVoltageSources) {
            long packedConnection = e.firstLong();
            double v = e.secondDouble();
            int first = DataPacker.unpackFirstI(packedConnection);
            int second = DataPacker.unpackSecondI(packedConnection);

            conductanceMatrix.set(i, first, 1d);
            conductanceMatrix.set(i, second, -1d);
            conductanceMatrix.set(first, i, 1d);
            conductanceMatrix.set(second, i, -1d);
            rhsVector[i] = -v;
            matrixNonSPD = true;
            i++;
        }

        for (CoupledProperties cp : coupledProperties) {
            SimulationNode p1 = builder.getNode(cp.nodes().node1());
            SimulationNode p2 = builder.getNode(cp.nodes().node2());
            SimulationNode s1 = builder.getNode(cp.coupledNodes().node1());
            SimulationNode s2 = builder.getNode(cp.coupledNodes().node2());
            if (p1 == null || p2 == null || s1 == null || s2 == null)
                continue;

            int ip1 = p1.simNodeID;
            int ip2 = p2.simNodeID;
            int is1 = s1.simNodeID;
            int is2 = s2.simNodeID;

            // Primary (ROW I1)
            conductanceMatrix.set(ip1, i, +1);
            conductanceMatrix.set(ip2, i, -1);

            // Vp1 - Vp2 - n*(Vs1 - Vs2) = 0
            conductanceMatrix.set(i, ip1, +1);
            conductanceMatrix.set(i, ip2, -1);
            conductanceMatrix.add(i, is1, -cp.ratio());
            conductanceMatrix.add(i, is2, cp.ratio());
            i++;

            // Secondary (ROW I2)
            conductanceMatrix.set(is1, i, +1);
            conductanceMatrix.set(is2, i, -1);

            conductanceMatrix.set(i, i-1, cp.ratio());
            conductanceMatrix.set(i, i, 1);
            i++;
            matrixNonSPD = true;
        }
    }

    public void getResults(double[] mnaResult, double[] toFill, int microTick, int totalMicroTicks) {

        for (SimulationNode node : allNodes) {
            toFill[node.ordinal * totalMicroTicks + microTick] = mnaResult[node.simNodeID];
        }

        for (TopologyOptimizationEntry entry : optimizations) {
            if (entry instanceof SimpleTopologyOptimizationEntry properties) {
                double v1 = toFill[properties.node1() * totalMicroTicks + microTick];
                double v2 = toFill[properties.node2() * totalMicroTicks + microTick];
                properties.properties().getVoltages(v1, v2, toFill, microTick, totalMicroTicks);
            } else if (entry instanceof StarToDeltaEntry delta) {
                double va = toFill[delta.na.ordinal * totalMicroTicks + microTick];
                double vb = toFill[delta.nb.ordinal * totalMicroTicks + microTick];
                double vc = toFill[delta.nc.ordinal * totalMicroTicks + microTick];
                toFill[delta.centralNode.ordinal * totalMicroTicks + microTick] = delta.calculateCenter(va, vb, vc);
            } else if (entry instanceof SetVoltageOptimizationEntry setV) {
                double vBase = toFill[setV.base() * totalMicroTicks + microTick];
                toFill[setV.dead() * totalMicroTicks + microTick] = vBase;
            } else if (entry instanceof CoupledPropertiesOptimizationEntry optimization) {
                double leftPrimary = toFill[optimization.leftPrimary() * totalMicroTicks + microTick];
                double rightPrimary = toFill[optimization.rightPrimary() * totalMicroTicks + microTick];
                double voltage = leftPrimary - rightPrimary;
                double current = voltage / optimization.replacementResistance();
                double scaledCurrent = current / optimization.ratio();
                double scaledVoltage = voltage * optimization.ratio();

                // set the origin voltage so it doesn't mess stuff up when a branch is connected to one of the nodes
                double originVoltage = 0;
                if (optimization.mode() == CoupledPropertiesOptimizationEntry.MODE_LEFT_BRANCH)
                    originVoltage = toFill[optimization.leftNode() * totalMicroTicks + microTick];
                else if (optimization.mode() == CoupledPropertiesOptimizationEntry.MODE_RIGHT_BRANCH)
                    originVoltage = toFill[optimization.rightNode() * totalMicroTicks + microTick] - scaledVoltage;
                else if (optimization.mode() == CoupledPropertiesOptimizationEntry.MODE_CENTER_BRANCH)
                    originVoltage = toFill[optimization.node() * totalMicroTicks + microTick] - (optimization.leftResistance() * scaledCurrent);
                //

                toFill[optimization.leftNode() * totalMicroTicks + microTick] = originVoltage;
                toFill[optimization.node() * totalMicroTicks + microTick] = originVoltage + optimization.leftResistance() * scaledCurrent;
                toFill[optimization.rightNode() * totalMicroTicks + microTick] = originVoltage + scaledVoltage;
            } else if (entry instanceof AdvancedCoupledPropertiesOptimizationEntry optimization) {
                double leftPrimary = toFill[optimization.leftPrimary() * totalMicroTicks + microTick];
                double rightPrimary = toFill[optimization.rightPrimary() * totalMicroTicks + microTick];
                double voltage = leftPrimary - rightPrimary;
                double scaledVoltage = voltage * optimization.ratio();

                // set the origin voltage so it doesn't mess stuff up when a branch is connected to one of the nodes
                double originVoltage = 0;
                if (optimization.mode() == CoupledPropertiesOptimizationEntry.MODE_LEFT_BRANCH)
                    originVoltage = toFill[optimization.leftNode() * totalMicroTicks + microTick];
                else if (optimization.mode() == CoupledPropertiesOptimizationEntry.MODE_RIGHT_BRANCH)
                    originVoltage = toFill[optimization.rightNode() * totalMicroTicks + microTick] - scaledVoltage;
                //

                double v1 = originVoltage;
                double v2 = originVoltage + scaledVoltage;
                toFill[optimization.leftNode() * totalMicroTicks + microTick] = v2;
                toFill[optimization.rightNode() * totalMicroTicks + microTick] = v1;

                optimization.properties().getVoltages(v1, v2, toFill, microTick, totalMicroTicks);
            }

        }
        // Fix grounds - offset the nodes of each network so that ground is at zero volts

        double[] currentRegionZeroPotential = builder.currentRegionZeroPotential;
        int[] nodeCurrentRegionID = builder.nodeCurrentRegionID;
        double[] nodeGroundConductance = builder.nodeGroundConductance;

        for (int id : allOriginalNodeIDs) {
            if (nodeGroundConductance[id] != 0) {
                currentRegionZeroPotential[nodeCurrentRegionID[id]] = toFill[id * totalMicroTicks + microTick];
            }
        }

        for (int id : allOriginalNodeIDs) {
            double offset = currentRegionZeroPotential[nodeCurrentRegionID[id]];
            toFill[id * totalMicroTicks + microTick] -= offset;
        }
    }

    double[] lastMNAResult;
    public void runSolver(double[] allVoltages, int microTick, int totalMicroTicks) {
        for (int i = 0;; i++) {
            formMatrix();
            double[] mnaResults;

            // Check if it's converged
            if (lastMNAResult != null) {
                double[] res = new double[x.length];
                conductanceMatrix.computeResidualInto(lastMNAResult, rhsVector, res);
                double normSqr = VectorHelpers.normSqr(res);
                if (normSqr < 1e-2d) {
                    getResults(lastMNAResult, allVoltages, microTick, totalMicroTicks);
                    break;
                }
            }

            if (matrixNonSPD)
                mnaResults = LUSolver.solve(conductanceMatrix, rhsVector);
            else
                mnaResults = CholeskySolver.solve(conductanceMatrix, rhsVector);
            lastMNAResult = mnaResults;
            if (x == null || x.length != mnaResults.length)
                x = mnaResults.clone();

            System.arraycopy(mnaResults, 0, x, 0, x.length);

            // Max iterations
            if (i >= MAX_ITERATIONS) {
                getResults(mnaResults, allVoltages, microTick, totalMicroTicks);
                break;
            }
        }
    }
}
