package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import net.minecraft.world.inventory.ContainerData;

public class CannonHopperServerData implements ContainerData {
	private final CannonHopperBlockEntity cannonHopper;

	public CannonHopperServerData(CannonHopperBlockEntity cannonHopperIn) {
		this.cannonHopper = cannonHopperIn;
	}

	@Override
	public int get(int index) {
		return index == 0 ? this.cannonHopper.getPowderAmount() : 1;
	}

	@Override
	public void set(int index, int value) {
		if (index == 0) this.cannonHopper.setPowderAmount(value);
	}

	@Override
	public int getCount() {
		return 1;
	}  
}
