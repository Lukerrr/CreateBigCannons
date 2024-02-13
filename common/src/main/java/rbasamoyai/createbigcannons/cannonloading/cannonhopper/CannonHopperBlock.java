package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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

}
