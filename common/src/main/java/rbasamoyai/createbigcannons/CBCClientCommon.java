
package rbasamoyai.createbigcannons;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import rbasamoyai.createbigcannons.cannon_control.carriage.AbstractCannonCarriageEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractPitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.index.CBCBlockPartials;
import rbasamoyai.createbigcannons.index.CBCFluids;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.index.CBCParticleTypes;
import rbasamoyai.createbigcannons.multiloader.IndexPlatform;
import rbasamoyai.createbigcannons.multiloader.NetworkPlatform;
import rbasamoyai.createbigcannons.network.ServerboundFiringActionPacket;
import rbasamoyai.createbigcannons.network.ServerboundSetFireRatePacket;
import rbasamoyai.createbigcannons.ponder.CBCPonderIndex;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CBCClientCommon {

	private static final String KEY_ROOT = "key." + CreateBigCannons.MOD_ID;
	public static final KeyMapping PITCH_MODE = IndexPlatform.createSafeKeyMapping(KEY_ROOT + ".pitch_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C);
	public static final KeyMapping FIRE_CONTROLLED_CANNON = IndexPlatform.createSafeKeyMapping(KEY_ROOT + ".fire_controlled_cannon", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT);

	public static void onRegisterParticleFactories(Minecraft mc, ParticleEngine engine) {
		CBCParticleTypes.registerFactories();
	}

	public static void onClientSetup() {
		CBCPonderIndex.register();
		CBCPonderIndex.registerTags();
		CBCBlockPartials.resolveDeferredModels();

		IndexPlatform.registerClampedItemProperty(CBCItems.PARTIALLY_FORMED_AUTOCANNON_CARTRIDGE.get(), CreateBigCannons.resource("formed"), (stack, level, player, a) -> {
			return stack.getOrCreateTag().getCompound("SequencedAssembly").getInt("Step") - 1;
		});
	}

	public static void registerKeyMappings(Consumer<KeyMapping> cons) {
		cons.accept(PITCH_MODE);
		cons.accept(FIRE_CONTROLLED_CANNON);
	}
	
	public static void setFogColor(Camera info, SetColorWrapper wrapper) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos)) return;

		Fluid fluid = fluidState.getType();

		if (CBCFluids.MOLTEN_CAST_IRON.get().isSame(fluid)) {
			wrapper.setFogColor(70 / 255f, 10 / 255f, 11 / 255f);
			return;
		}
		if (CBCFluids.MOLTEN_BRONZE.get().isSame(fluid)) {
			wrapper.setFogColor(99 / 255f, 66 / 255f, 22 / 255f);
		}
		if (CBCFluids.MOLTEN_STEEL.get().isSame(fluid)) {
			wrapper.setFogColor(111 / 255f, 110 / 255f, 106 / 255f);
			return;
		}
		if (CBCFluids.MOLTEN_NETHERSTEEL.get().isSame(fluid)) {
			wrapper.setFogColor(76 / 255f, 50 / 255f, 58 / 255f);
			return;
		}
	}

	public interface SetColorWrapper {
		void setFogColor(float r, float g, float b);
	}
	
	public static float getFogDensity(Camera info, float currentDensity) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		BlockPos blockPos = info.getBlockPosition();
		FluidState fluidState = level.getFluidState(blockPos);
		if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos)) return -1;

		Fluid fluid = fluidState.getType();
		
		List<Fluid> moltenMetals = Arrays.asList(
				CBCFluids.MOLTEN_CAST_IRON.get(),
				CBCFluids.MOLTEN_BRONZE.get(),
				CBCFluids.MOLTEN_STEEL.get(),
				CBCFluids.MOLTEN_NETHERSTEEL.get());
		
		for (Fluid fluid1 : moltenMetals) {
			if (fluid1.isSame(fluid)) {
				return 1f / 32f;
			}
		}
		return -1;
	}

	public static void onClientGameTick(Minecraft mc) {
		if (mc.player == null || mc.level == null) return;
		if (mc.player.getRootVehicle() instanceof AbstractCannonCarriageEntity carriage) {
			net.minecraft.client.player.Input input = mc.player.input;
			boolean isPitching = CBCClientCommon.PITCH_MODE.isDown();
			carriage.setInput(input.left, input.right, input.up, input.down, isPitching);
			mc.player.handsBusy |= input.left | input.right | input.up | input.down;
		}

		if (CBCClientCommon.FIRE_CONTROLLED_CANNON.isDown() && isControllingCannon(mc.player)) {
			mc.player.handsBusy = true;
			NetworkPlatform.sendToServer(new ServerboundFiringActionPacket());
		}
	}

	public static boolean onScrollMouse(Minecraft mc, double delta) {
		if (mc.player == null || mc.level == null) return false;
		if (mc.player.getRootVehicle() instanceof AbstractCannonCarriageEntity) {
			int fireRateAdjustment = 0;
			if (delta > 0) fireRateAdjustment = 1;
			else if (delta < 0) fireRateAdjustment = -1;
			if (fireRateAdjustment != 0) {
				mc.player.handsBusy = true;
				NetworkPlatform.sendToServer(new ServerboundSetFireRatePacket(fireRateAdjustment));
				return true;
			}
		}
		return false;
	}

	public static float onFovModify(Minecraft mc, float oldFov) {
		if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) return oldFov;
		return mc.options.keyUse.isDown() && isControllingCannon(mc.player) ? oldFov * 0.5f : oldFov;
	}

	public static void onPlayerRenderPre(PoseStack stack, Player player, float partialTicks) {
		if (player.getVehicle() instanceof AbstractPitchOrientedContraptionEntity poce && poce.getSeatPos(player) != null) {
			Vector3f pVec = new Vector3f(player.getPosition(partialTicks));
			stack.translate(-pVec.x(), -pVec.y(), -pVec.z());

			BlockPos seatPos = poce.getSeatPos(player);
			double offs = player.getEyeHeight() + player.getMyRidingOffset() - 0.15;
			Vec3 vec = new Vec3(poce.getInitialOrientation().step()).scale(0.25);
			Vector3f pVec1 = new Vector3f(poce.toGlobalVector(Vec3.atCenterOf(seatPos).subtract(vec).subtract(0, offs, 0), partialTicks));
			stack.translate(pVec1.x(), pVec1.y(), pVec1.z());

			float yr = (-Mth.lerp(partialTicks, player.yRotO, player.getYRot()) + 90) * Mth.DEG_TO_RAD;
			Vector3f vec3 = new Vector3f(Mth.sin(yr), 0, Mth.cos(yr));
			float xr = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
			stack.mulPose(vec3.rotationDegrees(xr));
		}
	}

	private static boolean isControllingCannon(Entity entity) {
		Entity vehicle = entity.getVehicle();
		return vehicle instanceof AbstractCannonCarriageEntity || vehicle instanceof AbstractPitchOrientedContraptionEntity;
	}
	
}
