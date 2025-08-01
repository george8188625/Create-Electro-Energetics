package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEENodeConfigurations;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.foundation.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EnergyMeterBlock extends SimpleDeviceBlock implements IWrenchable, IBE<EnergyMeterBlockEntity>, ProperWaterloggedBlock {
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public final boolean triPolar;

    public EnergyMeterBlock(Properties properties, boolean triPolar) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
        this.triPolar = triPolar;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        withBlockEntityDo(level, pos, be -> be.owner = placer.getUUID());
    }

    @Override
    protected SimulatedDevice getDevice() {
        return triPolar ? CEESimulatedDevices.TRI_POLAR_ENERGY_METER : CEESimulatedDevices.ENERGY_METER;
    }

    @Override
    protected CompoundTag getExtraData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Closed", true);
        return tag;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllItems.WRENCH.isIn(stack) || CEEItems.WIRE_SPOOL.isIn(stack) || CEEItems.EMPTY_SPOOL.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> withBlockEntityDo(level, pos, be -> this.displayScreen(be, player)));
        return ItemInteractionResult.SUCCESS;
    }

    @OnlyIn(value = Dist.CLIENT)
    protected void displayScreen(EnergyMeterBlockEntity be, Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new EnergyMeterScreen(be));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INVERTED, WATERLOGGED, FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CEEShapes.ENERGY_METER.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(INVERTED, (context.getPlayer().isShiftKeyDown()) ^ (context.getNearestLookingVerticalDirection() == Direction.DOWN)), context);
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (triPolar) {
            if (state.getValue(INVERTED))
                return CEENodeConfigurations.TRI_POLAR_METERING_MIRRORED.getNodes(state.getValue(FACING));
            return CEENodeConfigurations.TRI_POLAR_METERING.getNodes(state.getValue(FACING));
        }
        if (state.getValue(INVERTED))
            return CEENodeConfigurations.BI_POLAR_METERING_MIRRORED.getNodes(state.getValue(FACING));
        return CEENodeConfigurations.BI_POLAR_METERING.getNodes(state.getValue(FACING));
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
        if (triPolar) {
            if (state.getValue(INVERTED))
                return CEENodeConfigurations.TRI_POLAR_METERING_MIRRORED.getNodePos(state.getValue(FACING), id);
            return CEENodeConfigurations.TRI_POLAR_METERING.getNodePos(state.getValue(FACING), id);
        }
        if (state.getValue(INVERTED))
            return CEENodeConfigurations.BI_POLAR_METERING_MIRRORED.getNodePos(state.getValue(FACING), id);
        return CEENodeConfigurations.BI_POLAR_METERING.getNodePos(state.getValue(FACING), id);
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        if (triPolar)
            return id == 0 ? Component.translatable("electroenergetics.nodes.feed_negative") :
                   id == 2 ? Component.translatable("electroenergetics.nodes.feed_positive") :
                   id == 3 ? Component.translatable("electroenergetics.nodes.load_negative") :
                   id == 5 ? Component.translatable("electroenergetics.nodes.load_positive") :
                            Component.translatable("electroenergetics.nodes.neutral");
        return id == 0 ? Component.translatable("electroenergetics.nodes.feed") :
                id == 2 ? Component.translatable("electroenergetics.nodes.load") :
                        Component.translatable("electroenergetics.nodes.neutral");
    }

    @Override
    public Class<EnergyMeterBlockEntity> getBlockEntityClass() {
        return EnergyMeterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnergyMeterBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ENERGY_METER.get();
    }
}
