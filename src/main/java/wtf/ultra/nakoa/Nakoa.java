package wtf.ultra.nakoa;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "nakoa", version = "1.8.9")
public class Nakoa {

    private final InitChest command = new InitChest();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(command);
    }

    @SubscribeEvent
    public void onOpenChat(InputEvent.KeyInputEvent event) {
        if (command.active && Keyboard.getEventKey() == Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode()) {
            command.say("Nakoa has been disabled");
            command.active = false;
        }
    }

    @SubscribeEvent
    public void onGUIOpen(GuiOpenEvent event) {
        if (command.active) {
            event.setCanceled(true);
        }
    }
}
