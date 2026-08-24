package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventSprintingAbility extends Ability {

    public static final MapCodec<PreventSprintingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventSprintingAbility::new));

    public PreventSprintingAbility(AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_SPRINTING.get();
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity instanceof Player player && player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    public static class Serializer extends AbilitySerializer<PreventSprintingAbility> {

        @Override
        public MapCodec<PreventSprintingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventSprintingAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the player from sprinting.")
                    .addExampleObject(new PreventSprintingAbility(AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}