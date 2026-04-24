package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
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

public class InfrastructureScenes {
    public static void energy_meter(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("energy_meter", "Using energy meters");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        WireType positiveWireType = CEEWireTypes.COLORED_WIRES[DyeColor.RED.ordinal()].get();
        WireType negativeWireType = CEEWireTypes.COLORED_WIRES[DyeColor.BLACK.ordinal()].get();

        BlockPos meter = util.grid().at(2, 3, 2);
        BlockPos source = util.grid().at(2, 1, 0);
        BlockPos bulb = util.grid().at(2, 5, 2);

        Selection board = util.select().fromTo(1, 1, 3, 3, 5, 3);

        scene.world().showSection(board, Direction.DOWN);
        scene.idle(20);

        scene.world().showSection(util.select().position(meter), Direction.SOUTH);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Energy meters can be used to measure used energy")
                .pointAt(util.vector().blockSurface(meter, Direction.SOUTH, -6/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.world().showSection(util.select().position(bulb), Direction.SOUTH);
        scene.idle(20);
        scene.world().showSection(util.select().fromTo(1, 1, 0, 3, 1, 0), Direction.DOWN);

        scene.idle(40);

        connections.createConnection(new InWorldNode(1, 2, 1, 0), new InWorldNode(0, 1, 1, 0), positiveWireType);
        connections.createConnection(new InWorldNode(0, 1, 1, 0), new InWorldNode(0, 2, 3, 2), positiveWireType);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Connect the positive feed to the 'feed' node")
                .pointAt(util.vector().blockSurface(meter, Direction.SOUTH, -6/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createConnection(new InWorldNode(2, 2, 3, 2), new InWorldNode(1, 2, 5, 2), positiveWireType);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Connect the positive load to the 'load' node")
                .pointAt(util.vector().blockSurface(meter, Direction.SOUTH, -6/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createConnection(new InWorldNode(0, 2, 5, 2), new InWorldNode(3, 2, 3, 2), negativeWireType);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Finally, connect the return through the 'neutral' node")
                .pointAt(util.vector().blockSurface(meter, Direction.SOUTH, -6/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createConnection(new InWorldNode(1, 2, 3, 2), new InWorldNode(0, 3, 1, 0), negativeWireType);
        connections.createConnection(new InWorldNode(0, 3, 1, 0), new InWorldNode(0, 2, 1, 0), negativeWireType);

        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(1));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The meter will start counting energy that flowed through it")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(meter, Direction.SOUTH, -6/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createCurrentVisualization(new InWorldNode(1, 2, 1, 0), new InWorldNode(0, 1, 1, 0), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 0), new InWorldNode(0, 2, 3, 2), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(2, 2, 3, 2), new InWorldNode(1, 2, 5, 2), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 2, 5, 2), new InWorldNode(3, 2, 3, 2), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, 2, 3, 2), new InWorldNode(0, 3, 1, 0), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 3, 1, 0), new InWorldNode(0, 2, 1, 0), 1, 1, true);
        scene.idle(60);
    }
}
