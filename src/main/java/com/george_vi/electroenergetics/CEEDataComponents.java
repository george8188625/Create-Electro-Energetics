package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.foundation.Node;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class CEEDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateElecrtoEnergetics.ID);

    public static final DataComponentType<Node> SELECTED_NODE = register("selected_node", builder -> builder.persistent(Node.CODEC).networkSynchronized(Node.STREAM_CODEC));
    public static final DataComponentType<UUID> OWNER = register("owner", builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));
    public static final DataComponentType<String> OWNER_NAME = register("owner_name", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.stringUtf8(32)));
    public static final DataComponentType<Double> ENERGY = register("energy", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    public static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }

}
