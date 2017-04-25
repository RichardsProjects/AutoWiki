package net.richardsprojects.autowiki;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class for the AutoWiki plugin.
 *
 * @author RichardB122
 * @version 4/20/17
 */
public class AutoWiki extends JavaPlugin {

    public ConcurrentHashMap<UUID, Long> lastWikiLookup = new ConcurrentHashMap<UUID, Long>();

    @Override
    public void onEnable() {
        getCommand("wiki").setExecutor(new WikiCommand(this));
    }
}
