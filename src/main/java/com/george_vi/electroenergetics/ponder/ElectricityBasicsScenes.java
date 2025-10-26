package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
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

        ElementLink<WirePonderElement> sourceWire1 = connections.createConnection(new InWorldNode(1, source), new InWorldNode(1, diode));
        scene.idle(2);
        connections.createConnection(new InWorldNode(1, diode), new InWorldNode(0, bulb));
        scene.idle(2);
        ElementLink<WirePonderElement> sourceWire2 = connections.createConnection(new InWorldNode(1, bulb), new InWorldNode(0, source));
        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 2), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.light = 1);
        scene.idle(2);

        ElementLink<CurrentVisualizationPonderElement> visualization1 = connections.createCurrentVisualization(new InWorldNode(1, source), new InWorldNode(1, diode), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization2 = connections.createCurrentVisualization(new InWorldNode(1, diode), new InWorldNode(0, bulb), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization3 = connections.createCurrentVisualization(new InWorldNode(1, bulb), new InWorldNode(0, source), 1, 1, true);

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
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.light = 0);
        scene.world().modifyBlock(source, bs -> bs.setValue(CreativeBatteryBlock.FACING, Direction.EAST), false);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeConnection(sourceWire1);
        connections.removeConnection(sourceWire2);
        connections.createConnection(new InWorldNode(0, source), new InWorldNode(1, diode));
        connections.createConnection(new InWorldNode(1, bulb), new InWorldNode(1, source));

        connections.createCurrentVisualization(new InWorldNode(1, diode), new InWorldNode(0, bulb), 1, -0.1f, false);
        connections.createCurrentVisualization(new InWorldNode(1, bulb), new InWorldNode(1, source), 1, -0.1f, false);

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

    public static void voltage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("voltage", "What is voltage?");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection batteries = util.select().fromTo(0, 1, 3, 4, 1, 3);
        BlockPos voltmeter = util.grid().at(2, 1, 1);

        scene.world().showSection(batteries, Direction.DOWN);
        scene.idle(2);
        connections.createConnection(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, util.grid().at(1, 1, 3)));
        scene.idle(2);
        connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 3)), new InWorldNode(1, util.grid().at(1, 1, 3)));
        scene.idle(2);
        connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 3)), new InWorldNode(0, util.grid().at(3, 1, 3)));
        scene.idle(2);
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 3)), new InWorldNode(1, util.grid().at(3, 1, 3)));

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Voltage is the measurement of difference of electric potential")
                .colored(PonderPalette.BLUE)
                .pointAt(batteries.getCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(30)
                .text("160V")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 3), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(30)
                .text("70V")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().blockSurface(util.grid().at(3, 1, 3), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(30)
                .text("-160V")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(0, 1, 3))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(130)
                .text("Sometimes a voltage value may describe a single point. In that case, it is a value relative to ground, which is always 0V")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(0, 1, 3))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        scene.overlay().showText(30)
                .sharedText("voltage0")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(2, 1, 3))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(30)
                .text("70V")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(4, 1, 3))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        ElementLink<WirePonderElement> voltmeterWire1 = connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 3)), new InWorldNode(1, voltmeter));
        scene.idle(2);
        ElementLink<WirePonderElement> voltmeterWire2 = connections.createConnection(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, voltmeter));
        scene.idle(2);
        scene.world().showSection(util.select().position(voltmeter), Direction.DOWN);
        scene.idle(2);

        scene.overlay().showText(30)
                .text("230V")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(voltmeter))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(130)
                .text("Notice how the voltage at each connector is -160V and 70V, but the difference is 230V")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().topOf(voltmeter))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        scene.overlay().showText(130)
                .text("Voltage doesn't mean work. It shows how much work could be done, it's not current or flow, just the possibility of it")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().topOf(voltmeter))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        scene.world().replaceBlocks(util.select().position(voltmeter), CEEBlocks.BULB.getDefaultState().setValue(BulbBlock.FACING, Direction.UP), true);
        connections.removeConnection(voltmeterWire1);
        connections.removeConnection(voltmeterWire2);
        scene.idle(20);
        voltmeterWire1 = connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 3)), new InWorldNode(1, voltmeter));
        voltmeterWire2 = connections.createConnection(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, voltmeter));
        scene.world().modifyBlock(voltmeter, bs -> bs.setValue(BulbBlock.LIGHT, 2), false);
        scene.world().modifyBlockEntity(voltmeter, BulbBlockEntity.class, be -> be.light = 1);
        ElementLink<CurrentVisualizationPonderElement> visualization1 = connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, util.grid().at(4, 1, 3)), 0, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization5 = connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(4, 1, 3)), new InWorldNode(1, voltmeter), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization6 = connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, voltmeter), 1, -1, true);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Connect a device to a voltage difference, and current will start flowing and doing work")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(voltmeter))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(150);

        scene.overlay().showText(70)
                .text("Never directly connect different voltages!")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(voltmeter))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.world().replaceBlocks(util.select().position(voltmeter), CEEBlocks.CONNECTOR.getDefaultState().setValue(BulbBlock.FACING, Direction.UP), true);
        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization5);
        connections.removeCurrentVisualization(visualization6);
        connections.removeConnection(voltmeterWire1);
        connections.removeConnection(voltmeterWire2);
        scene.idle(20);
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 3)), new InWorldNode(1, voltmeter));
        connections.createConnection(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, voltmeter));
        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, util.grid().at(4, 1, 3)), 0, 5, false);
        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(4, 1, 3)), new InWorldNode(0, voltmeter), 1, 5, false);
        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(0, 1, 3)), new InWorldNode(0, voltmeter), 1, -5, false);

        scene.overlay().showText(70)
                .text("When two different potentials are connected with low resistance, a lot of current starts flowing")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(voltmeter))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);
    }
}
