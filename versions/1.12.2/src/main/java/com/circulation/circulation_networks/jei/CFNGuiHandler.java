package com.circulation.circulation_networks.jei;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CFNGuiHandler implements IAdvancedGuiHandler<CFNBaseGui>, IGhostIngredientHandler<CFNBaseGui> {
    @Override
    public @NotNull Class<CFNBaseGui> getGuiContainerClass() {
        return CFNBaseGui.class;
    }

    @Override
    public @Nullable List<Rectangle> getGuiExtraAreas(@NotNull CFNBaseGui guiContainer) {
        return Arrays.asList(guiContainer.getAllComponents());
    }

    @Override
    public <I> @NotNull List<Target<I>> getTargets(@NotNull CFNBaseGui cfnBaseGui, @NotNull I i, boolean b) {
        return Collections.emptyList();
    }

    @Override
    public void onComplete() {

    }
}
