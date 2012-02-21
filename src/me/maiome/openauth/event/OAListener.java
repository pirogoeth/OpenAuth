package me.maiome.openauth.event;

import com.sk89q.util.StringUtil; // string processing

// bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

// internal
import me.maiome.openauth.bukkit.OpenAuth;

public class OAListener implements Listener {

    private final OpenAuth oa;

    public OAListener(OpenAuth oa) {
        this.oa = oa;
    }

    /**
     * Called when a player tries to use a command.
     *
     * Most of this method is "borrowed" from WorldEdit.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        String[] split = event.getMessage().split(" ");

        if (split.length > 0) {
            split = this.oa.detectCommands(split);
            final String label = split[0];
            split[0] = "/" + split[0];
        }

        final String new_message = StringUtil.joinString(split, " ");
        if (!(new_message.equals(event.getMessage()))) {
            event.setMessage(new_message);
            this.oa.getServer().getPluginManager().callEvent(event);
            if (!(event.isCancelled())) {
                if (event.getMessage().length() > 0) {
                    this.oa.getServer().dispatchCommand(
                        event.getPlayer(),
                        event.getMessage().substring(1));
                }
                event.setCancelled(true);
            }
        }
    }

    /** 
     * Called when a player logs in.
     *
     * This will be used to for ban and whitelist features.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
	// this is a STUUUUB
	return;
    }
}
