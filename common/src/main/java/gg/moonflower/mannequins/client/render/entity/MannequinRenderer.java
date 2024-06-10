package gg.moonflower.mannequins.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gg.moonflower.mannequins.client.render.model.BasicMannequinModel;
import gg.moonflower.mannequins.client.render.model.MannequinModel;
import gg.moonflower.mannequins.client.render.model.MannequinsModelLayers;
import gg.moonflower.mannequins.common.entity.AbstractMannequin;
import gg.moonflower.mannequins.common.entity.Mannequin;
import gg.moonflower.mannequins.core.Mannequins;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MannequinRenderer extends AbstractMannequinRenderer<Mannequin> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Mannequins.MOD_ID, "textures/entity/mannequin/mannequin.png");
    private static final ResourceLocation TROLLED = new ResourceLocation(Mannequins.MOD_ID, "textures/entity/mannequin/mannequin_trolled.png");
    private static final ResourceLocation[] EXPRESSIONS = new ResourceLocation[]{
            new ResourceLocation(Mannequins.MOD_ID, "textures/entity/mannequin/mannequin_neutral.png"),
            new ResourceLocation(Mannequins.MOD_ID, "textures/entity/mannequin/mannequin_happy.png"),
            new ResourceLocation(Mannequins.MOD_ID, "textures/entity/mannequin/mannequin_surprised.png"),
            new ResourceLocation(Mannequins.MOD_ID, "textures/entity/mannequin/mannequin_upset.png")
    };

    public MannequinRenderer(EntityRendererProvider.Context context) {
        super(context, new MannequinModel(context.bakeLayer(MannequinsModelLayers.MANNEQUIN)), new BasicMannequinModel<>(context.bakeLayer(MannequinsModelLayers.MANNEQUIN_INNER_ARMOR)), new BasicMannequinModel<>(context.bakeLayer(MannequinsModelLayers.MANNEQUIN_OUTER_ARMOR)));
    }

    @Override
    protected void setupRotations(Mannequin entity, PoseStack poseStack, float f, float g, float partialTicks) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - g));
        float i = (float) (entity.level().getGameTime() - entity.lastHit) + partialTicks;
        if (!entity.hasAnimation() && i < 5.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(i / 1.5F * (float) Math.PI) * 3.0F));
        }
    }

    @Override
    public ResourceLocation getMannequinTexture(Mannequin entity) {
        return entity.isTrolled() ? TROLLED : TEXTURE;
    }

    @Override
    public ResourceLocation getMannequinExpressionTexture(Mannequin entity, AbstractMannequin.Expression expression) {
        return EXPRESSIONS[expression.ordinal() % EXPRESSIONS.length];
    }
}
