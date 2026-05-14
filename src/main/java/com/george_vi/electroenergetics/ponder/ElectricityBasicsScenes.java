package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

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
        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(1));
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
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(0));
        scene.world().modifyBlock(source, bs -> bs.setValue(CreativeBatteryBlock.FACING, Direction.EAST), false);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeConnection(sourceWire1);
        connections.removeConnection(sourceWire2);
        connections.createConnection(new InWorldNode(0, source), new InWorldNode(1, diode));
        connections.createConnection(new InWorldNode(1, bulb), new InWorldNode(1, source));

        connections.createCurrentVisualization(new InWorldNode(0, diode), new InWorldNode(0, bulb), 1, -0.1f, false);
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

    public static void gauges(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("gauges", "Ammeters & Voltmeters");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        Selection battery = util.select().position(4, 1, 8);
        Selection voltmeter = util.select().position(4, 1, 4);
        Selection ammeter = util.select().position(2, 1, 2);
        Selection connectors = util.select().fromTo(2, 1, 0, 6, 1, 8)
                .substract(battery)
                .substract(voltmeter)
                .substract(ammeter);


        scene.world().showSection(connectors, Direction.DOWN);
        scene.world().showSection(battery, Direction.DOWN);

        scene.idle(20);

        connections.createConnection(new InWorldNode(0, 6, 1, 8), new InWorldNode(0, 2, 1, 8), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, 2, 1, 8), new InWorldNode(0, 2, 1, 0), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, 2, 1, 0), new InWorldNode(0, 4, 1, 0), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(1, 4, 1, 0), new InWorldNode(0, 6, 1, 0), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, 6, 1, 0), new InWorldNode(0, 6, 1, 8), CEEWireTypes.IRON_BUS.get());

        connections.setBulbState(util.grid().at(4, 1, 0), 1);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("To measure the properties of an electric circuit, use voltmeters and ammeters")
                .pointAt(util.vector().centerOf(2, 1, 4))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(130);

        connections.createCurrentVisualization(new InWorldNode(0, 6, 1, 8), new InWorldNode(0, 2, 1, 8), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 8), new InWorldNode(0, 2, 1, 0), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 0), new InWorldNode(0, 4, 1, 0), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, 4, 1, 0), new InWorldNode(0, 6, 1, 0), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 6, 1, 0), new InWorldNode(0, 6, 1, 8), 0, 1, true);
        scene.idle(20);

        scene.world().showSection(ammeter, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("An ammeter measures current flow")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(2, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("It is connected in series")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(2, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(130)
                .text("This means it is placed along the wire. Notice how it oversees the flow of current")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        scene.overlay().showText(70)
                .text("0.3A")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        // Voltmeter:
        scene.world().showSection(voltmeter, Direction.DOWN);
        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 4), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .0f));

        scene.idle(20);
        connections.createConnection(new InWorldNode(0, 2, 1, 4), new InWorldNode(0, 6, 1, 4), CEEWireTypes.IRON_BUS.get());

        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 4), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .3f));

        scene.overlay().showText(70)
                .text("A voltmeter measures the voltage difference")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(4, 1, 4))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("It is connected in parallel to the circuit")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(4, 1, 4))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(130)
                .text("This means it is placed between the two wires. Notice how it probes the points and measures the voltage")
                .pointAt(util.vector().centerOf(4, 1, 4))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        scene.overlay().showText(70)
                .sharedText("voltage300")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);
    }

    public static void voltageIntroduction(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("voltage_introduction", "What is voltage?");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        Selection battery = util.select().position(2, 1, 3);
        Selection voltmeter = util.select().position(2, 1, 1);
        Selection connectors = util.select().fromTo(0, 1, 1, 4, 1, 3)
                .substract(battery)
                .substract(voltmeter);

        scene.world().showSection(battery, Direction.DOWN);
        scene.world().showSection(connectors, Direction.DOWN);

        scene.idle(20);

        connections.createConnection(new InWorldNode(0, 4, 1, 1), new InWorldNode(0, 4, 1, 3), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, 0, 1, 1), new InWorldNode(0, 0, 1, 3), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 0, 1, 3), CEEWireTypes.IRON_BUS.get());

        scene.idle(20);


        scene.overlay().showText(70)
                .text("Voltage is the measurement of difference of electric potential")
                .colored(PonderPalette.BLUE)
                .pointAt(battery.getCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.overlay().showText(30)
                .sharedText("voltage300")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(30)
                .sharedText("voltage300")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(util.grid().at(0, 1, 1)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(30)
                .sharedText("voltage0")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(util.grid().at(4, 1, 1)))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(50);

        scene.world().showSection(voltmeter, Direction.DOWN);

        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 1), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .0f));
        scene.idle(40);

        connections.createConnection(new InWorldNode(0, 0, 1, 1), new InWorldNode(0, 2, 1, 1), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(1, 2, 1, 1), new InWorldNode(0, 4, 1, 1), CEEWireTypes.IRON_BUS.get());

        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 1), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .3f));

        scene.overlay().showText(70)
                .text("The voltmeter measures the voltage difference between the two points.")
                .colored(PonderPalette.BLUE)
                .pointAt(voltmeter.getCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        ElementLink<CurrentVisualizationPonderElement> visualization1 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 1), 0, 0, true);
        ElementLink<CurrentVisualizationPonderElement> visualization2 = connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 1), new InWorldNode(0, 0, 1, 3), 0, 0, true);
        ElementLink<CurrentVisualizationPonderElement> visualization3 = connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 3), new InWorldNode(0, 4, 1, 3), 0, 0, true);
        ElementLink<CurrentVisualizationPonderElement> visualization4 = connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 1), new InWorldNode(0, 0, 1, 1), 0, 0, true);
        ElementLink<CurrentVisualizationPonderElement> visualization5 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 1), new InWorldNode(1, 2, 1, 1), 0, 0, true);

        scene.overlay().showText(70)
                .text("Keep in mind there is no current flow yet!")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(0, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("Think of voltage as the pressure driving current flow.")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(0, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(100)
                .text("Currently, no energy is being moved, since there is no current flow.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(0, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(120);

        scene.world().replaceBlocks(voltmeter, CEEBlocks.BULB.getDefaultState().setValue(BulbBlock.FACING, Direction.UP), true);

        connections.setBulbState(util.grid().at(2, 1, 1), 1);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        connections.removeCurrentVisualization(visualization5);

        visualization1 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 1), 0, -1, true);
        visualization2 = connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 1), new InWorldNode(0, 0, 1, 3), 0, -1, true);
        visualization3 = connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 3), new InWorldNode(0, 4, 1, 3), 0, -1, true);
        visualization4 = connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 1), new InWorldNode(0, 0, 1, 1), 0, -1, true);
        visualization5 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 1), new InWorldNode(1, 2, 1, 1), 0, -1, true);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("After connecting a load to the circuit, current starts flowing")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(2, 1, 1))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(200);

        scene.overlay().showText(30)
                .sharedText("voltage600")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(50);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        connections.removeCurrentVisualization(visualization5);

        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 1), 0, -4, true);
        connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 1), new InWorldNode(0, 0, 1, 3), 0, -4, true);
        connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 3), new InWorldNode(0, 4, 1, 3), 0, -4, true);
        connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 1), new InWorldNode(0, 0, 1, 1), 0, -4, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 1), new InWorldNode(1, 2, 1, 1), 0, -4, true);

        scene.idle(20);

        scene.overlay().showText(100)
                .text("The higher the voltage, the more current flows")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(0, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(120);

        scene.idle(50);
    }

    public static void theBasics(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("basics", "An intuitive approach to understanding electricity");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos connector1 = util.grid().at(4, 1, 3);
        BlockPos connector2 = util.grid().at(4, 1, 1);
        BlockPos connector3 = util.grid().at(4, 1, 0);
        BlockPos connector4 = util.grid().at(0, 1, 1);
        BlockPos connector5 = util.grid().at(0, 1, 3);
        BlockPos connector6 = util.grid().at(0, 1, 0);
        BlockPos battery = util.grid().at(2, 1 ,3);
        BlockPos bulb1 = util.grid().at(2, 1 ,1);
        BlockPos bulb2 = util.grid().at(2, 1 ,0);

        scene.world().showSection(util.select().fromTo(connector1, connector2), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(connector4, connector5), Direction.DOWN);

        scene.idle(10);
        connections.createConnection(new InWorldNode(0, connector1), new InWorldNode(0, connector2), CEEWireTypes.IRON_BUS.get());
        ElementLink<WirePonderElement> connection1 = connections.createConnection(new InWorldNode(0, connector2), new InWorldNode(0, connector4), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, connector4), new InWorldNode(0, connector5), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, connector5), new InWorldNode(0, connector1), CEEWireTypes.IRON_BUS.get());
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Imagine a wire connected in a loop.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(battery))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        ElementLink<CurrentVisualizationPonderElement> visualization1 = connections.createCurrentVisualization(new InWorldNode(0, connector1), new InWorldNode(0, connector2), 0, 0, true);
        ElementLink<CurrentVisualizationPonderElement> visualization2 = connections.createCurrentVisualization(new InWorldNode(0, connector2), new InWorldNode(0, connector4), 0, 0, true);
        ElementLink<CurrentVisualizationPonderElement> visualization3 = connections.createCurrentVisualization(new InWorldNode(0, connector4), new InWorldNode(0, connector5), 0, 0, true);
        ElementLink<CurrentVisualizationPonderElement> visualization4 = connections.createCurrentVisualization(new InWorldNode(0, connector5), new InWorldNode(0, connector1), 0, 0, true);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Little dots are placed on this wire. Think of them as electrons.")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(battery))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("Its like a chain. These dots can move, but they can't move away from each other.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(battery))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.world().showSection(util.select().position(battery), Direction.DOWN);

        scene.idle(20);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        visualization1 = connections.createCurrentVisualization(new InWorldNode(0, connector1), new InWorldNode(0, connector2), 0, 1, true);
        visualization2 = connections.createCurrentVisualization(new InWorldNode(0, connector2), new InWorldNode(0, connector4), 0, 1, true);
        visualization3 = connections.createCurrentVisualization(new InWorldNode(0, connector4), new InWorldNode(0, connector5), 0, 1, true);
        visualization4 = connections.createCurrentVisualization(new InWorldNode(0, connector5), new InWorldNode(0, connector1), 0, 1, true);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("Voltage sources try to move this chain. They try to pull from one side, and push to the other.")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(battery))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(150);

        scene.overlay().showText(120)
                .text("Notice how the number of these dots stay constant. These dots don't represent energy themselves, their movement is energy.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(bulb1))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(150);

        scene.overlay().showText(70)
                .text("This is actually a short circuit.")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(bulb1))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        visualization1 = connections.createCurrentVisualization(new InWorldNode(0, connector1), new InWorldNode(0, connector2), 0, 10, true);
        visualization2 = connections.createCurrentVisualization(new InWorldNode(0, connector2), new InWorldNode(0, connector4), 0, 10, true);
        visualization3 = connections.createCurrentVisualization(new InWorldNode(0, connector4), new InWorldNode(0, connector5), 0, 10, true);
        visualization4 = connections.createCurrentVisualization(new InWorldNode(0, connector5), new InWorldNode(0, connector1), 0, 10, true);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("There is no resistance, and the voltage source is able to move these dots really fast!")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(bulb1))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(120);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        visualization1 = connections.createCurrentVisualization(new InWorldNode(0, connector1), new InWorldNode(0, connector2), 0, 0, false);
        visualization3 = connections.createCurrentVisualization(new InWorldNode(0, connector4), new InWorldNode(0, connector5), 0, 0, false);
        visualization4 = connections.createCurrentVisualization(new InWorldNode(0, connector5), new InWorldNode(0, connector1), 0, 0, false);

        connections.removeConnection(connection1);
        scene.idle(20);
        scene.world().showSection(util.select().position(bulb1), Direction.DOWN);
        scene.idle(20);

        connections.createConnection(new InWorldNode(0, connector2), new InWorldNode(1, bulb1), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, bulb1), new InWorldNode(0, connector4), CEEWireTypes.IRON_BUS.get());

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        visualization1 = connections.createCurrentVisualization(new InWorldNode(0, connector1), new InWorldNode(0, connector2), 0, 1, true);
        visualization3 = connections.createCurrentVisualization(new InWorldNode(0, connector4), new InWorldNode(0, connector5), 0, 1, true);
        visualization4 = connections.createCurrentVisualization(new InWorldNode(0, connector5), new InWorldNode(0, connector1), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, connector2), new InWorldNode(1, bulb1), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, bulb1), new InWorldNode(0, connector4), 0, 1, true);

        scene.world().modifyBlock(bulb1, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb1, BulbBlockEntity.class, be -> be.setLight(1));
        scene.idle(40);

        scene.overlay().showText(70)
                .text("Connect it through a device instead.")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(bulb1))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("Devices use the movement of these dots as energy.")
                .colored(PonderPalette.WHITE)
                .pointAt(util.vector().centerOf(bulb1))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("In this case, this movement heats up the bulb's filament, the heat makes it glow.")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(bulb1))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.world().showSection(util.select().fromTo(connector3, connector6), Direction.DOWN);
        scene.idle(30);
        connections.createConnection(new InWorldNode(0, bulb2), new InWorldNode(0, connector6), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, connector3), new InWorldNode(1, bulb2), CEEWireTypes.IRON_BUS.get());

        connections.createConnection(new InWorldNode(0, connector2), new InWorldNode(0, connector3), CEEWireTypes.IRON_BUS.get());
        connections.createConnection(new InWorldNode(0, connector6), new InWorldNode(0, connector4), CEEWireTypes.IRON_BUS.get());

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        visualization1 = connections.createCurrentVisualization(new InWorldNode(0, connector1), new InWorldNode(0, connector2), 0, 2, true);
        visualization3 = connections.createCurrentVisualization(new InWorldNode(0, connector4), new InWorldNode(0, connector5), 0, 2, true);
        visualization4 = connections.createCurrentVisualization(new InWorldNode(0, connector5), new InWorldNode(0, connector1), 0, 2, true);
        connections.createCurrentVisualization(new InWorldNode(0, bulb2), new InWorldNode(0, connector6), 0, 1, 0.5f, true);
        connections.createCurrentVisualization(new InWorldNode(0, connector3), new InWorldNode(1, bulb2), 0, 1, 0.5f, true);
        connections.createCurrentVisualization(new InWorldNode(0, connector2), new InWorldNode(0, connector3), 0, 1, 0.5f, true);
        connections.createCurrentVisualization(new InWorldNode(0, connector6), new InWorldNode(0, connector4), 0, 1, 0.5f, true);

        scene.world().modifyBlock(bulb2, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb2, BulbBlockEntity.class, be -> be.setLight(1));

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Notice how the number of incoming dots is equal to the number of outgoing dots.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(connector2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("This is the definition of Kirchhoff's Current Law.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(connector2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.idle(60);
    }

    public static void resistance(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);

        scene.title("resistance", "Effect of a Resistor on a Circuit");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos bulbWithoutResistorPos = util.grid().at(2,1,1);
        Selection bulbWithoutResistor = util.select().position(bulbWithoutResistorPos);
        BlockPos bulbWithResistorPos = util.grid().at(1,1,1);
        Selection bulbWithResistor = util.select().position(bulbWithResistorPos);
        Selection battery = util.select().position(2,1,3);
        BlockPos resistorPos = util.grid().at(3,1,1);
        Selection resistor = util.select().position(resistorPos);
        Selection connectors = util.select().position(0, 1, 1)
                .add(util.select().position(0, 1, 3))
                .add(util.select().position(4, 1, 1))
                .add(util.select().position(4, 1, 3));


        scene.world().showSection(bulbWithoutResistor, Direction.DOWN);
        scene.world().showSection(battery, Direction.DOWN);
        scene.world().showSection(connectors, Direction.DOWN);

        scene.idle(20);

        connections.createConnection(new InWorldNode(1, 2, 1, 3), new InWorldNode(0, 4, 1, 3));
        connections.createConnection(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 1));
        ElementLink<WirePonderElement> connection1 = connections.createConnection(new InWorldNode(0, 4, 1, 1), new InWorldNode(1, 2, 1, 1));
        ElementLink<WirePonderElement> connection2 = connections.createConnection(new InWorldNode(0, 2, 1, 1), new InWorldNode(0, 0, 1, 1));
        connections.createConnection(new InWorldNode(0, 0, 1, 1), new InWorldNode(0, 0, 1, 3));
        connections.createConnection(new InWorldNode(0, 0, 1, 3), new InWorldNode(0, 2, 1, 3));

        scene.world().modifyBlock(bulbWithoutResistorPos, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulbWithoutResistorPos, BulbBlockEntity.class, be -> be.setLight(1));

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Without a resistor, the bulb shines at full intensity")
                .pointAt(util.vector().centerOf(bulbWithoutResistorPos))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.removeConnection(connection1);
        connections.removeConnection(connection2);
        scene.world().showSection(resistor, Direction.DOWN);
        scene.world().showSection(bulbWithResistor, Direction.DOWN);
        scene.world().hideSection(bulbWithoutResistor, Direction.UP);

        scene.idle(20);

        scene.overlay().showText(20)
                .text("1kΩ")
                .pointAt(util.vector().blockSurface(resistorPos, Direction.DOWN, -4/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        connections.createConnection(new InWorldNode(0, 4, 1, 1), new InWorldNode(1, 3, 1, 1));
        connections.createConnection(new InWorldNode(0, 3, 1, 1), new InWorldNode(1, 1, 1, 1));
        connections.createConnection(new InWorldNode(0, 1, 1, 1), new InWorldNode(0, 0, 1, 1));

        scene.world().modifyBlock(bulbWithResistorPos, bs -> bs.setValue(BulbBlock.LIGHT, 7), false);
        scene.world().modifyBlockEntity(bulbWithResistorPos, BulbBlockEntity.class, be -> be.setLight(0.6f));

        scene.idle(20);
        connections.createCurrentVisualization(new InWorldNode(1, 2, 1, 3), new InWorldNode(0, 4, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 1), new InWorldNode(1, 3, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, 3, 1, 1), new InWorldNode(0, 3, 1, 1), 0, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 3, 1, 1), new InWorldNode(1, 1, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 1), new InWorldNode(0, 0, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 1), new InWorldNode(0, 0, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 3), new InWorldNode(0, 2, 1, 3), 1, 1, true);

        scene.idle(20);
        scene.overlay().showText(70)
                .text("Notice how the bulb is dimmer")
                .pointAt(util.vector().centerOf(bulbWithResistorPos))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .text("The resistor is limiting the current flowing through the bulb")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(resistorPos, Direction.DOWN, -4/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.effects().emitParticles(util.vector().topOf(resistorPos.below()), scene.effects().particleEmitterWithinBlockSpace(ParticleTypes.SMOKE, new Vec3(0, 0, 0)), 1, Integer.MAX_VALUE);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("The downside is, some power is wasted as heat")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().blockSurface(resistorPos, Direction.DOWN, -4/16f))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);
    }

    public static void grounding(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("grounding", "The ground as a conductor of electricity");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos bulb = util.grid().at(4, 1, 7);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("The ground can actually conduct electricity.")
                .pointAt(util.vector().topOf(1, 0, 4))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(20);

        connections.createConnection(new InWorldNode(0, 1, 1, 1), new InWorldNode(0, 4, 1, 1));
        connections.createConnection(new InWorldNode(1, 4, 1, 1), new InWorldNode(0, 7, 1, 1));
        connections.createConnection(new InWorldNode(0, 7, 1, 1), new InWorldNode(0, 7, 1, 4));
        connections.createConnection(new InWorldNode(0, 7, 1, 4), new InWorldNode(0, 7, 1, 7));
        connections.createConnection(new InWorldNode(0, 7, 1, 7), new InWorldNode(1, 4, 1, 7));
        connections.createConnection(new InWorldNode(0, 4, 1, 7), new InWorldNode(0, 1, 1, 7));

        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(1));
        scene.idle(20);

        connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 1), new InWorldNode(0, 4, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 7, 1, 1), new InWorldNode(0, 7, 1, 4), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, 4, 1, 1), new InWorldNode(0, 7, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 7, 1, 4), new InWorldNode(0, 7, 1, 7), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 7, 1, 7), new InWorldNode(1, 4, 1, 7), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 7), new InWorldNode(0, 1, 1, 7), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 7), new InWorldNode(0, 1, 1, 1), 0, 1, true);
        scene.idle(60);
    }

}
