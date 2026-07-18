package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ModifyHealingAbility extends Ability {

    public static final MapCodec<ModifyHealingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("modifiers", List.of()).forGetter(a -> a.modifiers),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyHealingAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> modifiers;

    public ModifyHealingAbility(List<KryptoniteModifiers.ValueModifier> modifiers, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.modifiers = modifiers;
    }

    public float applyModifiers(float heal, AbilityInstance<?> instance, LivingEntity entity) {
        return Math.max(0F, KryptoniteModifiers.applyModifiers(heal, modifiers, DataContext.forAbility(entity, instance)));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_HEALING.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyHealingAbility> {

        @Override
        public MapCodec<ModifyHealingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyHealingAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Healing")
                    .setDescription("Modifies the amount of health you get from all sources of healing (e.g natural regen, instant health effect, regeneration effect)")
                    .add("modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers to apply to the healing.")
                    .addExampleObject(new ModifyHealingAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-0.5), KryptoniteModifiers.Operation.MULTIPLY_TOTAL_ADDITIVE)), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}