package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class TransformerScenes {
    public static void transformer(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("transformer", "Changing voltages");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.world().showSection(util.select().position(4, 1, 0), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 4), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 8), Direction.DOWN);
        scene.idle(20);

        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 6), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Transformers can be used to change voltage.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.UP))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(80);

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 0)),
                new InWorldNode(1, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 2)));

        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));

        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 2), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .3f));

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Simply set the ratio.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(20)
                .sharedText("ratio1over2")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 8)),
                new InWorldNode(1, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(2, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 6)));

        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 8)),
                new InWorldNode(0, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(3, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 6)));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);
        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 6), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .6f));

        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("voltage300")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .sharedText("voltage600")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.NORTH))
                .placeNearTarget();
        scene.idle(50);
    }

    public static void losses(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("transformer_losses", "Transformers and energy losses");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.world().showSection(util.select().position(4, 1, 0), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 4), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 8), Direction.DOWN);
        scene.world().showSection(util.select().position(2, 1, 3), Direction.DOWN);

        ElementLink<WirePonderElement> wire1 = connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(2, 1, 3)));
        ElementLink<WirePonderElement> wire2 = connections.createConnection(new InWorldNode(1, util.grid().at(2, 1, 3)),
                new InWorldNode(0, util.grid().at(4, 1, 4)));
        ElementLink<WirePonderElement> wire3 = connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 8)));

        ElementLink<WirePonderElement> wire4 = connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 8)),
                new InWorldNode(1, util.grid().at(4, 1, 4)));
        ElementLink<WirePonderElement> wire5 =  connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 0)));
        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 3), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .5f));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Wires have some resistance, therefore they cause energy losses.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.overlay().showText(20)
                .sharedText("voltage300")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 0), Direction.WEST))
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(20)
                .text("299.5V")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(20)
                .text("299.4V")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 8), Direction.WEST))
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(20)
                .text("10A")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(130)
                .text("Energy losses are directly proportional to the resistance and the square of the amperage.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(150);

        connections.removeConnection(wire1);
        connections.removeConnection(wire2);
        connections.removeConnection(wire3);
        connections.removeConnection(wire4);
        connections.removeConnection(wire5);
        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 0);

        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 3), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", 0f));

        scene.idle(20);

        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 6), Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Transformers can be used to increase the voltage, therefore decrease the amperage.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(80);

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(1, util.grid().at(4, 1, 2)));

        connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 3)),
                new InWorldNode(3, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(2, 1, 3)));


        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 6)));

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 8)),
                new InWorldNode(2, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 8)),
                new InWorldNode(3, util.grid().at(4, 1, 6)));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);

        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 3), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .2f));

        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 4), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .6f));

        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("ratio1over3")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .sharedText("ratio3over1")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .text("3.3A")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(70)
                .text("For large transmission lines, stepping-up the voltage can greatly decrease energy losses.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);
    }

    public static void turns(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("transformer_turns", "How transformers work");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos source = util.grid().at(4, 1, 0);
        BlockPos primaryMeter = util.grid().at(4, 1, 2);
        BlockPos primary = util.grid().at(4, 1, 4);
        BlockPos secondary = util.grid().at(4, 1, 5);
        BlockPos secondaryMeter = util.grid().at(4, 1, 7);
        BlockPos bulb = util.grid().at(4, 2, 7);

        scene.world().showSection(util.select().fromTo(primary, secondary), Direction.DOWN);
        scene.world().showSection(util.select().position(source), Direction.DOWN);
        scene.world().showSection(util.select().position(primaryMeter), Direction.DOWN);
        scene.world().showSection(util.select().position(secondaryMeter), Direction.DOWN);
        scene.world().showSection(util.select().position(bulb), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Transformers are used to step-up or step-down voltage")
                .pointAt(util.vector().topOf(primary).add(0, 0, 0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createConnection(new InWorldNode(0, source), new InWorldNode(0, primaryMeter));
        connections.createConnection(new InWorldNode(0, primaryMeter), new InWorldNode(1, primary));
        connections.createConnection(new InWorldNode(1, primaryMeter), new InWorldNode(0, primary));
        connections.createConnection(new InWorldNode(1, source), new InWorldNode(1, primaryMeter));

        scene.world().modifyBlockEntityNBT(util.select().position(primaryMeter), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", 1f));

        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("turns50")
                .pointAt(util.vector().blockSurface(primary, Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        scene.rotateCameraY(-90);
        scene.idle(60);

        scene.overlay().showText(20)
                .sharedText("turns10")
                .pointAt(util.vector().blockSurface(secondary, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        scene.rotateCameraY(90);
        scene.idle(60);

        scene.overlay().showText(20)
                .sharedText("voltage1500")
                .pointAt(util.vector().blockSurface(primaryMeter, Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        connections.createConnection(new InWorldNode(0, secondary), new InWorldNode(0, secondaryMeter));
        connections.createConnection(new InWorldNode(1, secondary), new InWorldNode(1, secondaryMeter));

        scene.world().modifyBlockEntityNBT(util.select().position(secondaryMeter), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", 0.3f));
        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("voltage300")
                .pointAt(util.vector().blockSurface(secondaryMeter, Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        connections.createConnection(new InWorldNode(0, secondary), new InWorldNode(0, bulb));
        connections.createConnection(new InWorldNode(1, secondary), new InWorldNode(1, bulb));

        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(1));

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Notice how the turns ratio is equal to the voltage ratio")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(primary).add(0, 0, 0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createCurrentVisualization(new InWorldNode(0, source), new InWorldNode(0, primaryMeter), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, primaryMeter), new InWorldNode(1, primary), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, primaryMeter), new InWorldNode(0, primary), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(1, source), new InWorldNode(1, primaryMeter), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(0, secondary), new InWorldNode(0, bulb), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, secondary), new InWorldNode(1, bulb), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(1, bulb), new InWorldNode(0, bulb), 1, 1, true);

        scene.idle(20);

        scene.rotateCameraY(-55);
        scene.idle(60);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, "CEEPonderTransformerIsolation", AABB.encapsulatingFullBlocks(primary, secondary).inflate(0.5, 0.5, -0.9), 80);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Additionally, the transformer doesn't connect the both circuits")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(primary).add(0, 0, 0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("Instead, it couples them magnetically")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(primary).add(0, 0, 0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);
    }

    public static void transformerCore(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);

        scene.title("transformer_core_multiblock", "Transformer Core Basics");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection battery = util.select().position(4, 1, 0);
        BlockPos primaryPos = util.grid().at(4, 1, 3);
        BlockPos secondaryPos = util.grid().at(4, 1, 4);
        Selection primary = util.select().position(primaryPos);
        Selection secondary = util.select().position(secondaryPos);
        Selection resistor = util.select().position(4, 1, 8);

        scene.world().showSection(battery, Direction.DOWN);
        scene.world().showSection(primary, Direction.DOWN);
        scene.world().showSection(secondary, Direction.DOWN);
        scene.world().showSection(resistor, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Transformer cores can be configured with a specific number of turns.")
                .pointAt(util.vector().topOf(primaryPos).add(0, 0, 0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(20)
                .sharedText("turns10")
                .pointAt(util.vector().blockSurface(primaryPos, Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        scene.rotateCameraY(-90);
        scene.idle(60);

        scene.overlay().showText(20)
                .sharedText("turns50")
                .pointAt(util.vector().blockSurface(secondaryPos, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        scene.rotateCameraY(90);
        scene.idle(60);

        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(1, util.grid().at(4, 1, 3)));
        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(4, 1, 3)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 8)));
        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 8)));
        scene.idle(20);


        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(1, util.grid().at(4, 1, 3)), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(1, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(4, 1, 3)), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 8)), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 8)), 1, 1, true);
        scene.idle(40);

        Selection radiators = util.select().fromTo(3, 1, 3, 5, 1, 4)
                .substract(primary)
                .substract(secondary);

        scene.world().showSection(radiators, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Surrounding transformer core with radiator panels or waterlogging them increases its power rating.")
                .pointAt(util.vector().topOf(primaryPos).add(0, 0, 0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("Exceeding the transformer's power rating will cause it to overload.")
                .pointAt(util.vector().topOf(primaryPos).add(0, 0, 0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);
    }
}
