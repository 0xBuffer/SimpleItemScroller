package com.simpleitemscroller;

import com.simpleitemscroller.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public final class ItemScroller {
    private static long lastClickTime;
    private static int lastSlotId = -1;

    private ItemScroller() {
    }

    public static void onHover(MinecraftClient client, ScreenHandler handler, Slot slot) {
        if (slot == null || !slot.hasStack() || slot.id < 0) {
            return;
        }
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        if (!Config.enabled) {
            return;
        }
        long handle = client.getWindow().getHandle();
        boolean left = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean shift = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!left || !shift) {
            lastSlotId = -1;
            return;
        }
        if (slot.id == lastSlotId) {
            return;
        }
        long now = Util.getMeasuringTimeMs();
        if (now - lastClickTime < Config.delayMs) {
            return;
        }
        lastClickTime = now;
        lastSlotId = slot.id;
        client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
    }
}
