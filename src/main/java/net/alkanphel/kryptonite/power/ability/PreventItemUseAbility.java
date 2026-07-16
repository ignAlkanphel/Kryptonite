package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventItemUseAbility extends Ability {

    public static final MapCodec<PreventItemUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventItemUseAbility::new));

    private final List<ItemCondition> itemConditions;

    public PreventItemUseAbility(List<ItemCondition> itemConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.itemConditions = itemConditions;
    }

    public boolean doesPrevent(Entity holder, ItemStack stack) {
        if (!itemConditions.isEmpty() && !ItemCondition.checkConditions(itemConditions, holder.level(), stack)) {
            return false;
        }

        return true;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_ITEM_USE.get();
    }

    public static class Serializer extends AbilitySerializer<PreventItemUseAbility> {

        @Override
        public MapCodec<PreventItemUseAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventItemUseAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Prevent Item Use")
                    .setDescription("Prevents the player from using items. (right-click actions such as eating food, using a shield, or placing them as blocks still work).")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, only items that fulfills these item conditions will be prevented from being used.")
                    .addExampleObject(new PreventItemUseAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}