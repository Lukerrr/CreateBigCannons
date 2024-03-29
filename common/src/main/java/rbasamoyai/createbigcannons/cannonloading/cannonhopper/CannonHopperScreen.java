package rbasamoyai.createbigcannons.cannonloading.cannonhopper;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static rbasamoyai.createbigcannons.index.CBCGuiTextures.CANNON_HOPPER_CONTAINER_BG;
import static rbasamoyai.createbigcannons.index.CBCGuiTextures.CANNON_HOPPER_CONTAINER_SELECTOR;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.multiloader.NetworkPlatform;
import rbasamoyai.createbigcannons.network.ServerboundSetContainerValuePacket;

public class CannonHopperScreen extends AbstractSimiContainerScreen<CannonHopperMenu> {

	protected ScrollInput setValue;
	protected int lastUpdated = -1;
	protected IconButton confirmButton;

	private final int MAX_POWDER = 16;

	public CannonHopperScreen(CannonHopperMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}

	@Override
	protected void init() {
		// TODO: figure out why the hell classloading causes lag here
		this.setWindowSize(CANNON_HOPPER_CONTAINER_BG.width, CANNON_HOPPER_CONTAINER_BG.height + 4 + PLAYER_INVENTORY.height);
		this.setWindowOffset(1, 0);
		super.init();

		this.setValue = this.getScrollInput();

		this.setValue.onChanged();
		this.addRenderableWidget(this.setValue);

		this.confirmButton = new IconButton(this.leftPos + this.imageWidth - 33, this.topPos + 59, AllIcons.I_CONFIRM);
		this.confirmButton.withCallback(this::onClose);
		this.addRenderableWidget(this.confirmButton);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int invX = this.getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = this.topPos + CANNON_HOPPER_CONTAINER_BG.height + 4;
		this.renderPlayerInventory(graphics, invX, invY);

		CANNON_HOPPER_CONTAINER_BG.render(graphics, this.leftPos, this.topPos);
		graphics.drawCenteredString(this.font, this.title, this.leftPos + this.imageWidth / 2 - 4, this.topPos + 3, 0xffffff);

		float scrollWidth = 6.5f * 8;
		float scrollStep = (float)scrollWidth / (float)MAX_POWDER;
		float scrollOffset = scrollStep * (float)this.setValue.getState();
		int offsX = (int)(scrollOffset - 8.f);
		CANNON_HOPPER_CONTAINER_SELECTOR.render(graphics, this.leftPos + 86 + offsX, this.topPos + 23);
	}

	@Override
	protected void containerTick() {
		super.containerTick();

		if (this.lastUpdated >= 0) {
			this.lastUpdated++;
		}
		if (this.lastUpdated >= 20) {
			this.updateServer();
			this.lastUpdated = -1;
		}
	}

	@Override
	public void removed() {
		super.removed();
		this.updateServer();
	}

	@Override
	public void onClose() {
		this.updateServer();
		super.onClose();
	}

	private void updateServer() {
		NetworkPlatform.sendToServer(new ServerboundSetContainerValuePacket(this.setValue.getState()));
	}

	protected ScrollInput getScrollInput() {
		return new ScrollInput(this.leftPos + 87, this.topPos + 31, 47, 6)
			.withRange(1, MAX_POWDER + 1)
			.calling(state -> {
				this.lastUpdated = 0;
				this.setValue.titled(Lang.builder(CreateBigCannons.MOD_ID).translate("gui.cannon_hopper.powder_amount", state).component());
			})
			.setState(Mth.clamp(this.menu.getValue(), 1, MAX_POWDER));
	}

}