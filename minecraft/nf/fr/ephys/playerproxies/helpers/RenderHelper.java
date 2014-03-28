package nf.fr.ephys.playerproxies.helpers;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class RenderHelper {
	public static void loadTexturesMap() {
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
	}

	public static void renderItem3D(ItemStack item) {
		for (int i = 0; i < item.getItem().getRenderPasses(item.getItemDamage()); i++) {
			Icon icon = item.getItem().getIcon(item, i);
        	if(icon != null) {
        		 Color color = new Color(item.getItem().getColorFromItemStack(item, i));
        		 GL11.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
        		 float f = icon.getMinU();
                 float f1 = icon.getMaxU();
                 float f2 = icon.getMinV();
                 float f3 = icon.getMaxV();
                 ItemRenderer.renderItemIn2D(Tessellator.instance, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 1F / 16F);
                 GL11.glColor3f(1F, 1F, 1F);
        	}
		}

		int renderPass = 0;
	}
}