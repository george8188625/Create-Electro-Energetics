package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.simulation.WireTypes;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;

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

        ElementLink<WirePonderElement> wire = connections.createConnection(util.vector().of(2, 1.5, 4.5), util.vector().centerOf(1, 1, 2), WireTypes.STANDARD);

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

        connections.createConnection(util.vector().of(2, 1.5, 4.5), util.vector().centerOf(1, 1, 2), WireTypes.STANDARD);
        scene.idle(2);
        connections.createConnection(util.vector().of(46/16f, 19/16f, 0.5), util.vector().centerOf(3, 1, 2), WireTypes.STANDARD);
        scene.idle(2);
        connections.createConnection(util.vector().of(3, 1.5, 4.5), util.vector().centerOf(3, 1, 2), WireTypes.STANDARD);
        scene.idle(2);
        connections.createConnection(util.vector().of(34/16f, 19/16f, 0.5), util.vector().centerOf(1, 1, 2), WireTypes.STANDARD);
        scene.idle(2);

        scene.world().modifyBlock(util.grid().at(2, 1, 0), s -> s.setValue(BulbBlock.LIGHT, 2), false);

        scene.idle(60);
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
        connections.createConnection(util.vector().of(2, 1.5,0.5), util.vector().centerOf(1, 1, 2), WireTypes.STANDARD);
        connections.createConnection(util.vector().of(3, 1.5,0.5), util.vector().centerOf(3, 1, 2), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(1, 1,2), util.vector().centerOf(1, 1, 4), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(3, 1,2), util.vector().centerOf(3, 1, 4), WireTypes.STANDARD);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Electricity stays functional outside of loaded chunks.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 5), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(wires, Direction.DOWN);
        scene.idle(15);
        connections.createConnection(util.vector().centerOf(1, 1,13), util.vector().centerOf(1, 1, 4), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(3, 1,13), util.vector().centerOf(3, 1, 4), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(1, 1,13), util.vector().centerOf(1, 1, 22), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(3, 1,13), util.vector().centerOf(3, 1, 22), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(1, 1,31), util.vector().centerOf(1, 1, 22), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(3, 1,31), util.vector().centerOf(3, 1, 22), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(1, 1,31), util.vector().centerOf(1, 1, 40), WireTypes.STANDARD);
        connections.createConnection(util.vector().centerOf(3, 1,31), util.vector().centerOf(3, 1, 40), WireTypes.STANDARD);
        connections.createConnection(util.vector().of(34/16f, 19/16f, 43.5), util.vector().centerOf(1, 1, 40), WireTypes.STANDARD);
        connections.createConnection(util.vector().of(46/16f, 19/16f, 43.5), util.vector().centerOf(3, 1, 40), WireTypes.STANDARD);


        scene.idle(60);
    }
}
