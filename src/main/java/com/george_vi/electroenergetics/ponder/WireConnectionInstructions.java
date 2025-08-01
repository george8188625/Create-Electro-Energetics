package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.simulation.WireType;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.instruction.FadeIntoSceneInstruction;
import net.createmod.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class WireConnectionInstructions {
    final SceneBuilder builder;

    public WireConnectionInstructions(SceneBuilder builder) {
        this.builder = builder;
    }

    public ElementLink<WirePonderElement> createConnection(Vec3 pos1, Vec3 pos2, WireType type) {
        return createConnection(pos1, pos2, type, 1);
    }

    public ElementLink<WirePonderElement> createConnection(Vec3 pos1, Vec3 pos2) {
        return createConnection(pos1, pos2, CEEWireTypes.STANDARD.get(), 1);
    }

    public ElementLink<WirePonderElement> createConnection(Vec3 pos1, Vec3 pos2, WireType type, int duration) {
        CreateWireConnectionInstruction instruction = new CreateWireConnectionInstruction(duration, Direction.DOWN, new WirePonderElement(pos1, pos2, type));
        builder.addInstruction(instruction);
        return instruction.createLink(builder.getScene());
    }

    public void removeConnection(ElementLink<WirePonderElement> elementLink) {
        removeConnection(elementLink, 1);
    }

    public void removeConnection(ElementLink<WirePonderElement> elementLink, int duration) {
        builder.addInstruction(new RemoveWireConnectionInstruction(duration, Direction.UP, elementLink));
    }

    public static class CreateWireConnectionInstruction extends FadeIntoSceneInstruction<WirePonderElement> {
        public CreateWireConnectionInstruction(int fadeInTicks, Direction fadeInFrom, WirePonderElement element) {
            super(fadeInTicks, fadeInFrom, element);
        }

        @Override
        protected Class<WirePonderElement> getElementClass() {
            return WirePonderElement.class;
        }
    }

    public static class RemoveWireConnectionInstruction extends FadeOutOfSceneInstruction<WirePonderElement> {
        public RemoveWireConnectionInstruction(int fadeOutTicks, Direction fadeOutTo, ElementLink<WirePonderElement> link) {
            super(fadeOutTicks, fadeOutTo, link);
        }
    }
}
