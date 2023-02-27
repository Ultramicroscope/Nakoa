package wtf.ultra.nakoa;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "nakoa", version = "1.8.9")
public class Nakoa {

    private final InitChest nakoa = new InitChest();
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean handlingGui = true;
    private int tick, nakoaState, failCount, stageTwoTick;

    /* Register event handlers and the initialization command. */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(nakoa);
    }

    /* When the open-chat-key is pressed, disable Nakoa. // Fails if keyBindChat is set to a mouse button. */
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (nakoa.isActive() && Keyboard.getEventKey() == Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode()) {
            say("Nakoa has been disabled");
            end();
        }
    }

    /* Prevents Guis to allow Nakoa to run even when Minecraft is minimized or the computer is idle (screensaver). */
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (nakoa.isActive() && !handlingGui) {
            event.setCanceled(true);
        }
    }

    /* Open and close a chest repeatedly using a tick counter to stall actions. */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Do nothing until the /nakoa || /nak command has been successfully executed.
        if (!nakoa.isActive()) return;
        tick++;
        switch (nakoaState) {
            case 0: {
                // Prompt user to begin the mod by opening the gold mine chest.
                say("Open the chest quickly then AFK!");
                // Progress the stage of the mod.
                nakoaState = 1;
                tick = 0;
                break;
            }
            case 1: {
                // Wait 160 ticks (8s. 20tps).
                if (tick == 160) {
                    // Check if the player has opened the chest as they were prompted.
                    if (mc.thePlayer.openContainer instanceof ContainerChest) {
                        // Close the chest.
                        mc.thePlayer.closeScreen();
                        // Set the select hot-bar slot to slot 2. In Ventureland, slot 2 is never null.
                        // When sending a PlayerBlockPlacement packet, it lost if the itemStack is null.
                        mc.thePlayer.inventory.currentItem = 1;
                        System.out.println("[NAKOA]: STARTING");
                        nakoaState = 2;
                        tick = 0;
                        // Set a time for the next stage to begin execution. 5 + 0-14 ticks is about 0.25 to 1 second.
                        stageTwoTick = 5 + (int)(Math.random() * 15);
                    } else {
                        // If the player failed to open the chest in 5 seconds, end execution.
                        say("No chest was opened");
                        end();
                    }
                }
                break;
            }
            case 2: {
                // Once the random number of ticks have passed, begin attempting to reopen the chest.
                if (tick == stageTwoTick) {
                    // If this stage fails 4 times, end execution.
                    if (failCount++ == 4) {
                        say("catastrophic failure");
                        end();
                    } else {
                        // Make sure the player is still within sqrt(10) blocks of the chest.
                        if (nakoa.chestPos().distanceSq(mc.thePlayer.getPosition()) <= 10) {
                            // Flag to disable the Gui handler so the chest can be opened and closed.
                            handlingGui = true;
                            System.out.println("[NAKOA]: OPENING");
                            // Sending a PlayerBlockPlacement packet to the client's Net Handler queue.
                            // The face and cursor positions params are -1 because the server ignores them anyway.
                            // Technically the position and held item are also ignored.
                            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getPosition(), -1, mc.thePlayer.getHeldItem(),-1,-1,-1));
                            nakoaState = 3;
                        } else {
                            // If the player is too far from the chest, end execution.
                            end();
                        }
                    }
                    // Random number of ticks until the next stage begins.
                    // Opening a chest needs permission from the server, which takes some time.
                    // Because of this, immediate progression will cause failure.
                    // I think if you are too laggy, this time will need to be increased, but I have not seen it fail.
                    stageTwoTick = 5 + (int)(Math.random() * 15);
                    tick = 0;
                }
                break;
            }
            case 3: {
                if (tick == stageTwoTick) {
                    // Check if the chest successfully opened from the packet sent in the previous stage.
                    if (mc.thePlayer.openContainer instanceof ContainerChest) {
                        System.out.println("[NAKOA]: CLOSING");
                        // Close the chest.
                        mc.thePlayer.closeScreen();
                        // Re-enable the Gui handler to prevent Guis from opening.
                        handlingGui = false;
                        // Reset the fail counter of the previous stage because it was successful.
                        failCount = 0;
                        // Set the counter to wait 30 to 40 minutes before opening the chest again.
                        stageTwoTick = 36000 + (int)(Math.random() * 12000);
                    } else {
                        // If the chest failed to open, wait 0.25 to 1 second then try again.
                        stageTwoTick = 5 + (int)(Math.random() * 15);
                    }
                    nakoaState = 2;
                    tick = 0;
                }
                break;
            }
        }
    }

    private void say(String statement) {
        mc.thePlayer.addChatMessage(new ChatComponentText(statement));
    }

    private void end() {
        nakoa.deactivate();
        handlingGui = true;
        tick = 0;
        nakoaState = 0;
        failCount = 0;
        stageTwoTick = 0;
    }
}
