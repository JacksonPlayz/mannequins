package gg.moonflower.mannequins.common.entity;

import gg.moonflower.mannequins.core.registry.MannequinsItems;
import gg.moonflower.mannequins.core.registry.MannequinsSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class Mannequin extends AbstractMannequin {
    private float attackAnimationXFactor;
    private float attackAnimationZFactor;
    private int attackAnimation;
    private float hurtDir;

    public Mannequin(EntityType<? extends AbstractMannequin> type, Level level) {
        super(type, level);
    }

    @Override
    public float getHurtDir() {
        return hurtDir;
    }

    @Override
    public void onAttack(float attackYaw) {
        float rotation = attackYaw - (float) (this.yBodyRot / 180.0 * Math.PI);
        this.hurtDir = attackYaw;
        this.attackAnimation = 40;
        this.attackAnimationXFactor = Mth.cos(rotation);
        this.attackAnimationZFactor = Mth.sin(rotation);
    }

    @Override
    public boolean canChangeExpression(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).getItem() instanceof AxeItem;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.attackAnimation <= 0)
                return;
            this.attackAnimation--;
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(MannequinsItems.MANNEQUIN.get());
    }

    @Override
    public ParticleOptions getParticle() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState());
    }

    @Override
    public SoundEvent getHitSound() {
        return MannequinsSounds.ENTITY_MANNEQUIN_HIT.get();
    }

    @Override
    public SoundEvent getBrokenSound() {
        return MannequinsSounds.ENTITY_MANNEQUIN_BREAK.get();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return MannequinsSounds.ENTITY_MANNEQUIN_PLACE.get();
    }

    @Override
    public Fallsounds getFallSounds() {
        return new Fallsounds(MannequinsSounds.ENTITY_MANNEQUIN_FALL.get(), MannequinsSounds.ENTITY_MANNEQUIN_FALL.get());
    }

    // Graph: https://www.desmos.com/calculator/xkhzglfwkm
    private float getAttackAnimation(float partialTicks) {
        float x = this.attackAnimation - partialTicks;
        return Mth.cos(x) / 2F * Mth.sqrt(x) / (50 - x);
    }

    @Environment(EnvType.CLIENT)
    public boolean hasAnimation() {
        return this.attackAnimation > 0;
    }

    @Environment(EnvType.CLIENT)
    public float getAnimationRotationX(float partialTicks) {
        return this.attackAnimationXFactor * this.getAttackAnimation(partialTicks);
    }

    @Environment(EnvType.CLIENT)
    public float getAnimationRotationZ(float partialTicks) {
        return this.attackAnimationZFactor * this.getAttackAnimation(partialTicks);
    }
}
