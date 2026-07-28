package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.power.ability.ModifyEntityTypeTagAbility;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public abstract class TagLoaderMixin<T> {

    @Shadow @Final private String directory;

    @Inject(method = "build", at = @At("HEAD"))
    private void kryptonite$rebuildTagsInTags(Map<Identifier, List<TagLoader.EntryWithSource>> builders, CallbackInfoReturnable<Map<Identifier, List<T>>> cir) {
        ModifyEntityTypeTagAbility.setTagCache(this.directory, builders);
    }

}