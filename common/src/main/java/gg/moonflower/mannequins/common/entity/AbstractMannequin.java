package gg.moonflower.mannequins.common.entity;

import com.google.common.collect.ImmutableList;
import gg.moonflower.mannequins.common.menu.MannequinInventoryMenu;
import gg.moonflower.mannequins.common.network.MannequinsMessages;
import gg.moonflower.mannequins.common.network.play.ClientboundAttackMannequin;
import gg.moonflower.mannequins.common.network.play.ClientboundOpenMannequinScreen;
import gg.moonflower.mannequins.core.mixin.ServerPlayerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

/**
 * @author Jackson, Ocelot
 */
public abstract class AbstractMannequin extends LivingEntity {
    public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(AbstractMannequin.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(AbstractMannequin.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(AbstractMannequin.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(AbstractMannequin.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Boolean> DATA_DISABLED = SynchedEntityData.defineId(AbstractMannequin.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_TROLLED = SynchedEntityData.defineId(AbstractMannequin.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<OptionalInt> DATA_EXPRESSION = SynchedEntityData.defineId(AbstractMannequin.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

    private static final Predicate<Entity> MINECART = (entity) -> entity instanceof AbstractMinecart && ((AbstractMinecart) entity).getMinecartType() == AbstractMinecart.Type.RIDEABLE;
    private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0F, 0.0F, -10.0F);
    private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-10.0F, 0.0F, 10.0F);

    private final SimpleContainer inventory = new SimpleContainer(4);
    public long lastHit;
    private Rotations headPose = DEFAULT_HEAD_POSE;
    private Rotations bodyPose = DEFAULT_BODY_POSE;
    private Rotations leftArmPose = DEFAULT_LEFT_ARM_POSE;
    private Rotations rightArmPose = DEFAULT_RIGHT_ARM_POSE;

    public AbstractMannequin(EntityType<? extends AbstractMannequin> type, Level level) {
        super(type, level);
        this.setMaxUpStep(0.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
        this.entityData.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
        this.entityData.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
        this.entityData.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
        this.entityData.define(DATA_EXPRESSION, OptionalInt.empty());
        this.entityData.define(DATA_DISABLED, false);
        this.entityData.define(DATA_TROLLED, false);
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return ImmutableList.of(this.inventory.getItem(2), this.inventory.getItem(3));
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return ImmutableList.of(this.inventory.getItem(0), this.inventory.getItem(1));
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        if (slot == null)
            return ItemStack.EMPTY;

        switch (slot) {
            case HEAD:
                return this.inventory.getItem(0);
            case CHEST:
                return this.inventory.getItem(1);
            case MAINHAND:
                return this.inventory.getItem(2);
            case OFFHAND:
                return this.inventory.getItem(3);
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        if (slot == null)
            return;

        switch (slot) {
            case HEAD:
                this.inventory.setItem(0, stack);
                break;
            case CHEST:
                this.inventory.setItem(1, stack);
                break;
            case MAINHAND:
                this.inventory.setItem(2, stack);
                break;
            case OFFHAND:
                this.inventory.setItem(3, stack);
                break;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Pose", this.writePose());

        this.getExpression().ifPresent(expression -> tag.putInt("Expression", expression.ordinal()));
        tag.putBoolean("Trolled", this.isTrolled());
        tag.putBoolean("Disabled", this.isDisabled());

        ListTag listTag = new ListTag();
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.putByte("Slot", (byte) i);
                itemStack.save(compoundTag2);
                listTag.add(compoundTag2);
            }
        }
        tag.put("Items", listTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPose(tag.getCompound("Pose"));
        if (tag.contains("Expression", Tag.TAG_INT)) {
            this.setExpression(Expression.byId(tag.getInt("Expression")));
        }

        this.setTrolled(tag.getBoolean("Trolled"));
        this.entityData.set(DATA_DISABLED, tag.getBoolean("Disabled"));

        if (tag.contains("Items", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("Items", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag2 = listTag.getCompound(i);
                int j = compoundTag2.getByte("Slot") & 255;
                if (j < this.inventory.getContainerSize()) {
                    this.inventory.setItem(j, ItemStack.of(compoundTag2));
                }
            }
        }
    }

    private void readPose(CompoundTag tag) {
        ListTag headPose = tag.getList("Head", 5);
        this.setHeadPose(headPose.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(headPose));
        ListTag bodyPose = tag.getList("Body", 5);
        this.setBodyPose(bodyPose.isEmpty() ? DEFAULT_BODY_POSE : new Rotations(bodyPose));
        ListTag leftArmPose = tag.getList("LeftArm", 5);
        this.setLeftArmPose(leftArmPose.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(leftArmPose));
        ListTag rightArmPose = tag.getList("RightArm", 5);
        this.setRightArmPose(rightArmPose.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(rightArmPose));
    }

    private CompoundTag writePose() {
        CompoundTag pose = new CompoundTag();
        if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
            pose.put("Head", this.headPose.save());
        }

        if (!DEFAULT_BODY_POSE.equals(this.bodyPose)) {
            pose.put("Body", this.bodyPose.save());
        }

        if (!DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
            pose.put("LeftArm", this.leftArmPose.save());
        }

        if (!DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
            pose.put("RightArm", this.rightArmPose.save());
        }

        return pose;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), MINECART);

        for (Entity entity : list) {
            if (this.distanceToSqr(entity) <= 0.2D) {
                entity.push(this);
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.isDisabled())
            return InteractionResult.FAIL;
        if (!player.getItemInHand(hand).isEmpty() && !player.isSecondaryUseActive())
            return InteractionResult.PASS;
        if (this.level().isClientSide())
            return InteractionResult.SUCCESS;

        if (this.canChangeExpression(player, hand)) {
            if ("Trolled".equals(player.getItemInHand(hand).getHoverName().getString())) {
                if (!this.isTrolled()) {
                    this.setExpression(null);
                    this.setTrolled(true);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), this.getHitSound(), this.getSoundSource(), 1.0F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            } else {
                this.setTrolled(false);
                this.cycleExpression();
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), this.getHitSound(), this.getSoundSource(), 1.0F, 1.0F);
                return InteractionResult.CONSUME;
            }
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (serverPlayer.containerMenu != serverPlayer.inventoryMenu) {
            serverPlayer.closeContainer();
        }

        ServerPlayerAccessor access = (ServerPlayerAccessor) serverPlayer;
        access.callNextContainerCounter();
        MannequinsMessages.PLAY.sendTo(serverPlayer, new ClientboundOpenMannequinScreen(access.getContainerCounter(), this.getId()));
        serverPlayer.containerMenu = new MannequinInventoryMenu(access.getContainerCounter(), serverPlayer.getInventory(), this.inventory, this);
        access.callInitMenu(serverPlayer.containerMenu);

        return InteractionResult.CONSUME;
    }

    @Override
    public float getHurtDir() {
        return (float) -Mth.atan2(this.position().x() - this.getX(), this.position().z() - this.getZ());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || !this.isAlive()) return false;

        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            this.remove(RemovalReason.KILLED);
            return false;
        }

        if (this.isInvulnerableTo(source))
            return false;

        boolean hurt = this.hurtBySource(source);
        if (hurt) return true;

        Entity entity = source.getEntity();
        if (!canBreak(source, entity)) {
            Vec3 pos = source.getSourcePosition();
            if (pos == null)
                return false;
            this.level().broadcastEntityEvent(this, (byte) 32);
            MannequinsMessages.PLAY.sendToTracking(this, new ClientboundAttackMannequin(this.getId(), this.getHurtDir()));
            if (amount > 0) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, this.getX(), this.getEyeY(), this.getZ(), (int) ((double) amount * 0.5D), 0.1D, 0.0D, 0.1D, 0.2D);
            }
            return false;
        }

        if (source.isCreativePlayer()) {
            this.playBrokenSound();
            this.showBreakingParticles();
            this.remove(RemovalReason.KILLED);
            return true;
        }

        long time = this.level().getGameTime();
        if (time - this.lastHit > 5L) {
            this.level().broadcastEntityEvent(this, (byte) 32);
            this.lastHit = time;
        } else {
            Block.popResource(this.level(), this.blockPosition(), this.getItem());
            this.breakMannequin(source);
            this.showBreakingParticles();
            this.remove(RemovalReason.KILLED);
        }

        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handleEntityEvent(byte event) {
        if (event == 32) {
            if (this.level().isClientSide) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getHitSound(), this.getSoundSource(), 0.3F, 1.0F, false);
                this.lastHit = this.level().getGameTime();
            }
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    protected float tickHeadTurn(float p_110146_1_, float p_110146_2_) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.getYRot();
        return 0.0F;
    }

    @Override
    public double getMyRidingOffset() {
        return 0.1F;
    }

    @Override
    public void setYBodyRot(float rot) {
        this.yBodyRotO = this.yRotO = rot;
        this.yHeadRotO = this.yHeadRot = rot;
    }

    @Override
    public void setYHeadRot(float rot) {
        this.yBodyRotO = this.yRotO = rot;
        this.yHeadRotO = this.yHeadRot = rot;
    }

    @Override
    public void tick() {
        super.tick();
        Rotations headPose = this.entityData.get(DATA_HEAD_POSE);
        if (!this.headPose.equals(headPose)) {
            this.setHeadPose(headPose);
        }

        Rotations bodyPose = this.entityData.get(DATA_BODY_POSE);
        if (!this.bodyPose.equals(bodyPose)) {
            this.setBodyPose(bodyPose);
        }

        Rotations leftArmPose = this.entityData.get(DATA_LEFT_ARM_POSE);
        if (!this.leftArmPose.equals(leftArmPose)) {
            this.setLeftArmPose(leftArmPose);
        }

        Rotations rightArmPose = this.entityData.get(DATA_RIGHT_ARM_POSE);
        if (!this.rightArmPose.equals(rightArmPose)) {
            this.setRightArmPose(rightArmPose);
        }
    }

    @Override
    public void kill() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return entity instanceof Player && !this.level().mayInteract((Player) entity, this.blockPosition());
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public boolean attackable() {
        return false;
    }

    public Optional<Expression> getExpression() {
        OptionalInt id = this.entityData.get(DATA_EXPRESSION);
        if (id.isEmpty())
            return Optional.empty();
        return Optional.of(Expression.byId(id.getAsInt()));
    }

    public void setExpression(@Nullable Expression expression) {
        this.entityData.set(DATA_EXPRESSION, expression == null ? OptionalInt.empty() : OptionalInt.of(expression.ordinal()));
    }

    public boolean isTrolled() {
        return this.entityData.get(DATA_TROLLED);
    }

    public void setTrolled(boolean trolled) {
        this.entityData.set(DATA_TROLLED, trolled);
    }

    public boolean isDisabled() {
        return this.entityData.get(DATA_DISABLED);
    }

    public Rotations getHeadPose() {
        return this.headPose;
    }

    public void setHeadPose(Rotations pose) {
        this.headPose = pose;
        this.entityData.set(DATA_HEAD_POSE, pose);
    }

    public Rotations getBodyPose() {
        return this.bodyPose;
    }

    public void setBodyPose(Rotations pose) {
        this.bodyPose = pose;
        this.entityData.set(DATA_BODY_POSE, pose);
    }

    public Rotations getLeftArmPose() {
        return this.leftArmPose;
    }

    public void setLeftArmPose(Rotations pose) {
        this.leftArmPose = pose;
        this.entityData.set(DATA_LEFT_ARM_POSE, pose);
    }

    public Rotations getRightArmPose() {
        return this.rightArmPose;
    }

    public void setRightArmPose(Rotations pose) {
        this.rightArmPose = pose;
        this.entityData.set(DATA_RIGHT_ARM_POSE, pose);
    }

    @Override
    public ItemStack getPickResult() {
        return this.getItem();
    }

    private void showBreakingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel) this.level()).sendParticles(this.getParticle(), this.getX(), this.getY(0.6666666666666666D), this.getZ(), 10, this.getBbWidth() / 4.0F, this.getBbHeight() / 4.0F, this.getBbWidth() / 4.0F, 0.05D);
        }
    }

    private void causeDamage(DamageSource source, float damage) {
        float f = this.getHealth();
        f = f - damage;
        if (f <= 0.5F) {
            this.breakMannequin(source);
            this.remove(RemovalReason.KILLED);
        } else {
            this.setHealth(f);
        }
    }

    private void breakMannequin(DamageSource source) {
        this.playBrokenSound();
        this.dropAllDeathLoot(source);

        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
                Block.popResource(this.level(), this.blockPosition().above(), stack);
                this.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private void playBrokenSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), this.getBrokenSound(), this.getSoundSource(), 1.0F, 1.0F);
    }

    public boolean hurtBySource(DamageSource source) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            this.breakMannequin(source);
            this.remove(RemovalReason.KILLED);
            return true;
        }

        if (source.is(DamageTypes.IN_FIRE)) {
            if (this.isOnFire()) {
                this.causeDamage(source, 0.15F);
            } else {
                this.setSecondsOnFire(5);
            }

            return true;
        }

        if (source.is(DamageTypes.ON_FIRE) && this.getHealth() > 0.5F) {
            this.causeDamage(source, 4.0F);
            return true;
        }
        return false;
    }

    public void cycleExpression() {
        Optional<Expression> expressionOptional = this.getExpression();
        if (!expressionOptional.isPresent()) {
            this.setExpression(Expression.byId(0));
            return;
        }

        Expression current = expressionOptional.get();
        if (current.ordinal() == Expression.values().length - 1) {
            this.setExpression(null);
            return;
        }

        this.setExpression(Expression.byId(current.ordinal() + 1));
    }

    public void onAttack(float attackYaw) {
    }

    public boolean canBreak(DamageSource source, Entity entity) {
        return entity != null && entity.isShiftKeyDown() && source.getDirectEntity() instanceof Player && ((Player) source.getDirectEntity()).getAbilities().mayBuild;
    }

    public abstract boolean canChangeExpression(Player player, InteractionHand hand);

    public abstract ItemStack getItem();

    public abstract ParticleOptions getParticle();

    public abstract SoundEvent getHitSound();

    public abstract SoundEvent getBrokenSound();

    public abstract SoundEvent getPlaceSound();

    public enum Expression {
        NEUTRAL("neutral"),
        HAPPY("happy"),
        SURPRISED("surprised"),
        UPSET("upset");

        private final String name;

        Expression(String name, boolean available) {
            this.name = name;
        }

        Expression(String name) {
            this(name, true);
        }

        public static Expression byId(int id) {
            Expression[] expressions = values();
            if (id < 0 || id >= expressions.length) {
                id = 0;
            }

            return expressions[id];
        }

        public String getName() {
            return name;
        }
    }
}