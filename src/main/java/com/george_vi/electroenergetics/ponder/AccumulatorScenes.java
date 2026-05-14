package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AccumulatorScenes {
    public static void accumulator(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("accumulator", "Storing energy via accumulators");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection accumulator = util.select().position(2, 1, 1);
        Selection battery = util.select().position(2, 1, 3);
        Selection bulb = util.select().position(2, 1, 0);

        scene.world().showSection(battery, Direction.DOWN);
        ElementLink<WorldSectionElement> accumulatorSection = scene.world().showIndependentSection(accumulator, Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("An accumulator can be used to store electrical energy")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(util.grid().at(2, 1, 1)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(30)
                .sharedText("voltage24")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(70)
                .text("Charge it with a voltage of 24V")
                .pointAt(util.vector().centerOf(util.grid().at(2, 1, 1)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Simply connect the positive to the positive and negative to the negative")
                .pointAt(util.vector().centerOf(util.grid().at(2, 1, 1)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(120);

        ElementLink<WirePonderElement> connection1 = connections.createConnection(new InWorldNode(0, 2, 1, 1), new InWorldNode(1, 2, 1, 3));
        ElementLink<WirePonderElement> connection2 = connections.createConnection(new InWorldNode(1, 2, 1, 1), new InWorldNode(0, 2, 1, 3));

        scene.overlay().showText(70)
                .text("The accumulator will start charging")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(util.grid().at(2, 1, 1)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.removeConnection(connection1);
        connections.removeConnection(connection2);

        scene.world().showSection(bulb, Direction.DOWN);
        scene.world().hideSection(battery, Direction.UP);

        scene.world().moveSection(accumulatorSection, new Vec3(0, 0, 2), 40);

        scene.idle(40);

        scene.world().hideIndependentSection(accumulatorSection, Direction.DOWN);
        scene.world().replaceBlocks(battery, CEEBlocks.ACCUMULATOR.getDefaultState()
                .setValue(AccumulatorBlock.FACING, Direction.UP)
                .setValue(AccumulatorBlock.FLIP, false), false);
        scene.world().replaceBlocks(accumulator, Blocks.AIR.defaultBlockState(), false);
        scene.world().showIndependentSectionImmediately(battery);

        connections.createConnection(new InWorldNode(0, 2, 1, 3), new InWorldNode(0, 2, 1, 0));
        connections.createConnection(new InWorldNode(1, 2, 1, 3), new InWorldNode(1, 2, 1, 0));

        scene.overlay().showText(70)
                .text("After it's charged, it can power other components")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(util.grid().at(2, 1, 1)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.world().modifyBlockEntityNBT(bulb, IndicatorBulbBlockEntity.class, tag -> tag.putFloat("FirstLight", 1));
        scene.idle(60);
    }

    public static void accumulatorsSeries(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("accumulators_series", "Connecting accumulators in series");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection accumulators = util.select().fromTo(2, 1, 3, 6, 1, 3);
        Selection connectors = util.select().fromTo(2, 1, 6, 6, 1, 6);

        scene.world().showSection(accumulators, Direction.DOWN);

        scene.idle(40);

        scene.rotateCameraY(35);

        scene.idle(40);
        connections.createConnection(new InWorldNode(2, 6, 1, 3), new InWorldNode(1, 6, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(3, 5, 1, 3), new InWorldNode(0, 6, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(2, 5, 1, 3), new InWorldNode(1, 5, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(3, 4, 1, 3), new InWorldNode(0, 5, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(2, 4, 1, 3), new InWorldNode(1, 4, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(3, 3, 1, 3), new InWorldNode(0, 4, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(2, 3, 1, 3), new InWorldNode(1, 3, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(3, 2, 1, 3), new InWorldNode(0, 3, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(2);
        connections.createConnection(new InWorldNode(2, 2, 1, 3), new InWorldNode(1, 2, 1, 3), CEEWireTypes.IRON_BUS.get());
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Accumulators can be connected in series to increase their voltage")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(util.grid().at(4, 1, 3)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Each accumulator feeds into the next")
                .pointAt(util.vector().centerOf(util.grid().at(4, 1, 3)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(90);

        scene.rotateCameraY(-35);
        scene.idle(40);

        for (int i = 0; i < 10; i++) {
            scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, "ponderAccumulatorOutline" + i,
                    new AABB(2 + (i / 2f), 1, 3, 2.5f + (i / 2f), 11/16f + 1, 4f), 10);
            scene.idle(2);
        }
        scene.idle(20);

        scene.overlay().showText(70)
                .text("This battery pack contains 10 batteries at 24V, charge it with 240V")
                .pointAt(util.vector().centerOf(util.grid().at(4, 1, 3)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(90);

        scene.world().showSection(connectors, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(30)
                .sharedText("voltage240")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(50);

        connections.createConnection(new InWorldNode(1, 4, 1, 6), new InWorldNode(0, 2, 1, 6), CEEWireTypes.STANDARD.get());
        connections.createConnection(new InWorldNode(0, 6, 1, 6), new InWorldNode(0, 4, 1, 6), CEEWireTypes.STANDARD.get());
        connections.createConnection(new InWorldNode(3, 6, 1, 3), new InWorldNode(0, 6, 1, 6), CEEWireTypes.STANDARD.get());
        connections.createConnection(new InWorldNode(0, 2, 1, 3), new InWorldNode(0, 2, 1, 6), CEEWireTypes.STANDARD.get());



        connections.createCurrentVisualization(new InWorldNode(2, 6, 1, 3), new InWorldNode(1, 6, 1, 3), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(3, 5, 1, 3), new InWorldNode(0, 6, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(2, 5, 1, 3), new InWorldNode(1, 5, 1, 3), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(3, 4, 1, 3), new InWorldNode(0, 5, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(2, 4, 1, 3), new InWorldNode(1, 4, 1, 3), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(3, 3, 1, 3), new InWorldNode(0, 4, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(2, 3, 1, 3), new InWorldNode(1, 3, 1, 3), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(3, 2, 1, 3), new InWorldNode(0, 3, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(2, 2, 1, 3), new InWorldNode(1, 2, 1, 3), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(1, 4, 1, 6), new InWorldNode(0, 2, 1, 6), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 6, 1, 6), new InWorldNode(0, 4, 1, 6), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(3, 6, 1, 3), new InWorldNode(0, 6, 1, 6), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 6), new InWorldNode(0, 2, 1, 3), 1, 1, true);

        scene.idle(60);
    }
}
