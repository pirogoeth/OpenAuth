import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.LogHandler;

import com.sk89q.minecraft.util.commands.*;

import java.lang.reflect.*;
import java.util.*;

public class Peek implements IMixin, Listener {

    private final String name = "Peek";
    private OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public Peek() {
        this.controller = (OpenAuth) OpenAuth.getInstance();
    }

    public Peek(OpenAuth instance) {
        this.controller = instance;
    }

    public String getName() {
        return this.name;
    }

    public void onInit() {
        this.controller.getCommandsManagerRegistration().register(Peek.class);
    }

    public void onTeardown() { };

    @Console
    @Command(aliases = {"peek"}, usage = "<user>", desc = "Allows one to peek into the targets inventory.",
             max = 1, min = 1, flags = "ac")
    @CommandPermissions({ "openauth.mixin.peek" })
    public void peek(CommandContext args, CommandSender sender) {
        Player player = OAPlayer.getPlayer(args.getString(0)).getPlayer();
        if (player == null) {
            sender.sendMessage("Player " + args.getString(0) + " is not online.");
            return;
        }
        if (args.hasFlag('c')) { // clears the player's inventory
            player.getInventory().clear();
            sender.sendMessage(String.format("\u00A7a%s's inventory has been cleared.", player.getName()));
            return;
        } else if (args.hasFlag('a')) { // shows the active item
            ItemStack active = player.getItemInHand();
            sender.sendMessage(String.format("\u00A7a%s's active item: %s [%s]", player.getName(), active.getType(), active.getAmount()));
            return;
        }
        ItemStack active = player.getItemInHand();
        StringBuilder data = new StringBuilder();
        data.append(String.format("\u00A7a%s's inventory: \u00A7f", player.getName()));
        PlayerInventory inv = player.getInventory();
        List<ItemStack> stacks = Arrays.asList(inv.getContents());
        for (ItemStack stack : stacks) {
            String ds;
            if (stack == null) {
                continue;
            }
            String c = "\u00A7f";
            if (stack.equals(active)) {
                c = "\u00A7b";
            }
            ds = String.format("%s%s [%s]", c, stack.getType(), stack.getAmount());
            data.append(ds + ", ");
        }
        String s = data.toString();
        s = s.substring(0, s.length() - 2);
        sender.sendMessage(s);
    }
}