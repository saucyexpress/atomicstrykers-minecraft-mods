package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = InfernalMobsCore.MOD_ID)
public class OverlayChoking {

    protected static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

    private static Minecraft mc;

    private static int airOverrideValue = -999;
    private static long airDisplayTimeout;

    public static void onAirPacket(int air) {
        airOverrideValue = air;
        airDisplayTimeout = System.currentTimeMillis() + 3000L;
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        mc = Minecraft.getInstance();

        LayeredDraw layers;
        for (Field field : mc.gui.getClass().getDeclaredFields()) {
            if (field.getType().isAssignableFrom(LayeredDraw.class)) {
                field.setAccessible(true);
                try {
                    layers = (LayeredDraw) field.get(mc.gui);
                    layers.add(new InfernalMobsChokingGuiOverlay());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class InfernalMobsChokingGuiOverlay implements LayeredDraw.Layer {
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, float partialTick) {
            if (System.currentTimeMillis() > airDisplayTimeout) {
                airOverrideValue = -999;
            }

            // modded Gui.renderPlayerHealth 'air' section
            if (!mc.player.isEyeInFluid(FluidTags.WATER) && airOverrideValue != -999) {

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);

                int leftScreenCoordinate = mc.getWindow().getGuiScaledWidth() / 2 + 91;
                int topScreenCoordinate = mc.getWindow().getGuiScaledHeight() - 59;
                int maxHearts = getVehicleMaxHearts(mc.player);
                int maxAir = mc.player.getMaxAirSupply();
                int currentAir = Math.min(airOverrideValue, maxAir);
                int rowCount = getVisibleVehicleHeartRows(maxHearts) - 1;
                topScreenCoordinate = topScreenCoordinate - rowCount * 10;
                int fullBubbles = Mth.ceil((double) (currentAir - 2) * 10.0D / (double) maxAir);
                int partialBubbles = Mth.ceil((double) currentAir * 10.0D / (double) maxAir) - fullBubbles;

                for (int j5 = 0; j5 < fullBubbles + partialBubbles; ++j5) {
                    if (j5 < fullBubbles) {
                        guiGraphics.blit(GUI_ICONS_LOCATION, leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 16, 18, 9, 9);
                    } else {
                        guiGraphics.blit(GUI_ICONS_LOCATION, leftScreenCoordinate - j5 * 8 - 9, topScreenCoordinate, 25, 18, 9, 9);
                    }
                }
            }
        }
    }

    private static int getVehicleMaxHearts(LivingEntity livingEntity) {
        if (livingEntity != null && livingEntity.showVehicleHealth()) {
            float maxHealth = livingEntity.getMaxHealth();
            int roundedHalf = (int) (maxHealth + 0.5F) / 2;
            if (roundedHalf > 30) {
                roundedHalf = 30;
            }

            return roundedHalf;
        } else {
            return 0;
        }
    }

    private static int getVisibleVehicleHeartRows(int heartCount) {
        return (int) Math.ceil((double) heartCount / 10.0D);
    }

}
