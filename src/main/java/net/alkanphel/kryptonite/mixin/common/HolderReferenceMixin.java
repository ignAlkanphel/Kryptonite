package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.alkanphel.kryptonite.power.ability.ModifyEntityTypeTagAbility;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedType;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Holder.Reference.class)
public abstract class HolderReferenceMixin<T> {

    @ModifyReturnValue(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"))
    private boolean kryptonite$inTagProxy(boolean original, TagKey<T> tag) {
        if (original) return true;

        Holder.Reference<T> self = (Holder.Reference<T>) (Object) this;
        if (!(self.value() instanceof EntityType<?> entityType)) return original;

        Entity entity = ((EntityLinkedType) entityType).kryptonite$getEntity();
        TagKey<EntityType<?>> entityTag = (TagKey<EntityType<?>>) tag;

        return ModifyEntityTypeTagAbility.doesApply(entity, entityTag);
    }

}