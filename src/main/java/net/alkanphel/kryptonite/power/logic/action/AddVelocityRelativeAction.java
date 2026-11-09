package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.alkanphel.kryptonite.util.apoli.Space;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Locale;
import java.util.function.BiFunction;

public class AddVelocityRelativeAction extends Action {

    public static final MapCodec<AddVelocityRelativeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.Vec3fValue.CODEC.optionalFieldOf("velocity", KryptoniteCodecs.Vec3fValue.ZERO).forGetter(a -> a.velocity),
            Reference.CODEC.optionalFieldOf("reference", Reference.POSITION).forGetter(a -> a.reference),
            Value.CODEC.optionalFieldOf("set", new StaticValue(false)).forGetter(a -> a.set)
    ).apply(instance, AddVelocityRelativeAction::new));

    private final KryptoniteCodecs.Vec3fValue velocity;
    private final Reference reference;
    private final Value set;

    public AddVelocityRelativeAction(KryptoniteCodecs.Vec3fValue velocity, Reference reference, Value set) {
        this.velocity = velocity;
        this.reference = reference;
        this.set = set;
    }

    @Override
    protected boolean execute(DataContext context) {
        Entity source = context.getSource();
        Entity target = context.getTarget();
        if (source == null || target == null) return false;

        DataContext sourceContext = DataContext.forEntity(source);

        Vector3f velocityCopy = this.velocity.get(sourceContext);
        TriConsumer<Float, Float, Float> method = set.getAsBoolean(sourceContext)
                ? target::setDeltaMovement
                : target::push;

        Vec3 referenceVec = reference.apply(source, target);
        Space.transformVectorToBase(referenceVec, velocityCopy, source.getYRot(), true); // vector is normalized by method

        method.accept(velocityCopy.x(), velocityCopy.y(), velocityCopy.z());
        target.hurtMarked = true;
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.ADD_VELOCITY_RELATIVE.get();
    }

    public static class Serializer extends ActionSerializer<AddVelocityRelativeAction> {

        @Override
        public MapCodec<AddVelocityRelativeAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, AddVelocityRelativeAction> builder, HolderLookup.Provider provider) {
            builder.setName("Add Velocity Relative")
                    .setDescription("Adds or sets the target entity's velocity relative to the source entity. For this action, the \"source\" is the entity used as the reference for calculating velocity, and the \"target\" is the entity receiving the velocity.")
                    .addOptional("velocity", KryptoniteDocumented.TYPE_VECTOR3f_VALUE, "The amount of velocity to apply to the xyz axis.", Vec3.ZERO)
                    .addOptional("reference", SettingType.enumList(Reference.values()), "Determines whether to use the direction from the source entity to the target entity's position or the source entity's rotation.", Reference.POSITION)
                    .addOptional("set", KryptoniteDocumented.TYPE_BOOLEAN_VALUE, "If true, replaces velocity instead of adding to it.", false)
                    .addExampleObject(new AddVelocityRelativeAction(new KryptoniteCodecs.Vec3fValue(new StaticValue(0F), new StaticValue(0.9F), new StaticValue(0F)), Reference.POSITION, new StaticValue(false)));
        }
    }

    public enum Reference implements StringRepresentable, BiFunction<Entity, Entity, Vec3> {

        POSITION {
            @Override
            public Vec3 apply(Entity source, Entity target) {
                return target.position().subtract(source.position());
            }
        },

        ROTATION {
            @Override
            public Vec3 apply(Entity source, Entity target) {
                float pitch = source.getXRot();
                float yaw = source.getYRot();

                float i = 0.017453292F;

                float j = -Mth.sin(yaw * i) * Mth.cos(pitch * i);
                float k = -Mth.sin(pitch * i);
                float l = Mth.cos(yaw * i) * Mth.cos(pitch * i);

                return new Vec3(j, k, l);
            }
        };

        public static final Codec<Reference> CODEC = StringRepresentable.fromEnum(Reference::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}