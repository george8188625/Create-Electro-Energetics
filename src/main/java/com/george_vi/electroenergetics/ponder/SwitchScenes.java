package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.cut_off_switch.HVSwitchBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class SwitchScenes {

    public static void hvSwitch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("hv_switch", "Setting up a High Voltage Switch");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection connectors = util.select().fromTo(1,1,4,3,1,4);
        Selection battery = util.select().position(4,1,2);
        Selection bulb = util.select().position(1,1,2);
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
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), BulbBlockEntity.class, be -> be.light = 1);
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
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), BulbBlockEntity.class, be -> be.light = 0);
        scene.idle(105);

        scene.world().modifyBlock(util.grid().at(3, 1, 1), bs -> bs.setValue(BlockStateProperties.POWER, 0), false);
        scene.world().modifyBlock(util.grid().at(3, 1, 2), bs -> bs.setValue(BlockStateProperties.POWERED, false), false);

        scene.world().modifyBlockEntity(util.grid().at(3, 1, 0), HVSwitchBlockEntity.class, be -> be.connected = true);
        scene.idle(85);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), BulbBlockEntity.class, be -> be.light = 1);
        scene.idle(60);
    }
}
