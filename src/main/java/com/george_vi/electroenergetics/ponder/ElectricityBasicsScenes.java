package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ElectricityBasicsScenes {
    public static void diode(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("diodes", "Controlling the direction of current");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos source = util.grid().at(2, 1, 3);
        BlockPos bulb = util.grid().at(3, 1, 1);
        BlockPos diode = util.grid().at(1, 1, 1);

        scene.world().showSection(util.select().position(source), Direction.DOWN);
        scene.idle(2);
        scene.world().showSection(util.select().position(bulb), Direction.DOWN);
        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 0), false);
        scene.idle(2);
        scene.world().showSection(util.select().position(diode), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Diodes can be used to control the direction of current")
                .colored(PonderPalette.BLUE)
                .pointAt(diode.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        connections.createConnection(util.vector().blockSurface(source, Direction.WEST), util.vector().of(19/16f, 18/16f, 24/16f));
        scene.idle(2);
        connections.createConnection(util.vector().of(1 + 13/16f, 18/16f, 24/16f), util.vector().of(3 + 2/16f, 1 + 3/16f, 24/16f));
        scene.idle(2);
        connections.createConnection(util.vector().of(3 + 14/16f, 1 + 3/16f, 24/16f), util.vector().blockSurface(source, Direction.EAST));
        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 2), false);
        scene.idle(2);

        ElementLink<CurrentVisualizationPonderElement> visualization1 = connections.createCurrentVisualization(util.vector().blockSurface(source, Direction.WEST), util.vector().of(19/16f, 18/16f, 24/16f), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization2 = connections.createCurrentVisualization(util.vector().of(1 + 13/16f, 18/16f, 24/16f), util.vector().of(3 + 2/16f, 1 + 3/16f, 24/16f), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization3 = connections.createCurrentVisualization(util.vector().of(3 + 14/16f, 1 + 3/16f, 24/16f), util.vector().blockSurface(source, Direction.EAST), 1, 1, true);

        scene.idle(20);

        scene.overlay().showText(100)
                .text("Diodes will allow current to flow, if the anode is connected to the higher potential ...")
                .colored(PonderPalette.WHITE)
                .pointAt(diode.getBottomCenter().add(-0.5f, 0, 0))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(130);

        scene.overlay().showText(100)
                .text("... and the cathode is connected to the lower potential")
                .colored(PonderPalette.WHITE)
                .pointAt(diode.getBottomCenter().add(0.5f, 0, 0))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(130);

        scene.overlay().showText(100)
                .text("That's called Forward Bias")
                .colored(PonderPalette.GREEN)
                .pointAt(diode.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(130);

        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 0), false);
        scene.world().modifyBlock(source, bs -> bs.setValue(CreativeBatteryBlock.FACING, Direction.EAST), false);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);

        connections.createCurrentVisualization(util.vector().of(1 + 13/16f, 18/16f, 24/16f), util.vector().of(3 + 2/16f, 1 + 3/16f, 24/16f), 1, -0.1f, false);
        connections.createCurrentVisualization(util.vector().of(3 + 14/16f, 1 + 3/16f, 24/16f), util.vector().blockSurface(source, Direction.EAST), 1, -0.1f, false);

        scene.overlay().showText(100)
                .text("Otherwise, the diode blocks current flow")
                .colored(PonderPalette.RED)
                .pointAt(diode.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(100)
                .text("That's called Reverse Bias")
                .colored(PonderPalette.RED)
                .pointAt(diode.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(130);

    }
}
