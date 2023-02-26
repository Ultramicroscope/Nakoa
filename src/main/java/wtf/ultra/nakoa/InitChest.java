package wtf.ultra.nakoa;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import javax.annotation.Nonnull;

public class InitChest implements ICommand {

    private final Minecraft mc = Minecraft.getMinecraft();
    private BlockPos blockPos;
    private boolean active = false;

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
        blockPos = mc.objectMouseOver.getBlockPos();
        if (mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockChest) {
            active = true;
        } else mc.thePlayer.addChatMessage(new ChatComponentText("dis ain't no ches fr"));
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

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public BlockPos chestPos() {
        return blockPos;
    }
}