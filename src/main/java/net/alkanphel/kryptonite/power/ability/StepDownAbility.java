package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class StepDownAbility extends Ability {

    public static final MapCodec<StepDownAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("fall_speed").forGetter(a -> a.fallSpeed),
            Value.CODEC.fieldOf("fall_distance").forGetter(a -> a.fallDistance),
            Value.CODEC.optionalFieldOf("safety_checks", new StaticValue(true)).forGetter(a -> a.safetyChecks),
            Value.CODEC.optionalFieldOf("allow_vehicles", new StaticValue(false)).forGetter(a -> a.allowVehicles),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, StepDownAbility::new));

    public final Value fallSpeed;
    public final Value fallDistance;
    public final Value safetyChecks;
    public final Value allowVehicles;

    public StepDownAbility(Value fallSpeed, Value fallDistance, Value safetyChecks, Value allowVehicles, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
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
            if (this.allowVehicles.getAsBoolean(context) && entity.getVehicle() instanceof LivingEntity livingVehicle) {
                target = livingVehicle;
            }

            if (this.safetyChecks.getAsBoolean(context)) {
                if (!target.onGround() || target.isSwimming() || target.isSpectator() || target.isFallFlying()) {
                    return false;
                }
            }

            if (target.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4) return false;

            float fallDistance = this.fallDistance.getAsFloat(context);

            boolean groundBelow = !target.level().noCollision(target.getBoundingBox().move(0.0, -(fallDistance + 0.01F), 0.0));
            boolean groundImmediatelyBelow = !target.level().noCollision(target.getBoundingBox().move(0.0, -0.01F, 0.0));

            float fallSpeed = this.fallSpeed.getAsFloat(context);

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
                    .add("fall_speed", TYPE_VALUE, "Speed at which you fall off the block.")
                    .add("fall_distance", TYPE_VALUE, "Fall distance it will enable at.")
                    .addOptional("safety_checks", TYPE_VALUE, "If false, checks for if the ability SHOULD be enabled such as the entity being in spectator, fall flying, swimming, or on the ground will be disabled.", true)
                    .addOptional("allow_vehicles", TYPE_VALUE, "If true, it will work for living vehicles.", false)
                    .addExampleObject(new StepDownAbility(new StaticValue(1), new StaticValue(1), new StaticValue(true), new StaticValue(false), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}