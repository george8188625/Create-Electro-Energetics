package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
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

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 0)),
                new InWorldNode(1, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 2)));

        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));

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
                .sharedText("ratio1over2")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 8)),
                new InWorldNode(1, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(2, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 6)));

        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 8)),
                new InWorldNode(0, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(3, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 6)));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);
        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 6), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .6f));

        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("voltage300")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .sharedText("voltage600")
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

        ElementLink<WirePonderElement> wire1 = connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(2, 1, 3)));
        ElementLink<WirePonderElement> wire2 = connections.createConnection(new InWorldNode(1, util.grid().at(2, 1, 3)),
                new InWorldNode(0, util.grid().at(4, 1, 4)));
        ElementLink<WirePonderElement> wire3 = connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 8)));

        ElementLink<WirePonderElement> wire4 = connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 8)),
                new InWorldNode(1, util.grid().at(4, 1, 4)));
        ElementLink<WirePonderElement> wire5 =  connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 0)));
        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 3), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .5f));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Wires have some resistance, therefore they cause energy losses.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.overlay().showText(20)
                .sharedText("voltage300")
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
                .text("10A")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .placeNearTarget();

        scene.idle(50);

        scene.overlay().showText(130)
                .text("Energy losses are directly proportional to the resistance and the square of the amperage.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(150);

        connections.removeConnection(wire1);
        connections.removeConnection(wire2);
        connections.removeConnection(wire3);
        connections.removeConnection(wire4);
        connections.removeConnection(wire5);
        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 0);

        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 3), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", 0f));

        scene.idle(20);

        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 1, 6), Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Transformers can be used to increase the voltage, therefore decrease the amperage.")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(80);

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 0)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 0)),
                new InWorldNode(1, util.grid().at(4, 1, 2)));

        connections.createConnection(new InWorldNode(0, util.grid().at(2, 1, 3)),
                new InWorldNode(3, util.grid().at(4, 1, 2)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(2, 1, 3)));


        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 2)));

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 4)),
                new InWorldNode(0, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 4)),
                new InWorldNode(1, util.grid().at(4, 1, 6)));

        connections.createConnection(new InWorldNode(1, util.grid().at(4, 1, 8)),
                new InWorldNode(2, util.grid().at(4, 1, 6)));
        connections.createConnection(new InWorldNode(0, util.grid().at(4, 1, 8)),
                new InWorldNode(3, util.grid().at(4, 1, 6)));

        scene.world().setKineticSpeed(util.select().position(4, 1, 8), 256);

        scene.world().modifyBlockEntityNBT(util.select().position(2, 1, 3), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .2f));

        scene.world().modifyBlockEntityNBT(util.select().position(4, 1, 4), ElectricGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .6f));

        scene.idle(20);

        scene.overlay().showText(20)
                .sharedText("ratio1over3")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .sharedText("ratio3over1")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 6), Direction.WEST))
                .placeNearTarget();
        scene.idle(50);

        scene.overlay().showText(20)
                .text("3.3A")
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

    public static void turns(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("transformer_turns", "How transformers work");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos source = util.grid().at(4, 1, 0);
        BlockPos primaryMeter = util.grid().at(4, 1, 2);
        BlockPos primary = util.grid().at(4, 1, 4);
        BlockPos secondary = util.grid().at(4, 1, 5);
        BlockPos secondaryMeter = util.grid().at(4, 1, 7);
        BlockPos bulb = util.grid().at(4, 2, 7);


    }
}
