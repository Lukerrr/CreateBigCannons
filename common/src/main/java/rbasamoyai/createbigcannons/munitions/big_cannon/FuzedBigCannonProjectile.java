package rbasamoyai.createbigcannons.munitions.big_cannon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class FuzedBigCannonProjectile extends AbstractBigCannonProjectile {

	private ItemStack fuze = ItemStack.EMPTY;
	private boolean startedPlaySound = false;

	protected FuzedBigCannonProjectile(EntityType<? extends FuzedBigCannonProjectile> type, Level level) {
		super(type, level);
	}

	public void setFuze(ItemStack stack) { this.fuze = stack == null ? ItemStack.EMPTY : stack; }

	@Override
	public void tick() {
		super.tick();
		if (this.canDetonate(fz -> fz.onProjectileTick(this.fuze, this))) this.detonate();
	}

	@Override
	protected boolean onClip(ProjectileContext ctx, Vec3 pos) {
		targetProximityCheck(pos);
		if (super.onClip(ctx, pos)) return true;
		if (this.canDetonate(fz -> fz.onProjectileClip(this.fuze, this, pos, ctx))) {
			this.detonate();
			return true;
		}
		return false;
	}

	@Override
	protected void onImpact(HitResult result, boolean stopped) {
		super.onHit(result);
		if (this.canDetonate(fz -> fz.onProjectileImpact(this.fuze, this, result, stopped))) this.detonate();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.put("Fuze", this.fuze.save(new CompoundTag()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.fuze = ItemStack.of(tag.getCompound("Fuze"));
	}

	protected final boolean canDetonate(Predicate<FuzeItem> cons) {
		return !this.level().isClientSide && this.level().hasChunkAt(this.blockPosition()) && this.fuze.getItem() instanceof FuzeItem fuzeItem && cons.test(fuzeItem);
	}

	protected abstract void detonate();

	private void targetProximityCheck(Vec3 pos) {		
		float l = 50;
		Vec3 dir = this.getDeltaMovement().normalize();
		Vec3 right = dir.cross(new Vec3(Direction.UP.step()));
		Vec3 up = dir.cross(right);
		dir = dir.scale(l);
		double reach = Math.max(this.getBbWidth(), this.getBbHeight()) * 0.5;

		AABB currentMovementRegion = this.getBoundingBox()
			.expandTowards(dir.scale(1.75))
			.inflate(1)
			.move(pos.subtract(this.position()));
		List<Entity> entities = this.level().getEntities(this, currentMovementRegion, this::canHitEntity);

		int radius = 2;
		double scale = 1.5;
		for (int i = -radius; i <= radius; ++i) {
			for (int j = -radius; j <= radius; ++j) {
				Vec3 ray = dir.add(right.scale(i * scale)).add(up.scale(j * scale));
				Vec3 rayEnd = pos.add(ray);

				BlockHitResult hitRes = this.level().clip(new ClipContext(pos, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
				if (hitRes.getType() != HitResult.Type.MISS) {
					playProximitySound(hitRes.getLocation());
					return;
				}

				for (Entity target : entities) {
					AABB targetBox = target.getBoundingBox().inflate(reach);
					Optional<Vec3> entityHitPos = targetBox.clip(pos, rayEnd);
					if (entityHitPos.isPresent()) {
						playProximitySound(entityHitPos.get());
						return;
					}
				}
			}
		}
	}

	private void playProximitySound(Vec3 pos) {
		if (startedPlaySound) {
			return;
		}

		startedPlaySound = true;

		if (!this.level().isClientSide) {
			this.level().playSound(this, new BlockPos((int)pos.x, (int)pos.y, (int)pos.z), SoundEvents.ENDERMAN_STARE, SoundSource.BLOCKS, 1f, 1f);
		}
	}

}
