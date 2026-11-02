package com.java.beipuo.applied_oritech.client;

import appeng.client.gui.Icon;
import com.java.beipuo.applied_oritech.menu.AutoReturnMenu;
import net.minecraft.client.gui.GuiGraphics;
import appeng.client.gui.widgets.TabButton;
import net.minecraft.network.chat.Component;

public class AutoReturnButton extends TabButton {
    private final AutoReturnMenu menu;
    private Boolean displayedState;

    public AutoReturnButton(int x, int y, int width, int height, AutoReturnMenu menu) {
        super((Icon) null, Component.translatable("gui.applied_oritech.auto_return"),
                button -> menu.appliedOritech$toggleAutoReturn());
        setPosition(x, y);
        setSize(width, height);
        this.menu = menu;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        setFocused(false);
        return handled;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean enabled = menu.appliedOritech$isAutoReturn();
        if (displayedState == null || displayedState != enabled) {
            displayedState = enabled;
            var state = Component.translatable(enabled
                    ? "message.applied_oritech.toggle_on" : "message.applied_oritech.toggle_off");
            var label = Component.translatable("gui.applied_oritech.auto_return_state", state);
            setMessage(label);
        }
        Icon.TAB_BUTTON_BACKGROUND.getBlitter().dest(getX(), getY()).blit(graphics);
        float brightness = enabled ? 1.0f : 0.35f;
        appeng.client.gui.style.Blitter.texture(Icon.TEXTURE, Icon.TEXTURE_WIDTH, Icon.TEXTURE_HEIGHT)
                .src(16, 240, 16, 16)
                .dest(getX() + (getWidth() - 16) / 2, getY() + (getHeight() - 16) / 2 - 1, 16, 16)
                .color(brightness, brightness, brightness)
                .blit(graphics);
    }
}
