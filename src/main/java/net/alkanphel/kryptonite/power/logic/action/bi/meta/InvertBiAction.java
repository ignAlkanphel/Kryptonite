package net.alkanphel.kryptonite.power.logic.action.bi.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializer;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiActionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiActionContext;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.List;

public class InvertBiAction extends BiAction {

    public static final MapCodec<InvertBiAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.fieldOf("actions").forGetter(a -> a.actions)
    ).apply(instance, InvertBiAction::new));

    private final List<BiAction> actions;

    public InvertBiAction(List<BiAction> actions) {
        this.actions = actions;
    }

    @Override
    public boolean run(BiActionContext context) {
        return BiAction.runList(actions, context.target(), context.actor());
    }

    @Override
    public BiActionSerializer<?> getSerializer() {
        return BiActionSerializers.INVERT.get();
    }

    public static class Serializer extends BiActionSerializer<InvertBiAction> {

        @Override
        public MapCodec<InvertBiAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiAction, InvertBiAction> builder, HolderLookup.Provider provider) {
            builder.setName("Invert")
                    .setDescription("Swaps the actor and target entity before running the bi actions.")
                    .add("actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run with the context swapped.")
                    .addExampleObject(new InvertBiAction(List.of()));
        }
    }

}