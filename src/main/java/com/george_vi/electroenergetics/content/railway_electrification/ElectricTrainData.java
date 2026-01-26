package com.george_vi.electroenergetics.content.railway_electrification;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.foundation.nodes.AttachedNode;
import com.george_vi.electroenergetics.foundation.nodes.PositionedAttachedNode;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ElectricTrainData {
    public double lastSpeed;
    public AttachedNode trainNode;
    public AttachedNode groundNode;
    public List<TrainPantographEntry> pantographs = new ArrayList<>();
    public int accumulators = 0;
    public double accumulatorCharge = 0d;
    public boolean hasCreativeSource = false;
}
