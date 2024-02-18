package quek.undergarden.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import quek.undergarden.entity.rotspawn.RotspawnMonster;
import quek.undergarden.registry.UGItems;
import quek.undergarden.registry.UGSoundEvents;

import javax.annotation.Nullable;

public class Forgotten extends AbstractSkeleton {

    public Forgotten(EntityType<? extends AbstractSkeleton> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, RotspawnMonster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return UGSoundEvents.FORGOTTEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return UGSoundEvents.FORGOTTEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return UGSoundEvents.FORGOTTEN_DEATH.get();
    }

    @Override
    protected SoundEvent getStepSound() {
        return UGSoundEvents.FORGOTTEN_STEP.get();
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (random.nextInt(50) == 0) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(UGItems.CLOGGRUM_BATTLEAXE.get()));
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(UGItems.FORGOTTEN_HELMET.get()));
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(UGItems.FORGOTTEN_CHESTPLATE.get()));
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(UGItems.FORGOTTEN_LEGGINGS.get()));
        } else {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack armorStack = this.getItemBySlot(slot);
                    if (armorStack.isEmpty()) {
                        Item item = getEquipmentForSlot(slot);
                        if (item != null && random.nextBoolean()) {
                            this.setItemSlot(slot, new ItemStack(item));
                        }
                    }
                }
            }
            if (random.nextBoolean()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(UGItems.CLOGGRUM_SWORD.get()));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(UGItems.CLOGGRUM_AXE.get()));
            }
        }
    }

    //don't drop armor
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HAND) {
                ItemStack itemstack = this.getItemBySlot(slot);
                float dropChance = this.getEquipmentDropChance(slot);
                boolean doDrop = dropChance > 1.0F;
                if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (recentlyHit || doDrop) && Math.max(this.random.nextFloat() - (float) looting * 0.01F, 0.0F) < dropChance) {
                    if (!doDrop && itemstack.isDamageableItem()) {
                        itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
                    }
                    this.spawnAtLocation(itemstack);
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
                if (itemstack.is(UGItems.CLOGGRUM_BATTLEAXE.get()) && !EnchantmentHelper.hasVanishingCurse(itemstack) && recentlyHit /*&& Math.max(this.random.nextFloat() - (float) looting * 0.01F, 0.0F) < dropChance*/) {
                    if (itemstack.isDamageableItem()) {
                        itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
                    }
                    this.spawnAtLocation(itemstack);
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> UGItems.FORGOTTEN_HELMET.get();
            case CHEST -> UGItems.FORGOTTEN_CHESTPLATE.get();
            case LEGS -> UGItems.FORGOTTEN_LEGGINGS.get();
            default -> null;
        };
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return 1.9F;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }
}