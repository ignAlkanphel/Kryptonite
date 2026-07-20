package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventBlockSelectionAbility extends Ability {

    public static final MapCodec<PreventBlockSelectionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(ab -> ab.blockConditions),
            Value.CODEC.optionalFieldOf("visual_only", new StaticValue(false)).forGetter(a -> a.visualOnly),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventBlockSelectionAbility::new));

    public final List<BlockCondition> blockConditions;
    public final Value visualOnly;

    public PreventBlockSelectionAbility(List<BlockCondition> blockConditions, Value visualOnly, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.blockConditions = blockConditions;
        this.visualOnly = visualOnly;
    }

    public boolean doesPrevent(Level level, BlockPos pos) {
        if (!blockConditions.isEmpty() && !BlockCondition.checkConditions(blockConditions, level, pos)) {
            return false;
        }

        return true;
    }

    public static boolean doesPrevent(LivingEntity living, BlockPos pos) {
        if (living == null) return false;
        return AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_BLOCK_SELECTION.get()).stream().anyMatch(instance -> !instance.getAbility().visualOnly.getAsBoolean(DataContext.forAbility(living, instance)) && instance.getAbility().doesPrevent(living.level(), pos));
    }

    public static boolean doesPreventOutline(Player player, BlockPos pos) {
        if (player == null) return false;
        return AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_BLOCK_SELECTION.get()).stream().anyMatch(instance -> instance.getAbility().doesPrevent(player.level(), pos));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_BLOCK_SELECTION.get();
    }

    public static class Serializer extends AbilitySerializer<PreventBlockSelectionAbility> {

        @Override
        public MapCodec<PreventBlockSelectionAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventBlockSelectionAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the selection of blocks. The player won't be able to mine or interact with said blocks; meaning actions will pass through to whatever is behind said blocks.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only prevents selection of blocks that fulfill these block conditions.")
                    .addOptional("visual_only", TYPE_VALUE, "If true, the ability will be purely visual.", false)
                    .addExampleObject(new PreventBlockSelectionAbility(List.of(), new StaticValue(false), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventBlockSelectionAbility(List.of(new BlockBlockCondition(provider.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.LEAVES))), new StaticValue(false), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}