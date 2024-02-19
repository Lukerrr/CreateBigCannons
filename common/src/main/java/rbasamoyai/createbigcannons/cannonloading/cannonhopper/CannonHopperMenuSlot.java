package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
public class CannonHopperMenuSlot extends Slot {

	private final CannonHopperBlockEntity hopperContainer;

	public CannonHopperMenuSlot(CannonHopperBlockEntity container, int slot, int x, int y) {
		super(container, slot, x, y);
		this.hopperContainer = container;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return hopperContainer.mayPlace(getContainerSlot(), stack);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 64;
	}

}
