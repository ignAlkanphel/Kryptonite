package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
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
            ItemFilter.CODEC.optionalFieldOf("item_filters", ItemFilter.EMPTY).forGetter(a -> a.itemFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventItemUseAbility::new));

    private final ItemFilter itemFilter;

    public PreventItemUseAbility(ItemFilter itemFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.itemFilter = itemFilter;
    }

    public boolean doesPrevent(Entity holder, ItemStack stack) {
        return itemFilter.test(holder.level(), stack);
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
            builder.setDescription("Prevents the player from using items. (right-click actions such as eating, shield use, or placing blocks still work).")
                    .addOptional("item_filters", ItemFilter.documentation(provider), "Item filter(s) the items must pass for use to be prevented.")
                    .addExampleObject(new PreventItemUseAbility(ItemFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}