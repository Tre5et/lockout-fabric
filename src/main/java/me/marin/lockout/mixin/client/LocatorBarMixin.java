package me.marin.lockout.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.datafixers.util.Either;
import me.marin.lockout.LockoutTeam;
import me.marin.lockout.client.LockoutClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;
import java.util.UUID;

@Mixin(LocatorBarRenderer.class)
public abstract class LocatorBarMixin implements ContextualBarRenderer {

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private static Identifier LOCATOR_BAR_ARROW_UP;
    @Shadow @Final private static Identifier LOCATOR_BAR_ARROW_DOWN;

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;forEachWaypoint(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Consumer;)V")
    )
    private void lockout$renderHeads(ClientWaypointManager instance, Entity entity, Consumer<TrackedWaypoint> originalAction, GuiGraphics context, DeltaTracker tickCounter) {
        Level world = entity.level();
        int centerY = top(this.minecraft.getWindow());
        PartialTickSupplier entityTickProgress = e -> tickCounter.getGameTimeDeltaPartialTick(!world.tickRateManager().isEntityFrozen(e));

        instance.forEachWaypoint(entity, waypoint -> {
            // Original filtering logic
            if ((Boolean) waypoint.id().left().map(uuid -> uuid.equals(entity.getUUID())).orElse(false)) {
                return;
            }

            double yaw = waypoint.yawAngleToCamera(world, (TrackedWaypoint.Camera) this.minecraft.gameRenderer.getMainCamera(), entityTickProgress);
            if (yaw <= -60.0 || yaw > 60.0) {
                return;
            }

            int centerX = Mth.ceil((this.minecraft.getWindow().getGuiScaledWidth() - 9) / 2.0F);
            int offset = Mth.floor(yaw * 173.0 / 2.0 / 60.0);
            int x = centerX + offset;
            int y = centerY - 2;
            int size = 9;

            Waypoint.Icon config = waypoint.icon();
            Either<UUID, String> source = waypoint.id();

            boolean renderedHead = false;

            if (source.left().isPresent()) {
                UUID uuid = source.left().get();
                PlayerInfo entry = this.minecraft.getConnection() != null ? this.minecraft.getConnection().getPlayerInfo(uuid) : null;

                if (entry != null) {
                    LockoutTeam team = LockoutClient.lockout != null ? LockoutClient.lockout.getPlayerTeam(uuid) : null;
                    int teamColor = -1;
                    if (team != null) {
                        Integer val = team.getColor().getColor();
                        if (val != null) teamColor = val | 0xFF000000;
                    }

                    PlayerSkin textures = entry.getSkin();
                    Identifier skin = textures.body().texturePath();

                    // Render border if team color exists
                    if (teamColor != -1) {
                        int bt = 1; // Border thickness
                        context.fill(RenderPipelines.GUI, x - bt, y - bt, x + size + bt, y, teamColor); // top
                        context.fill(RenderPipelines.GUI, x - bt, y + size, x + size + bt, y + size + bt, teamColor); // bottom
                        context.fill(RenderPipelines.GUI, x - bt, y, x, y + size, teamColor); // left
                        context.fill(RenderPipelines.GUI, x + size, y, x + size + bt, y + size, teamColor); // right
                    }

                    // Render head base
                    context.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 8, 8, size, size, 8, 8, 64, 64, -1);
                    // Render hat layer
                    context.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 40, 8, size, size, 8, 8, 64, 64, -1);
                    renderedHead = true;
                }
            }

            if (!renderedHead) {
                // Fallback to original sprite rendering
                WaypointStyle style = this.minecraft.getWaypointStyles().get(config.style);
                float dist = Mth.sqrt((float) waypoint.distanceSquared(entity));
                Identifier identifier = style.sprite(dist);
                int color = config.color.orElseGet(() -> 0xFFFFFF);

                context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x, y, size, size, color);
            }

            // Original pitch/arrow logic
            TrackedWaypoint.PitchDirection pitch = waypoint.pitchDirectionToCamera(world, (TrackedWaypoint.Projector) this.minecraft.gameRenderer, entityTickProgress);
            if (pitch != TrackedWaypoint.PitchDirection.NONE) {
                int arrowY;
                Identifier arrowSprite;
                if (pitch == TrackedWaypoint.PitchDirection.DOWN) {
                    arrowY = 6;
                    arrowSprite = LOCATOR_BAR_ARROW_DOWN;
                } else {
                    arrowY = -6;
                    arrowSprite = LOCATOR_BAR_ARROW_UP;
                }
                context.blitSprite(RenderPipelines.GUI_TEXTURED, arrowSprite, x + 1, centerY + arrowY, 7, 5);
            }
        });
    }
}
