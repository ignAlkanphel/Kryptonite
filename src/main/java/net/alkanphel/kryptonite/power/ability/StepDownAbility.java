package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class StepDownAbility extends Ability {

    public static final MapCodec<StepDownAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FloatValueHolder.CODEC.fieldOf("fall_speed").forGetter(ab -> ab.fallSpeed),
            FloatValueHolder.CODEC.fieldOf("fall_distance").forGetter(ab -> ab.fallDistance),
            BooleanValueHolder.CODEC.optionalFieldOf("safety_checks", BooleanValueHolder.TRUE).forGetter(a -> a.safetyChecks),
            BooleanValueHolder.CODEC.optionalFieldOf("allow_vehicles", BooleanValueHolder.FALSE).forGetter(a -> a.allowVehicles),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, StepDownAbility::new));

    public final FloatValueHolder fallSpeed;
    public final FloatValueHolder fallDistance;
    public final BooleanValueHolder safetyChecks;
    public final BooleanValueHolder allowVehicles;

    public StepDownAbility(FloatValueHolder fallSpeed, FloatValueHolder fallDistance, BooleanValueHolder safetyChecks, BooleanValueHolder allowVehicles, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.fallSpeed = fallSpeed;
        this.fallDistance = fallDistance;
        this.safetyChecks = safetyChecks;
        this.allowVehicles = allowVehicles;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.STEP_DOWN.get();
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled) {
            DataContext context = DataContext.forAbility(entity, abilityInstance);

            LivingEntity target = entity;
            if (this.allowVehicles.get(context) && entity.getVehicle() instanceof LivingEntity livingVehicle) {
                target = livingVehicle;
            }

            if (this.safetyChecks.get(context)) {
                if (!target.onGround() || target.isSwimming() || target.isSpectator() || target.isFallFlying()) {
                    return false;
                }
            }

            if (target.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4) return false;

            float fallDistance = this.fallDistance.get(DataContext.forAbility(entity, abilityInstance));

            boolean groundBelow = !target.level().noCollision(target.getBoundingBox().move(0.0, -(fallDistance + 0.01F), 0.0));
            boolean groundImmediatelyBelow = !target.level().noCollision(target.getBoundingBox().move(0.0, -0.01F, 0.0));

            float fallSpeed = this.fallSpeed.get(DataContext.forAbility(entity, abilityInstance));

            if (groundBelow && !groundImmediatelyBelow) {
                target.setDeltaMovement(target.getDeltaMovement().x, -fallSpeed, target.getDeltaMovement().z);
                target.hurtMarked = true;
                target.resetFallDistance();
            }
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    public static class Serializer extends AbilitySerializer<StepDownAbility> {

        @Override
        public MapCodec<StepDownAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, StepDownAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows for stepping down blocks fast.")
                    .add("fall_speed", TYPE_FLOAT_VALUE, "Speed at which you fall off the block.")
                    .add("fall_distance", TYPE_FLOAT_VALUE, "Fall distance it will enable at.")
                    .addOptional("safety_checks", TYPE_BOOLEAN_VALUE, "If false, checks for if the ability SHOULD be enabled such as the entity being in spectator, fall flying, swimming, or on the ground will be disabled.", true)
                    .addOptional("allow_vehicles", TYPE_BOOLEAN_VALUE, "If true, it will work for living vehicles.", false)
                    .addExampleObject(new StepDownAbility(FloatValueHolder.ONE, FloatValueHolder.ONE, BooleanValueHolder.TRUE, BooleanValueHolder.FALSE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}