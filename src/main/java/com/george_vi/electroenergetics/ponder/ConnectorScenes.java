package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.foundation.InWorldNode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;

public class ConnectorScenes {
    public static void connectors(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("connectors", "Connecting Wires");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection connectors = util.select().fromTo(1,1,2,3,1,2);
        Selection battery = util.select().position(2,1,4);
        Selection bulb = util.select().position(2,1,0);

        scene.world().showSection(battery, Direction.DOWN);
        scene.world().showSection(connectors, Direction.DOWN);
        scene.world().showSection(bulb, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Connectors and Wires can be used to relay electricity.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 2), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().of(2, 1.5, 4.5), Pointing.DOWN, 20)
                        .withItem(CEEItems.WIRE_SPOOL.asStack())
                        .rightClick();
        scene.idle(40);

        scene.overlay().showControls(util.vector().centerOf(1, 1, 2), Pointing.DOWN, 20)
                .withItem(CEEItems.WIRE_SPOOL.asStack())
                .rightClick();
        scene.idle(40);

        ElementLink<WirePonderElement> wire = connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 4)), new InWorldNode(0, util.grid().at(1, 1, 2)));

        scene.idle(20);

        scene.overlay().showText(70)
                .text("To remove existing wires, use an empty spool.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 2), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().of(2, 1.5, 4.5), Pointing.DOWN, 20)
                .withItem(CEEItems.EMPTY_SPOOL.asStack())
                .rightClick();
        scene.idle(40);

        scene.overlay().showControls(util.vector().centerOf(1, 1, 2), Pointing.DOWN, 20)
                .withItem(CEEItems.EMPTY_SPOOL.asStack())
                .rightClick();
        scene.idle(40);

        connections.removeConnection(wire);

        scene.idle(40);

        ElementLink<WirePonderElement> coloredWire = connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 4)), new InWorldNode(0, util.grid().at(1, 1, 2)));
        scene.idle(20);

        scene.overlay().showText(40)
                .text("Insulated wires can be dyed.")
                .pointAt(util.vector().of(1.65, 1.5, 3.5))
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(60);

        scene.overlay().showControls(util.vector().of(1.65, 1.5, 3.5), Pointing.DOWN, 20)
                .withItem(Items.LIGHT_BLUE_DYE.getDefaultInstance())
                .rightClick();
        scene.idle(40);

        connections.removeConnection(coloredWire);
        connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 4)), new InWorldNode(0, util.grid().at(1, 1, 2)), CEEWireTypes.COLORED_WIRES.get(DyeColor.LIGHT_BLUE).get());

        scene.idle(40);

        connections.createConnection(new InWorldNode(1, util.grid().at(2, 1, 0)), new InWorldNode(0, util.grid().at(3, 1, 2)));
        scene.idle(2);
        connections.createConnection(new InWorldNode(1, util.grid().at(2, 1, 4)), new InWorldNode(0, util.grid().at(3, 1, 2)));
        scene.idle(2);
        connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 0)), new InWorldNode(0, util.grid().at(1, 1, 2)));
        scene.idle(2);

        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(2, 1, 4)), new InWorldNode(0, util.grid().at(1, 1, 2)), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(1, util.grid().at(2, 1, 0)), new InWorldNode(0, util.grid().at(3, 1, 2)), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(1, util.grid().at(2, 1, 4)), new InWorldNode(0, util.grid().at(3, 1, 2)), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(2, 1, 0)), new InWorldNode(0, util.grid().at(1, 1, 2)), 1, 1, true);

        scene.world().modifyBlock(util.grid().at(2, 1, 0), s -> s.setValue(BulbBlock.LIGHT, 2), false);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 0), BulbBlockEntity.class, be -> be.light = 1);
    }

    public static void chunks(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("connectors_chunks", "Wires through unloaded Chunks");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        Selection firstPlatform = util.select().fromTo(0,1,0,4,1,4);
        Selection secondPlatform = util.select().fromTo(0,0,40,4,1,44);

        Selection wires = util.select().fromTo(1,1,13,3,1,31);

        scene.world().showSection(firstPlatform, Direction.DOWN);

        scene.idle(20);
        connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 0)), new InWorldNode(0, util.grid().at(1, 1, 2)));
        connections.createConnection(new InWorldNode(1, util.grid().at(2, 1, 0)), new InWorldNode(0, util.grid().at(3, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(1, 1, 2)), new InWorldNode(0, util.grid().at(1, 1, 4)));
        connections.createConnection(new InWorldNode(0, util.grid().at(3, 1, 2)), new InWorldNode(0, util.grid().at(3, 1, 4)));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Electricity stays functional outside of loaded chunks.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 5), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(wires, Direction.DOWN);
        scene.idle(15);
        connections.createConnection(new InWorldNode(0, util.grid().at(1, 1,13)), new InWorldNode(0, util.grid().at(1, 1, 4)));
        connections.createConnection(new InWorldNode(0, util.grid().at(3, 1,13)), new InWorldNode(0, util.grid().at(3, 1, 4)));
        connections.createConnection(new InWorldNode(0, util.grid().at(1, 1,13)), new InWorldNode(0, util.grid().at(1, 1, 22)));
        connections.createConnection(new InWorldNode(0, util.grid().at(3, 1,13)), new InWorldNode(0, util.grid().at(3, 1, 22)));
        connections.createConnection(new InWorldNode(0, util.grid().at(1, 1,31)), new InWorldNode(0, util.grid().at(1, 1, 22)));
        connections.createConnection(new InWorldNode(0, util.grid().at(3, 1,31)), new InWorldNode(0, util.grid().at(3, 1, 22)));
        connections.createConnection(new InWorldNode(0, util.grid().at(1, 1,31)), new InWorldNode(0, util.grid().at(1, 1, 40)));
        connections.createConnection(new InWorldNode(0, util.grid().at(3, 1,31)), new InWorldNode(0, util.grid().at(3, 1, 40)));

        scene.idle(60);
    }
}
