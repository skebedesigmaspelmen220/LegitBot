package com.legitbot.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.legitbot.config.ModConfig;
import com.legitbot.mixin.LegitAimbotMixin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class EspRenderer {

    public static void register() {
        WorldRenderEvents.LAST.register(EspRenderer::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        if (!ModConfig.espEnabled || LegitAimbotMixin.currentTarget == null) return;
        LivingEntity target = LegitAimbotMixin.currentTarget;
        if (!target.isAlive()) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();

        double tX = MathHelper.lerp(context.tickDelta(), target.prevX, target.getX()) - cameraPos.x;
        double tY = MathHelper.lerp(context.tickDelta(), target.prevY, target.getY()) - cameraPos.y;
        double tZ = MathHelper.lerp(context.tickDelta(), target.prevZ, target.getZ()) - cameraPos.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer(); 
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        double time = System.currentTimeMillis() / 1000.0; 

        for (int i = 0; i < 4; i++) {
            double angle = (time * ModConfig.orbitSpeed) + (Math.PI * 2 / 4) * i;
            double x = tX + Math.cos(angle) * ModConfig.orbitRadius;
            double z = tZ + Math.sin(angle) * ModConfig.orbitRadius;
            double y = tY + ModConfig.orbitHeight + Math.sin((time * 3) + i) * 0.1;

            float s = ModConfig.cubeSize / 2.0f;
            drawCube(matrices, buffer, x, y, z, s);
        }

        tessellator.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void drawCube(MatrixStack ms, BufferBuilder b, double x, double y, double z, float s) {
        Matrix4f m = ms.peek().getPositionMatrix();
        float r = ModConfig.r, g = ModConfig.g, blue = ModConfig.b, a = ModConfig.alpha;
        
        b.vertex(m, (float)(x-s), (float)(y+s), (float)(z-s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x-s), (float)(y+s), (float)(z+s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y+s), (float)(z+s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y+s), (float)(z-s)).color(r,g,blue,a).next();

        b.vertex(m, (float)(x-s), (float)(y-s), (float)(z-s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y-s), (float)(z-s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y-s), (float)(z+s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x-s), (float)(y-s), (float)(z+s)).color(r,g,blue,a).next();
        
        b.vertex(m, (float)(x-s), (float)(y-s), (float)(z-s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x-s), (float)(y+s), (float)(z-s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y+s), (float)(z-s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y-s), (float)(z-s)).color(r,g,blue,a).next();

        b.vertex(m, (float)(x-s), (float)(y-s), (float)(z+s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y-s), (float)(z+s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x+s), (float)(y+s), (float)(z+s)).color(r,g,blue,a).next();
        b.vertex(m, (float)(x-s), (float)(y+s), (float)(z+s)).color(r,g,blue,a).next();
    }
}