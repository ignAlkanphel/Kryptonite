package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.util.apoli.SavedBlockPosition;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyBlockHarvestAbility extends Ability implements Prioritized, Comparable<ModifyBlockHarvestAbility> {

    public static final MapCodec<ModifyBlockHarvestAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            Codec.BOOL.optionalFieldOf("allow", true).forGetter(a -> a.allow),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyBlockHarvestAbility::new));

    public final List<BlockCondition> blockConditions;
    public final boolean allow;
    public final int priority;

    public ModifyBlockHarvestAbility(List<BlockCondition> blockConditions, boolean allow, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.blockConditions = blockConditions;
        this.allow = allow;
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull ModifyBlockHarvestAbility other) {
        int priorityResult = Integer.compare(this.getPriority(), other.getPriority());
        return priorityResult != 0
                ? priorityResult
                : Boolean.compare(this.isAllowed(), other.isAllowed());
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean isAllowed() {
        return allow;
    }

    public boolean doesApply(SavedBlockPosition savedBlockPosition) {
        if (!blockConditions.isEmpty() && !BlockCondition.checkConditions(blockConditions, savedBlockPosition)) {
            return false;
        }

        return true;
    }

    public static Optional<Boolean> resolve(LivingEntity entity, SavedBlockPosition savedBlock) {
        return AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.MODIFY_BLOCK_HARVEST.get())
                .stream()
                .map(AbilityInstance::getAbility)
                .filter(ability -> ability.doesApply(savedBlock))
                .max(ModifyBlockHarvestAbility::compareTo)
                .map(ModifyBlockHarvestAbility::isAllowed);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_BLOCK_HARVEST.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyBlockHarvestAbility> {

        @Override
        public MapCodec<ModifyBlockHarvestAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyBlockHarvestAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Block Harvest")
                    .setDescription("Modifies if a player is able to harvest (block drops) a block regardless of the tool.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only applies to blocks that fulfill these conditions.")
                    .addOptional("allow", TYPE_BOOLEAN, "If true, allows harvesting. If false, prevents it.", true)
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ModifyBlockHarvestAbility(List.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("stone")))))), true, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}