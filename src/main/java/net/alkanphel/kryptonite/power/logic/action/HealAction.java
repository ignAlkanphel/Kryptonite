package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class HealAction extends Action {

    public static final MapCodec<HealAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("amount").forGetter(a -> a.amount)
    ).apply(instance, HealAction::new));

    public final Value amount;

    public HealAction(Value amount) {
        this.amount = amount;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getLivingEntity();
        if (entity == null) return false;

        entity.heal(Math.max(0, amount.getAsFloat(context)));
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.HEAL.get();
    }

    public static class Serializer extends ActionSerializer<HealAction> {

        @Override
        public MapCodec<HealAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, HealAction> builder, HolderLookup.Provider provider) {
            builder.setName("Heal")
                    .setDescription("Heals the entity.")
                    .addOptional("amount", KryptoniteSettingType.floatValueRange(0, Float.MAX_VALUE), "The amount of health to heal.")
                    .addExampleObject(new HealAction(new StaticValue(2)));
        }
    }

}