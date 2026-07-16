package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;
import java.util.Optional;

public class ModifyFogTypeAbility extends Ability {
    public static final Codec<FogType> FOGTYPE_CODEC = Codec.STRING.xmap(s -> FogType.valueOf(s.toUpperCase()), f -> f.name().toLowerCase());

    public static final MapCodec<ModifyFogTypeAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FOGTYPE_CODEC.optionalFieldOf("from").forGetter(ab -> ab.from),
            FOGTYPE_CODEC.fieldOf("to").forGetter(ab -> ab.to),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyFogTypeAbility::new));

    public final Optional<FogType> from;
    public final FogType to;

    public ModifyFogTypeAbility(Optional<FogType> from, FogType to, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.from = from;
        this.to = to;
    }

    public static Optional<FogType> tryReplace(LivingEntity entity, FogType original) {
        return AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.MODIFY_FOG_TYPE.get()).stream()
                .map(AbilityInstance::getAbility)
                .filter(ability -> ability.from.isEmpty() || ability.from.get() == original)
                .map(ability -> ability.to)
                .findFirst();
    }

    @Override
    public AbilitySerializer<ModifyFogTypeAbility> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_FOG_TYPE.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyFogTypeAbility> {

        @Override
        public MapCodec<ModifyFogTypeAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyFogTypeAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows you to view the world as if inside a fluid.")
                    .addOptional("from", KryptoniteDocumented.TYPE_FOG_TYPE, "If specified, only this submersion type will be modified.")
                    .add("to", KryptoniteDocumented.TYPE_FOG_TYPE, "Which submersion type to change to.")
                    .addExampleObject(new ModifyFogTypeAbility(Optional.empty(), FogType.WATER, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyFogTypeAbility(Optional.of(FogType.LAVA), FogType.WATER, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}