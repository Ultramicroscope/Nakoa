package wtf.ultra.nakoa;

import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ClientCommandHandler;
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
    private int tick, nakoaState, failCount, tickBuffer;

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
            say("[NAKOA]: DISABLING");
            end();
        }
    }

    /* Open and close a chest repeatedly using a tick counter to stall actions. */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Do nothing until the /nakoa command has been successfully executed.
        if (!nakoa.isActive() || event.phase == TickEvent.Phase.START) return;

        EntityPlayerSP player = mc.thePlayer;
        tick++;
        switch (nakoaState) {
            case 0: {
                // Prompt user to begin the mod by opening the gold mine chest.
                say("[NAKOA]: Open the chest quickly then AFK!");
                // Progress the stage of the mod.
                nakoaState = 1;
                tick = 0;
                break;
            }
            case 1: {
                // Wait some amount of ticks.
                if (tick == 360) {
                    // Check if the player has opened the chest as they were prompted.
                    if (player.openContainer instanceof ContainerChest) {
                        // Close the chest.
                        player.closeScreen();
                        // Set the selected hot-bar slot to slot 2. This ensures consistency
                        // because in VentureLand, hot-bar slot 2 is always the same.
                        player.inventory.currentItem = 1;
                        System.out.println("[NAKOA]: STARTING");
                        nakoaState = 2;
                        tick = 0;
                        // Set a time for the next stage to begin execution.
                        tickBuffer = 5 + (int)(Math.random() * 15);
                    } else {
                        // If the player failed to open the chest in 5 seconds, end execution.
                        say("[NAKOA]: No chest was opened");
                        end();
                    }
                }
                break;
            }
            case 2: {
                // Once the random number of ticks have passed, begin attempting to reopen the chest.
                if (tick == tickBuffer) {
                    // If this stage fails 4 times, end execution.
                    if (failCount++ == 4) {
                        say("[NAKOA]: CATASTROPHIC FAILURE");
                        end();
                    } else {
                        // Make sure the player is still looking at the chest.
                        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
                        if (mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockChest) {
                            System.out.println("[NAKOA]: OPENING");
                            
                            MovingObjectPosition mop = player.rayTrace(6.0, 1.0F);
                            BlockPos pos = mop.getBlockPos();
                            int face = mop.sideHit.getIndex();
                            Vec3 hitVec = mop.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());
                            float x = (float)hitVec.xCoord;
                            float y = (float)hitVec.yCoord;
                            float z = (float)hitVec.zCoord;
                            player.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(pos, face, player.inventory.getCurrentItem(), x, y, z));
                            player.sendQueue.addToSendQueue(new C0APacketAnimation());
                            
                            nakoaState = 3;
                        } else {
                            // If the player is not looking at the chest, end execution.
                            end();
                        }
                    }
                    // Random number of ticks until the next stage begins.
                    tickBuffer = 5 + (int)(Math.random() * 15);
                    tick = 0;
                }
                break;
            }
            case 3: {
                if (tick == tickBuffer) {
                    // Check if the chest successfully opened from the packet queued in the previous stage.
                    if (player.openContainer instanceof ContainerChest) {
                        System.out.println("[NAKOA]: CLOSING");
                        // Close the chest.
                        player.closeScreen();
                        // Reset the fail counter of the previous stage because it was successful.
                        failCount = 0;
                        // Set the counter to wait awhile before opening the chest again.
                        tickBuffer = 36000 + (int)(Math.random() * 12000);
                    } else {
                        // If the chest failed to open, wait a bit then try again.
                        tickBuffer = 5 + (int)(Math.random() * 15);
                    }
                    nakoaState = 2;
                    tick = 0;
                }
            }
        }
    }

    private void say(String statement) {
        mc.thePlayer.addChatMessage(new ChatComponentText(statement));
    }

    private void end() {
        System.out.println("[NAKOA]: DISENGAGING");
        nakoa.deactivate();
        tick = 0;
        nakoaState = 0;
        failCount = 0;
        tickBuffer = 0;
    }
}
