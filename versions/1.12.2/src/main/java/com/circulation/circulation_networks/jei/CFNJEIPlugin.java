package com.circulation.circulation_networks.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class CFNJEIPlugin implements IModPlugin {
    public static CFNGuiHandler guiHandler;

    @Override
    public void register(IModRegistry registry) {
        guiHandler = new CFNGuiHandler();
        registry.addAdvancedGuiHandlers(guiHandler);
        registry.addGhostIngredientHandler(guiHandler.getGuiContainerClass(), guiHandler);
    }
}
