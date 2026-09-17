package com.simpleitemscroller.mixin;

import com.simpleitemscroller.ItemScroller;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected Slot focusedSlot;

    @Inject(method = "render", at = @At("TAIL"))
    private void simpleitemscroller$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ItemScroller.onHover(MinecraftClient.getInstance(), this.focusedSlot);
    }
}
