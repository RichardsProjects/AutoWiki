package net.richardsprojects.autowiki;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Main class for the AutoWiki plugin
 *
 * @author RichardB122
 * @version 4/20/17
 */
public class AutoWiki extends JavaPlugin {

    public static Logger log;

    @Override
    public void onEnable() {
        log = this.getLogger();

        getCommand("wiki").setExecutor(new WikiCommand(this));
    }
}
