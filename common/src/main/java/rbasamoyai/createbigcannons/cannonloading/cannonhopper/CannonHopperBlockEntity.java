package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.index.CBCMenuTypes;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

public class CannonHopperBlockEntity extends KineticBlockEntity implements MenuProvider, Container {

	public static final int FUZE_SLOT = 0;
	public static final int PROJECTILE_SLOT = 1;
	public static final String POWDER_AMOUNT_KEY = "PowderAmount";

	private List<ItemStack> items = new ArrayList<ItemStack>(2);
	private int powderAmount = 1;

	public CannonHopperBlockEntity(BlockEntityType<? extends CannonHopperBlockEntity> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		items.add(ItemStack.EMPTY);
		items.add(ItemStack.EMPTY);
	}

	public InteractionResult use(Player player) {
		if (player instanceof ServerPlayer splayer && player.mayBuild()) {
			CBCMenuTypes.CANNON_HOPPER_CONTAINER.open(splayer, this.getDisplayName(), this, buf -> {
				buf.writeVarInt(this.powderAmount);
				buf.writeBlockPos(getBlockPos());
			});
		}
		return InteractionResult.CONSUME;
	}

	public int getPowderAmount() {
		return powderAmount;
	}

	public void setPowderAmount(int powderAmountIn) {
		powderAmount = powderAmountIn;
		this.notifyUpdate();
	}

	public boolean mayPlace(int slot, ItemStack itemStack) {
		return switch (slot) {
			case FUZE_SLOT -> { 
				yield itemStack.getItem() instanceof FuzeItem;
			}
			case PROJECTILE_SLOT -> {
				if (itemStack.getItem() instanceof BlockItem blockItem) {
					yield blockItem.getBlock() instanceof ProjectileBlock;
				}
				yield false;
			}
			default -> false;
		};
	}

	public AbstractCannonProjectile getProjectile(Level level) {
		if (isEmpty()) {
			return null;
		}

		ItemStack projectileItem = removeItem(PROJECTILE_SLOT, 1);
		AbstractCannonProjectile projectile = null;

		if (projectileItem.getItem() instanceof BlockItem projectileBlockItem) {
			if (projectileBlockItem.getBlock() instanceof ProjectileBlock projectileBlock) {
				projectile = projectileBlock.getProjectile(level, getBlockState(), getBlockPos(), null);
			}
		}

		if (projectile != null) {
			if (projectile instanceof FuzedBigCannonProjectile fuzedProjectile) {
				ItemStack fuzeItem = removeItem(FUZE_SLOT, 1);
				if (!fuzeItem.isEmpty()) {
					fuzedProjectile.setFuze(fuzeItem);
				}
			}
		}

		return projectile;
	}

	@Nonnull
	private String getSlotName(int slot) {
		return switch (slot) {
			case FUZE_SLOT -> "Fuze";
			case PROJECTILE_SLOT -> "Projectile";
			default -> "Unknown";
		};
	}

	@Override public int getContainerSize() { return items.size(); }

	@Override
	public boolean isEmpty() {
		return this.getItem(PROJECTILE_SLOT).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		if (slot >= getContainerSize()) return ItemStack.EMPTY;
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (amount <= 0) return ItemStack.EMPTY;
		if (slot >= getContainerSize()) return ItemStack.EMPTY;

		ItemStack ammo = this.getItem(slot);
		if (ammo.isEmpty()) return ItemStack.EMPTY;

		ItemStack split = ammo.split(amount);
		this.notifyUpdate();
		return split;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot >= getContainerSize()) return ItemStack.EMPTY;
		ItemStack ret = this.getItem(slot);
		items.set(slot, ItemStack.EMPTY);
		return ret;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot >= getContainerSize()) return;
		items.set(slot, stack);
		this.notifyUpdate();
	}

	@Override public boolean stillValid(Player player) { return true; }

	@Override
	public void clearContent() {
		items.set(FUZE_SLOT, ItemStack.EMPTY);
		items.set(PROJECTILE_SLOT, ItemStack.EMPTY);
	}

	@Override public Component getDisplayName() {
		return this.getBlockState().getBlock().getName();
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return CannonHopperMenu.getServerMenu(i, inventory, this);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		var fuzeTag = compound.getCompound(getSlotName(FUZE_SLOT));
		if (fuzeTag != null) {
			items.set(FUZE_SLOT, ItemStack.of(fuzeTag));
		}
	
		var projectileTag = compound.getCompound(getSlotName(PROJECTILE_SLOT));
		if (projectileTag != null) {
			items.set(PROJECTILE_SLOT, ItemStack.of(projectileTag));
		}
	
		powderAmount = compound.getInt(POWDER_AMOUNT_KEY);
		super.read(compound, clientPacket);
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.put(getSlotName(FUZE_SLOT), getItem(FUZE_SLOT).save(new CompoundTag()));
		compound.put(getSlotName(PROJECTILE_SLOT), getItem(PROJECTILE_SLOT).save(new CompoundTag()));
		compound.putInt(POWDER_AMOUNT_KEY, powderAmount);
		super.write(compound, clientPacket);
	}
}
