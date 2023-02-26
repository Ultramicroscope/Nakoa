package wtf.ultra.nakoa;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import javax.annotation.Nonnull;

public class InitChest implements ICommand {

    private final Minecraft mc = Minecraft.getMinecraft();
    public boolean active = false;

    public String getCommandName() {
        return "nakoa";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "nakoa";
    }

    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        commandAliases.add("nak");
        return commandAliases;
    }

    public void processCommand(ICommandSender icommandsender, String[] args) {
        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
        if (mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockChest) {
            new Thread(() -> {
                EntityPlayerSP player = mc.thePlayer;
                say("Open chest within 5s then AFK!");
                try {
                    Thread.sleep(5000);
                    if (player.openContainer instanceof ContainerChest) {
                        player.closeScreen();
                        active = true;
                        int slot;
                        while ((slot = player.inventory.currentItem) != 1) player.inventory.changeCurrentItem(slot - 1);
                        System.out.println("[NAKOA]: STARTING");
                        Thread.sleep(2000);
                        repeat: while(active && blockPos.distanceSq(player.getPosition()) <= 10) {
                            int failCount = -1;
                            while (!(player.openContainer instanceof ContainerChest)) {
                                if (++failCount == 4) {
                                    active = false;
                                    say("catastrophic failure");
                                    break repeat;
                                }
                                System.out.println("[NAKOA]: OPENING");
                                active = false;
                                player.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(player.getPosition(), -1, player.getHeldItem(),-1,-1,-1));
                                Thread.sleep((int)(Math.random() * 750 + 250));
                                active = true;
                            }
                            System.out.println("[NAKOA]: CLOSING");
                            active = false;
                            player.closeScreen();
                            active = true;
                            Thread.sleep((int)(Math.random() * 600000 + 1800000));
                        }
                    } else {
                        say("No chest was opened.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (active) {
                    active = false;
                    say("Nakoa disengaged.");
                }
            }).start();
        } else say("dis ain't no ches fr");
    }

    public boolean canCommandSenderUseCommand(ICommandSender icommandsender){
        return true;
    }

    public List<String> addTabCompletionOptions(ICommandSender icommandsender, String[] strings, BlockPos pos){
        return null;
    }

    public boolean isUsernameIndex(String[] strings, int i){
        return false;
    }

    public int compareTo(@Nonnull ICommand a) {
        return 0;
    }

    public void say(String statement) {
        mc.thePlayer.addChatMessage(new ChatComponentText(statement));
    }
}