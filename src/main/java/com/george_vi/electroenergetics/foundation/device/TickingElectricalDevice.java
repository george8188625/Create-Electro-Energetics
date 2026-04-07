package com.george_vi.electroenergetics.foundation.device;

import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;

public interface TickingElectricalDevice extends ElectricalDevice {

    void preTick(BridgeCollector bridges);

    void postTick(SimulationResults results);
}
