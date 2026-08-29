package me.marin.lockout.client.goal.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public record ColoredTriangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2fc pose,
        int x0,
        int y0,
        int x1,
        int y1,
        int x2,
        int y2,
        int col,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public ColoredTriangleRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, int x0, int y0, int x1, int y1, int x2, int y2, int col, @Nullable ScreenRectangle scissorArea) {
        this(pipeline, textureSetup, pose, x0, y0, x1, y1, x2, y2, col, scissorArea, getBounds(x0, y0, x1, y1, x2, y2, pose, scissorArea));
    }

    @Override
    public void buildVertices(final VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x0(), (float)this.y0()).setColor(this.col());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x1(), (float)this.y1()).setColor(this.col());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x2(), (float)this.y2()).setColor(this.col());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x2(), (float)this.y2()).setColor(this.col());
    }

    public static @Nullable ScreenRectangle getBounds(int x0, int y0, int x1, int y1, int x2, int y2, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
        int minX = x0;
        int maxX = x0;
        int minY = y0;
        int maxY = y0;

        if(x1 < minX) minX = x1;
        if(x1 > maxX) maxX = x1;
        if(x2 < minX) minX = x2;
        if(x2 > maxX) maxX = x2;
        if(y1 < minY) minY = y1;
        if(y1 > maxY) maxY = y1;
        if(y2 < minY) minY = y2;
        if(y2 > maxY) maxY = y2;

        ScreenRectangle bounds = (new ScreenRectangle(minX, minY, maxX - minX, maxY - minY)).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
