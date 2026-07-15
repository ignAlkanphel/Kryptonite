package net.alkanphel.kryptonite.power.logic.condition.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DamageConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record DamageTypeDamageCondition(HolderSet<DamageType> damageType) implements DamageCondition {

    public static final MapCodec<DamageTypeDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(DamageTypeDamageCondition::damageType)
    ).apply(instance, DamageTypeDamageCondition::new));

    @Override
    public boolean test(DamageConditionContext context) {
        return damageType.contains(context.source().typeHolder());
    }

    @Override
    public DamageConditionSerializer<DamageTypeDamageCondition> getSerializer() {
        return DamageConditionSerializers.DAMAGE_TYPE.get();
    }

    public static class Serializer extends DamageConditionSerializer<DamageTypeDamageCondition> {

        @Override
        public MapCodec<DamageTypeDamageCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DamageCondition, DamageTypeDamageCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Damage Type")
                    .setDescription("Checks whether the damage source is of a certain damage type.")
                    .addOptional("damage_type", TYPE_DAMAGE_TYPE_HOLDER_SET, "Damage types or tags to check against.")
                    .addExampleObject(new DamageTypeDamageCondition(provider.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_FALL)));
        }
    }

}