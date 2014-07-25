package nf.fr.ephys.playerproxies.common.item;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import nf.fr.ephys.playerproxies.common.PlayerProxies;
import nf.fr.ephys.playerproxies.helpers.BlockHelper;
import nf.fr.ephys.playerproxies.helpers.CommandHelper;
import nf.fr.ephys.playerproxies.helpers.DebugHelper;
import nf.fr.ephys.playerproxies.helpers.NBTHelper;

import java.util.List;

public class ItemUnemptyingBucket extends Item {
	public static final int METADATA_FILL = 0;
	public static final int METADATA_EMPTY = 1;
	public static final int RANGE = 32;

	public static void register() {
		PlayerProxies.Items.unemptyingBucket = new ItemUnemptyingBucket();
		PlayerProxies.Items.unemptyingBucket.setUnlocalizedName("PP_UnemptyingBucket")
				.setMaxStackSize(1)
				.setCreativeTab(PlayerProxies.creativeTab)
				.setTextureName("bucket_empty");

		GameRegistry.registerItem(PlayerProxies.Items.unemptyingBucket, PlayerProxies.Items.unemptyingBucket.getUnlocalizedName());
	}

	public static void registerCraft() {
		GameRegistry.addRecipe(new ItemStack(PlayerProxies.Items.unemptyingBucket),
				"i i", " i ", " l ",
				'i', PlayerProxies.Items.dragonScaleIngot,
				'l', PlayerProxies.Items.linkFocus);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addInformation(ItemStack stack, EntityPlayer player, List data, boolean unknown) {
		Fluid fluid = getLiquid(stack);

		data.add(String.format(StatCollector.translateToLocal("pp_tooltip.bucket_contains"), fluid == null ? StatCollector.translateToLocal("pp_tooltip.nothing") : fluid.getLocalizedName()));
		data.add("§5" + (stack.getItemDamage() == METADATA_EMPTY ? StatCollector.translateToLocal("pp_tooltip.bucket_mode_empty") : StatCollector.translateToLocal("pp_tooltip.bucket_mode_fill")));
	}

	public static void setLiquid(ItemStack stack, Fluid liquid) {
		NBTTagCompound nbt = NBTHelper.getNBT(stack);

		if (liquid == null)
			nbt.removeTag("fluid");
		else
			nbt.setInteger("fluid", FluidRegistry.getFluidID(liquid.getName()));
	}

	public static Fluid getLiquid(ItemStack stack) {
		NBTTagCompound nbt = NBTHelper.getNBT(stack);

		if (!nbt.hasKey("fluid")) return null;

		return FluidRegistry.getFluid(nbt.getInteger("fluid"));
	}

	public static void setFluidHandler(ItemStack stack, TileEntity te, int side) {
		if (te == null)
			NBTHelper.getNBT(stack).removeTag("fluidHandler");
		else {
			NBTTagCompound tileNBT = new NBTTagCompound();
			tileNBT.setIntArray("coords", BlockHelper.getCoords(te));
			tileNBT.setInteger("worldID", te.getWorldObj().provider.dimensionId);
			tileNBT.setInteger("side", side);

			NBTHelper.getNBT(stack).setTag("fluidHandler", tileNBT);
		}
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		DebugHelper.sidedDebug(world, "ON ITEM USE FIRST");
		if (player.isSneaking()) return false;

		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof IFluidHandler) {
			IFluidHandler fluidHandler = (IFluidHandler) te;

			Fluid fluid = getLiquid(stack);

			if (fluid == null) {
				attemptDrain(stack, fluidHandler, ForgeDirection.getOrientation(side));
			} else {
				attemptFill(stack, fluidHandler, ForgeDirection.getOrientation(side), fluid);
			}

			refill(stack, world, player);

			return !world.isRemote;
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		Fluid fluid = getLiquid(stack);
		boolean empty = fluid == null;

		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, empty);
		TileEntity te = mop == null ? null : world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);

		if (player.isSneaking()) {
			if (te instanceof IFluidHandler) {
				setFluidHandler(stack, te, mop.sideHit);
				CommandHelper.sendChatMessage(player, this.getItemStackDisplayName(stack) + " linked to " + world.getBlock(mop.blockX, mop.blockY, mop.blockZ).getLocalizedName());
			} else {
				switchMode(stack, player);
			}

			refill(stack, world, player);

			return stack;
		}

		if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return stack;

