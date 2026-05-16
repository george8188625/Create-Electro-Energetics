package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.bundled_wire.BundledWireTerminationState;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class CEEDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateElectroEnergetics.ID);

    public static final DataComponentType<InWorldNode> SELECTED_NODE = register("selected_node",
            builder -> builder
                    .persistent(InWorldNode.CODEC)
                    .networkSynchronized(InWorldNode.STREAM_CODEC));

    public static final DataComponentType<UUID> OWNER = register("owner",
            builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    public static final DataComponentType<String> OWNER_NAME = register("owner_name",
            builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.stringUtf8(32)));

    public static final DataComponentType<Double> ENERGY = register("energy",
            builder -> builder
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE));

    public static final DataComponentType<InWorldNodeConnection> NODE_CONNECTION = register("node_connection",
            builder -> builder
                    .persistent(InWorldNodeConnection.CODEC)
                    .networkSynchronized(InWorldNodeConnection.STREAM_CODEC));

    public static final DataComponentType<BundledWireTerminationState> SELECTED_FREE_POS = register("selected_free_pos",
            builder -> builder
                    .persistent(BundledWireTerminationState.CODEC)
                    .networkSynchronized(BundledWireTerminationState.STREAM_CODEC));

    public static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }

}
