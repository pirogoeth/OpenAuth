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
        StringBuilder data = new StringBuilder();
        data.append(String.format("\u00A7a%s's inventory: ", player.getName()));
        PlayerInventory inv = player.getInventory();
        ItemStack[] stacks = inv.getContents();
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            String ds = String.format("%s%s [%s]", ((stack == player.getItemInHand()) ? "\u00A7b" : "\u00A7f"), stack.getType(), stack.getAmount());
            data.append(ds + (i == (stacks.length - 1) ? "" : ", "));
        }
        sender.sendMessage(data.toString());
    }
}