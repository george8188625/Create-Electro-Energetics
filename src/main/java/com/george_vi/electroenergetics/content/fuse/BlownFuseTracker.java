package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlownFuseTracker {
    private static final Map<Player, Pair<BlockPos, IntIntMutablePair>> FUSES = new HashMap<>();

    public static void tick() {
        for (Iterator<Pair<BlockPos, IntIntMutablePair>> iterator = FUSES.values().iterator(); iterator.hasNext(); ) {
            Pair<BlockPos, IntIntMutablePair> p = iterator.next();
            IntIntMutablePair pair = p.getSecond();
            int timeLeft = pair.firstInt();
            if (timeLeft <= 0)
                iterator.remove();
            else
                pair.first(timeLeft - 1);
        }
    }

    public static void onFuseBlow(Player player, BlockPos pos) {
        CEEAdvancements.BLOWN_FUSE.awardTo(player);
        Pair<BlockPos, IntIntMutablePair> p = FUSES.computeIfAbsent(player, k -> Pair.of(pos, new IntIntMutablePair(0, 0)));
        BlockPos prevPos = p.getFirst();
        IntIntMutablePair pair = p.getSecond();
        pair.first(600);

        pair.second(prevPos.equals(pos) ? pair.secondInt() + 1 : 1);
        FUSES.put(player, Pair.of(pos, pair));

        if (pair.secondInt() >= 3) {
            FUSES.remove(player);
            CEEAdvancements.FUSE_STREAK.awardTo(player);
        }
    }
}
