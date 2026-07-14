package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.compat.lambdynlights.DynLightsCompat;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.IntegerDataAttachmentValue;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.Easing;

import java.util.List;

public class DynamicLightsAbility extends Ability {

    public static final MapCodec<DynamicLightsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("luminance", new StaticValue(0)).forGetter(ab -> ab.luminance),
            Value.CODEC.optionalFieldOf("model_light", new StaticValue(0)).forGetter(ab -> ab.modelLight),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, DynamicLightsAbility::new));

    public final Value luminance;
    public final Value modelLight;

    public DynamicLightsAbility(Value luminance, Value modelLight, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.luminance = luminance;
        this.modelLight = modelLight;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.DYNAMIC_LIGHTS.get();
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled && entity.level().isClientSide()) {
            DynLightsCompat.ABILITY_HANDLER.triggerLight(entity, abilityInstance, luminance);
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    public static class Serializer extends AbilitySerializer<DynamicLightsAbility> {

        @Override
        public MapCodec<DynamicLightsAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, DynamicLightsAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows you to emit light.")
                    .addOptional("luminance", TYPE_VALUE, "Light level to emit at the location of the entity. This field requires the LambDynamicLights mod to function.", 0)
                    .addOptional("model_light", TYPE_VALUE, "Lights up the entity model without emitting light to your surroundings.", 0)
                    .addExampleObject(new DynamicLightsAbility(new StaticValue(7),new StaticValue(0), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new DynamicLightsAbility(new StaticValue(15), new StaticValue(0), new AbilityProperties().animationTimer(new AnimationTimerSetting(0, 15, AnimationTimerSetting.Behaviour.FOLLOW, Easing.INOUTCUBIC)), AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new DynamicLightsAbility(new IntegerDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "luminance")), 0, ""), new StaticValue(0), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));

        }
    }

}