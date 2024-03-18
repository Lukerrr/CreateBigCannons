package rbasamoyai.createbigcannons.munitions.big_cannon;

import java.util.HashSet;

import javax.annotation.Nullable;

import com.mojang.math.Constants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.index.CBCDamageTypes;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.CannonDamageSource;
import rbasamoyai.createbigcannons.munitions.config.BlockHardnessHandler;

public abstract class AbstractBigCannonProjectile extends AbstractCannonProjectile {

	private HashSet<BlockPos> blocksWherePlayedSound = new HashSet<BlockPos>();

	protected AbstractBigCannonProjectile(EntityType<? extends AbstractBigCannonProjectile> type, Level level) {
		super(type, level);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide && !this.isInGround()) {
			targetProximityCheck();
		}
	}

	private void targetProximityCheck() {
		Vec3 curPos = this.position();
		Vec3 curVel = this.getDeltaMovement();

		if (curVel.length() < 3.f) {
			return;
		}

		BlockState targetBlock = Blocks.AIR.defaultBlockState();

		final int incomeSoundTicks = CBCConfigs.SERVER.cannons.bigCannonShellIncomeSoundTicks.get();
		final int minIncomeTicks = CBCConfigs.SERVER.cannons.bigCannonShellIncomeTicksMin.get();
		final int maxIncomeTicks = CBCConfigs.SERVER.cannons.bigCannonShellIncomeTicksMax.get();

		final double maxDistCheck = this.getDeltaMovement().length() * maxIncomeTicks;

		float traveledDist = 0.f;
		int traveledTicks = 0;
		while (traveledDist <= maxDistCheck) {
			traveledTicks += 1;
			Vec3 vel = curVel;
			Vec3 newPos = curPos.add(vel);
			if (!this.isNoGravity()) vel = vel.add(0.0f, this.getGravity(), 0.0f);
			vel = vel.scale(this.getDrag());
			newPos = newPos.add(vel.subtract(curVel).scale(0.5));
			traveledDist += curPos.distanceTo(newPos);
			curPos = newPos;
			curVel = vel;

			targetBlock = this.level().getBlockState(BlockPos.containing(curPos));
			if (!targetBlock.isAir()) {
				break;
			}
		}

		if (!targetBlock.isAir()) {
			if (traveledTicks >= minIncomeTicks && traveledTicks <= maxIncomeTicks) {
				BlockPos targetBlockPos = BlockPos.containing(curPos);
				if (!blocksWherePlayedSound.contains(targetBlockPos)) {
					blocksWherePlayedSound.add(targetBlockPos);
					Vec3 vecFromTarget = this.position().subtract(curPos);
					double distFromTargetFlat = vecFromTarget.length();
					final double maxOffset =  CBCConfigs.SERVER.cannons.bigCannonShellIncomeMaxSoundOffset.get();
					double offset = distFromTargetFlat * 0.3;
					if (offset > maxOffset) {
						offset = maxOffset;
					}
					BlockPos soundPos = BlockPos.containing(curPos.add(vecFromTarget.normalize().multiply(new Vec3(offset, offset, offset))));

					float soundPitch = (float)incomeSoundTicks / (float)traveledTicks;
					CBCSoundEvents.INCOMING_SHELL.playOnServer(
						this.level(),
						soundPos,
						CBCConfigs.SERVER.cannons.bigCannonShellIncomeSoundVolume.getF(),
						soundPitch);

					CreateBigCannons.LOGGER.info(String.format(
						"Spawned sound, ticks remain: %d, velocity: %f, pitch: %f",
						traveledTicks, this.getDeltaMovement().length(), soundPitch));
				}
			}
		}
	}

	@Override
	protected void onTickRotate() {
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();

		if (!this.isInGround()) {
			Vec3 vel = this.getDeltaMovement();
			if (vel.lengthSqr() > 0.005d) {
				this.setYRot((float) (Mth.atan2(vel.x, vel.z) * (double) Constants.RAD_TO_DEG));
				this.setXRot((float) (Mth.atan2(vel.y, vel.horizontalDistance()) * (double) Constants.RAD_TO_DEG));
			}

			this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
			this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
		}
	}

	public abstract BlockState getRenderedBlockState();

	@Nullable
	@Override
	protected ParticleOptions getTrailParticles() {
		return ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
	}

	@Override
	protected void onDestroyBlock(BlockState state, BlockHitResult result) {
		double mass = this.getProjectileMass();
		Vec3 curVel = this.getDeltaMovement();
		double mag = curVel.length();
		double bonus = 1 + Math.max(0, (mag - CBCConfigs.SERVER.munitions.minVelocityForPenetrationBonus.getF())
			* CBCConfigs.SERVER.munitions.penetrationBonusScale.getF());

		double hardness = BlockHardnessHandler.getHardness(state) / bonus;
		this.setProjectileMass((float) Math.max(mass - hardness, 0));

		if (!this.level().isClientSide()) this.level().destroyBlock(result.getBlockPos(), false);
	}

	@Override
	protected boolean canDeflect(BlockHitResult result) {
		return super.canDeflect(result) && this.random.nextFloat() < CBCConfigs.SERVER.munitions.bigCannonDeflectChance.getF();
	}

	@Override
	protected boolean canBounceOffOf(BlockState state) {
		return super.canBounceOffOf(state) && this.random.nextFloat() < CBCConfigs.SERVER.munitions.bigCannonDeflectChance.getF();
	}

	@Override
	protected DamageSource getEntityDamage(Entity entity) {
		return new CannonDamageSource(CannonDamageSource.getDamageRegistry(this.level()).getHolderOrThrow(CBCDamageTypes.BIG_CANNON_PROJECTILE), this);
	}

}
