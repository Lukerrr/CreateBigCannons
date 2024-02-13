package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CannonHopperBlockEntity extends KineticBlockEntity {

	protected int extensionLength;

	public CannonHopperBlockEntity(BlockEntityType<? extends CannonHopperBlockEntity> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
}
