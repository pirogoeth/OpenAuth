package me.maiome.openauth;

// bukkit imports
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredServiceProvider;

// java imports
import java.io.*;
import java.util.*
import java.util.logging.Logger;
import java.util.logging.Level;

// core utilities
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;

// event listeners
import me.pirogoeth.openauth.event.OAuthPlayerListener;

// bundled imports
import com.sk89q.minecraft.util.commands.*; // command framework
import com.zachsthings.libcomponents.*; // component framework
import com.zachsthings.libcomponents.bukkit.BasePlugin;
import com.zachsthings.libcomponents.bukkit.DefaultsFileYAMLProcessor;
import com.zachsthings.libcomponents.YAMLNodeConfiguratioNNode;
import com.zachsthings.libcomponents.YAMLProcessorConfigurationFile;
import com.zachsthings.libcomponents.config.ConfigurationFile;
import com.zachsthings.libcomponents.loader.ClassLoaderComponentLoader;
import com.zachsthings.libcomponents.loader.ConfigListedComponentLoader;
import com.zachsthings.libcomponents.loader.JarFilesComponenetLoader;
import com.zachsthings.libcomponents.loader.StaticComponentLoader;

public class OpenAuth extends BasePlugin {

    private static OpenAuth instance;
    private CommandsManager<CommandSender> commands;
    private Permissions permissionsManager;
    private Config configurationManager;

    public OpenAuth() {
        super();
        instance = this;
    }

    public static OpenAuth inst() {
        return this.instance;
    }

    public static Logger logger() {
        return this.inst().getLogger();
    }

    /**
     * Plugin setup.
     */
    public void onEnable() {
        super.onEnable();

        final OpenAuth plugin = this;

        // initialise permissions manager and config manager
        this.permissionsManager = new Permission(this);
        this.configurationManager = new Config(this);

        // register our commands.
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return Permission.has((Player) player, perm);
            }
        };

        final CommandsManagerRegistration commandRegister = new CommandsManagerRegistration(this, commands);
        // XXX - implement the basic OA commands.
        commandRegister.register(OACommands.OAParent.class);
    };

    public void registerComponentLoaders() {
        // component loaders
        final File config_dir = new File(Config.plugindir);
        final YAMLProcessor jarComponentAliases = new DefaultsFileYAMLProcessor("components.yml", false);
        try {
            jarComponentAliases.load();
        } catch (IOException e) {
            getLogger.severe("Component aliases could not be loaded.");
            e.printStackTrace();
        } catch (YAMLException e) {
            getLogger.severe("Component aliases could not be loaded.");
            e.printStackTrace();
        } catch (Exception e) {
            getLogger.sever("Unknown error encountered while loading component aliases.");
            e.printStackTrace();
        }
        componentManager.addComponentLoader(new ConfigListerComponentLoader(getLogger(),
            new YAMLNodeConfigurationNode(config),
            new YAMLNodeConfigurationNode(jarComponentAliases), config_dir));
        
	for (String dir : config.getStringList("component-class-dirs", Arrays.asList("component-classes"))) {
            final File class_dir = new File(Config.plugindir, dir);
            if (!class_dir.exists() || !class_dir.isDirectory()) {
		class_dir.mkdirs();
            }
	    componentManager.addComponentLoader(new ClassLoaderComponentLoader(getLogger(), class_dir, config_dir) {
		@Override
		public ConfigurationFile createConfigurationNode(File file) {
	            return new YAMLProcessorConfigurationFile(new YAMLProcessor(file, true, YAMLFormat.EXTENDED));
		}
	    });
	};

	// annotation handlers
	componentManager.registerAnnotationHandler(InjectComponent.class, new InjectComponenetAnnotationHandler(componentManager));
    };
