package gg.moonflower.mannequins.client.render.entity;

import gg.moonflower.mannequins.client.render.model.BasicMannequinModel;
import gg.moonflower.mannequins.common.entity.AbstractMannequin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * @author Ocelot, Jackson
 */
public abstract class AbstractMannequinRenderer<T extends AbstractMannequin> extends LivingEntityRenderer<T, BasicMannequinModel<T>> {

    public AbstractMannequinRenderer(EntityRendererProvider.Context context, BasicMannequinModel<T> model, BasicMannequinModel<T> innerArmorModel, BasicMannequinModel<T> outerArmorModel) {
        super(context, model, 0.0F);
        this.addLayer(new HumanoidArmorLayer<>(this, innerArmorModel, outerArmorModel, context.getModelManager()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return super.shouldShowName(entity) && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    public final ResourceLocation getTextureLocation(T entity) {
        Optional<AbstractMannequin.Expression> expression = entity.getExpression();
        if (expression.isEmpty())
            return this.getMannequinTexture(entity);
        return this.getMannequinExpressionTexture(entity, expression.get());
    }

    public abstract ResourceLocation getMannequinTexture(T entity);

    public abstract ResourceLocation getMannequinExpressionTexture(T entity, AbstractMannequin.Expression expression);
}