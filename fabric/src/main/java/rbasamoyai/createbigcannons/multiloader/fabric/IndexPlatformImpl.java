package rbasamoyai.createbigcannons.multiloader.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import io.github.fabricators_of_create.porting_lib.fake_players.FakePlayer;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.ItemGroupUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.AbstractCannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.carriage.AbstractCannonCarriageEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedAutocannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractPitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.crafting.boring.AbstractCannonDrillBlockEntity;
import rbasamoyai.createbigcannons.crafting.casting.AbstractCannonCastBlockEntity;
import rbasamoyai.createbigcannons.fabric.CreateBigCannonsFabric;
import rbasamoyai.createbigcannons.fabric.cannon_control.CannonCarriageEntity;
import rbasamoyai.createbigcannons.fabric.cannon_control.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.fabric.cannon_control.MountedAutocannonContraption;
import rbasamoyai.createbigcannons.fabric.cannon_control.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.fabric.cannons.AutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.fabric.crafting.CannonCastBlockEntity;
import rbasamoyai.createbigcannons.fabric.crafting.CannonCastBlockEntityRenderer;
import rbasamoyai.createbigcannons.fabric.crafting.CannonDrillBlockEntity;
import rbasamoyai.createbigcannons.fabric.index.fluid_utils.FabricFluidBuilder;
import rbasamoyai.createbigcannons.fabric.mixin.KeyMappingAccessor;
import rbasamoyai.createbigcannons.fabric.munitions.fluid_shell.FluidShellBlockEntity;
import rbasamoyai.createbigcannons.index.fluid_utils.CBCFlowingFluid;
import rbasamoyai.createbigcannons.index.fluid_utils.FluidBuilder;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.AbstractFluidShellBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.EndFluidStack;

public class IndexPlatformImpl {

	public static boolean isFakePlayer(Player player) { return player instanceof FakePlayer; }

	public static AbstractCannonDrillBlockEntity makeDrill(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		return new CannonDrillBlockEntity(type, pos, state);
	}

	public static AbstractCannonCastBlockEntity makeCast(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		return new CannonCastBlockEntity(type, pos, state);
	}

	public static AbstractCannonMountBlockEntity makeCannonMount(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		return new CannonMountBlockEntity(type, pos, state);
	}

	public static AbstractAutocannonBreechBlockEntity makeAutocannonBreech(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		return new AutocannonBreechBlockEntity(type, pos, state);
	}

	public static AbstractFluidShellBlockEntity makeFluidShellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		return new FluidShellBlockEntity(type, pos, state);
	}

	public static AbstractMountedAutocannonContraption makeAutocannon() {
		return new MountedAutocannonContraption();
	}

	public static AbstractPitchOrientedContraptionEntity makePitchContraption(EntityType<?> type, Level level) {
		return new PitchOrientedContraptionEntity(type, level);
	}

	public static AbstractCannonCarriageEntity makeCannonCarriage(EntityType<?> type, Level level) {
		return new CannonCarriageEntity(type, level);
	}

	public static ParticleOptions createFluidDripParticle(EndFluidStack stack) {
		return FluidFX.getFluidParticle(new FluidStack(stack.fluid(), stack.amount(), stack.data()));
	}

	public static NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context,
			BlockEntityRenderer<? super AbstractCannonCastBlockEntity>>> getCastRenderer() {
		return () -> CannonCastBlockEntityRenderer::new;
	}

	public static int getModGroupId() { return ItemGroupUtil.expandArrayAndGetId(); }

	public static <T extends CBCFlowingFluid, P> FluidBuilder<T, P> createFluidBuilder(AbstractRegistrate<?> owner,
			P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
			NonNullFunction<CBCFlowingFluid.Properties, T> factory) {
		return new FabricFluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, factory);
	}

	public static <T extends CBCFlowingFluid, P> FluidBuilder<T, P> doFluidBuilderTransforms(FluidBuilder<T, P> builder) {
		FabricFluidBuilder<T, P> builderc = (FabricFluidBuilder<T, P>) builder;
		return builderc.renderHandler(() -> SimpleFluidRenderHandler::new);
	}

	public static void registerDeferredParticleType(String name, ParticleType<?> type) {
		CreateBigCannonsFabric.PARTICLE_REGISTER.register(name, () -> type);
	}

	public static void registerDeferredParticles() { CreateBigCannonsFabric.PARTICLE_REGISTER.register(); }

	// Provided by TelepathicGrunt - thanks! --ritchie
	@Environment(EnvType.CLIENT)
	public static KeyMapping createSafeKeyMapping(String description, InputConstants.Type type, int keycode) {
		InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keycode);
		KeyMapping oldMapping = KeyMappingAccessor.getMAP().get(key);
		KeyMapping keyMapping = new KeyMapping(description, type, keycode, "key." + CreateBigCannons.MOD_ID + ".category");
		KeyMappingAccessor.getMAP().put(key, oldMapping);
		KeyMappingAccessor.getALL().remove(description);
		return keyMapping;
	}

	@Environment(EnvType.CLIENT)
	public static <T extends ItemPropertyFunction> void registerClampedItemProperty(Item item, ResourceLocation loc, T func) {
		ItemProperties.register(item, loc, func::call);
	}

}
