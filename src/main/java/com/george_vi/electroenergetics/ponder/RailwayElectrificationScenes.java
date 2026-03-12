package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlockEntity;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;

public class RailwayElectrificationScenes {

    public static void setup(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("railway_electrification", "Electrifying Trains");
        scene.configureBasePlate(0, 0, 15);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.scaleSceneView(0.75f);

        Selection train = util.select().fromTo(1, 2, 2, 5, 4, 4);
        Selection tracks = util.select().fromTo(0, 1, 0, 14, 1, 3);
        Selection poles = util.select().fromTo(1, 1, 6, 13, 7, 6);
        Selection powerSupply = util.select().fromTo(8, 1, 8, 12, 1, 9);
        BlockPos holder1 = util.grid().at(1, 6, 3);
        BlockPos holder2 = util.grid().at(13, 6, 3);

        scene.world().showSection(tracks, Direction.EAST);

        scene.idle(20);

        scene.world().showSection(util.select().fromTo(holder1, holder2), Direction.DOWN);
        scene.idle(20);

        scene.world().showSection(poles, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Catenary Holders will automatically connect to nearby pole-like blocks")
                .colored(PonderPalette.BLUE)
                .pointAt(holder1.getCenter())
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.overlay().showControls(holder1.getCenter(), Pointing.DOWN, 20)
                .withItem(CEEItems.COPPER_WIRE_SPOOL.asStack())
                .rightClick();
        scene.idle(40);

        scene.overlay().showControls(holder2.getCenter(), Pointing.DOWN, 20)
                .withItem(CEEItems.COPPER_WIRE_SPOOL.asStack())
                .rightClick();
        scene.idle(40);

        connections.createCatenaryConnection(holder1, holder2, CEEWireTypes.STANDARD.get(), 1);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("A catenary system can be used to power trains")
                .pointAt(holder1.getBottomCenter().add(holder2.getBottomCenter()).scale(0.5))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        ElementLink<WorldSectionElement> trainElement = scene.world().showIndependentSection(train, Direction.DOWN);

        scene.overlay().showText(100)
                .text("A train, that uses electricity, must have a pantograph that touches the catenary wires ...")
                .pointAt(util.vector().topOf(4, 4, 3))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(70)
                .text("and an Electric Motor ...")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 3), Direction.NORTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(100);

        scene.rotateCameraY(-90);

        scene.idle(20);

        connections.createConnection(new InWorldNode(0, util.grid().at(13, 6, 3)),
                new InWorldNode(0, util.grid().at(12, 6, 6)));
        scene.idle(20);

        scene.world().showSection(powerSupply, Direction.DOWN);
        scene.idle(20);

        connections.createConnection(new InWorldNode(0, util.grid().at(12, 1, 8)),
                new InWorldNode(0, util.grid().at(12, 6, 6)));
        scene.idle(2);

        connections.createConnection(new InWorldNode(1, util.grid().at(10, 1, 9)),
                new InWorldNode(0, util.grid().at(12, 1, 8)));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("To power the Catenary System, connect the overhead lines to the positive terminal")
                .pointAt(util.vector().blockSurface(util.grid().at(10, 1, 9), Direction.EAST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        connections.createConnection(new InWorldNode(0, util.grid().at(10, 1, 9)),
                new InWorldNode(0, util.grid().at(8, 1, 9)));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Connect the negative to a Ground Rod")
                .pointAt(util.vector().blockSurface(util.grid().at(10, 1, 9), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(100);

        scene.overlay().showText(40)
                .text("2500 V")
                .pointAt(util.vector().topOf(10, 1, 9))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(60);

        scene.rotateCameraY(90);
        scene.idle(60);

        ElementLink<ParrotElement> birb =
                scene.special().createBirb(util.vector().centerOf(2, 3, 3), ParrotPose.FacePointOfInterestPose::new);
        scene.idle(20);

        scene.world().moveSection(trainElement, util.vector().of(9, 0, 0), 60);
        scene.world().animateBogey(util.grid().at(3, 2, 3), -9, 60);
        scene.special().moveParrot(birb, util.vector().of(9, 0, 0), 60);
        scene.idle(80);

        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(13, 6, 3)),
                new InWorldNode(0, util.grid().at(12, 6, 6)), 1, -1, true);
        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(12, 1, 8)),
                new InWorldNode(0, util.grid().at(12, 6, 6)), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(1, util.grid().at(10, 1, 9)),
                new InWorldNode(0, util.grid().at(12, 1, 8)), 1, 1, true);
        connections.createCurrentVisualization(new InWorldNode(0, util.grid().at(10, 1, 9)),
                new InWorldNode(0, util.grid().at(8, 1, 9)), 1, -1, true);

        scene.idle(60);

        scene.overlay().showText(70)
                .text("Pantographs can be dyed")
                .pointAt(util.vector().topOf(13, 3, 3))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(13, 3, 3), Pointing.DOWN, 20)
                .withItem(Items.RED_DYE.getDefaultInstance())
                .rightClick();

