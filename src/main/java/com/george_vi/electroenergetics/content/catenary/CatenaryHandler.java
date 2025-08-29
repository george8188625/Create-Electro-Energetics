package com.george_vi.electroenergetics.content.catenary;

import com.george_vi.electroenergetics.events.AddToElectricGraphEvent;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.foundation.Node;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CatenaryHandler {

    static Map<UUID, Node> trains = new HashMap<>();

    public static void addToGraph(AddToElectricGraphEvent event) {
        trains.clear();

        Map<Train, List<Pair<Float, Couple<BlockPos>>>> catenaryConnections = new HashMap<>();

        for (Train train : Create.RAILWAYS.trains.values()) {
            for (Carriage carriage : train.carriages) {
                List<Pair<BlockPos, Boolean>> pantographs = ((IPantographList)carriage).getPantographList();
                if (carriage.presentInMultipleDimensions())
                    continue;
                Carriage.DimensionalCarriageEntity dce = carriage.getDimensional(carriage.getPresentDimensions().getFirst());

                Vec3 positionVec = dce.rotationAnchors.getFirst();
                Vec3 coupledVec = dce.rotationAnchors.getSecond();
                if (positionVec == null || coupledVec == null)
                    continue;

                double diffX = positionVec.x - coupledVec.x;
                double diffY = positionVec.y - coupledVec.y;
                double diffZ = positionVec.z - coupledVec.z;

                float yaw = (float) (Mth.atan2(diffZ, diffX) * 180 / Math.PI) + 180;
                float pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI) * -1;

                Vec3 pivotPosition = carriage.isOnTwoBogeys() ? positionVec : VecHelper.lerp(0.5f, positionVec, coupledVec);

                for (Pair<BlockPos, Boolean> pantograph : pantographs) {


                    Vec3 pantographPos = Vec3.atLowerCornerOf(pantograph.getFirst()).add(0, 1.5, pantograph.getSecond() ? 0.5 : -0.5);

                    pantographPos = VecHelper.rotate(pantographPos, pitch, Direction.Axis.X);
                    pantographPos = VecHelper.rotate(pantographPos, -yaw - 90, Direction.Axis.Y);

                    pantographPos = pivotPosition.add(pantographPos);

                    for (Couple<BlockPos> connection : event.sd.getAllCatenaryConnections()) {
                        Vec3 start = connection.getFirst().getBottomCenter();
                        Vec3 end = connection.getSecond().getBottomCenter();

                        double t = 0;

                        Vec3 ab = end.subtract(start);
                        Vec3 ap = pantographPos.subtract(start);
                        double denom = ab.lengthSqr();
                        if (denom != 0) {
                            t = ap.dot(ab) / denom;
                            t = Math.max(0, Math.min(1, t));
                        }

                        Vec3 closest = VecHelper.lerp((float) t, start, end);

                        Vec3 distance = pantographPos.subtract(closest);
                        distance = VecHelper.rotate(distance, yaw + 90, Direction.Axis.Y);

                        if (!(Math.abs(distance.x()) > 1.5) && !(Math.abs(distance.z()) > 0.2) && !(Math.abs(distance.y()) > 1)) {
                            event.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, closest.x, closest.y, closest.z, 3, 0, 0, 0, 0);
                            catenaryConnections.computeIfAbsent(train, k -> new ArrayList<>());
                            catenaryConnections.get(train).add(Pair.of((float) t, connection));
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void finishSimulation(FinishElectricSimulationEvent event) {

    }
}
