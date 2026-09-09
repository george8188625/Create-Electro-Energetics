package com.george_vi.electroenergetics.content.railway_electrification;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.simulation.infrastructure.WireSimulationState;

import java.util.ArrayList;
import java.util.List;

public class ElectricTrainData {
    public List<TrainPantographEntry> pantographs = new ArrayList<>();
    public int accumulators = 0;
    public double accumulatorCharge = 0d;
    public double accumulatorChargeVoltage = 0d;
    public double accumulatorActualVoltage = 0d;
    public boolean hasCreativeSource = false;
    public boolean isPowered = false;
    public double lastVoltage = 0d;

    public double lastSpeed;
    public float maxSpeed;
    public AttachedNode trainNode;
    public AttachedNode groundNode;
    public WireSimulationState connectedWireState;
    public WireSimulationState.WireCutHandle wireCutHandle;

    // Total current draw for display on ammeters attached to train contraptions
    public double displayCurrent = 0;

    // Packet throttling for gauge data sync
    public double lastSyncedVoltage = 0;
    public double lastSyncedCurrent = 0;
    public int ticksSinceGaugeSync = 0;
}
