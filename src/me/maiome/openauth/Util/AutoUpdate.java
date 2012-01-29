package me.maiome.openauth.Util;

// java imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
// bukkit imports
import org.bukkit.util.FileUtil;
import org.bukkit.Bukkit;
// XXX switch to new configuration scheme
import org.bukkit.util.config.Configuration;
// java imports
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.util.logging.Logger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
// internal imports
import me.maiome.openauth.OpenAuth;

public class AutoUpdate {
    public File jar;
    public Logger log = Logger.getLogger("Minecraft");
    public OpenAuth plugin;
    public Configuration main;
    public AutoUpdate (OpenAuth instance) {
        plugin = instance;
        main = (Configuration) plugin.config.getMain();
    }

    // borrowed from Afforess
    public void finalise () {
        try {
            File directory = new File(Bukkit.getServer().getUpdateFolder());
            if (directory.exists()) {
                File p = new File(directory.getPath(), "OpenAuth.jar");
                if (p.exists()) {
                    FileUtil.copy(p, plugin.fileGet());
                    p.delete();
                    log.info("[OpenAuth] Update finalised.");
                }
            }
        }
        catch (Exception e) {}
    }

    protected int getVersion () {
        try {
            String[] split = plugin.getDescription().getVersion().split("\\.");
            return Integer.parseInt(split[0]) * 100 + Integer.parseInt(split[1]) * 10 + Integer.parseInt(split[2]);
        }
        catch (Exception e) {}
        return -1;
    }

    protected boolean checkUpdate () {
        if (!((String) main.getString("autoupdate")).equalsIgnoreCase("true")) {
           log.info("[OpenAuth] Auto-update is disabled.");
           return false;
        }
        try {
            URL versionfile = new URL("http://maio.me/downloads/OpenAuth/version.txt");
            log.info("[OpenAuth] Checking for updates..");
            BufferedReader in = new BufferedReader(new InputStreamReader(versionfile.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                String[] split = str.split("\\.");
                int version = Integer.parseInt(split[0]) * 100 + Integer.parseInt(split[1]) * 10 + Integer.parseInt(split[2]);
                if (version > getVersion()){
                   in.close();
                   log.info(String.format("[OpenAuth] Update found. %s->%s :: Now Updating.", getVersion(), version));
                   return true;
                }
            }
            in.close();
        } catch (Exception e) {
            log.info("[OpenAuth] Error while checking for updates. (It could be a dev build.)");
            return false;
        }
        log.info("[OpenAuth] No updates available.");
        return false;
    }

    public void doUpdate () {
        if (!checkUpdate()) {
            return;
        }
        try {
            URL source = new URL("http://maio.me/downloads/OpenAuth/OpenAuth.jar");
            File directory = new File(Bukkit.getServer().getUpdateFolder());
            if (!directory.exists()) {
               directory.mkdir();
            }
            File plugin = new File(directory.getPath(), "OpenAuth.jar");
            if (!plugin.exists()) {
                HttpURLConnection con = (HttpURLConnection)(source.openConnection());
                ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                FileOutputStream fos = new FileOutputStream(plugin);
                fos.getChannel().transferFrom(rbc, 0, 1 << 24);
                fos.close();
            }
        }
        catch (Exception e) {}
    }
}
