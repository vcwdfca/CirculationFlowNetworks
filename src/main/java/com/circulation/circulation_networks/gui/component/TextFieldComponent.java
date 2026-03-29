package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;
//? if <1.20 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
//?} else {
/*import net.minecraft.client.Minecraft;
*///?}

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class TextFieldComponent extends Component {

    private static final BooleanSupplier ALLOW_ALL_INPUT = () -> true;
    private static int nextId = 1;

    //? if <1.20 {
    private final GuiTextField textField;
    //?} else {
    /*private final Object textField = null;
    *///?}
    private BooleanSupplier inputAllowed = ALLOW_ALL_INPUT;
    @Nullable
    private Boolean backgroundDrawing;

    public TextFieldComponent(int x, int y, int width, int height, CFNBaseGui<?> gui) {
        this(x, y, width, height, gui, 10, null);
    }

    public TextFieldComponent(int x, int y, int width, int height, CFNBaseGui<?> gui, int maxLength) {
        this(x, y, width, height, gui, maxLength, null);
    }

    public TextFieldComponent(int x, int y, int width, int height, CFNBaseGui<?> gui, int maxLength, @Nullable Boolean backgroundDrawing) {
        super(x, y, width, height, gui);
        this.backgroundDrawing = backgroundDrawing;
        //? if <1.20 {
        this.textField = new GuiTextField(nextId++, Minecraft.getMinecraft().fontRenderer, getAbsoluteX(), getAbsoluteY(), width, height);
        this.textField.setMaxStringLength(Math.max(0, maxLength));
        applyNativeState();
        //?} else {
        
        //?}
    }

    public String getText() {
        //? if <1.20 {
        return textField.getText();
        //?} else {
        /*return "";
        *///?}
    }

    public TextFieldComponent setText(String text) {
        //? if <1.20 {
        textField.setText(text != null ? text : "");
        //?} else {
        
        //?}
        return this;
    }

    public int getMaxLength() {
        //? if <1.20 {
        return textField.getMaxStringLength();
        //?} else {
        /*return 0;
        *///?}
    }

    public TextFieldComponent setMaxLength(int maxLength) {
        //? if <1.20 {
        textField.setMaxStringLength(Math.max(0, maxLength));
        //?} else {
        
        //?}
        return this;
    }

    public boolean isFocused() {
        //? if <1.20 {
        return textField.isFocused();
        //?} else {
        /*return false;
        *///?}
    }

    public TextFieldComponent setFocused(boolean focused) {
        //? if <1.20 {
        textField.setFocused(focused);
        //?} else {
        
        //?}
        return this;
    }

    public BooleanSupplier getInputAllowed() {
        return inputAllowed;
    }

    public TextFieldComponent setInputAllowed(BooleanSupplier inputAllowed) {
        this.inputAllowed = inputAllowed != null ? inputAllowed : ALLOW_ALL_INPUT;
        return this;
    }

    @Nullable
    public Boolean getBackgroundDrawing() {
        return backgroundDrawing;
    }

    public TextFieldComponent setBackgroundDrawing(@Nullable Boolean backgroundDrawing) {
        this.backgroundDrawing = backgroundDrawing;
        //? if <1.20 {
        applyNativeState();
        //?} else {
        
        //?}
        return this;
    }

    @Override
    protected void render(int mouseX, int mouseY, float partialTicks) {
        //? if <1.20 {
        syncTextFieldBounds();
        applyNativeState();
        restoreGuiRenderState();
        textField.drawTextBox();
        restoreGuiRenderState();
        //?} else {
        
        //?}
    }

    @Override
    public void update() {
        //? if <1.20 {
        syncTextFieldBounds();
        applyNativeState();
        textField.updateCursorCounter();
        //?} else {
        
        //?}
    }

    @Override
    protected boolean onMouseClicked(int mouseX, int mouseY, int button) {
        //? if <1.20 {
        syncTextFieldBounds();
        applyNativeState();
        return textField.mouseClicked(mouseX, mouseY, button);
        //?} else {
        /*return false;
        *///?}
    }

    @Override
    protected boolean onKeyTyped(char typedChar, int keyCode) {
        //? if <1.20 {
        if (!inputAllowed.getAsBoolean()) {
            return false;
        }
        applyNativeState();
        return textField.textboxKeyTyped(typedChar, keyCode);
        //?} else {
        /*return false;
        *///?}
    }

    @Override
    protected void onGlobalMouseClicked(int mouseX, int mouseY, int button) {
        //? if <1.20 {
        if (!textField.isFocused() || super.contains(mouseX, mouseY)) {
            return;
        }
        syncTextFieldBounds();
        textField.mouseClicked(mouseX, mouseY, button);
        //?} else {
        
        //?}
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        //? if <1.20 {
        syncTextFieldBounds();
        //?} else {
        
        //?}
    }

    //? if <1.20 {
    private void syncTextFieldBounds() {
        textField.x = getAbsoluteX();
        textField.y = getAbsoluteY();
        textField.width = width;
        textField.height = height;
    }

    private void applyNativeState() {
        textField.setEnabled(isEnabled());
        textField.setVisible(isVisible());
        if (backgroundDrawing != null) {
            textField.setEnableBackgroundDrawing(backgroundDrawing);
        }
    }
    //?} else {
    
    //?}
}