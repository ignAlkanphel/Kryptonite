package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.FloatDataAttachmentValue;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ModifyDamageTintAbility extends Ability {

    public static final MapCodec<ModifyDamageTintAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.RGBAValue.CODEC.optionalFieldOf("color", KryptoniteCodecs.RGBAValue.WHITE).forGetter(ab -> ab.color),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyDamageTintAbility::new));

    public final KryptoniteCodecs.RGBAValue color;

    public ModifyDamageTintAbility(KryptoniteCodecs.RGBAValue color, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.color = color;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_DAMAGE_TINT.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyDamageTintAbility> {

        @Override
        public MapCodec<ModifyDamageTintAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyDamageTintAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Modifies the damage tint color of the entity that has this ability.")
                    .addOptional("color", KryptoniteDocumented.TYPE_RGB_VALUE, "The RGB color values.", new KryptoniteCodecs.RGBAValue(new StaticValue(1.0D), new StaticValue(1.0D), new StaticValue(1.0D), new StaticValue(1.0D)))
                    .addExampleObject(new ModifyDamageTintAbility(new KryptoniteCodecs.RGBAValue(new FloatDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "tint_red")), 1.0F, ""), new FloatDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "tint_green")), 1.0F, ""), new FloatDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "tint_blue")), 1.0F, ""), new FloatDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("test", "tint_alpha")), 1.0F, "")), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}