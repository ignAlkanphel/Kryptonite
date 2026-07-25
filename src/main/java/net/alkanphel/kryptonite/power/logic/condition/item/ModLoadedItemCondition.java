package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.neoforged.fml.ModList;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Objects;

public final class ModLoadedItemCondition implements ItemCondition {

    public static final MapCodec<ModLoadedItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("mod_id").forGetter(ModLoadedItemCondition::modId)
    ).apply(instance, ModLoadedItemCondition::new));

    private final String modId;
    private final boolean loaded;

    public ModLoadedItemCondition(String modId) {
        this.modId = modId;
        this.loaded = ModList.get().isLoaded(modId);
    }

    @Override
    public boolean test(ItemConditionContext context) {
        return this.loaded;
    }

    @Override
    public ItemConditionSerializer<?> getSerializer() {
        return ItemConditionSerializers.MOD_LOADED.get();
    }

    public String modId() {
        return this.modId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ModLoadedItemCondition that = (ModLoadedItemCondition) obj;
        return Objects.equals(this.modId, that.modId);
    }

    public static class Serializer extends ItemConditionSerializer<ModLoadedItemCondition> {

        @Override
        public MapCodec<ModLoadedItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, ModLoadedItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Mod Loaded")
                    .setDescription("Checks if the specified mod was loaded into the game.")
                    .add("mod_id", TYPE_STRING, "The mod id that is being looked for.")
                    .addExampleObject(new ModLoadedItemCondition("example_mod_id"));
        }
    }

}