		if (world.canMineBlock(player, mop.blockX, mop.blockY, mop.blockZ)) {
			if (empty) {
				if (player.canPlayerEdit(mop.blockX, mop.blockY, mop.blockZ, mop.sideHit, stack)) {
					Block block = world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
					int l = world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);

					Fluid targetFluid = FluidRegistry.lookupFluidForBlock(block);
					if (l == 0 && targetFluid != null) {
						setLiquid(stack, targetFluid);
						world.setBlockToAir(mop.blockX, mop.blockY, mop.blockZ);
					}
				}
			} else {
				int[] coords = BlockHelper.getAdjacentBlock(mop.blockX, mop.blockY, mop.blockZ, mop.sideHit);

				if (placeFluidInWorld(player, coords, mop.sideHit, stack, world, fluid))
					setLiquid(stack, null);
			}
		}

		refill(stack, world, player);

		return stack;
	}

	private void switchMode(ItemStack stack, EntityPlayer player) {
		if (stack.getItemDamage() == METADATA_FILL) {
			CommandHelper.sendChatMessage(player, "Switching to empty mode");
			stack.setItemDamage(METADATA_EMPTY);
		} else {
			CommandHelper.sendChatMessage(player, "Switching to fill mode");
			stack.setItemDamage(METADATA_FILL);
		}
	}

	private boolean placeFluidInWorld(EntityPlayer player, int[] coords, int side, ItemStack stack, World world, Fluid fluid) {
		if (!fluid.canBePlacedInWorld()) {
			CommandHelper.sendChatMessage(player, "Can't place this fluid in world. :(");

			return false;
		}

		if (player.canPlayerEdit(coords[0], coords[1], coords[2], side, stack)) {
			Material material = world.getBlock(coords[0], coords[1], coords[2]).getMaterial();
			boolean flag = !material.isSolid();

			if (world.provider.isHellWorld && fluid == FluidRegistry.WATER) {
				world.playSoundEffect(coords[0] + 0.5D, coords[1] + 0.5D, coords[2] + 0.5D, "random.fizz", 0.5F, 2.6F + world.rand.nextFloat() - world.rand.nextFloat() * 0.8F);

				for (int l = 0; l < 8; ++l) {
					world.spawnParticle("largesmoke", coords[0] + Math.random(), coords[1] + Math.random(), coords[2] + Math.random(), 0.0D, 0.0D, 0.0D);
				}
			} else {
				if (!world.isRemote && flag && !material.isLiquid()) {
					world.func_147480_a(coords[0], coords[1], coords[2], true);
				}

				Block block = fluid.getBlock();

				if (block == Blocks.water)
					block = Blocks.flowing_water;
				else if (block == Blocks.lava)
					block = Blocks.flowing_lava;

				world.setBlock(coords[0], coords[1], coords[2], block, 0, 3);
			}

			return true;
		}

		return false;
	}

	private void refill(ItemStack stack, World world, EntityPlayer player) {
		Fluid fluid = getLiquid(stack);

		int metadata = stack.getItemDamage();
		if (metadata == METADATA_EMPTY && fluid == null) return;
		if (metadata == METADATA_FILL && fluid != null) return;

		NBTTagCompound nbt = NBTHelper.getNBT(stack);

		if (!nbt.hasKey("fluidHandler"))  return;
		NBTTagCompound tileNBT = nbt.getCompoundTag("fluidHandler");

		int tileWorld = tileNBT.getInteger("worldID");
		if (world.provider.dimensionId != tileWorld) return;

		int[] tileCoords = tileNBT.getIntArray("coords");

		if (Math.abs(tileCoords[0] - player.posX) > RANGE
				|| Math.abs(tileCoords[1] - player.posY) > RANGE
				|| Math.abs(tileCoords[2] - player.posZ) > RANGE) return;

		TileEntity te = world.getTileEntity(tileCoords[0], tileCoords[1], tileCoords[2]);

		if (!(te instanceof IFluidHandler)) {
			setFluidHandler(stack, null, 0);

			return;
		}

		IFluidHandler fluidHandler = (IFluidHandler) te;

		int side = tileNBT.getInteger("side");

		ForgeDirection direction = ForgeDirection.getOrientation(side);
		switch (metadata) {
			case METADATA_EMPTY:
				attemptFill(stack, fluidHandler, direction, fluid);
				break;

			case METADATA_FILL:
				attemptDrain(stack, fluidHandler, direction);
		}
	}

	private void attemptFill(ItemStack stack, IFluidHandler fluidHandler, ForgeDirection direction, Fluid fluid) {
		if (fluidHandler.canFill(direction, fluid)) {
			FluidStack fstack = new FluidStack(fluid, 1000);
			int filled = fluidHandler.fill(direction, fstack, false);

			if (filled != 1000) return;

			fluidHandler.fill(direction, fstack, true);
			setLiquid(stack, null);
		}
	}

	private void attemptDrain(ItemStack stack, IFluidHandler fluidHandler, ForgeDirection direction) {
		FluidStack fstack = fluidHandler.drain(direction, 1000, false);
		if (fstack == null || fstack.amount != 1000) return;

		FluidStack newFluid = fluidHandler.drain(direction, 1000, true);

		setLiquid(stack, newFluid.getFluid());
	}
}