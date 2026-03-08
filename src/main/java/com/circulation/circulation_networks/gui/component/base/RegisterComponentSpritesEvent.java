package com.circulation.circulation_networks.gui.component.base;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Fired on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} during
 * {@code FMLPreInitializationEvent}, before the {@link ComponentAtlas} begins
 * stitching. Subscribe to this event to register custom sprites or backgrounds
 * into the shared atlas.
 *
 * <p>Example (client-only subscriber):
 * <pre>{@code
 * @SideOnly(Side.CLIENT)
 * @SubscribeEvent
 * public void onRegisterSprites(RegisterComponentSpritesEvent event) {
 *     event.register("my_sprite");
 *     event.registerBackground("furnace_bg");
 * }
 * }</pre>
 *
 * <p>Component sprites must be located at
 * {@code assets/circulation_networks/textures/gui/component/<name>.png}.
 * Background sprites must be located at
 * {@code assets/circulation_networks/textures/gui/background/<name>.png}.
 * All files must be reachable via the active {@code ResourceManager} stack.
 */
@SideOnly(Side.CLIENT)
public final class RegisterComponentSpritesEvent extends Event {

    RegisterComponentSpritesEvent() {

    }

    /**
     * Registers a component sprite base-name (without {@code .png} extension) to be
     * included in the atlas. The file must reside under
     * {@code assets/circulation_networks/textures/gui/component/}.
     * Duplicate names are silently ignored.
     */
    public void register(String name) {
        ComponentAtlas.INSTANCE.addSprite(name);
    }

    /**
     * Registers a UI background image base-name (without {@code .png} extension) to be
     * stitched into the same atlas as component sprites. The file must reside under
     * {@code assets/circulation_networks/textures/gui/background/}.
     *
     * <p>Retrieve the stitched region at render time via
     * {@link ComponentAtlas#getBackground(String)} or the convenience helper
     * {@link com.circulation.circulation_networks.gui.CFNBaseGui#drawAtlasBackground}.
     * Duplicate names are silently ignored.
     */
    public void registerBackground(String name) {
        ComponentAtlas.INSTANCE.addBackground(name);
    }
}
