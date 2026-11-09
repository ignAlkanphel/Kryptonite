package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class AllowEndermanStareAbility extends Ability {

    public static final MapCodec<AllowEndermanStareAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(ab -> ab.entityCondition),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, AllowEndermanStareAbility::new));

    public final Condition entityCondition;

    public AllowEndermanStareAbility(Condition entityCondition, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityCondition = entityCondition;
    }

    public boolean appliesTo(Entity source, Entity target, AbilityInstance<?> abilityInstance) {
        return this.entityCondition.test(DataContext.forAffected(target, source, abilityInstance));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ALLOW_ENDERMAN_STARE.get();
    }

    public static class Serializer extends AbilitySerializer<AllowEndermanStareAbility> {

        @Override
        public MapCodec<AllowEndermanStareAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, AllowEndermanStareAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows looking into the eyes of endermen without angering them. For this ability, the \"source\" is the entity looking at the enderman & the \"target\" is the enderman.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the enderman must meet to be affected.")
                    .addExampleObject(new AllowEndermanStareAbility(TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}