package gg.moonflower.mannequins.common.entity;

import gg.moonflower.mannequins.core.registry.MannequinsItems;
import gg.moonflower.mannequins.core.registry.MannequinsSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;

public class Statue extends AbstractMannequin {

    public Statue(EntityType<? extends AbstractMannequin> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean canBreak(DamageSource source, Entity entity) {
        if (!super.canBreak(source, entity))
            return false;

        boolean creativePlayer = source.getEntity() instanceof Player && ((Player)source.getEntity()).getAbilities().instabuild;
        if (creativePlayer) {
            return true;
        } else {
            return ((Player)source.getEntity()).getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PickaxeItem;
        }
    }

    @Override
    public boolean canChangeExpression(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).getItem() instanceof PickaxeItem;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(MannequinsItems.STATUE.get());
    }

    @Override
    public ParticleOptions getParticle() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
    }

    @Override
    public SoundEvent getHitSound() {
        return MannequinsSounds.ENTITY_STATUE_HIT.get();
    }

    @Override
    public SoundEvent getBrokenSound() {
        return MannequinsSounds.ENTITY_STATUE_BREAK.get();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return MannequinsSounds.ENTITY_STATUE_PLACE.get();
    }

    @Override
    public Fallsounds getFallSounds() {
        return new Fallsounds(MannequinsSounds.ENTITY_STATUE_FALL.get(), MannequinsSounds.ENTITY_STATUE_FALL.get());
    }

}
