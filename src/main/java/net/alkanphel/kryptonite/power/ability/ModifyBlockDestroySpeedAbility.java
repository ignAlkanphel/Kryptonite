package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;

public class ModifyBlockDestroySpeedAbility extends Ability {

    public static final MapCodec<ModifyBlockDestroySpeedAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("modifiers", List.of()).forGetter(a -> a.modifiers),
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("hardness_modifiers", List.of()).forGetter(a -> a.hardnessModifiers),
            BlockFilter.CODEC.optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(a -> a.blockFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyBlockDestroySpeedAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> modifiers, hardnessModifiers;
    public final BlockFilter blockFilter;

    public ModifyBlockDestroySpeedAbility(List<KryptoniteModifiers.ValueModifier> modifiers, List<KryptoniteModifiers.ValueModifier> hardnessModifiers, BlockFilter blockFilter, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.modifiers = modifiers;
        this.hardnessModifiers = hardnessModifiers;
        this.blockFilter = blockFilter;
    }

    public boolean doesApply(LivingEntity entity, BlockPos pos) {
        return blockFilter.test(entity.level(), pos);
    }

    public float applySpeedModifiers(float value, AbilityInstance<?> instance, LivingEntity entity) {
        if (modifiers.isEmpty()) return value;

        return KryptoniteModifiers.applyModifiers(value, modifiers, DataContext.forAbility(entity, instance));
    }

    public float applyHardnessModifiers(float value, AbilityInstance<?> instance, LivingEntity entity) {
        if (hardnessModifiers.isEmpty()) return value;

        return KryptoniteModifiers.applyModifiers(value, hardnessModifiers, DataContext.forAbility(entity, instance));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_BLOCK_DESTROY_SPEED.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyBlockDestroySpeedAbility> {

        @Override
        public MapCodec<ModifyBlockDestroySpeedAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyBlockDestroySpeedAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Modifies how fast the player destroys blocks.")
                    .addOptional("modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "Modifier(s) applied to the destroy speed of the block.")
                    .addOptional("hardness_modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "Modifier(s) applied to the effective destroy speed value of the block while calculating the block's destroy speed.")
                    .addOptional("block_filters", BlockFilter.documentation(provider), "Block filter(s) the blocks must pass for the modifiers to apply.")
                    .addExampleObject(new ModifyBlockDestroySpeedAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0.5), KryptoniteModifiers.Operation.MULTIPLY_BASE_ADDITIVE)), List.of(), BlockFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyBlockDestroySpeedAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0.35), KryptoniteModifiers.Operation.MULTIPLY_BASE_ADDITIVE)), List.of(), BlockFilter.ofBlock((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("stone"))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyBlockDestroySpeedAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-0.9), KryptoniteModifiers.Operation.MULTIPLY_BASE_ADDITIVE)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-1.0), KryptoniteModifiers.Operation.MAX_TOTAL), new KryptoniteModifiers.ValueModifier(new StaticValue(0.1), KryptoniteModifiers.Operation.MIN_TOTAL)), BlockFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}