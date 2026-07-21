package net.alkanphel.kryptonite.power.logic.condition.item;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Dynamic;
import net.alkanphel.kryptonite.mixin.common.DelayedDataComponentMapAccessor;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.component.DelayedDataComponentMap;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Map;
import java.util.Objects;

public record ComponentItemCondition(DelayedDataComponentMap components) implements ItemCondition {

    public static final MapCodec<ComponentItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DelayedDataComponentMap.CODEC.fieldOf("components").forGetter(ComponentItemCondition::components)
    ).apply(instance, ComponentItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentItemCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(DelayedDataComponentMap.CODEC), ComponentItemCondition::components,
            ComponentItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();

        Map<DataComponentType<?>, Dynamic<?>> values = ((DelayedDataComponentMapAccessor) components).kryptonite$getValues();

        var ops = context.level()
                .registryAccess()
                .createSerializationContext(JsonOps.INSTANCE);

        for (Map.Entry<DataComponentType<?>, Dynamic<?>> entry : values.entrySet()) {
            DataComponentType type = entry.getKey();

            if (!stack.has(type)) return false;

            Dynamic<?> dynamic = entry.getValue();

            if (dynamic.getValue() instanceof JsonObject jsonObject && jsonObject.isEmpty()) {
                continue;
            }

            Object expected = type.codecOrThrow().parse(ops, dynamic.getValue()).getOrThrow();

            if (!Objects.equals(expected, stack.get(type))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemConditionSerializer<ComponentItemCondition> getSerializer() {
        return ItemConditionSerializers.COMPONENT.get();
    }

    public static class Serializer extends ItemConditionSerializer<ComponentItemCondition> {

        @Override
        public MapCodec<ComponentItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, ComponentItemCondition> builder, HolderLookup.Provider provider) {
            JsonObject root = new JsonObject();
            JsonObject components = new JsonObject();
            components.addProperty("minecraft:rarity", "epic");
            components.add("minecraft:food", new JsonObject());
            JsonObject damageResistant = new JsonObject();
            damageResistant.addProperty("types", "#minecraft:is_fire");
            components.add("minecraft:damage_resistant", damageResistant);
            root.add("components", components);

            builder.setName("Component")
                    .setDescription("Checks if an item has the specified (data) components. Leave empty (\"minecraft:damage_resistant\": {} to check if the component just exists on the item.")
                    .add("components", KryptoniteDocumented.TYPE_DATA_COMPONENT, "The components to check.")
                    .addExampleObject(new ComponentItemCondition(DelayedDataComponentMap.EMPTY))
                    .addExampleJson(root);
        }
    }

}