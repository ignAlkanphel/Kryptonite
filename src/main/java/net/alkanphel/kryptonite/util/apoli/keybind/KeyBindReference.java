package net.alkanphel.kryptonite.util.apoli.keybind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public record KeyBindReference(String id, boolean continuous) {

	public static final KeyBindReference NONE = new KeyBindReference("none");

    public static final Codec<KeyBindReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(KeyBindReference::id),
            Codec.BOOL.optionalFieldOf("continuous", false).forGetter(KeyBindReference::continuous)
    ).apply(instance, KeyBindReference::new));

	public KeyBindReference(String id) {
		this(id, false);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		else if (obj instanceof KeyBindReference that) {
			return this.id().equals(that.id()) && this.continuous() == that.continuous();
		}

		else {
			return false;
		}

	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id(), this.continuous());
	}

}