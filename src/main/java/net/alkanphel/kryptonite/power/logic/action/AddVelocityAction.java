package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.alkanphel.kryptonite.util.apoli.Space;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Vector3f;

public class AddVelocityAction extends Action {

    public static final MapCodec<AddVelocityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.Vec3fValue.CODEC.optionalFieldOf("velocity", KryptoniteCodecs.Vec3fValue.ZERO).forGetter(a -> a.velocity),
            Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(a -> a.space),
            Value.CODEC.optionalFieldOf("set", new StaticValue(false)).forGetter(a -> a.set)
    ).apply(instance, AddVelocityAction::new));

    private final KryptoniteCodecs.Vec3fValue velocity;
    private final Space space;
    private final Value set;

    public AddVelocityAction(KryptoniteCodecs.Vec3fValue velocity, Space space, Value set) {
        this.velocity = velocity;
        this.space = space;
        this.set = set;
    }

    public boolean run(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        Vector3f velocityCopy = this.velocity.get(context);
        TriConsumer<Float, Float, Float> method = set.getAsBoolean(context)
                ? entity::setDeltaMovement
                : entity::push;

        space.toGlobal(velocityCopy, entity);
        method.accept(velocityCopy.x(), velocityCopy.y(), velocityCopy.z());

        entity.hurtMarked = true;
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.ADD_VELOCITY.get();
    }

    public static class Serializer extends ActionSerializer<AddVelocityAction> {

        @Override
        public MapCodec<AddVelocityAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, AddVelocityAction> builder, HolderLookup.Provider provider) {
            builder.setName("Add Velocity")
                    .setDescription("Adds or sets velocity to the entity.")
                    .addOptional("velocity", KryptoniteDocumented.TYPE_VECTOR3f_VALUE, "The amount of velocity to apply to the xyz axis.", Vec3.ZERO)
                    .addOptional("space", KryptoniteDocumented.TYPE_SPACE, "How the direction of the velocity to add/set will be calculated.", Space.WORLD)
                    .addOptional("set", TYPE_VALUE, "If true, replaces velocity instead of adding to it.", false)
                    .addExampleObject(new AddVelocityAction(new KryptoniteCodecs.Vec3fValue(new StaticValue(0F), new StaticValue(0.9F), new StaticValue(0F)), Space.LOCAL, new StaticValue(false)));
        }
    }

}