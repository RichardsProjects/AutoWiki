package net.richardsprojects.autowiki;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * CommandExecutor for the wiki command.
 *
 * @author RichardB122
 * @version 4/21/17
 */
public class WikiCommand implements CommandExecutor {

    private AutoWiki plugin;

    public WikiCommand(AutoWiki plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length > 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                new WikiLookupTask(args, player).runTaskAsynchronously(plugin);
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        } else {
            return false;
        }
    }

    private class WikiLookupTask extends BukkitRunnable {

        private String[] args;
        private Player player;

        public WikiLookupTask(String[] args, Player player) {
            this.args = args;
            this.player = player;
        }

        public void run() {
            String topic;
            String topicUnderscore;

            // determine topic
            StringBuilder topicBuilder = new StringBuilder();
            for (String arg : args) {
                topicBuilder.append(arg);
                topicBuilder.append(" ");
            }
            topic = topicBuilder.toString();
            topic = topic.substring(0, topic.length() - 1);
            topicUnderscore = topic.replace(" ", "_");

            // check website
            Document doc;
            try {
                doc = Jsoup.connect("https://minecraft.gamepedia.com/Special:Search/" +
                        topicUnderscore).get();
                Elements searchResults = doc.select("div.mw-search-result-heading > a");

                if (searchResults.size() > 0) {
                    // get the first 5 search results
                    ArrayList<String> results = new ArrayList<String>();
                    for (int i = 0; i < searchResults.size() && i < 5; i++) {
                        Element element = searchResults.get(i);
                        results.add(element.attr("title"));
                    }

                    // display these results to the user
                    player.sendMessage(ChatColor.UNDERLINE + topic + ChatColor.RESET + " returne" +
                            "d " + results.size() + " results:");
                    for (String result : results) {
                        // create link
                        String cmd = "/wiki " + result;
                        TextComponent link = new TextComponent(ChatColor.BLUE + result);
                        link.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Look up " + result).create()));
                        player.spigot().sendMessage(link);
                    }
                } else {
                    // check if there is content
                    Elements elements = doc.select("div#mw-content-text > p");
                    if (elements.size() > 0) {
                        String description = elements.get(0).text();

                        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor. BOLD + topic);
                        player.sendMessage(description);
                    } else {
                        String msg = ChatColor.RED + "Unable to find any results for that topic.";
                        player.sendMessage(msg);
                    }
                }
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "The Minecraft Wiki could not accessed. Check " +
                        "your internet connection");
            }

        }
    }
}
