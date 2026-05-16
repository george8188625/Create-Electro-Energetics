package com.george_vi.electroenergetics.content.bundled_wire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BundledWireTerminationState(BlockPos pos, Direction facing, boolean roll, boolean flip) {
    public static final Codec<BundledWireTerminationState> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.fieldOf("pos").forGetter(BundledWireTerminationState::pos))
            .and(Direction.CODEC.fieldOf("facing").forGetter(BundledWireTerminationState::facing))
            .and(Codec.BOOL.fieldOf("roll").forGetter(BundledWireTerminationState::roll))
            .and(Codec.BOOL.fieldOf("flip").forGetter(BundledWireTerminationState::flip))
            .apply(instance, BundledWireTerminationState::new));

    public static final StreamCodec<ByteBuf, BundledWireTerminationState> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BundledWireTerminationState::pos,
            Direction.STREAM_CODEC, BundledWireTerminationState::facing,
            ByteBufCodecs.BOOL, BundledWireTerminationState::roll,
            ByteBufCodecs.BOOL, BundledWireTerminationState::flip,
            BundledWireTerminationState::new
    );
}
