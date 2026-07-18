package net.alkanphel.kryptonite.power.logic.action.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockActionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

// TODO Try converting to Value
public class ModifyBlockStateBlockAction extends BlockAction {

    public static final MapCodec<ModifyBlockStateBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("property").forGetter(a -> a.property),
            ResourceOperation.CODEC.optionalFieldOf("operation", ResourceOperation.ADD).forGetter(a -> a.operation),
            Codec.INT.optionalFieldOf("change").forGetter(a -> a.change),
            Codec.BOOL.optionalFieldOf("value").forGetter(a -> a.boolValue),
            Codec.STRING.optionalFieldOf("enum").forGetter(a -> a.enumValue),
            Codec.BOOL.optionalFieldOf("cycle", false).forGetter(a -> a.cycle)
    ).apply(instance, ModifyBlockStateBlockAction::new));

    private final String property;
    private final ResourceOperation operation;
    private final Optional<Integer> change;
    private final Optional<Boolean> boolValue;
    private final Optional<String> enumValue;
    private final boolean cycle;

    public ModifyBlockStateBlockAction(String property, ResourceOperation operation, Optional<Integer> change, Optional<Boolean> boolValue, Optional<String> enumValue, boolean cycle) {
        this.property = property;
        this.operation = operation;
        this.change = change;
        this.boolValue = boolValue;
        this.enumValue = enumValue;
        this.cycle = cycle;
    }

    @Override
    public boolean run(BlockActionContext context) {
        Level level = context.level();
        BlockPos pos = context.pos();

        BlockState blockState = level.getBlockState(pos);
        Property<?> blockProperty = blockState.getProperties()
                .stream()
                .filter(prop -> prop.getName().equals(property))
                .findFirst()
                .orElse(null);

        if (blockProperty == null) return false;

        if (cycle) {
            level.setBlockAndUpdate(pos, blockState.cycle(blockProperty));
            return true;
        }

        switch (blockProperty) {
            case EnumProperty<?> enumProp when enumValue.isPresent() && !enumValue.get().isEmpty() ->
                    setEnumProperty(enumProp, enumValue.get(), level, pos, blockState);
            case BooleanProperty boolProp when boolValue.isPresent() ->
                    level.setBlockAndUpdate(pos, blockState.setValue(boolProp, boolValue.get()));
            case IntegerProperty intProp when change.isPresent() -> {
                int newValue = switch (operation) {
                    case ADD -> Optional.of(blockState.getValue(intProp)).orElse(0) + change.get();
                    case SET -> change.get();
                };

                if (intProp.getPossibleValues().contains(newValue)) {
                    level.setBlockAndUpdate(pos, blockState.setValue(intProp, newValue));
                }
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    private <T extends Enum<T> & StringRepresentable> void setEnumProperty(EnumProperty<T> property, String name, Level level, BlockPos pos, BlockState originalState) {
        property.getValue(name).ifPresentOrElse(
                propValue -> level.setBlockAndUpdate(pos, originalState.setValue(property, propValue)),
                () -> Kryptonite.LOGGER.warn("Couldn't set enum property \"{}\" of block at {} to \"{}\"! Expected value to be any of {}", property.getName(), pos.toShortString(), name, property.getPossibleValues().stream().map(StringRepresentable::getSerializedName).collect(Collectors.joining(", ")))
        );
    }

    @Override
    public BlockActionSerializer<?> getSerializer() {
        return BlockActionSerializers.MODIFY_BLOCK_STATE.get();
    }

    public static class Serializer extends BlockActionSerializer<ModifyBlockStateBlockAction> {

        @Override
        public MapCodec<ModifyBlockStateBlockAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockAction, ModifyBlockStateBlockAction> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Block State")
                    .setDescription("Modifies the block state property of the block. Depending on the property type, different values are expected: boolean properties use value, enumeration properties use enum, and integer properties use operation and change.")
                    .add("property", TYPE_STRING, "The name of the block state property to modify. See: https://minecraft.wiki/w/Block_states#List_of_block_states")
                    .addOptional("operation", SettingType.enumList(ResourceOperation.values()), "Determines how the value specified in the change field is operated on the specified property.", ResourceOperation.ADD)
                    .addOptional("change", TYPE_INT, "If specified, the value to add, remove or set to/from the specified property if the specified property is an integer.")
                    .addOptional("value", TYPE_BOOLEAN, "If specified, the boolean to use as the new value for the specified property if the specified property is a boolean.")
                    .addOptional("enum", TYPE_STRING, "If specified, the string to use as the new value for the specified property if the specified property is a string.")
                    .addOptional("cycle", TYPE_BOOLEAN, "If set to true, changes the property to the next state in the cycle, ignoring all other optional fields. All property types can use this operation.", false)
                    .addExampleObject(new ModifyBlockStateBlockAction("waterlogged", ResourceOperation.SET, Optional.empty(), Optional.of(true), Optional.empty(), false))
                    .addExampleObject(new ModifyBlockStateBlockAction("facing", ResourceOperation.ADD, Optional.empty(), Optional.empty(), Optional.empty(), true));
        }
    }

    public enum ResourceOperation implements StringRepresentable {
        ADD("add"),
        SET("set");

        public static final Codec<ResourceOperation> CODEC = StringRepresentable.fromEnum(ResourceOperation::values);
        private final String name;

        ResourceOperation(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

}