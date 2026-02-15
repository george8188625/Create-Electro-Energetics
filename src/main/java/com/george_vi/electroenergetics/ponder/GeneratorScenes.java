package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.WireType;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class GeneratorScenes {

    public static void alternator(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("alternator", "Setting up an alternator");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        WireType positiveWireType = CEEWireTypes.COLORED_WIRES.get(DyeColor.RED).get();
        WireType negativeWireType = CEEWireTypes.COLORED_WIRES.get(DyeColor.BLACK).get();
        Selection rotors = util.select().fromTo(6, 3, 6, 8, 3, 6);
        Selection magnets = util.select().fromTo(7, 1, 8, 8, 5, 4).substract(rotors);
        Selection connectors1 = util.select().fromTo(6, 1, 8, 6, 5, 4).substract(rotors);
        Selection cogs = util.select().fromTo(9, 0, 5, 9, 3, 6);
        Selection switches = util.select().fromTo(0, 1, 5, 4, 1, 7);
        BlockPos transformerPos = util.grid().at(1, 1, 2);
        Selection transformer = util.select().position(transformerPos);
        BlockPos gaugePos = util.grid().at(1, 1, 4);
        Selection gauge = util.select().position(gaugePos);
        BlockPos bulbPos = util.grid().at(4, 1, 1);
        Selection bulb = util.select().position(bulbPos);
        Selection connectors2 = util.select().fromTo(0, 1, 0, 2, 1, 0);

        scene.world().showSection(cogs, Direction.DOWN);
        scene.world().showSection(rotors, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Alternators can be used to convert rotational force to electricity")
                .pointAt(util.grid().at(6, 3, 6).getCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);
        scene.world().showSection(magnets, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Surround the rotors with magnets")
                .pointAt(util.grid().at(7, 3, 4).getCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);
        scene.world().setKineticSpeed(util.select().fromTo(7, 3, 6, 9, 1, 6), 16);
        scene.world().setKineticSpeed(util.select().fromTo(9, 2, 5, 9, 0, 5), -8);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("The alternator will start inducing a voltage.")
                .pointAt(util.grid().at(6, 3, 6).getCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);
        scene.world().showSection(connectors1, Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(switches, Direction.DOWN);
        scene.idle(20);
        connections.createConnection(new InWorldNode(1, 6, 3, 6), new InWorldNode(0, 6, 3, 8), positiveWireType);
        connections.createConnection(new InWorldNode(0, 6, 3, 8), new InWorldNode(0, 4, 1, 7), positiveWireType);
        connections.createConnection(new InWorldNode(0, 2, 1, 7), new InWorldNode(0, 0, 1, 7), positiveWireType);
        connections.createConnection(new InWorldNode(0, 0, 1, 7), new InWorldNode(0, 0, 1, 5), positiveWireType);

        connections.createConnection(new InWorldNode(0, 6, 3, 6), new InWorldNode(0, 6, 3, 4), negativeWireType);
        connections.createConnection(new InWorldNode(0, 6, 3, 4), new InWorldNode(0, 4, 1, 5), negativeWireType);
        scene.idle(20);
        scene.world().showSection(gauge, Direction.DOWN);
        scene.idle(20);
        connections.createConnection(new InWorldNode(0, 2, 1, 5), new InWorldNode(1, gaugePos), negativeWireType);
        connections.createConnection(new InWorldNode(0, 0, 1, 5), new InWorldNode(0, gaugePos), positiveWireType);
        scene.world().modifyBlockEntityNBT(gauge, ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .18f));
        scene.idle(20);

        scene.overlay().showText(20)
                .text("184V")
                .pointAt(gauge.getCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(60);

        scene.world().showSection(transformer, Direction.DOWN);

        scene.overlay().showText(70)
                .text("Use a Transformer to fine-tune the voltage.")
                .pointAt(transformer.getCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);
        connections.createConnection(new InWorldNode(0, 0, 1, 5), new InWorldNode(0, transformerPos), positiveWireType);

        connections.createConnection(new InWorldNode(0, 2, 1, 5), new InWorldNode(1, transformerPos), negativeWireType);
        scene.idle(20);

        scene.world().showSection(connectors2, Direction.DOWN);
        scene.world().showSection(bulb, Direction.DOWN);
        scene.idle(20);
        connections.createConnection(new InWorldNode(3, transformerPos), new InWorldNode(0, bulbPos), negativeWireType);

        connections.createConnection(new InWorldNode(2, transformerPos), new InWorldNode(0, 0, 1, 0), positiveWireType);
        connections.createConnection(new InWorldNode(0, 0, 1, 0), new InWorldNode(3, 2, 1, 0), positiveWireType);
        connections.createConnection(new InWorldNode(3, 2, 1, 0), new InWorldNode(1, bulbPos), positiveWireType);
        scene.world().modifyBlock(bulbPos, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulbPos, BulbBlockEntity.class, be -> be.light = 1);
        scene.idle(40);
        connections.createCurrentVisualization(new InWorldNode(1, 6, 3, 6), new InWorldNode(0, 6, 3, 8), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 6, 3, 8), new InWorldNode(0, 4, 1, 7), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 7), new InWorldNode(0, 0, 1, 7), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 7), new InWorldNode(0, 0, 1, 5), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 6, 3, 6), new InWorldNode(0, 6, 3, 4), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 6, 3, 4), new InWorldNode(0, 4, 1, 5), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 5), new InWorldNode(0, 2, 1, 5), 0, -1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 7), new InWorldNode(0, 2, 1, 7), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 5), new InWorldNode(0, transformerPos), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 5), new InWorldNode(1, transformerPos), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(3, transformerPos), new InWorldNode(0, bulbPos), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(2, transformerPos), new InWorldNode(0, 0, 1, 0), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 0), new InWorldNode(3, 2, 1, 0), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(3, 2, 1, 0), new InWorldNode(1, bulbPos), 1, 1, true);
        scene.idle(40);

        scene.overlay().showText(70)
                .text("You might want to use a Voltage Regulator to regulate the voltage even further.")
                .pointAt(transformer.getCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);
    }
}
