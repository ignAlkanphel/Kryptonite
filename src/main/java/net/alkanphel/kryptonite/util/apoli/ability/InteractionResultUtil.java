package net.alkanphel.kryptonite.util.apoli.ability;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.NotNull;

public class InteractionResultUtil {

    public static boolean shouldSwingHand(InteractionResult result) {
        return result instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE;
    }

    public static boolean shouldOverride(InteractionResult oldResult, InteractionResult newResult) {
        boolean newAccepted = newResult.consumesAction();
        boolean oldAccepted = oldResult.consumesAction();

        boolean newSwings = newResult instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE;
        boolean oldSwings = oldResult instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE;

        return (newAccepted && !oldAccepted) || (newSwings && !oldSwings);
    }

    public enum InteractionResultType implements StringRepresentable {
        SUCCESS("success", InteractionResult.SUCCESS),
        SUCCESS_SERVER("success_server", InteractionResult.SUCCESS_SERVER),
        CONSUME("consume", InteractionResult.CONSUME),
        FAIL("fail", InteractionResult.FAIL),
        PASS("pass", InteractionResult.PASS),
        TRY_WITH_EMPTY_HAND("try_with_empty_hand", InteractionResult.TRY_WITH_EMPTY_HAND);

        private final String name;
        private final InteractionResult value;
        public static final Codec<InteractionResultType> CODEC = StringRepresentable.fromEnum(InteractionResultType::values);

        InteractionResultType(String name, InteractionResult value) {
            this.name = name;
            this.value = value;
        }

        public InteractionResult getValue() {
            return this.value;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    public static final Codec<InteractionResult> INTERACTION_RESULT_CODEC = InteractionResultType.CODEC.xmap(InteractionResultType::getValue, result -> {
        for (InteractionResultType type : InteractionResultType.values()) {
            if (type.getValue() == result) return type;
        }

        return InteractionResultType.SUCCESS; // fallback
    });

}