package rbasamoyai.createbigcannons.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.crafting.boring.AbstractCannonDrillBlockEntity;
import rbasamoyai.createbigcannons.crafting.boring.CannonDrillBlock;
import rbasamoyai.createbigcannons.crafting.builtup.CannonBuilderBlock;
import rbasamoyai.createbigcannons.crafting.builtup.CannonBuilderBlockEntity;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.munitions.config.BlockHardnessHandler;

public class CBCCommonEvents {

	public static void serverLevelTickEnd(Level level) {
		CreateBigCannons.BLOCK_DAMAGE.tick(level);
	}

	public static void onPlayerLogin(Player player) {
		CreateBigCannons.BLOCK_DAMAGE.playerLogin(player);
	}

	public static void onPlayerLogout(Player player) {
		CreateBigCannons.BLOCK_DAMAGE.playerLogout(player);
	}

	public static void onPlayerBreakBlock(BlockState state, LevelAccessor level, BlockPos pos, Player player) {
		if (AllBlocks.PISTON_EXTENSION_POLE.has(state)) {
			BlockPos drillPos = destroyPoleContraption(CBCBlocks.CANNON_DRILL_BIT.get(), CBCBlocks.CANNON_DRILL.get(),
					CannonDrillBlock.maxAllowedDrillLength(), state, level, pos, player);
			if (drillPos != null) {
				level.setBlock(drillPos, level.getBlockState(drillPos).setValue(CannonDrillBlock.STATE, MechanicalPistonBlock.PistonState.RETRACTED), 3);
				if (level.getBlockEntity(pos) instanceof AbstractCannonDrillBlockEntity drill) {
					drill.onLengthBroken();
				}
				return;
			}
			BlockPos builderPos = destroyPoleContraption(CBCBlocks.CANNON_BUILDER_HEAD.get(), CBCBlocks.CANNON_BUILDER.get(),
					CannonBuilderBlock.maxAllowedBuilderLength(), state, level, pos, player);
			if (builderPos != null) {
				level.setBlock(builderPos, level.getBlockState(builderPos).setValue(CannonBuilderBlock.STATE, CannonBuilderBlock.BuilderState.UNACTIVATED), 3);
				if (level.getBlockEntity(pos) instanceof CannonBuilderBlockEntity builder) {
					builder.onLengthBroken();
				}
				return;
			}
		}
	}

	private static BlockPos destroyPoleContraption(Block head, Block base, int limit, BlockState state, LevelAccessor level,
												   BlockPos pos, Player player) {
		Direction.Axis axis = state.getValue(BlockStateProperties.FACING).getAxis();
		Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);

		BlockPos headPos = null;
		BlockPos basePos = null;

		for (int mod : new int[] { 1, -1 }) {
			for (int offs = mod; mod * offs < limit; offs += mod) {
				BlockPos pos1 = pos.relative(positive, offs);
				BlockState state1 = level.getBlockState(pos1);

				if (AllBlocks.PISTON_EXTENSION_POLE.has(state1) && axis == state1.getValue(BlockStateProperties.FACING).getAxis()) {
					continue;
				}
				if (state1.is(head) && axis == state1.getValue(BlockStateProperties.FACING).getAxis()) {
					headPos = pos1;
				}
				if (state1.is(base) && axis == state1.getValue(BlockStateProperties.FACING).getAxis()) {
					basePos = pos1;
				}
				break;
			}
		}
		if (headPos == null || basePos == null) return null;
		BlockPos baseCopy = basePos.immutable();
		BlockPos.betweenClosedStream(headPos, basePos)
				.filter(p -> !p.equals(pos) && !p.equals(baseCopy))
				.forEach(p -> level.destroyBlock(p, !player.isCreative()));
		return baseCopy;
	}

	public static void onLoadLevel(LevelAccessor level) {
		CreateBigCannons.BLOCK_DAMAGE.levelLoaded(level);
		if (level.getServer() != null && level.getServer().overworld() == level) {
			BlockHardnessHandler.loadTags();
		}
	}

	public static void onDatapackSync(Player player) {
		if (player == null) { // Only do on server reload, not when a player joins
			BlockHardnessHandler.loadTags();
		}
	}

}