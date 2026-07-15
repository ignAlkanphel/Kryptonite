package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.util.apoli.keybind.KeyBindActivity;
import net.alkanphel.kryptonite.util.apoli.keybind.KeyBindActivityManager;
import net.alkanphel.kryptonite.util.apoli.keybind.KeyBindReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public record KeyBindCondition(KeyBindReference key) implements Condition {

    public static final MapCodec<KeyBindCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KeyBindReference.CODEC.fieldOf("key").forGetter(KeyBindCondition::key)
    ).apply(instance, KeyBindCondition::new));

    @Override
    public boolean test(DataContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        KeyBindActivity activity = KeyBindActivityManager.get(player);
        if (activity == null) return false;

        String keyId = key.id();
        return key.continuous()? activity.isPressed(keyId): activity.isFreshlyPressed(keyId);
    }

    @Override
    public ConditionSerializer<KeyBindCondition> getSerializer() {
        return KryptoniteConditionSerializers.KEY_BIND.get();
    }

    public static class Serializer extends ConditionSerializer<KeyBindCondition> {

        @Override
        public MapCodec<KeyBindCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, KeyBindCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Key Bind")
                    .setDescription("Checks if the player is pressing the specified keybinding.")
                    .add("key", SettingType.simple("Keybind ID"), "The keybinding to check.")
                    .addExampleObject(new KeyBindCondition(new KeyBindReference("key.jump", true)));
        }
    }

}