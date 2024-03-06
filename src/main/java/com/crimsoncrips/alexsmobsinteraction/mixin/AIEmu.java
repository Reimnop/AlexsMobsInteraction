package com.crimsoncrips.alexsmobsinteraction.mixin;

import com.crimsoncrips.alexsmobsinteraction.AInteractionTagRegistry;
import com.crimsoncrips.alexsmobsinteraction.config.AInteractionConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityAnaconda;
import com.github.alexthe666.alexsmobs.entity.EntityEmu;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;


@Mixin(EntityEmu.class)
public class AIEmu extends Mob {
    
    protected AIEmu(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }
    EntityEmu emu = (EntityEmu)(Object)this;

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void EmuGoals(CallbackInfo ci){
        Predicate<LivingEntity> emuHoldEgg = (livingEntity) -> {
            return livingEntity.isHolding( Ingredient.of(AMItemRegistry.EMU_EGG.get()));
        };
        Predicate<LivingEntity> emuTrifle = (livingEntity) -> {
            return livingEntity.isHolding(Ingredient.of(AInteractionTagRegistry.EMU_TRIGGER));
        };

        if (AInteractionConfig.emueggattack) {
            this.targetSelector.addGoal(8, new EntityAINearestTarget3D<>(this, LivingEntity.class, 0, true, false,
                    emuHoldEgg) {
                public boolean canUse() {
                    return super.canUse() && !emu.isBaby();
                }
            });
        }
        if (AInteractionConfig.emurangedattack){
            this.targetSelector.addGoal(8, new EntityAINearestTarget3D<>(this, LivingEntity.class, 70, true, false,
                    emuTrifle){
                public boolean canUse() {
                    return super.canUse() && !emu.isBaby();
                }
            });
        }

        this.targetSelector.addGoal(4, new EntityAINearestTarget3D<>(this, LivingEntity.class, 55, true, true, AMEntityRegistry.buildPredicateFromTag(AInteractionTagRegistry.EMU_KILL)));
        if(AInteractionConfig.emuscuffle){
            this.targetSelector.addGoal(8, new EntityAINearestTarget3D<>(this, EntityEmu.class, 1000, false, true, null) {
                public boolean canUse() {
                    return !isLeashed() && super.canUse() && level().isDay() && !isBaby();
                }
            });
        }
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void AlexInteraction$tick(CallbackInfo ci) {
        if (AInteractionConfig.emuscuffle) {
            LivingEntity livingEntity = getTarget();
            if ((random.nextDouble() < 0.02 || emu.isLeashed() || emu.isInLove()) && livingEntity instanceof EntityEmu )
                setTarget(null);
        }
    }


}
