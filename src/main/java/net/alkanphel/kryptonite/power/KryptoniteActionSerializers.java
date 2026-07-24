package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteActionSerializers {

    public static final DeferredRegister<ActionSerializer<?>> ACTION_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.ACTION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ActionSerializer<?>, AbilityCooldownAction.Serializer> ABILITY_COOLDOWN = ACTION_SERIALIZERS.register("ability_cooldown", AbilityCooldownAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, AddVelocityAction.Serializer> ADD_VELOCITY = ACTION_SERIALIZERS.register("add_velocity", AddVelocityAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, AreaOfEffectAction.Serializer> AREA_OF_EFFECT = ACTION_SERIALIZERS.register("area_of_effect", AreaOfEffectAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, BlockActionAtAction.Serializer> BLOCK_ACTION_AT = ACTION_SERIALIZERS.register("block_action_at", BlockActionAtAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, DisplayItemActivationAction.Serializer> DISPLAY_ITEM_ACTIVATION = ACTION_SERIALIZERS.register("display_item_activation", DisplayItemActivationAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, EquipmentAction.Serializer> EQUIPMENT = ACTION_SERIALIZERS.register("equipment", EquipmentAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, ExplodeAction.Serializer> EXPLODE = ACTION_SERIALIZERS.register("explode", ExplodeAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, ExtinguishAction.Serializer> EXTINGUISH = ACTION_SERIALIZERS.register("extinguish", ExtinguishAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, GameEventAction.Serializer> GAME_EVENT = ACTION_SERIALIZERS.register("game_event", GameEventAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, HealAction.Serializer> HEAL = ACTION_SERIALIZERS.register("heal", HealAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, PocketAnvilAction.Serializer> POCKET_ANVIL = ACTION_SERIALIZERS.register("pocket_anvil", PocketAnvilAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, PocketCraftingTableAction.Serializer> POCKET_CRAFTING_TABLE = ACTION_SERIALIZERS.register("pocket_crafting_table", PocketCraftingTableAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, PocketEnchantingTableAction.Serializer> POCKET_ENCHANTING_TABLE = ACTION_SERIALIZERS.register("pocket_enchanting_table", PocketEnchantingTableAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, PocketEnderChestAction.Serializer> POCKET_ENDER_CHEST = ACTION_SERIALIZERS.register("pocket_ender_chest", PocketEnderChestAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, RaycastAction.Serializer> RAYCAST = ACTION_SERIALIZERS.register("raycast", RaycastAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, RespawnResetAction.Serializer> RESPAWN_RESET = ACTION_SERIALIZERS.register("respawn_reset", RespawnResetAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, RespawnTeleportAction.Serializer> RESPAWN_TELEPORT = ACTION_SERIALIZERS.register("respawn_teleport", RespawnTeleportAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetFallDistanceAction.Serializer> SET_FALL_DISTANCE = ACTION_SERIALIZERS.register("set_fall_distance", SetFallDistanceAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetOnFireAction.Serializer> SET_ON_FIRE = ACTION_SERIALIZERS.register("set_on_fire", SetOnFireAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, VehicleDismountAction.Serializer> VEHICLE_DISMOUNT = ACTION_SERIALIZERS.register("vehicle_dismount", VehicleDismountAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, VehiclePassengerActionAction.Serializer> VEHICLE_PASSENGER_ACTION = ACTION_SERIALIZERS.register("vehicle_passenger_action", VehiclePassengerActionAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, VehicleRidingActionAction.Serializer> VEHICLE_RIDING_ACTION = ACTION_SERIALIZERS.register("vehicle_riding_action", VehicleRidingActionAction.Serializer::new);
}