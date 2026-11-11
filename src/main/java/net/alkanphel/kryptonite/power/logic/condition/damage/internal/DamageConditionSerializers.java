package net.alkanphel.kryptonite.power.logic.condition.damage.internal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.damage.*;
import net.alkanphel.kryptonite.power.logic.condition.damage.meta.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;

public class DamageConditionSerializers {

    public static final DeferredRegister<DamageConditionSerializer<?>> DAMAGE_CONDITION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.DAMAGE_CONDITION_SERIALIZER, Kryptonite.MOD_ID);
    public static final Codec<DamageConditionSerializer<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(DamageConditionSerializers::read, KryptoniteRegistries.DAMAGE_CONDITION_SERIALIZER::getKey);

    public static final DeferredHolder<DamageConditionSerializer<?>, AndDamageCondition.Serializer> AND = DAMAGE_CONDITION_SERIALIZERS.register("and", AndDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, NotDamageCondition.Serializer> NOT = DAMAGE_CONDITION_SERIALIZERS.register("not", NotDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, OrDamageCondition.Serializer> OR = DAMAGE_CONDITION_SERIALIZERS.register("or", OrDamageCondition.Serializer::new);

    static {
        DAMAGE_CONDITION_SERIALIZERS.addAlias(Identifier.fromNamespaceAndPath(Palladium.MOD_ID, "and"), Kryptonite.id("and"));
        DAMAGE_CONDITION_SERIALIZERS.addAlias(Identifier.fromNamespaceAndPath(Palladium.MOD_ID, "or"), Kryptonite.id("or"));
        DAMAGE_CONDITION_SERIALIZERS.addAlias(Identifier.fromNamespaceAndPath(Palladium.MOD_ID, "not"), Kryptonite.id("not"));
    }

    public static final DeferredHolder<DamageConditionSerializer<?>, AmountDamageCondition.Serializer> AMOUNT = DAMAGE_CONDITION_SERIALIZERS.register("amount", AmountDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, AttackerDamageCondition.Serializer> ATTACKER = DAMAGE_CONDITION_SERIALIZERS.register("attacker", AttackerDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, DamageTypeDamageCondition.Serializer> DAMAGE_TYPE = DAMAGE_CONDITION_SERIALIZERS.register("damage_type", DamageTypeDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, ProjectileDamageCondition.Serializer> PROJECTILE = DAMAGE_CONDITION_SERIALIZERS.register("projectile", ProjectileDamageCondition.Serializer::new);

    private static DataResult<DamageConditionSerializer<?>> read(Identifier id) {
        var serializer = KryptoniteRegistries.DAMAGE_CONDITION_SERIALIZER.getValue(id);
        return serializer != null ? DataResult.success(serializer) : DataResult.error(() -> "Unknown condition type: " + id);
    }

}