        scene.idle(40);

        scene.world().modifyBlockEntity(util.grid().at(4, 4, 3), PantographBlockEntity.class, be -> be.color = DyeColor.RED);

        scene.idle(60);
    }

    public static void sounds(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("electric_train_sounds", "Changing the electric train sound");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.scaleSceneView(0.75f);

        Selection train = util.select().fromTo(0, 2, 3, 3, 4, 5);
        Selection fullTrain = util.select().fromTo(0, 2, 3, 4, 4, 5);
        Selection additionalBlock = util.select().position(4, 2, 4);
        Selection tracks = util.select().fromTo(0, 1, 1, 8, 1, 4);
        Selection poles = util.select().fromTo(1, 1, 7, 7, 7, 7);
        BlockPos holder1 = util.grid().at(0, 6, 4);
        BlockPos holder2 = util.grid().at(8, 6, 4);

        scene.world().showSection(tracks, Direction.EAST);
        scene.idle(20);

        scene.world().showSection(util.select().fromTo(holder1, holder2), Direction.DOWN);
        scene.world().showSection(poles, Direction.DOWN);
        connections.createCatenaryConnection(holder1, holder2, CEEWireTypes.STANDARD.get(), 0);
        scene.idle(20);

        scene.world().showSection(train, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("Electric trains emit a different sound when powered")
                .pointAt(util.vector().topOf(1, 3, 4))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);

        scene.world().showSection(additionalBlock, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("Attaching certain blocks can change this sound to a different style")
                .pointAt(util.vector().topOf(4, 2, 4))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);

        ElementLink<WorldSectionElement> trainElement = scene.world().makeSectionIndependent(fullTrain);

        ElementLink<ParrotElement> birb =
                scene.special().createBirb(util.vector().centerOf(1, 3, 4), ParrotPose.FacePointOfInterestPose::new);
        scene.idle(20);

        scene.world().moveSection(trainElement, util.vector().of(4, 0, 0), 60);
        scene.world().animateBogey(util.grid().at(2, 2, 4), -4, 60);
        scene.special().moveParrot(birb, util.vector().of(4, 0, 0), 60);


        scene.idle(60);
    }

    public static void thirdRail(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        WireConnectionInstructions connections = new WireConnectionInstructions(builder);
        scene.title("third_rail", "Using third rail");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.scaleSceneView(0.75f);

        Selection train = util.select().fromTo(0, 2, 2, 3, 4, 5);
        Selection tracks = util.select().fromTo(0, 1, 1, 8, 1, 4);

        scene.world().showSection(tracks, Direction.EAST);
        scene.idle(20);

        connections.createConnection(new InWorldNode(0, util.grid().at(0, 1, 2)), new InWorldNode(0, util.grid().at(8, 1, 2)), CEEWireTypes.IRON_RAIL.get());
        scene.idle(20);

        scene.world().showSection(train, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("Electric trains can collect power from a third rail")
                .pointAt(util.vector().centerOf(4, 1, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(100)
                .text("For that, use a Rail Contact Shoe")
                .pointAt(util.vector().centerOf(1, 2, 2))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);

        ElementLink<WorldSectionElement> trainElement = scene.world().makeSectionIndependent(train);

        ElementLink<ParrotElement> birb =
                scene.special().createBirb(util.vector().centerOf(1, 3, 4), ParrotPose.FacePointOfInterestPose::new);
        scene.idle(20);

        scene.world().moveSection(trainElement, util.vector().of(4, 0, 0), 60);
        scene.world().animateBogey(util.grid().at(1, 2, 4), -4, 60);
        scene.special().moveParrot(birb, util.vector().of(4, 0, 0), 60);


        scene.idle(60);
    }

}
