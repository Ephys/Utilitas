package be.ephys.utilitas.feature.fluid_hopper;

import be.ephys.utilitas.Utilitas;
import be.ephys.utilitas.api.IToolTipped;
import be.ephys.utilitas.base.helpers.ChatHelper;
import be.ephys.utilitas.base.helpers.InputHelper;
import be.ephys.utilitas.base.helpers.NBTHelper;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockFluidHopper extends BlockHopper implements IToolTipped {

    public BlockFluidHopper() {
        this.setSoundType(SoundType.METAL);
        this.setRegistryName("fluid_hopper");

        this.setUnlocalizedName(this.getRegistryName().toString())
            .setHardness(3.0F)
            .setResistance(8.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityFluidHopper();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntityFluidHopper te = (TileEntityFluidHopper) world.getTileEntity(pos);

        if (te != null) {
            te.getFluidsFromStack(stack);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntityFluidHopper te = (TileEntityFluidHopper) world.getTileEntity(pos);

        if (te != null) {
            player.openGui(Utilitas.instance, FeatureFluidHopper.GUI_FLUID_HOPPER.getId(), world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);

        TileEntityFluidHopper tile = (TileEntityFluidHopper) world.getTileEntity(pos);

        if (tile != null) {
            tile.onBlockUpdate(neighbor);
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ArrayList<ItemStack> stacks = new ArrayList<>(1);
        stacks.add(getCustomItemStack(world, pos));

        return stacks;
    }


    private ItemStack getCustomItemStack(IBlockAccess world, BlockPos pos) {
        ItemStack hopper = new ItemStack(FeatureFluidHopper.FLUID_HOPPER);

        TileEntityFluidHopper te = (TileEntityFluidHopper) world.getTileEntity(pos);
        if (te != null) {
            te.setFluidsToStack(hopper);
        }

        return hopper;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return getCustomItemStack(world, pos);
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityFluidHopper) {
            return ((TileEntityFluidHopper) tile).getComparatorInput();
        }

        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> data, boolean debug) {
        data.add("Trivializing liquids even more !");

        if (!InputHelper.isShiftPressed()) {
            data.add(TextFormatting.LIGHT_PURPLE + "Press shift to show contents");
        } else {
            data.add(TextFormatting.LIGHT_PURPLE + "Contains:");

            NBTTagCompound fluidStackNBTs = NBTHelper.getNBT(stack).getCompoundTag("fluidStacks");

            boolean color = true;
            for (int i = 0; i < TileEntityFluidHopper.MAX_STACK_SIZE; i++) {
                if (fluidStackNBTs.hasKey(Integer.toString(i))) {
                    FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidStackNBTs.getCompoundTag(Integer.toString(i)));

                    data.add((color ? TextFormatting.DARK_AQUA : TextFormatting.AQUA) + ChatHelper.getDisplayName(fluid) + " (" + fluid.amount + "mb)");

                    color = !color;
                }
            }
        }
    }
}
