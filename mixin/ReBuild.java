import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.LogHandler;

import com.sk89q.minecraft.util.commands.*;

import java.lang.reflect.*;
import java.util.*;

public class ReBuild implements IMixin, Listener {

    private final String name = "ReBuild";
    private OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    // data store
    private Map<String, Boolean> tracking = new HashMap<String, Boolean>(); // <playerName, tracking?>
    private Map<String, String> rebuildSetNames = new HashMap<String, String>(); // <playerName, setName>
    private Map<String, List<BlockState>> rebuildSets = new HashMap<String, List<BlockState>>(); // <"playerName:setName", list of blockstates>

    public ReBuild() {
        this.controller = (OpenAuth) OpenAuth.getInstance();
    }

    public ReBuild(OpenAuth controller) {
        this.controller = controller;
    }

    public String getName() {
        return this.name;
    }

    public void onInit() {
        this.controller.getCommandsManagerRegistration().register(ReBuild.class);
        this.controller.getServer().getPluginManager().registerEvents(this, OpenAuth.getInstance());
    }

    public void onTeardown() { };

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block b = event.getBlock();
        BlockState s = b.getState();
        Player player = event.getPlayer();
        if (tracking.containsKey(player.getName())) {
            if (tracking.get(player.getName()).equals(true)) {
                String setName = rebuildSetNames.get(player.getName());
                String rebuildKey = player.getName() + ":" + setName;
                List<BlockState> blockSet = rebuildSets.get(rebuildKey);
                blockSet.add(s);
                rebuildSets.put(rebuildKey, blockSet);
            } else {
                return;
            }
        } else if (!(tracking.containsKey(player.getName()))) {
            tracking.put(player.getName(), false);
            rebuildSetNames.put(player.getName(), "");
            return;
        }
    }

    @Command(aliases = {"rebuild"}, usage = "<name>", desc = "Manages selected rebuild set.", max = 1, min = 0, flags = "acdsl")
    @CommandPermissions({"openauth.mixin.rebuild"})
    public void rebuildCmd(CommandContext args, CommandSender sender) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage("You must be a player to send messages.");
            return;
        }
        String setName = null;
        try {
            setName = args.getString(0);
        } catch (java.lang.Exception e) { }
        String combStateKey = sender.getName() + ":" + setName;
        if (args.hasFlag('a')) {
            if (rebuildSets.containsKey(combStateKey)) {
                for (BlockState state : rebuildSets.get(combStateKey)) {
                    state.update();
                }
                sender.sendMessage(ChatColor.BLUE + String.format("Reset %d blocks!", rebuildSets.get(combStateKey).size()));
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid set name!");
            }
            return;
        } else if (args.hasFlag('c')) {
            if (tracking.containsKey(sender.getName()) && setName == null) {
                if (tracking.get(sender.getName()).equals(true)) {
                    tracking.put(sender.getName(), false);
                    sender.sendMessage(ChatColor.BLUE + "No longer tracking.");
                } else if (!(tracking.get(sender.getName()).equals(true))) {
                    tracking.put(sender.getName(), true);
                    sender.sendMessage(ChatColor.BLUE + "Now tracking.");
                }
                return;
            } else if (tracking.containsKey(sender.getName()) && rebuildSetNames.get(sender.getName()).equalsIgnoreCase(setName)) {
                if (tracking.get(sender.getName()).equals(true)) {
                    tracking.put(sender.getName(), false);
                    sender.sendMessage(ChatColor.BLUE + "No longer tracking.");
                } else if (!(tracking.get(sender.getName()).equals(true))) {
                    tracking.put(sender.getName(), true);
                    sender.sendMessage(ChatColor.BLUE + "Now tracking.");
                }
                return;
            } else if (tracking.containsKey(sender.getName()) && tracking.get(sender.getName()).equals(true) && !(rebuildSetNames.get(sender.getName()).equalsIgnoreCase(setName))) {
                rebuildSetNames.put(sender.getName(), setName);
                sender.sendMessage(ChatColor.BLUE + "Sets switched; Now tracking set: " + setName + ".");
            } else if (!(rebuildSets.containsKey(combStateKey))) {
                tracking.put(sender.getName(), true);
                rebuildSetNames.put(sender.getName(), setName);
                rebuildSets.put(combStateKey, new ArrayList<BlockState>());
                sender.sendMessage(ChatColor.BLUE + "Tracking set: " + setName + ".");
            }
            return;
        } else if (args.hasFlag('d')) {
            if (!(rebuildSets.containsKey(combStateKey))) {
                sender.sendMessage(ChatColor.BLUE + "No set by that name to remove.");
            } else if (rebuildSets.containsKey(combStateKey)) {
                rebuildSets.remove(combStateKey);
                sender.sendMessage(ChatColor.BLUE + "Removed set: " + setName + ".");
            }
            return;
        } else if (args.hasFlag('s')) {
            if (!(tracking.containsKey(sender.getName()))) {
                sender.sendMessage(ChatColor.BLUE + "Not currently tracking any sets.");
                return;
            } else if (tracking.containsKey(sender.getName())) {
                tracking.put(sender.getName(), false);
                sender.sendMessage(ChatColor.BLUE + "Stopped tracking.");
                return;
            }
        } else if (args.hasFlag('l')) {
            int count = 0;
            for (Map.Entry<String, List<BlockState>> entry : rebuildSets.entrySet()) {
                String name = ((String) entry.getKey()).split("\\:")[0];
                sender.sendMessage(ChatColor.BLUE + "Rebuild sets:");
                if (name.equalsIgnoreCase(sender.getName())) {
                    String _setName = ((String) entry.getKey()).split("\\:")[1];
                    int setLength = entry.getValue().size();
                    sender.sendMessage(ChatColor.GREEN + String.format(" * %s [%d blocks]", _setName, setLength));
                    count++;
                }
            }
            if (count == 0) {
                sender.sendMessage(ChatColor.BLUE + "No sets are stored for you.");
                return;
            }
            return;
        }
    }
}