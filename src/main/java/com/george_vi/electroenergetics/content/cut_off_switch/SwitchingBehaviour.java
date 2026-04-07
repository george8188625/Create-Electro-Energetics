package com.george_vi.electroenergetics.content.cut_off_switch;

import com.george_vi.electroenergetics.CEESoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SwitchingBehaviour {
    public SwitchState state = SwitchState.OPEN;
    public boolean isArcing;
    public boolean isClosed;
    public int tick;

    public SwitchingBehaviour(CompoundTag tag) {
        state = SwitchState.values()[tag.getInt("State")];
        tick = tag.getInt("Tick");
        isArcing = tag.getBoolean("IsArcing");
        isClosed = tag.getBoolean("IsClosed");
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("State", state.ordinal());
        tag.putInt("Tick", tick);
        tag.putBoolean("IsArcing", isArcing);
        tag.putBoolean("IsClosed", isClosed);
        return tag;
    }

    public void close() {
        isClosed = true;
    }

    public void open() {
        isClosed = false;
    }

    public double resistance() {
        return switch (state) {
            case ARCING -> 400;
            case CLOSED -> 0.001;
            case OPENING -> 10000;
            default -> 0;
        };
    }

    public void postTick(double voltage, @Nullable Vec3 posToArc, Level level) {
        isArcing = false;

        if (isClosed)
            state = SwitchState.CLOSED;
        if ((!isClosed && state == SwitchState.OPENING) || state == SwitchState.ARCING) {
            if (Math.abs(voltage) > (state == SwitchState.ARCING ? 60 : 1000)) {
                if (state != SwitchState.ARCING)
                    tick = 10;
                state = SwitchState.ARCING;
            } else
                state = SwitchState.OPEN;
        }
        if (!isClosed && state == SwitchState.CLOSED)
            state = SwitchState.OPENING;

        if (state == SwitchState.ARCING)
            isArcing = true;


        if (isArcing && posToArc != null) {
            if (level.random.nextFloat() > 0.5)
                ((ServerLevel) level).sendParticles(ParticleTypes.BUBBLE_POP, posToArc.x, posToArc.y, posToArc.z, 10, 0.05, 0.05, 0.05, 0);

            if (tick >= 10) {
                level.playSound(null, posToArc.x, posToArc.y, posToArc.z, CEESoundEvents.SHORT_ARC.get(), SoundSource.BLOCKS, 0.7f, 1f);
                tick = 0;
            }
            tick++;
        }
    }

    public enum SwitchState {
        OPEN,
        CLOSED,
        OPENING,
        ARCING;
    }
}
