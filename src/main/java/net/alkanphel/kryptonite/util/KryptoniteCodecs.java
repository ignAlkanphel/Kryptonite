package net.alkanphel.kryptonite.util;

import com.mojang.serialization.Codec;
import net.minecraft.world.InteractionHand;

public class KryptoniteCodecs {

    public static final Codec<InteractionHand> HAND_CODEC = Codec.STRING.xmap(string -> InteractionHand.valueOf(string.toUpperCase()), hand -> hand.name().toLowerCase());

}