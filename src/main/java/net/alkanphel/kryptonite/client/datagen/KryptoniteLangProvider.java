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
        this.add(Kryptonite.MOD_ID + ".ability." + Objects.requireNonNull(id).getNamespace() + "." + id.getPath(), name);
    }

    public static class English extends KryptoniteLangProvider {

        public English(PackOutput output) {
            super(output, Kryptonite.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            // Abilities
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_BEING_USED, "Action On Being Used");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_BLOCK_BREAK, "Action On Block Break");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_BLOCK_PLACE, "Action On Block Place");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_BLOCK_USE, "Action On Block Use");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_CRITICAL_HIT, "Action On Critical Hit");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_DEATH, "Action On Death");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ENTITY_COLLISION, "Action On Entity Collision");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ENTITY_USE, "Action On Entity Use");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_FARMLAND_TRAMPLE, "Action On Farmland Trample");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_HIT, "Action On Hit");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_DROP, "Action On Item Drop");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_FISHED, "Action On Item Fished");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_PICKUP, "Action On Item Pickup");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_SWAP, "Action On Item Swap");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_ITEM_USE, "Action On Item Use");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_JUMP, "Action On Jump");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_LAND, "Action On Land");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_MOUNT, "Action On Mount");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_TAME, "Action On Land");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_ON_TOTEM_USE, "Action On Totem Use");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_WHEN_DAMAGE_TAKEN, "Action When Damage Taken");
            this.addAbility(KryptoniteAbilitySerializers.ACTION_WHEN_HIT, "Action When Hit");
            this.addAbility(KryptoniteAbilitySerializers.ALLOW_ENDERMAN_STARE, "Allow Enderman Stare");
            this.addAbility(KryptoniteAbilitySerializers.DYNAMIC_LIGHTS, "Dynamic Lights");
            this.addAbility(KryptoniteAbilitySerializers.GLOWING, "Glowing");
            this.addAbility(KryptoniteAbilitySerializers.IMMEDIATE_RESPAWN, "Immediate Respawn");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_BLOCK_DESTROY_SPEED, "Modify Block Destroy Speed");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_BLOCK_HARVEST, "Modify Block Harvest");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_DAMAGE_DEALT, "Modify Damage Dealt");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_DAMAGE_TAKEN, "Modify Damage Taken");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_DAMAGE_TINT, "Modify Damage Tint");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_EFFECTS, "Modify Effects");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_FOG_TYPE, "Modify Fog Type");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_FRICTION, "Modify Friction");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_HEALING, "Modify Healing");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_INVULNERABILITY_TICKS, "Modify Invulnerability Ticks");
            this.addAbility(KryptoniteAbilitySerializers.MODIFY_KNOCKBACK, "Modify Knockback");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_BEING_USED, "Prevent Being Used");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_BLOCK_SELECTION, "Prevent Block Selection");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_BLOCK_USE, "Prevent Block Use");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_CRITICAL_HIT, "Prevent Critical Hit");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_DAMAGE, "Prevent Damage");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_DEATH, "Prevent Death");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_EFFECTS, "Prevent Effects");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ENTITY_COLLISION, "Prevent Entity Collision");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ENTITY_RENDER, "Prevent Entity Render");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ENTITY_SELECTION, "Prevent Entity Selection");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ENTITY_USE, "Prevent Entity Use");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_FARMLAND_TRAMPLE, "Prevent Farmland Trample");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_GAME_EVENT, "Prevent Game Event");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_GLIDING, "Prevent Gliding");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_HEALING, "Prevent Healing");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ITEM_PICKUP, "Prevent Item Pickup");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_ITEM_USE, "Prevent Item Use");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_MOB_AGGRO, "Prevent Mob Aggro");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_SLEEPING, "Prevent Sleeping");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_SPRINTING, "Prevent Sprinting");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_TELEPORT, "Prevent Teleport");
            this.addAbility(KryptoniteAbilitySerializers.PREVENT_TOTEM_USE, "Prevent Totem Use");
            this.addAbility(KryptoniteAbilitySerializers.PROJECTILE_ACCURACY, "Projectile Accuracy");
            this.addAbility(KryptoniteAbilitySerializers.PROJECTILE_IMPACT, "Projectile Impact");
            this.addAbility(KryptoniteAbilitySerializers.SHAKING, "Shaking");
            this.addAbility(KryptoniteAbilitySerializers.STEP_DOWN, "Step Down");

            // Datapack
        }
    }

}