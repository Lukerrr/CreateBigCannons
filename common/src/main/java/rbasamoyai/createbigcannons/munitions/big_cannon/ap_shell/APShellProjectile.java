package rbasamoyai.createbigcannons.munitions.big_cannon.ap_shell;

import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.config.CBCCfgMunitions.ExpDamageCalcKeepBlocks;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;

public class APShellProjectile extends FuzedBigCannonProjectile {

	public APShellProjectile(EntityType<? extends APShellProjectile> type, Level level) {
		super(type, level);
	}

	@Override
	protected void detonate() {
		this.level().explode(null, this.getX(), this.getY(), this.getZ(), (float) this.getProperties().explosivePower(),
				CBCConfigs.SERVER.munitions.damageRestriction.get().explosiveInteraction());
		this.level().explode(null, (DamageSource)null, new ExpDamageCalcKeepBlocks(), this.getX(), this.getY(), this.getZ(), (float) (this.getProperties().explosivePower() * this.getProperties().explosiveDamageMult()), false,
				CBCConfigs.SERVER.munitions.damageRestriction.get().explosiveInteraction());
		this.discard();
	}

	@Override
	public BlockState getRenderedBlockState() {
		return CBCBlocks.AP_SHELL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.NORTH);
	}

}
