//package be.ephys.utilitas.feature.omnibucket;
//
//import net.minecraft.client.resources.IResource;
//import net.minecraft.client.resources.IResourceManager;
//import net.minecraft.client.resources.IResourceManagerReloadListener;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.fluids.Fluid;
//import net.minecraftforge.fluids.FluidStack;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//
//public class FluidColorRegistry implements IResourceManagerReloadListener {
//
//    private static IResourceManager resourceManager;
//    private static HashMap<IIcon, Integer> colorCache = new HashMap<>();
//
//    public static int getColorFromFluid(FluidStack fluid) {
//        return getColorFromIcon(RenderHelper.getFluidTexture(fluid));
//    }
//
//    public static int getColorFromFluid(Fluid fluid) {
//        return getColorFromIcon(RenderHelper.getFluidTexture(fluid));
//    }
//
//    public static int getColorFromIcon(IIcon icon) {
//        if (icon == null || resourceManager == null)
//            return 0xFFFFFF;
//
//        if (colorCache.containsKey(icon))
//            return colorCache.get(icon);
//
//        BufferedImage bi;
//        try {
//            InputStream is = getIconRess(icon).getInputStream();
//            bi = ImageIO.read(is);
//        } catch (IOException e) {
//            PlayerProxies.getLogger().warn("Failled to fetch color for icon " + icon.getIconName());
//            e.printStackTrace();
//
//            colorCache.put(icon, 0);
//            return 0;
//        }
//
//        // (yay to code copy from TextureAtlasSprite ?)
//        // TODO: TextureAtlasSprite stores aint in framesTextureData, might want to use that ?
//        int w = bi.getWidth();
//        int h = bi.getHeight();
//
//        int[] aint = new int[h * w];
//        bi.getRGB(0, 0, w, h, aint, 0, w);
//
//        int color = MathHelper.averageColorFromAint(aint);
//
//        colorCache.put(icon, color);
//        return color;
//    }
//
//    public static IResource getIconRess(IIcon icon) throws IOException {
//        String iconName = icon.getIconName();
//        String modName;
//
//        int separator = iconName.indexOf(':');
//
//        if (separator == -1) {
//            modName = "minecraft";
//        } else {
//            String[] names = iconName.split(":");
//            modName = names[0].toLowerCase();
//            iconName = names[1];
//        }
//
//        return resourceManager.getResource(new ResourceLocation(modName, "textures/blocks/" + iconName + ".png"));
//    }
//
//    @Override
//    public void onResourceManagerReload(IResourceManager resourceManager) {
//        colorCache.clear();
//        colorCache.put(null, 0xFFFFFF);
//
//        FluidColorRegistry.resourceManager = resourceManager;
//    }
//}
