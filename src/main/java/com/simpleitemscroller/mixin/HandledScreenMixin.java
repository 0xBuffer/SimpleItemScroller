package com.simpleitemscroller.mixin;

import com.simpleitemscroller.ItemScroller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {
    @Unique
    private double simpleitemscroller$lastMouseX;

    @Unique
    private double simpleitemscroller$lastMouseY;

    @Unique
    private boolean simpleitemscroller$hasLastMouse;

    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    protected abstract void slotClicked(Slot slot, int slotId, int button, ContainerInput containerInput);

    @Invoker("getHoveredSlot")
    public abstract Slot simpleitemscroller$getSlotAt(double mouseX, double mouseY);

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void simpleitemscroller$onMouseClicked(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        ItemScroller.onMouseClicked(Minecraft.getInstance(), this.menu, this.simpleitemscroller$getSlotAt(event.x(), event.y()), event.button(), event.hasShiftDown());
        this.simpleitemscroller$lastMouseX = event.x();
        this.simpleitemscroller$lastMouseY = event.y();
        this.simpleitemscroller$hasLastMouse = event.button() == 0 && event.hasShiftDown();
    }

    @Inject(method = "mouseDragged", at = @At("RETURN"))
    private void simpleitemscroller$onMouseDragged(MouseButtonEvent event, double offsetX, double offsetY, CallbackInfoReturnable<Boolean> cir) {
        double startX = this.simpleitemscroller$hasLastMouse ? this.simpleitemscroller$lastMouseX : event.x();
        double startY = this.simpleitemscroller$hasLastMouse ? this.simpleitemscroller$lastMouseY : event.y();
        double distanceX = event.x() - startX;
        double distanceY = event.y() - startY;
        int steps = Math.max(1, (int) Math.ceil(Math.hypot(distanceX, distanceY) / 3.0));
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            double mouseX = startX + distanceX * progress;
            double mouseY = startY + distanceY * progress;
            ItemScroller.onMouseDragged(Minecraft.getInstance(), this.menu, this.simpleitemscroller$getSlotAt(mouseX, mouseY), event.button(), event.hasShiftDown(), this::simpleitemscroller$quickMove);
        }
        this.simpleitemscroller$lastMouseX = event.x();
        this.simpleitemscroller$lastMouseY = event.y();
        this.simpleitemscroller$hasLastMouse = true;
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void simpleitemscroller$onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        this.simpleitemscroller$sampleFrame(event.x(), event.y());
        ItemScroller.onMouseReleased(this::simpleitemscroller$quickMove);
        this.simpleitemscroller$hasLastMouse = false;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void simpleitemscroller$onRender(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
            ItemScroller.onFrame(Minecraft.getInstance(), this.menu, this.simpleitemscroller$getSlotAt(sampleX, sampleY), this::simpleitemscroller$quickMove);
        }
        this.simpleitemscroller$lastMouseX = mouseX;
        this.simpleitemscroller$lastMouseY = mouseY;
        this.simpleitemscroller$hasLastMouse = true;
    }

    @Unique
    private void simpleitemscroller$quickMove(Slot slot) {
        this.slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
    }
}
