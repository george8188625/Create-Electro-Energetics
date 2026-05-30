package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.cut_off_switch.CutOffSwitchBlock;
import com.george_vi.electroenergetics.content.relay.RelayBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_switch.HVSwitchBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class SwitchScenes {

    public static void hvSwitch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("hv_switch", "Setting up a High Voltage Switch");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection connectors = util.select().fromTo(1, 1, 4, 3, 1, 4);
        Selection battery = util.select().position(4, 1, 2);
        Selection bulb = util.select().position(1, 1, 2);
        Selection hv_switch = util.select().position(3, 1, 0);
        Selection hv_switch_connector = util.select().position(1, 1, 0);
        Selection redstone = util.select().fromTo(3, 1, 1, 3, 1, 2);

        scene.world().showSection(hv_switch, Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("High Voltage Switches can be used to connect or disconnect high voltage circuits")
                .pointAt(util.vector().topOf(3, 1, 0))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(hv_switch_connector, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("To create one, place a Connector in the direction of the High Voltage Switch")
                .pointAt(util.vector().topOf(1, 1, 0))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, "CEEHVSwitchGapPonder", new AABB(util.grid().at(2, 1, 0)), 80);
        scene.idle(10);

        scene.overlay().showText(70)
                .text("Leave a one-block gap between them")
                .pointAt(util.vector().topOf(2, 1, 0))
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(connectors, Direction.DOWN);
        scene.world().showSection(battery, Direction.DOWN);
        scene.world().showSection(bulb, Direction.DOWN);
        connections.createConnection(new InWorldNode(0, util.grid().at(3, 1, 4)), new InWorldNode(0, util.grid().at(1, 1, 4)));
        connections.createConnection(new InWorldNode(0, util.grid().at(1, 1, 4)), new InWorldNode(0, util.grid().at(1, 1, 2)));
        connections.createConnection(new InWorldNode(1, util.grid().at(1, 1, 2)), new InWorldNode(0, util.grid().at(1, 1, 0)));
        connections.createConnection(new InWorldNode(0, util.grid().at(3, 1, 0)), new InWorldNode(1, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 2)), new InWorldNode(0, util.grid().at(3, 1, 4)));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The High Voltage Switch can be operated manually...")
                .pointAt(util.vector().topOf(3, 1, 0))
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(3, 1, 0), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(30);

        scene.world().modifyBlockEntity(util.grid().at(3, 1, 0), HVSwitchBlockEntity.class, be -> be.connected = true);
        scene.idle(85);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), BulbBlockEntity.class, be -> be.setLight(1));
        scene.idle(20);
        scene.overlay().showText(70)
                .text("...or with the help of Redstone")
                .pointAt(util.vector().topOf(3, 1, 0))
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .placeNearTarget();
        scene.idle(80);
        scene.world().showSection(redstone, Direction.DOWN);
        scene.idle(20);

        scene.world().modifyBlock(util.grid().at(3, 1, 1), bs -> bs.setValue(BlockStateProperties.POWER, 15), false);
        scene.world().modifyBlock(util.grid().at(3, 1, 2), bs -> bs.setValue(BlockStateProperties.POWERED, true), false);

        scene.world().modifyBlockEntity(util.grid().at(3, 1, 0), HVSwitchBlockEntity.class, be -> be.connected = false);
        scene.idle(15);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), BulbBlockEntity.class, be -> be.setLight(0));
        scene.idle(105);

        scene.world().modifyBlock(util.grid().at(3, 1, 1), bs -> bs.setValue(BlockStateProperties.POWER, 0), false);
        scene.world().modifyBlock(util.grid().at(3, 1, 2), bs -> bs.setValue(BlockStateProperties.POWERED, false), false);

        scene.world().modifyBlockEntity(util.grid().at(3, 1, 0), HVSwitchBlockEntity.class, be -> be.connected = true);
        scene.idle(85);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), BulbBlockEntity.class, be -> be.setLight(1));
        scene.idle(60);
    }

    public static void cutOffSwitch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("switch", "Using a switch");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);

        BlockPos cutOffSwitch = util.grid().at(3, 1, 1);
        BlockPos bulb = util.grid().at(1, 1, 1);
        BlockPos battery = util.grid().at(2, 1, 3);

        scene.idle(20);
        connections.createConnection(new InWorldNode(1, battery), new InWorldNode(1, cutOffSwitch));
        connections.createConnection(new InWorldNode(0, cutOffSwitch), new InWorldNode(1, bulb));
        connections.createConnection(new InWorldNode(0, bulb), new InWorldNode(0, battery));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Switches can be used to interrupt the flow of current")
                .pointAt(cutOffSwitch.getCenter())
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(cutOffSwitch.getCenter(), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(40);

        scene.world().modifyBlock(cutOffSwitch, bs -> bs.setValue(CutOffSwitchBlock.CLOSED, true), false);

        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(1));

        ElementLink<CurrentVisualizationPonderElement> visualization1 = connections.createCurrentVisualization(new InWorldNode(1, battery), new InWorldNode(1, cutOffSwitch), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization2 = connections.createCurrentVisualization(new InWorldNode(1, cutOffSwitch), new InWorldNode(0, cutOffSwitch), 0, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization3 = connections.createCurrentVisualization(new InWorldNode(0, cutOffSwitch), new InWorldNode(1, bulb), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization4 = connections.createCurrentVisualization(new InWorldNode(1, bulb), new InWorldNode(0, bulb), 0, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization5 = connections.createCurrentVisualization(new InWorldNode(0, bulb), new InWorldNode(0, battery), 1, 1, true);


        scene.idle(60);

        scene.overlay().showControls(cutOffSwitch.getCenter(), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(40);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        connections.removeCurrentVisualization(visualization5);

        scene.world().modifyBlock(cutOffSwitch, bs -> bs.setValue(CutOffSwitchBlock.CLOSED, false), false);

        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 0), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(0));
        scene.idle(20);

    }

    public static void relay(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("relay", "Using a relay");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos relay = util.grid().at(2, 1, 1);
        BlockPos bulb = util.grid().at(3, 1, 3);
        BlockPos lowSource = util.grid().at(1, 1, 4);
        BlockPos hiSource = util.grid().at(4, 1, 2);
        BlockPos cutOffSwitch = util.grid().at(1, 1, 0);

        Selection switchCircuit = util.select().fromTo(0, 1, 0, 2, 1, 4).substract(util.select().fromTo(1, 1, 3, 2, 1, 1));

        Selection powerCircuit = util.select().fromTo(1, 1, 1, 4, 1, 3).substract(util.select().position(relay));

        scene.world().showSection(util.select().position(relay), Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Relays are just switches that can are operated with a small electric current")
                .pointAt(relay.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.world().showSection(switchCircuit, Direction.DOWN);

        scene.idle(20);

        connections.createConnection(new InWorldNode(0, 1, 1, 4), new InWorldNode(0, 2, 1, 4));
        connections.createConnection(new InWorldNode(0, 2, 1, 4), new InWorldNode(1, 2, 1, 1));
        connections.createConnection(new InWorldNode(0, 2, 1, 1), new InWorldNode(0, 2, 1, 0));
        connections.createConnection(new InWorldNode(0, 2, 1, 0), new InWorldNode(1, 1, 1, 0));
        connections.createConnection(new InWorldNode(0, 1, 1, 0), new InWorldNode(0, 0, 1, 0));
        connections.createConnection(new InWorldNode(0, 0, 1, 0), new InWorldNode(0, 0, 1, 4));
        connections.createConnection(new InWorldNode(0, 0, 1, 4), new InWorldNode(1, 1, 1, 4));
        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("voltage12")
                .pointAt(util.vector().blockSurface(lowSource, Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        scene.overlay().showText(70)
                .text("Connect the controlling circuit to the 'coil' connectors")
                .pointAt(relay.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.world().showSection(powerCircuit, Direction.DOWN);

        scene.idle(20);

        connections.createConnection(new InWorldNode(1, 4, 1, 2), new InWorldNode(0, 4, 1, 1), CEEWireTypes.COLORED_WIRES[DyeColor.CYAN.ordinal()].get());
        connections.createConnection(new InWorldNode(0, 4, 1, 1), new InWorldNode(3, 2, 1, 1), CEEWireTypes.COLORED_WIRES[DyeColor.CYAN.ordinal()].get());
        connections.createConnection(new InWorldNode(2, 2, 1, 1), new InWorldNode(0, 1, 1, 1), CEEWireTypes.COLORED_WIRES[DyeColor.CYAN.ordinal()].get());
        connections.createConnection(new InWorldNode(0, 1, 1, 1), new InWorldNode(0, 1, 1, 3), CEEWireTypes.COLORED_WIRES[DyeColor.CYAN.ordinal()].get());
        connections.createConnection(new InWorldNode(0, 1, 1, 3), new InWorldNode(0, 3, 1, 3), CEEWireTypes.COLORED_WIRES[DyeColor.CYAN.ordinal()].get());
        connections.createConnection(new InWorldNode(1, 3, 1, 3), new InWorldNode(0, 4, 1, 3), CEEWireTypes.COLORED_WIRES[DyeColor.CYAN.ordinal()].get());
        connections.createConnection(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 2), CEEWireTypes.COLORED_WIRES[DyeColor.CYAN.ordinal()].get());
        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("voltage300")
                .pointAt(util.vector().blockSurface(hiSource, Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);

        scene.overlay().showText(70)
                .text("Connect the switched circuit to the remaining connectors")
                .pointAt(relay.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showControls(cutOffSwitch.getCenter(), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(40);

        scene.world().modifyBlock(cutOffSwitch, bs -> bs.setValue(CutOffSwitchBlock.CLOSED, true), false);
        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(1));

        ElementLink<CurrentVisualizationPonderElement> visualization1 = connections.createCurrentVisualization(new InWorldNode(1, 4, 1, 2), new InWorldNode(0, 4, 1, 1), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization2 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 1), new InWorldNode(3, 2, 1, 1), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization3 = connections.createCurrentVisualization(new InWorldNode(2, 2, 1, 1), new InWorldNode(0, 1, 1, 1), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization4 = connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 1), new InWorldNode(0, 1, 1, 3), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization5 = connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 3), new InWorldNode(0, 3, 1, 3), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization6 = connections.createCurrentVisualization(new InWorldNode(1, 3, 1, 3), new InWorldNode(0, 4, 1, 3), 1, 1, true);
        ElementLink<CurrentVisualizationPonderElement> visualization7 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 2), 1, 1, true);

        scene.idle(60);

        scene.overlay().showText(70)
                .text("Wrench the relay to change its state to Normally-Closed (NC)")
                .pointAt(relay.getBottomCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showControls(relay.getCenter(), Pointing.DOWN, 20)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(20);

        scene.world().modifyBlock(relay, bs -> bs.setValue(RelayBlock.INVERTED, true), false);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        connections.removeCurrentVisualization(visualization5);
        connections.removeCurrentVisualization(visualization6);
        connections.removeCurrentVisualization(visualization7);

        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 0), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(0));
        scene.idle(20);

        scene.overlay().showControls(cutOffSwitch.getCenter(), Pointing.DOWN, 20)
                .rightClick();
        scene.idle(40);

        scene.world().modifyBlock(cutOffSwitch, bs -> bs.setValue(CutOffSwitchBlock.CLOSED, false), false);
        scene.world().modifyBlock(bulb, bs -> bs.setValue(BulbBlock.LIGHT, 15), false);
        scene.world().modifyBlockEntity(bulb, BulbBlockEntity.class, be -> be.setLight(1));

        connections.createCurrentVisualization(new InWorldNode(1, 4, 1, 2), new InWorldNode(0, 4, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 1), new InWorldNode(3, 2, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(2, 2, 1, 1), new InWorldNode(0, 1, 1, 1), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 1), new InWorldNode(0, 1, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 1, 1, 3), new InWorldNode(0, 3, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, 3, 1, 3), new InWorldNode(0, 4, 1, 3), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 3), new InWorldNode(0, 4, 1, 2), 1, 1, true);

        scene.idle(60);
    }

    public static void sf6Breaker(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("sf6_breaker_introduction", "Using a Sulfur Hexafluoride Breaker");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection breaker = util.select().fromTo(2, 1, 0, 2, 2, 0);

        Selection connectors = util.select().fromTo(0, 1, 0, 0, 1, 4)
                .add(util.select().fromTo(4, 1, 0, 4, 1, 4));

        Selection redstone = util.select().fromTo(2, 1, 1, 2, 1, 2);

        BlockPos bulb = util.grid().at(0, 1, 2);


        scene.world().showSection(breaker, Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(120)
                .text("The sulfur hexafluoride breaker can be used to connect or disconnect high voltage circuits.")
                .pointAt(breaker.getCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(140);

        scene.world().showSection(connectors, Direction.DOWN);

        scene.idle(40);

        connections.createConnection(new InWorldNode(1, 4, 1, 2), new InWorldNode(0, 4, 1, 0));
        connections.createConnection(new InWorldNode(0, 4, 1, 0), new InWorldNode(0, 2, 2, 0));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Connect the input to the top side of the breaker")
                .pointAt(breaker.getCenter().add(0, 1, 0))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createConnection(new InWorldNode(0, 2, 1, 0), new InWorldNode(0, 0, 1, 0));
        connections.createConnection(new InWorldNode(0, 0, 1, 0), new InWorldNode(1, bulb));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Connect the output to the bottom side of the breaker")
                .pointAt(breaker.getCenter().add(0, -1, 0))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        connections.createConnection(new InWorldNode(0, bulb), new InWorldNode(0, 0, 1, 4));
        connections.createConnection(new InWorldNode(0, 0, 1, 4), new InWorldNode(0, 4, 1, 4));
        connections.createConnection(new InWorldNode(0, 4, 1, 4), new InWorldNode(0, 4, 1, 2));
        connections.setBulbState(bulb, 1);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("By default, the breaker allows current flow")
                .pointAt(breaker.getCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        var visualization1 = connections.createCurrentVisualization(new InWorldNode(1, 4, 1, 2), new InWorldNode(0, 4, 1, 0), 1, 1, true);
        var visualization2 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 0), new InWorldNode(0, 2, 2, 0), 1, 1, true);
        var visualization3 = connections.createCurrentVisualization(new InWorldNode(0, 2, 1, 0), new InWorldNode(0, 0, 1, 0), 1, 1, true);
        var visualization4 = connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 0), new InWorldNode(1, bulb), 1, 1, true);
        var visualization5 = connections.createCurrentVisualization(new InWorldNode(0, bulb), new InWorldNode(0, 0, 1, 4), 1, 1, true);
        var visualization6 = connections.createCurrentVisualization(new InWorldNode(0, 0, 1, 4), new InWorldNode(0, 4, 1, 4), 1, 1, true);
        var visualization7 = connections.createCurrentVisualization(new InWorldNode(0, 4, 1, 4), new InWorldNode(0, 4, 1, 2), 1, 1, true);

        scene.idle(20);

        scene.world().showSection(redstone, Direction.DOWN);

        scene.idle(40);

        scene.world().modifyBlock(util.grid().at(2, 1, 1), bs -> bs.setValue(BlockStateProperties.POWER, 15), false);
        scene.world().modifyBlock(util.grid().at(2, 1, 2), bs -> bs.setValue(BlockStateProperties.POWERED, true), false);

        connections.removeCurrentVisualization(visualization1);
        connections.removeCurrentVisualization(visualization2);
        connections.removeCurrentVisualization(visualization3);
        connections.removeCurrentVisualization(visualization4);
        connections.removeCurrentVisualization(visualization5);
        connections.removeCurrentVisualization(visualization6);
        connections.removeCurrentVisualization(visualization7);
        connections.setBulbState(bulb, 0);

        scene.idle(20);
        scene.overlay().showText(70)
                .text("Once powered with redstone, it disconnects the circuit and stops current flow")
                .pointAt(breaker.getCenter())
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

    }
}
