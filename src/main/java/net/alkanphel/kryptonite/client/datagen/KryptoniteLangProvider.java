package net.alkanphel.kryptonite.client.datagen;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
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

    public static class English extends KryptoniteLangProvider {

        public English(PackOutput output) {
            super(output, Kryptonite.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            // Abilities
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_BLOCK_BREAK, "Action On Block Break");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_DEATH, "Action On Death");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ENTITY_COLLISION, "Action On Entity Collision");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_FARMLAND_TRAMPLE, "Action On Farmland Trample");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_HIT, "Action On Hit");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_DROP, "Action On Item Drop");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_FISHED, "Action On Item Fished");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_JUMP, "Action On Jump");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_LAND, "Action On Land");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_MOUNT, "Action On Mount");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_TAME, "Action On Land");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_TOTEM_USE, "Action On Totem Use");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_WHEN_DAMAGE_TAKEN, "Action When Damage Taken");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_WHEN_HIT, "Action When Hit");
            this.addAbility(KryptoniteAbilitySerializers.ALLOW_ENDERMAN_STARE, "Allow Enderman Stare");
            this.addAbility(KryptoniteAbilitySerializers.DYNAMIC_LIGHTS, "Dynamic Lights");
            this.addAbility(KryptoniteAbilitySerializers.IMMEDIATE_RESPAWN, "Immediate Respawn");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_FOG_TYPE, "Modify Fog Type");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_BLOCK_SELECTION, "Prevent Block Selection");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_DAMAGE, "Prevent Damage");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_DEATH, "Prevent Death");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ENTITY_COLLISION, "Prevent Entity Collision");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ENTITY_RENDER, "Prevent Entity Render");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ENTITY_SELECTION, "Prevent Entity Selection");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_FARMLAND_TRAMPLE, "Prevent Farmland Trample");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_GAME_EVENT, "Prevent Game Event");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_GLIDING, "Prevent Gliding");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_HEALING, "Prevent Healing");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ITEM_USE, "Prevent Item Use");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_MOB_AGGRO, "Prevent Mob Aggro");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_SPRINTING, "Prevent Sprinting");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_TOTEM_USE, "Prevent Totem Use");
            this.addAbility(KryptoniteAbilitySerializers.PROJECTILE_ACCURACY, "Projectile Accuracy");
            this.addAbility(KryptoniteAbilitySerializers.PROJECTILE_IMPACT, "Projectile Impact");

            // Datapack
        }
    }

}