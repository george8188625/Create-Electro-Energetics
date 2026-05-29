package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.config.CEEConfigs;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public record ConfigureFusePacket(int targetCurrent) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, ConfigureFusePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConfigureFusePacket::targetCurrent,
            ConfigureFusePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        ItemStack mainHandItem = player.getMainHandItem();

        if (player.isSpectator() || !mainHandItem.is(CEETags.FUSE_AMPERAGE_SETTING))
            return;
        int targetCurrent = Mth.clamp(targetCurrent(), 1, CEEConfigs.server().maxFuseAmperage.get());

        if (targetCurrent == 100)
            mainHandItem.remove(CEEDataComponents.FUSE_AMPERAGE);
        else
            mainHandItem.set(CEEDataComponents.FUSE_AMPERAGE, targetCurrent);

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CONFIGURE_FUSE;
    }
}
