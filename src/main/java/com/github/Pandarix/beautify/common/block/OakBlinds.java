package com.github.Pandarix.beautify.common.block;

import java.util.List;

import com.github.Pandarix.beautify.core.init.SoundInit;
import com.github.Pandarix.beautify.util.Config;
import com.github.Pandarix.beautify.util.KeyBoardHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OakBlinds extends HorizontalDirectionalBlock {
	private static final VoxelShape OPEN_NORTH = Block.box(0, 13, 13, 16, 16, 16);
	private static final VoxelShape OPEN_SOUTH = Block.box(0, 13, 0, 16, 16, 3);
	private static final VoxelShape OPEN_WEST = Block.box(13, 13, 0, 16, 16, 16);
	private static final VoxelShape OPEN_EAST = Block.box(0, 13, 0, 3, 16, 16);
	private static final VoxelShape CLOSED_SOUTH = Block.box(0, 0, 0, 16, 16, 2);
	private static final VoxelShape CLOSED_NORTH = Block.box(0, 0, 13, 16, 16, 16);
	private static final VoxelShape CLOSED_EAST = Block.box(0, 0, 0, 3, 16, 16);
	private static final VoxelShape CLOSED_WEST = Block.box(13, 0, 0, 16, 16, 16);
	private static final VoxelShape SHAPE_HIDDEN = Block.box(0, 0, 0, 0, 0, 0);

	public static final BooleanProperty OPEN = BooleanProperty.create("open");
	public static final BooleanProperty HIDDEN = BooleanProperty.create("hidden");

	public OakBlinds(Properties p_54120_) {
		super(p_54120_);
		this.registerDefaultState(this.defaultBlockState().setValue(OPEN, false).setValue(FACING, Direction.NORTH)
				.setValue(HIDDEN, false));
	}

	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(OPEN, false).setValue(HIDDEN, false);
	}

	// changing the model of the blinds by shift-rightclicking
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
			BlockHitResult pResult) {
		if (!pLevel.isClientSide() && pHand == InteractionHand.MAIN_HAND && pPlayer.getItemInHand(pHand).isEmpty()) {
			final boolean currentlyOpen = pState.getValue(OPEN);

			{
				// changes clicked blinds
				pLevel.setBlock(pPos, pState.setValue(OPEN, !currentlyOpen), 3);
				// checks for blinds below clicked blind
				if (Config.SEARCHRADIUS.get() > 0) {
					for (int offsetDown = 1; offsetDown <= Config.SEARCHRADIUS.get(); ++offsetDown) {
						if (sameBlindType(pLevel, pPos.below(offsetDown), pState)) {
							updateHidden(pLevel, pPos.below(offsetDown), pState);
						} else {
							break;
						}
					}
				}

				if (Config.SEARCHRADIUS.get() > 0) {
					// NORTH-SOUTH axis
					if (pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH) {

						// checks blinds east of clicked blind
						for (int offsetEast = 1; offsetEast <= (int) Config.SEARCHRADIUS.get() / 2; ++offsetEast) {
							if (sameBlindType(pLevel, pPos.east(offsetEast), pState)) {
								// changes east blinds
								pLevel.setBlock(pPos.east(offsetEast), pState.setValue(OPEN, !currentlyOpen), 3);
								// checks for blinds below east blinds
								for (int offsetDown = 1; offsetDown <= Config.SEARCHRADIUS.get(); ++offsetDown) {
									if (sameBlindType(pLevel, pPos.below(offsetDown).east(offsetEast), pState)) {
										updateHidden(pLevel, pPos.below(offsetDown).east(offsetEast), pState);
									} else {
										break;
									}
								}
							} else {
								break;
							}
						}

						// checks blinds west of clicked blind
						for (int offsetWest = 1; offsetWest <= Config.SEARCHRADIUS.get() / 2; ++offsetWest) {
							if (sameBlindType(pLevel, pPos.west(offsetWest), pState)) {
								// changes west blinds
								pLevel.setBlock(pPos.west(offsetWest), pState.setValue(OPEN, !currentlyOpen), 3);
								// checks for blinds below west blinds
								for (int offsetDown = 1; offsetDown <= Config.SEARCHRADIUS.get(); ++offsetDown) {
									if (sameBlindType(pLevel, pPos.below(offsetDown).west(offsetWest), pState)) {
										updateHidden(pLevel, pPos.below(offsetDown).west(offsetWest), pState);
									} else {
										break;
									}
								}
							} else {
								break;
							}
						}
					}

					// EAST-WEST axis
					if (pState.getValue(FACING) == Direction.EAST || pState.getValue(FACING) == Direction.WEST) {

						// checks blinds north of clicked blind
						for (int offsetNorth = 1; offsetNorth <= Config.SEARCHRADIUS.get() / 2; ++offsetNorth) {
							if (sameBlindType(pLevel, pPos.north(offsetNorth), pState)) {
								// changes north blinds
								pLevel.setBlock(pPos.north(offsetNorth), pState.setValue(OPEN, !currentlyOpen), 3);
								// checks for blinds below north blinds
								for (int offsetDown = 1; offsetDown <= Config.SEARCHRADIUS.get(); ++offsetDown) {
									if (sameBlindType(pLevel, pPos.below(offsetDown).north(offsetNorth), pState)) {
										updateHidden(pLevel, pPos.below(offsetDown).north(offsetNorth), pState);
									} else {
										break;
									}
								}
							} else {
								break;
							}
						}

						// checks blinds south of clicked blind
						for (int offsetSouth = 1; offsetSouth <= Config.SEARCHRADIUS.get() / 2; ++offsetSouth) {
							if (sameBlindType(pLevel, pPos.south(offsetSouth), pState)) {
								// changes south blinds
								pLevel.setBlock(pPos.south(offsetSouth), pState.setValue(OPEN, !currentlyOpen), 3);
								// checks for blinds below south blinds
								for (int offsetDown = 1; offsetDown <= Config.SEARCHRADIUS.get(); ++offsetDown) {
									if (sameBlindType(pLevel, pPos.below(offsetDown).south(offsetSouth), pState)) {
										updateHidden(pLevel, pPos.below(offsetDown).south(offsetSouth), pState);
									} else {
										break;
									}
								}
							} else {
								break;
							}
						}
					}
					pLevel.playSound(null, pPos,
							currentlyOpen ? SoundInit.BLINDS_CLOSE.get() : SoundInit.BLINDS_OPEN.get(),
							SoundSource.BLOCKS, 1, 1);
					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	private final boolean sameBlindType(Level pLevel, BlockPos pPos, BlockState pState) {
		return pLevel.getBlockState(pPos).getBlock().getClass() == this.getClass()
				&& pLevel.getBlockState(pPos).getValue(FACING) == pState.getValue(FACING);
	}

	private final void updateHidden(Level pLevel, BlockPos pPos, BlockState pState) {
		if (!pState.getValue(OPEN)) {
			pLevel.setBlock(pPos, pState.setValue(OPEN, true).setValue(HIDDEN, false), 3);
		} else {
			pLevel.setBlock(pPos, pState.setValue(OPEN, false).setValue(HIDDEN, true), 3);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		if (state.getValue(HIDDEN)) {
			return SHAPE_HIDDEN;
		}

		if (!state.getValue(OPEN)) {
			return switch (state.getValue(FACING)) {
			case NORTH -> OPEN_NORTH;
			case SOUTH -> OPEN_SOUTH;
			case WEST -> OPEN_WEST;
			case EAST -> OPEN_EAST;
			default -> OPEN_NORTH;
			};
		}

		return switch (state.getValue(FACING)) {
		case NORTH -> CLOSED_NORTH;
		case SOUTH -> CLOSED_SOUTH;
		case WEST -> CLOSED_WEST;
		case EAST -> CLOSED_EAST;
		default -> CLOSED_NORTH;
		};
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter getter, List<Component> tooltip, TooltipFlag flag) {
		if (!KeyBoardHelper.isHoldingShift()) {
			tooltip.add(
					new TextComponent("\u00A77Hold\u00A77 \u00A7e\u00A7oSHIFT\u00A7o\u00A7r \u00A77for more.\u00A77"));
		}

		if (KeyBoardHelper.isHoldingShift()) {
			tooltip.add(
					new TextComponent("\u00A77Rightclick on Block to open or close blind and adjacent ones.\u00A77"));
			tooltip.add(new TextComponent(
					"\u00A7oNote:\u00A7o \u00A77After closing 1st time, blinds below topmost blind become invisible when open.\u00A77"));
		}
		super.appendHoverText(stack, getter, tooltip, flag);
	}

	// creates blockstate
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder);
		pBuilder.add(OPEN, FACING, HIDDEN);
	}
}