package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.content.SimpleDeviceBlock;
import com.george_vi.electroenergetics.simulation.DeviceBlock;
import com.george_vi.electroenergetics.simulation.SimulatedDevice;
import com.george_vi.electroenergetics.simulation.SimulatedDevices;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftScreen;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EnergyMeterBlock extends SimpleDeviceBlock implements IWrenchable, IBE<EnergyMeterBlockEntity> {
    public static final BooleanProperty UPSIDE_DOWN = BooleanProperty.create("upside_down");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public EnergyMeterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        withBlockEntityDo(level, pos, be -> {
            be.owner = placer.getUUID();
        });
    }

    @Override
    protected SimulatedDevice getDevice() {
        return SimulatedDevices.ENERGY_METER;
    }

    @Override
    protected CompoundTag getExtraData(Level level, BlockState state, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("closed", true);
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
        builder.add(UPSIDE_DOWN, WATERLOGGED, FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(UPSIDE_DOWN) ? CEEShapes.ENERGY_METER_UPSIDE_DOWN.get(state.getValue(FACING)) : CEEShapes.ENERGY_METER.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(UPSIDE_DOWN, (context.getPlayer().isShiftKeyDown()) ^ (context.getNearestLookingVerticalDirection() == Direction.DOWN));
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public Map<Vec3, Integer> getNodePositions(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(FACING) == Direction.NORTH)
            return state.getValue(UPSIDE_DOWN) ?
                    Map.of(new Vec3(2/16f, 14/16f, 14/16f), 2, new Vec3(0.5f, 14/16f, 14/16f), 1, new Vec3(14/16f, 14/16f, 14/16f), 0) :
                    Map.of(new Vec3(2/16f, 2/16f, 14/16f), 2, new Vec3(0.5f, 2/16f, 14/16f), 1, new Vec3(14/16f, 2/16f, 14/16f), 0);
        if (state.getValue(FACING) == Direction.EAST)
            return state.getValue(UPSIDE_DOWN) ?
                    Map.of(new Vec3(2/16f, 14/16f, 2/16f), 2, new Vec3(2/16f, 14/16f, 8/16f), 1, new Vec3(2/16f, 14/16f, 14/16f), 0) :
                    Map.of(new Vec3(2/16f, 2/16f, 2/16f), 2, new Vec3(2/16f, 2/16f, 8/16f), 1, new Vec3(2/16f, 2/16f, 14/16f), 0);
        if (state.getValue(FACING) == Direction.SOUTH)
            return state.getValue(UPSIDE_DOWN) ?
                    Map.of(new Vec3(2/16f, 14/16f, 2/16f), 0, new Vec3(0.5f, 14/16f, 2/16f), 1, new Vec3(14/16f, 14/16f, 2/16f), 2) :
                    Map.of(new Vec3(2/16f, 2/16f, 2/16f), 0, new Vec3(0.5f, 2/16f, 2/16f), 1, new Vec3(14/16f, 2/16f, 2/16f), 2);
        return state.getValue(UPSIDE_DOWN) ?
                Map.of(new Vec3(14/16f, 14/16f, 2/16f), 0, new Vec3(14/16f, 14/16f, 8/16f), 1, new Vec3(14/16f, 14/16f, 14/16f), 2) :
                Map.of(new Vec3(14/16f, 2/16f, 2/16f), 0, new Vec3(14/16f, 2/16f, 8/16f), 1, new Vec3(14/16f, 2/16f, 14/16f), 2);
    }

    @Override
    public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {

        if (state.getValue(FACING) == Direction.NORTH)
            return id == 2 ? new Vec3(2/16f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 14/16f) :
                    id == 1 ? new Vec3(0.5f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 14/16f) :
                            new Vec3(14/16f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 14/16f);

        if (state.getValue(FACING) == Direction.EAST)
            return id == 2 ? new Vec3(2/16f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 2/16f) :
                    id == 1 ? new Vec3(2/16f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 8/16f) :
                            new Vec3(2/16f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 14/16f);

        if (state.getValue(FACING) == Direction.SOUTH)
            return id == 0 ? new Vec3(2/16f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 2/16f) :
                    id == 1 ? new Vec3(0.5f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 2/16f) :
                            new Vec3(14/16f, state.getValue(UPSIDE_DOWN) ? 14/16f : 2/16f, 2/16f);

        return state.getValue(UPSIDE_DOWN) ?
               id == 0 ? new Vec3(14/16f, 14/16f, 2/16f) : id == 1 ? new Vec3(14/16f, 14/16f, 8/16f) :new Vec3(14/16f, 14/16f, 14/16f) :
               id == 0 ? new Vec3(14/16f, 2/16f, 2/16f) : id == 1 ? new Vec3(14/16f, 2/16f, 8/16f) : new Vec3(14/16f, 2/16f, 14/16f);

    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return id == 0 ? Component.translatable("electroenergetics.nodes.feed") : id == 1 ? Component.translatable("electroenergetics.nodes.neutral") : Component.translatable("electroenergetics.nodes.load");
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
