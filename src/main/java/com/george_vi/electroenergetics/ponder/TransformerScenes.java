package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;

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

        connections.createConnection(util.vector().blockSurface(util.grid().at(4, 1, 0), Direction.WEST),
                util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST));
        connections.createConnection(util.vector().of(4 + 5/16f, 30/16f, 4 + 3/16f),
                util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST));

        connections.createConnection(util.vector().blockSurface(util.grid().at(4, 1, 0), Direction.EAST),
                util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.EAST));
        connections.createConnection(util.vector().of(4 + 11/16f, 30/16f, 4 + 3/16f),
                util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.EAST));

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
                .text("1 / 2")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        connections.createConnection(util.vector().blockSurface(util.grid().at(4, 1, 8), Direction.WEST),
                util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.WEST));
        connections.createConnection(util.vector().of(4 + 5/16f, 30/16f, 4 + 13/16f),
                util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.WEST));

        connections.createConnection(util.vector().blockSurface(util.grid().at(4, 1, 8), Direction.EAST),
                util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.EAST));
        connections.createConnection(util.vector().of(4 + 11/16f, 30/16f, 4 + 13/16f),
                util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.EAST));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);
        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 6), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .6f));

        scene.idle(20);

        scene.overlay().showText(20)
                .text("300V")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .text("600V")
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

        ElementLink<WirePonderElement> wire1 = connections.createConnection(util.vector().of(4, 1, 0).add(0, 0.5, 0.5),
                util.vector().of(2, 1, 3).add(0.5, 0.5, 0));
        ElementLink<WirePonderElement> wire2 = connections.createConnection(util.vector().of(2, 1, 3).add(0.5, 0.5, 1),
                util.vector().of(4, 1, 4).add(0, 0.5, 0.5));
        ElementLink<WirePonderElement> wire3 = connections.createConnection(util.vector().of(4, 1, 4).add(0, 0.5, 0.5),
                util.vector().of(4, 1, 8).add(2/16f, 0.5, 3/16f));

        ElementLink<WirePonderElement> wire4 = connections.createConnection(util.vector().of(4, 1, 8).add(14/16f, 0.5, 3/16f),
                util.vector().of(4, 1, 4).add(1, 0.5, 0.5));
        ElementLink<WirePonderElement> wire5 =  connections.createConnection(util.vector().of(4, 1, 4).add(1, 0.5, 0.5),
                util.vector().of(4, 1, 0).add(1, 0.5, 0.5));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Wires cause energy losses.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.overlay().showText(20)
                .text("300V")
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
                .text("30A")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(70)
                .text("Energy losses are proportional to the square of the amperage.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(80);

        connections.removeConnection(wire1);
        connections.removeConnection(wire2);
        connections.removeConnection(wire3);
        connections.removeConnection(wire4);
        connections.removeConnection(wire5);
        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 0);

        scene.idle(20);

        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 6), Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Transformers can be used to increase the voltage and decrease the amperage.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(80);

        // CONNECT I DONT WANNA DO THIS PLS

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);
        scene.idle(20);

        scene.overlay().showText(20)
                .text("1 / 3")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .text("3 / 1")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .text("3A")
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
}
