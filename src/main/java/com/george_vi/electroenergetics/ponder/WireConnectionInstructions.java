package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.instruction.FadeIntoSceneInstruction;
import net.createmod.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.Collections;

public class WireConnectionInstructions {
    final SceneBuilder builder;

    public WireConnectionInstructions(SceneBuilder builder) {
        this.builder = builder;
    }

    public ElementLink<WirePonderElement> createConnection(InWorldNode node1, InWorldNode node2, WireType type) {
        return createConnection(node1, node2, type, 1);
    }

    public ElementLink<WirePonderElement> createConnection(InWorldNode node1, InWorldNode node2) {
        return createConnection(node1, node2, CEEWireTypes.STANDARD.get(), 1);
    }

    public ElementLink<WirePonderElement> createConnection(InWorldNode node1, InWorldNode node2, WireType type, int duration) {
        WireData data = new WireData(type, 0, Collections.emptyList(), 0);
        CreateWireConnectionInstruction instruction = new CreateWireConnectionInstruction(duration, Direction.DOWN,
                new WirePonderElement(node1, node2, data, false));
        builder.addInstruction(instruction);
        return instruction.createLink(builder.getScene());
    }

    public ElementLink<WirePonderElement> createCatenaryConnection(BlockPos pos1, BlockPos pos2, WireType type, int duration) {
        WireData data = new WireData(type, 0, Collections.emptyList(), 0);
        CreateWireConnectionInstruction instruction = new CreateWireConnectionInstruction(duration, Direction.DOWN,
                new WirePonderElement(new InWorldNode(0, pos1), new InWorldNode(0, pos2), data, true));
        builder.addInstruction(instruction);
        return instruction.createLink(builder.getScene());
    }

    public ElementLink<WirePonderElement> createConnection(InWorldNode node1, InWorldNode node2, WireData data) {
        CreateWireConnectionInstruction instruction = new CreateWireConnectionInstruction(1, Direction.DOWN,
                new WirePonderElement(node1, node2, data, false));
        builder.addInstruction(instruction);
        return instruction.createLink(builder.getScene());
    }

    public ElementLink<CurrentVisualizationPonderElement> createCurrentVisualization(InWorldNode node1,
                                                                                     InWorldNode node2, float sag,
                                                                                     float speed, boolean valid) {

        return createCurrentVisualization(node1, node2, sag, speed, 0, valid);
    }

    public ElementLink<CurrentVisualizationPonderElement> createCurrentVisualization(InWorldNode node1,
                                                                                     InWorldNode node2, float sag,
                                                                                     float speed, float offset,
                                                                                     boolean valid) {

        CreateCurrentVisualizationInstruction instruction = new CreateCurrentVisualizationInstruction(1, Direction.DOWN,
                new CurrentVisualizationPonderElement(node1, node2, speed, sag, offset, valid));
        builder.addInstruction(instruction);
        return instruction.createLink(builder.getScene());
    }

    public void removeCurrentVisualization(ElementLink<CurrentVisualizationPonderElement> elementLink) {
        builder.addInstruction(new RemoveCurrentVisualizationInstruction(1, Direction.UP, elementLink));

    }

    public void removeConnection(ElementLink<WirePonderElement> elementLink) {
        removeConnection(elementLink, 1);
    }

    public void removeConnection(ElementLink<WirePonderElement> elementLink, int duration) {
        builder.addInstruction(new RemoveWireConnectionInstruction(duration, Direction.UP, elementLink));
    }

    public void setBulbState(BlockPos bulbPos, float state) {
        builder.world().modifyBlock(bulbPos, s -> s.setValue(BulbBlock.LIGHT, Mth.clamp(Mth.floor(state * 15), 0, 15)), false);
        builder.world().modifyBlockEntity(bulbPos, BulbBlockEntity.class, be -> be.setLight(state));
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

    public static class CreateCurrentVisualizationInstruction extends FadeIntoSceneInstruction<CurrentVisualizationPonderElement> {
        public CreateCurrentVisualizationInstruction(int fadeInTicks, Direction fadeInFrom, CurrentVisualizationPonderElement element) {
            super(fadeInTicks, fadeInFrom, element);
        }

        @Override
        protected Class<CurrentVisualizationPonderElement> getElementClass() {
            return CurrentVisualizationPonderElement.class;
        }
    }

    public static class RemoveCurrentVisualizationInstruction extends FadeOutOfSceneInstruction<CurrentVisualizationPonderElement> {
        public RemoveCurrentVisualizationInstruction(int fadeOutTicks, Direction fadeOutTo, ElementLink<CurrentVisualizationPonderElement> link) {
            super(fadeOutTicks, fadeOutTo, link);
        }
    }
}
