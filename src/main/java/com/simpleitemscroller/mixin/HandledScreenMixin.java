package com.simpleitemscroller.mixin;

import com.simpleitemscroller.ItemScroller;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected ScreenHandler handler;

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Inject(method = "render", at = @At("TAIL"))
    private void simpleitemscroller$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ItemScroller.onHover(MinecraftClient.getInstance(), this.handler, this.simpleitemscroller$findSlot(mouseX, mouseY));
    }

    @Unique
    private Slot simpleitemscroller$findSlot(double mouseX, double mouseY) {
        for (Slot slot : this.handler.slots) {
            if (slot.isEnabled() && this.simpleitemscroller$isOver(slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Unique
    private boolean simpleitemscroller$isOver(Slot slot, double mouseX, double mouseY) {
        double px = mouseX - this.x;
        double py = mouseY - this.y;
        return px >= slot.x - 1 && px < slot.x + 17 && py >= slot.y - 1 && py < slot.y + 17;
    }
}
