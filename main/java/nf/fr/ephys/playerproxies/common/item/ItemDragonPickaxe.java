package nf.fr.ephys.playerproxies.common.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import nf.fr.ephys.playerproxies.client.registry.DragonColorRegistry;
import nf.fr.ephys.playerproxies.common.PlayerProxies;

public class ItemDragonPickaxe extends ItemPickaxe {
	public static boolean enabled = true;

	public static void register() {
		if (!enabled) return;

		PlayerProxies.Items.dragonPickaxe = new ItemDragonPickaxe();
		PlayerProxies.Items.dragonPickaxe.setUnlocalizedName("PP_DragonPick")
				.setMaxStackSize(1)
				.setCreativeTab(PlayerProxies.creativeTab)
				.setHarvestLevel("pickaxe", 4);

		GameRegistry.registerItem(PlayerProxies.Items.dragonPickaxe, PlayerProxies.Items.dragonPickaxe.getUnlocalizedName());
	}

	public static void registerCraft() {
		if (!enabled) return;

		GameRegistry.addRecipe(new ItemStack(PlayerProxies.Items.dragonPickaxe),
				"iii", " s ", " s ",
				'i', new ItemStack(PlayerProxies.Items.dragonScaleIngot),
				's', new ItemStack(Items.stick));
	}

	public ItemDragonPickaxe() {
		super(PlayerProxies.Items.matDragonScale);
	}

	@Override
	public float func_150893_a(ItemStack stack, Block block) {
		if (block == Blocks.obsidian) return 80F;

		return super.func_150893_a(stack, block);
	}

	@Override
	public boolean isItemTool(ItemStack par1ItemStack) {
		return true;
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack) {
		return EnumRarity.rare;
	}

	private IIcon[] icons = new IIcon[2];

	@Override
	public void registerIcons(IIconRegister register) {
		icons[0] = register.registerIcon("ephys.pp:dragonPick_2");
		icons[1] = register.registerIcon("ephys.pp:dragonPick_1");
	}

	@Override
	public IIcon getIcon(ItemStack stack, int renderPass) {
		return icons[renderPass];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		return pass == 0 ? DragonColorRegistry.getColor() : super.getColorFromItemStack(stack, pass);
	}

	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}
}