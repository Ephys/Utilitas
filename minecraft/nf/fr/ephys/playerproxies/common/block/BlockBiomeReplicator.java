package nf.fr.ephys.playerproxies.common.block;

import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import nf.fr.ephys.playerproxies.common.PlayerProxies;
import nf.fr.ephys.playerproxies.common.tileentity.TileEntityBiomeReplicator;
import nf.fr.ephys.playerproxies.common.tileentity.TileEntityItemTicker;
import nf.fr.ephys.playerproxies.helpers.BlockHelper;

public class BlockBiomeReplicator extends BlockContainer {
	public static int BLOCK_ID = 806;
	
	private Icon iconTop;
	private Icon iconSide;
	private Icon iconBottom;
	
	public static void register() {
		PlayerProxies.blockBiomeChanger = new BlockBiomeReplicator();
		PlayerProxies.blockBiomeChanger.setUnlocalizedName("PP_BiomeChanger");
		GameRegistry.registerBlock(PlayerProxies.blockBiomeChanger, "PP_BiomeChanger");
		GameRegistry.registerTileEntity(TileEntityBiomeReplicator.class, "PP_BiomeChanger");
		LanguageRegistry.instance().addName(PlayerProxies.blockBiomeChanger, "Biome transmuter");
	}
	
	public static void registerCraft() {
		GameRegistry.addRecipe(new ItemStack(PlayerProxies.blockBiomeChanger), 
				" c ", "pdp", " p ",
				'c', new ItemStack(PlayerProxies.itemLinkFocus),
				'p', new ItemStack(PlayerProxies.blockToughwoodPlank),
				'd', new ItemStack(Block.blockDiamond)
		);
	}
	
	public BlockBiomeReplicator() {
		super(BLOCK_ID, Material.wood);

		setHardness(1.0F);
		setCreativeTab(CreativeTabs.tabDecorations);
		
		setStepSound(soundWoodFootstep);
	}

	@Override
	public Icon getBlockTexture(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		switch(par5) {
			case 0: return iconBottom;
			case 1: return iconTop;
			default: return iconSide;
		}
	}
	
	@Override
	public Icon getIcon(int par1, int par2) {
		switch(par1) {
			case 0: return iconBottom;
			case 1: return iconTop;
			default: return iconSide;
		}
	}

	@Override
	public void registerIcons(IconRegister register) {
		iconTop    = register.registerIcon("ephys.pp:biomeChangerTop");
		iconBottom = register.registerIcon("ephys.pp:biomeChangerBottom");
		iconSide   = register.registerIcon("ephys.pp:biomeChangerSide");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityBiomeReplicator();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (world.isRemote) return true;

		TileEntityBiomeReplicator te = (TileEntityBiomeReplicator) world.getBlockTileEntity(x, y, z);

		if (te.hasBiome()) {
			BlockHelper.dropContents(te, world, x, y, z);
		}
		
		if (player.getHeldItem() != null && te.isItemValidForSlot(0, player.getHeldItem())) {
			te.setInventorySlotContents(0, player.getHeldItem().copy());
			player.getHeldItem().stackSize--;
		}

		return true;
	}
	
	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int metadata) {
		TileEntityBiomeReplicator te = (TileEntityBiomeReplicator) world.getBlockTileEntity(x, y, z);

		if (te != null)
			BlockHelper.dropContents(te, world, x, y, z);
		
		super.onBlockPreDestroy(world, x, y, z, metadata);
	}
}
