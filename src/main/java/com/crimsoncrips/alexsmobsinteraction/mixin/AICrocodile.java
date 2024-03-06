package com.crimsoncrips.alexsmobsinteraction.mixin;

import com.crimsoncrips.alexsmobsinteraction.AInteractionTagRegistry;
import com.crimsoncrips.alexsmobsinteraction.config.AInteractionConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityAlligatorSnappingTurtle;
import com.github.alexthe666.alexsmobs.entity.EntityCrocodile;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFindWater;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAILeaveWater;
import com.github.alexthe666.alexsmobs.entity.ai.BottomFeederAIWander;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

import static com.crimsoncrips.alexsmobsinteraction.AInteractionTagRegistry.CROCODILE_BABY_KILL;
import static com.crimsoncrips.alexsmobsinteraction.AInteractionTagRegistry.CROCODILE_KILL;


@Mixin(EntityCrocodile.class)
public class AICrocodile extends Mob {
    protected AICrocodile(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void CrocodileGoals(CallbackInfo ci){
        Predicate<LivingEntity> CROCODILE_BABY_TARGETS = AMEntityRegistry.buildPredicateFromTag(CROCODILE_BABY_KILL);;
        Predicate<LivingEntity> crocodileHuntBaby = (livingEntity) -> {
            return CROCODILE_BABY_TARGETS.test(livingEntity) && livingEntity.isBaby();
        };
        EntityCrocodile crocodile = (EntityCrocodile)(Object)this;
        this.targetSelector.addGoal(4, new EntityAINearestTarget3D(this, Player.class, 80, true, false, (Predicate)null) {
            public boolean canUse() {
                if (AInteractionConfig.weakened) {
                    return !crocodile.isTame() && !isBaby() && !(getHealth() <= 0.15F * getMaxHealth()) && !crocodile.isInLove() && super.canUse();
                } else
                {
                    return !crocodile.isTame() && !isBaby() && !crocodile.isInLove() && super.canUse();
                }
            }
        });
        this.targetSelector.addGoal(2, new EntityAINearestTarget3D<>(this, LivingEntity.class, 10, true, false, AMEntityRegistry.buildPredicateFromTag(CROCODILE_KILL)){
            protected AABB getTargetSearchArea(double targetDistance) {
                return this.mob.getBoundingBox().inflate(25D, 1D, 25D);
            }
            public boolean canUse() {
                return super.canUse() && !crocodile.isTame();
            }
        });
        this.targetSelector.addGoal(2, new EntityAINearestTarget3D<>(this, LivingEntity.class, 10000, true, false, crocodileHuntBaby){
            protected AABB getTargetSearchArea(double targetDistance) {
                return this.mob.getBoundingBox().inflate(25D, 1D, 25D);
            }
            public boolean canUse() {
                return super.canUse() && !crocodile.isTame();
            }
        });
    }
    public void awardKillScore(Entity entity, int score, DamageSource src) {
        if(entity instanceof LivingEntity living){
            final CompoundTag emptyNbt = new CompoundTag();
            living.addAdditionalSaveData(emptyNbt);
            emptyNbt.putString("DeathLootTable", BuiltInLootTables.EMPTY.toString());
            living.readAdditionalSaveData(emptyNbt);

        }
        super.awardKillScore(entity, score, src);
    }
}
