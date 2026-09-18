package com.simpleitemscroller;

import com.simpleitemscroller.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class ItemScroller {
    private static final Logger LOGGER = LoggerFactory.getLogger("simple-item-scroller");
    private static final ArrayDeque<Slot> pendingSlots = new ArrayDeque<>();
    private static ScreenHandler activeHandler;
    private static boolean active;
    private static long nextClickTime;
    private static Slot observedSlot;
    private static Slot lastTriggeredSlot;
    private static int triggeredSlots;

    private ItemScroller() {
    }

    public static void onMouseClicked(MinecraftClient client, ScreenHandler handler, Slot slot, int button, boolean shift) {
        if (!Config.enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !shift) {
            reset();
            return;
        }
        start(handler);
        LOGGER.debug("SIS screen: type={}, creative={}", client.currentScreen == null ? "none" : client.currentScreen.getClass().getSimpleName(), client.player != null && client.player.isInCreativeMode());
        if (slot != null && slot.id >= 0) {
            lastTriggeredSlot = slot;
            triggeredSlots = 1;
            nextClickTime = Util.getMeasuringTimeMs() + Config.delayMs;
        }
    }

    public static void onMouseDragged(MinecraftClient client, ScreenHandler handler, Slot slot, int button, boolean shift, Consumer<Slot> quickMove) {
        if (!canUse(client) || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !shift) {
            reset();
            return;
        }
        if (!active || activeHandler != handler) {
            start(handler);
        }
        observe(slot, "drag");
        enqueue(slot);
        process(handler, quickMove);
    }

    public static void onFrame(MinecraftClient client, ScreenHandler handler, Slot slot, Consumer<Slot> quickMove) {
        if (!active) {
            return;
        }
        if (!canUse(client) || activeHandler != handler) {
            reset();
            return;
        }
        observe(slot, "frame");
        enqueue(slot);
        process(handler, quickMove);
    }

    public static void onMouseReleased(Consumer<Slot> quickMove) {
        if (active && activeHandler != null) {
            process(activeHandler, quickMove, true);
        }
        reset();
    }

    private static boolean canUse(MinecraftClient client) {
        return Config.enabled && client.player != null && client.interactionManager != null;
    }

    private static void start(ScreenHandler handler) {
        pendingSlots.clear();
        activeHandler = handler;
        active = true;
        nextClickTime = 0L;
        observedSlot = null;
        lastTriggeredSlot = null;
        triggeredSlots = 0;
        LOGGER.debug("SIS gesture started: syncId={}", handler.syncId);
    }

    private static void observe(Slot slot, String source) {
        if (slot != observedSlot) {
            observedSlot = slot;
            LOGGER.debug("SIS hover: source={}, slot={}, hasStack={}", source, slot == null ? -1 : slot.id, slot != null && slot.hasStack());
        }
    }

    private static void enqueue(Slot slot) {
        if (slot == null || slot.id < 0 || slot == lastTriggeredSlot) {
            return;
        }
        lastTriggeredSlot = slot;
        if (slot.hasStack()) {
            pendingSlots.addLast(slot);
            triggeredSlots++;
            LOGGER.debug("SIS slot queued: id={}, pending={}", slot.id, pendingSlots.size());
        }
    }

    private static void process(ScreenHandler handler, Consumer<Slot> quickMove) {
        process(handler, quickMove, false);
    }

    private static void process(ScreenHandler handler, Consumer<Slot> quickMove, boolean flush) {
        long now = Util.getMeasuringTimeMs();
        int delay = Math.max(0, Config.delayMs);
        if (!flush && now < nextClickTime) {
            return;
        }
        int allowance;
        if (flush || delay == 0) {
            allowance = Integer.MAX_VALUE;
        } else {
            allowance = nextClickTime == 0L ? 1 : 1 + (int) Math.min(Integer.MAX_VALUE - 1L, (now - nextClickTime) / delay);
        }
        while (allowance > 0 && !pendingSlots.isEmpty()) {
            Slot slot = pendingSlots.removeFirst();
            if (slot.id < 0 || !slot.hasStack()) {
                continue;
            }
            LOGGER.debug("SIS quick move: syncId={}, slot={}", handler.syncId, slot.id);
            quickMove.accept(slot);
            allowance--;
            nextClickTime = delay == 0 ? now : Math.max(nextClickTime, now) + delay;
        }
    }

    private static void reset() {
        if (active) {
            LOGGER.debug("SIS gesture ended: pending={}, triggered={}", pendingSlots.size(), triggeredSlots);
        }
        pendingSlots.clear();
        activeHandler = null;
        active = false;
        nextClickTime = 0L;
        observedSlot = null;
        lastTriggeredSlot = null;
        triggeredSlots = 0;
    }
}
