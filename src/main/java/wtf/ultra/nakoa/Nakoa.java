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

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(nakoa);
    }

    @SubscribeEvent
    public void onOpenChat(InputEvent.KeyInputEvent event) {
        if (nakoa.isActive() && Keyboard.getEventKey() == Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode()) {
            say("Nakoa has been disabled");
            end();
        }
    }

    @SubscribeEvent
    public void onGUIOpen(GuiOpenEvent event) {
        if (nakoa.isActive() && !handlingGui) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!nakoa.isActive()) return;
        tick++;
        switch (nakoaState) {
            case 0: {
                say("Open the chest within 5s then AFK!");
                nakoaState = 1;
                tick = 0;
                break;
            }
            case 1: {
                if (tick == 100) {
                    if (mc.thePlayer.openContainer instanceof ContainerChest) {
                        mc.thePlayer.closeScreen();
                        int slot;
                        while ((slot = mc.thePlayer.inventory.currentItem) != 1) mc.thePlayer.inventory.changeCurrentItem(slot - 1);
                        System.out.println("[NAKOA]: STARTING");
                        nakoaState = 2;
                        stageTwoTick = 5 + (int)(Math.random() * 15);
                        tick = 0;
                    } else {
                        say("No chest was opened");
                        end();
                    }
                }
                break;
            }
            case 2: {
                if (tick == stageTwoTick) {
                    if (failCount++ == 4) {
                        say("catastrophic failure");
                        end();
                    } else {
                        if (nakoa.chestPos().distanceSq(mc.thePlayer.getPosition()) <= 10) {
                            handlingGui = true;
                            System.out.println("[NAKOA]: OPENING");
                            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getPosition(), -1, mc.thePlayer.getHeldItem(),-1,-1,-1));
                            nakoaState = 3;
                        } else {
                            end();
                        }
                    }
                    stageTwoTick = 5 + (int)(Math.random() * 15);
                    tick = 0;
                }
                break;
            }
            case 3: {
                if (tick == stageTwoTick) {
                    if (mc.thePlayer.openContainer instanceof ContainerChest) {
                        System.out.println("[NAKOA]: CLOSING");
                        mc.thePlayer.closeScreen();
                        handlingGui = false;
                        failCount = 0;
                        //stageTwoTick = 120 + (int)(Math.random() * 20); //5-6 seconds
                        stageTwoTick = 36000 + (int)(Math.random() * 12000); //30-40 minutes
                    } else {
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
