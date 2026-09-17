package com.simpleitemscroller;

import com.simpleitemscroller.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public final class ItemScroller {
    private static int currentSlot = -1;
    private static long enterTime;
    private static boolean fired;

    private ItemScroller() {
    }

    public static void onHover(MinecraftClient client, ScreenHandler handler, Slot slot) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        if (!Config.enabled) {
            return;
        }
        long window = client.getWindow().getHandle();
        boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean shift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!left || !shift) {
            currentSlot = -1;
            return;
        }
        if (slot == null || !slot.hasStack() || slot.id < 0) {
            currentSlot = -1;
            return;
        }
        long now = Util.getMeasuringTimeMs();
        if (slot.id != currentSlot) {
            currentSlot = slot.id;
            enterTime = now;
            fired = false;
            return;
        }
        if (fired) {
            return;
        }
        if (now - enterTime < Config.delayMs) {
            return;
        }
        fired = true;
        client.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, client.player);
    }
}
