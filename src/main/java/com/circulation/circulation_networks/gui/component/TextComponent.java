package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;
//? if <1.20 {
import net.minecraft.client.Minecraft;
//?} else if <1.21 {
/*import net.minecraft.client.Minecraft;
*///?} else {
/*import net.minecraft.client.Minecraft;
*///?}

import java.util.function.Supplier;

public class TextComponent extends Component {

    private static final int FONT_HEIGHT = 9;

    private final Supplier<String> textSupplier;
    private final int color;
    private final boolean shadow;

    public TextComponent(int x, int y, CFNBaseGui<?> gui, Supplier<String> textSupplier, int color) {
        this(x, y, gui, textSupplier, color, false);
    }

    public TextComponent(int x, int y, CFNBaseGui<?> gui, Supplier<String> textSupplier, int color, boolean shadow) {
        super(x, y, 0, FONT_HEIGHT, gui);
        this.textSupplier = textSupplier;
        this.color = color;
        this.shadow = shadow;
        setEnabled(false);
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        return false;
    }

    @Override
    protected void render(int mouseX, int mouseY, float partialTicks) {
        String text = textSupplier.get();
        if (text == null || text.isEmpty()) {
            setSize(0, FONT_HEIGHT);
            return;
        }

        //? if <1.20 {
        Minecraft mc = Minecraft.getMinecraft();
        setSize(mc.fontRenderer.getStringWidth(text), FONT_HEIGHT);
        mc.fontRenderer.drawString(text, getAbsoluteX(), getAbsoluteY(), color, shadow);
        //?} else {
        /*setSize(0, FONT_HEIGHT);
        *///?}
    }
}