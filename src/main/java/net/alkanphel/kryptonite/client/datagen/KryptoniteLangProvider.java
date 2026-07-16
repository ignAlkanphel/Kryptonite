package net.alkanphel.kryptonite.client.datagen;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.registry.PalladiumRegistries;

import java.util.Objects;

public abstract class KryptoniteLangProvider extends LanguageProvider {
    protected final String modid;

    public KryptoniteLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.modid = modid;
    }

    public void addAbility(Holder<? extends AbilitySerializer<?>> key, String name) {
        this.addAbilityTemplate(key.value(), name);
    }

    public void addAbilityTemplate(AbilitySerializer<?> key, String name) {
        var id = PalladiumRegistries.ABILITY_SERIALIZER.getKey(key);
        this.add("ability." + Objects.requireNonNull(id).getNamespace() + "." + id.getPath(), name);
    }

    public void addConfigEntry(ModConfigSpec.ConfigValue<?> configSpec, String name) {
        var key = configSpec.getSpec().getTranslationKey();
        if (key != null) this.add(key, name);
    }

    public static class English extends KryptoniteLangProvider {

        public English(PackOutput output) {
            super(output, Kryptonite.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            // Abilities
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_DEATH, "Action On Death");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_FARMLAND_TRAMPLE, "Action On Farmland Trample");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_FISHED, "Action On Item Fished");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_JUMP, "Action On Jump");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_LAND, "Action On Land");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_TOTEM_USE, "Action On Totem Use");
            this.addAbility(KryptoniteAbilitySerializers.DYNAMIC_LIGHTS, "Dynamic Lights");
            this.addAbility(KryptoniteAbilitySerializers.IMMEDIATE_RESPAWN, "Immediate Respawn");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_DAMAGE, "Prevent Damage");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_DEATH, "Prevent Death");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_FARMLAND_TRAMPLE, "Prevent Farmland Trample");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_GLIDING, "Prevent Gliding");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_HEALING, "Prevent Healing");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_SPRINTING, "Prevent Sprinting");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_TOTEM_USE, "Prevent Totem Use");

            // Datapack
        }
    }

}