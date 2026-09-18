package com.simpleitemscroller.mixin;

import com.simpleitemscroller.ItemScroller;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Unique
    private double simpleitemscroller$lastMouseX;

    @Unique
    private double simpleitemscroller$lastMouseY;

    @Unique
    private boolean simpleitemscroller$hasLastMouse;

    @Shadow
    protected ScreenHandler handler;

    @Shadow
    protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    @Invoker("getSlotAt")
    public abstract Slot simpleitemscroller$getSlotAt(double mouseX, double mouseY);

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void simpleitemscroller$onMouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        ItemScroller.onMouseClicked(MinecraftClient.getInstance(), this.handler, this.simpleitemscroller$getSlotAt(click.x(), click.y()), click.button(), click.hasShift());
        this.simpleitemscroller$lastMouseX = click.x();
        this.simpleitemscroller$lastMouseY = click.y();
        this.simpleitemscroller$hasLastMouse = click.button() == 0 && click.hasShift();
    }

    @Inject(method = "mouseDragged", at = @At("RETURN"))
    private void simpleitemscroller$onMouseDragged(Click click, double offsetX, double offsetY, CallbackInfoReturnable<Boolean> cir) {
        double startX = this.simpleitemscroller$hasLastMouse ? this.simpleitemscroller$lastMouseX : click.x();
        double startY = this.simpleitemscroller$hasLastMouse ? this.simpleitemscroller$lastMouseY : click.y();
        double distanceX = click.x() - startX;
        double distanceY = click.y() - startY;
        int steps = Math.max(1, (int) Math.ceil(Math.hypot(distanceX, distanceY) / 3.0));
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            double mouseX = startX + distanceX * progress;
            double mouseY = startY + distanceY * progress;
            ItemScroller.onMouseDragged(MinecraftClient.getInstance(), this.handler, this.simpleitemscroller$getSlotAt(mouseX, mouseY), click.button(), click.hasShift(), this::simpleitemscroller$quickMove);
        }
        this.simpleitemscroller$lastMouseX = click.x();
        this.simpleitemscroller$lastMouseY = click.y();
        this.simpleitemscroller$hasLastMouse = true;
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void simpleitemscroller$onMouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
        this.simpleitemscroller$sampleFrame(click.x(), click.y());
        ItemScroller.onMouseReleased(this::simpleitemscroller$quickMove);
        this.simpleitemscroller$hasLastMouse = false;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void simpleitemscroller$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.simpleitemscroller$sampleFrame(mouseX, mouseY);
    }

    @Unique
    private void simpleitemscroller$sampleFrame(double mouseX, double mouseY) {
        double startX = this.simpleitemscroller$hasLastMouse ? this.simpleitemscroller$lastMouseX : mouseX;
        double startY = this.simpleitemscroller$hasLastMouse ? this.simpleitemscroller$lastMouseY : mouseY;
        double distanceX = mouseX - startX;
        double distanceY = mouseY - startY;
        int steps = Math.max(1, (int) Math.ceil(Math.hypot(distanceX, distanceY) / 3.0));
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            double sampleX = startX + distanceX * progress;
            double sampleY = startY + distanceY * progress;
            ItemScroller.onFrame(MinecraftClient.getInstance(), this.handler, this.simpleitemscroller$getSlotAt(sampleX, sampleY), this::simpleitemscroller$quickMove);
        }
        this.simpleitemscroller$lastMouseX = mouseX;
        this.simpleitemscroller$lastMouseY = mouseY;
        this.simpleitemscroller$hasLastMouse = true;
    }

    @Unique
    private void simpleitemscroller$quickMove(Slot slot) {
        this.onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
    }
}
