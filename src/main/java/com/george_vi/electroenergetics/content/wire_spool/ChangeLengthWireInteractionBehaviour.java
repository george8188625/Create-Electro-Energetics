package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChangeLengthWireInteractionBehaviour extends WireInteractionBehaviour {

    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InWorldNodeConnection connection = point.connection();
        WireData connectionData = sd.getConnectionData(connection);

        if (connectionData == null)
            return;

        if (connectionData.wireType().getDrops() != stack.getItem())
            return;
            
        if (player.isShiftKeyDown())
            connectionData.length -= 0.25f;
        else
            connectionData.length += 0.25f;
        if (sd.wireSimulationState.relocateConnection(connection, connectionData)) {
            Vec3 pos = VecHelper.lerp(0.5f, connection.node1().sableSourcePos(level).getCenter(), connection.node2().sableSourcePos(level).getCenter());
            Containers.dropItemStack(level, pos.x, pos.y, pos.z, new ItemStack(sd.removeConnection(connection).wireType().getDrops(), CEEConfigs.server().wiresPerSpool.get()));
        } else
            sd.wireSync.handleWireAdded(connection, connectionData);
    }

    @Override
    public boolean isActiveFor(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        for (WireType wireType : CEERegistries.WIRE_TYPE)
            if (wireType.getSag() != 0 && wireType.getDrops() == stack.getItem())
                return true;
        return false;
    }

    @Override
    public DisplayType getWireDisplayType(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return DisplayType.NONE;
    }
}
