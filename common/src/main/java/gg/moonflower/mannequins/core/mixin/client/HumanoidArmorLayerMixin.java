package gg.moonflower.mannequins.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.moonflower.mannequins.client.render.model.TranslatedMannequin;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;copyPropertiesTo(Lnet/minecraft/client/model/HumanoidModel;)V", shift = At.Shift.AFTER))
    public void translateMannequinArmor(PoseStack poseStack, MultiBufferSource buffer, T entity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci) {
        if (this.getParentModel() instanceof TranslatedMannequin) {
            model.body.y += 2;
            model.body.xRot = 0;
            model.body.yRot = 0;
            model.body.zRot = 0;
            poseStack.pushPose();
            ((TranslatedMannequin) this.getParentModel()).translateToBody(poseStack);
        }
    }

    @Inject(method = "renderArmorPiece", at = @At("TAIL"))
    public void resetMannequinArmor(PoseStack poseStack, MultiBufferSource buffer, T entity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci) {
        if (!(this.getParentModel() instanceof TranslatedMannequin))
            return;

        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == slot) {
            poseStack.popPose();
        }
    }
}
