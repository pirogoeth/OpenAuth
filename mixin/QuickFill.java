import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.util.LogHandler;

import com.sk89q.minecraft.util.commands.*;

import java.util.*;

public class QuickFill implements IMixin {

    private final String name = "QuickFill";
    private OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public QuickFill() {
        this.controller = (OpenAuth) OpenAuth.getInstance();
    }

    public QuickFill(OpenAuth controller) {
        this.controller = controller;
    }

    public String getName() {
        return this.name;
    }

    public void onInit() {
        this.controller.getCommandsManagerRegistration().register(QuickFill.class);
    }

    public void onTeardown() { };

    @Command(aliases = {"qf", "quickfill"}, usage = "<item no.>", desc = "Quickly fills an inventory container with the specified item.",
             min = 1, max = 1)
    @CommandPermissions({ "quickfill.use" })
    public static void quickfill(CommandContext args, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.BLUE + "You must be a player to use QuickFill! :P");
            return;
        }
        Player player = (Player) sender;
        // Get the block that the player is targeting.
        Block target = player.getTargetBlock(null, 10);
        if (target == null) {
            player.sendMessage(ChatColor.GREEN + "You need to have an inventory block in your crosshairs to use this command.");
            return;
        }
        InventoryHolder container = null;
        if (target.getState() instanceof InventoryHolder) {
            container = (InventoryHolder) target.getState();
        } else {
            player.sendMessage(ChatColor.GREEN + "This is not a valid inventory block.");
            return;
        }
        int type = Integer.parseInt(args.getString(0));
        // Get the inventory instance from the container block.
        Inventory inv = container.getInventory();
        // Set the max stack size in the inventory to 128.
        inv.setMaxStackSize(128);
        // Create an ItemStack of <type> with <inv.getMaxStackSize()> items.
        ItemStack stack = new ItemStack(type);
        stack.setAmount(inv.getMaxStackSize());
        // Put <stack> in each slot of the container's inventory.
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, stack);
        }
        player.sendMessage(ChatColor.GREEN + "Finished stacking!");
    }
}