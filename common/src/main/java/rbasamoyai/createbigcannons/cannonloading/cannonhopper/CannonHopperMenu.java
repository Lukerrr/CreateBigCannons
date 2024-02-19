package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.base.SimpleValueContainer;
import rbasamoyai.createbigcannons.index.CBCMenuTypes;

public class CannonHopperMenu extends AbstractContainerMenu implements SimpleValueContainer {

	private static final ResourceLocation FUZE_SLOT = CreateBigCannons.resource("item/fuze_slot");
	private static final ResourceLocation PROJECTILE_SLOT = CreateBigCannons.resource("item/projectile_slot");

	public static CannonHopperMenu getServerMenu(int id, Inventory playerInv, CannonHopperBlockEntity ct) {
		return new CannonHopperMenu(CBCMenuTypes.CANNON_HOPPER_CONTAINER.get(), id, playerInv, ct, new CannonHopperServerData(ct));
	}

	public static CannonHopperMenu getClientMenu(MenuType<CannonHopperMenu> type, int id, Inventory playerInv, FriendlyByteBuf buf) {
		ContainerData data = new SimpleContainerData(1);
		data.set(0, buf.readVarInt());
		BlockPos ctPos = buf.readBlockPos();
		CannonHopperBlockEntity ct = (CannonHopperBlockEntity)playerInv.player.level().getBlockEntity(ctPos);
		return new CannonHopperMenu(type, id, playerInv, ct, data);
	}

	private final CannonHopperBlockEntity container;
	private final ContainerData data;
	private final Inventory playerInv;

	protected CannonHopperMenu(MenuType<? extends CannonHopperMenu> type, int id, Inventory playerInv,
										  CannonHopperBlockEntity ct, ContainerData data) {
		super(type, id);

		this.addSlot(new CannonHopperMenuSlot(ct, CannonHopperBlockEntity.FUZE_SLOT, 32, 26) {
			@Nullable
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, FUZE_SLOT);
			}
		});
		this.addSlot(new CannonHopperMenuSlot(ct, CannonHopperBlockEntity.PROJECTILE_SLOT, 59, 26) {
			@Nullable
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, PROJECTILE_SLOT);
			}
		});

		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInv, row * 9 + col + 9, col * 18 + 8, row * 18 + 105));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInv, i, i * 18 + 8, 163));
		}

		this.addDataSlots(data);
		this.data = data;
		this.container = ct;
		this.playerInv = playerInv;
	}

	@Override public boolean stillValid(Player player) { return true; }

	public int getValue() { return this.data.get(0); }

	@Override public void setValue(int value) { this.data.set(0, value);; }

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
