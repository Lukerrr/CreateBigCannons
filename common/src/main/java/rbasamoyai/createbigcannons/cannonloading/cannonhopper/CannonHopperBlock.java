package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.index.CBCBlockEntities;

public class CannonHopperBlock extends KineticBlock implements IBE<CannonHopperBlockEntity> {

	public CannonHopperBlock(Properties properties) {
		super(properties);
	}

	public static int maxAllowedLoaderLength() {
		return CBCConfigs.SERVER.kinetics.maxLoaderLength.get();
	}

	@Override
	public Class<CannonHopperBlockEntity> getBlockEntityClass() {
		return CannonHopperBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends CannonHopperBlockEntity> getBlockEntityType() {
		return CBCBlockEntities.CANNON_HOPPER.get();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Z;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		CannonHopperBlockEntity blockEnt = getBlockEntity(level, pos);
		if (blockEnt != null) {
			return blockEnt.use(player);
		}
		return InteractionResult.PASS;
	}
}
