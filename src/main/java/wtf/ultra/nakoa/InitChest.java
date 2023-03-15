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

import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

public class InitChest implements ICommand {

    private final Minecraft mc = Minecraft.getMinecraft();
    private BlockPos blockPos;
    private boolean active = false;

    public void processCommand(ICommandSender icommandsender, String[] args) {
        // Store the coordinates of the block that the player is looking at.
        blockPos = mc.objectMouseOver.getBlockPos();
        // Get the block from the block state of the coordinates and check if it is a chest.
        if (mc.objectMouseOver.typeOfHit == BLOCK && mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockChest) {
            // Activate the mod for execution in the PlayerTick handler.
            active = true;
        } else {
            // Clear the block position being stored and inform the player of their failure.
            blockPos = null;
            mc.thePlayer.addChatMessage(new ChatComponentText("dis ain't no ches fr"));
        }
    }

    /* End execution of the mod by setting active to false and clear the block position being stored. */
    public void deactivate() {
        active = false;
        blockPos = null;
    }

    /* Returns the coordinates of the chest the player initialized the mod with. */
    public BlockPos chestPos() {
        return blockPos;
    }

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
}