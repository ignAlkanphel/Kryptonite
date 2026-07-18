package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ModifyFrictionAbility extends Ability {

    public static final MapCodec<ModifyFrictionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("modifiers", List.of()).forGetter(a -> a.modifiers),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyFrictionAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> modifiers;
    public final List<BlockCondition> blockConditions;

    public ModifyFrictionAbility(List<KryptoniteModifiers.ValueModifier> modifiers, List<BlockCondition> blockConditions, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.modifiers = modifiers;
        this.blockConditions = blockConditions;
    }

    public boolean doesApply(LivingEntity entity, BlockPos pos) {
        return blockConditions.isEmpty() || BlockCondition.checkConditions(blockConditions, entity.level(), pos);
    }

    public float applyModifiers(float friction, AbilityInstance<?> instance, LivingEntity entity) {
        if (modifiers.isEmpty()) {
            return friction;
        }

        return KryptoniteModifiers.applyModifiers(friction, modifiers, DataContext.forAbility(entity, instance));
    }

    public static float modifyFriction(LivingEntity entity, BlockPos pos, float originalFriction) {
        float friction = originalFriction;
        for (AbilityInstance<ModifyFrictionAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.MODIFY_FRICTION.get())) {
            if (instance.getAbility().doesApply(entity, pos)) {
                friction = instance.getAbility().applyModifiers(friction, instance, entity);
            }
        }

        return friction;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_FRICTION.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyFrictionAbility> {

        @Override
        public MapCodec<ModifyFrictionAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyFrictionAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Friction")
                    .setDescription("Modifies the friction multiplier of blocks the entity moves on.")
                    .add("modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers to apply to the friction.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only applies when these block conditions are fulfilled.")
                    .addExampleObject(new ModifyFrictionAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0.75), KryptoniteModifiers.Operation.MULTIPLY_BASE_ADDITIVE)), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}