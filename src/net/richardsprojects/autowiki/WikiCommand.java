package net.richardsprojects.autowiki;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        WikiLookupTask(String[] args, Player player) {
            this.args = args;
            this.player = player;
        }

        public void run() {
            // check against cooldown
            if (plugin.lastWikiLookup.containsKey(player.getUniqueId())) {
                Date currentTime = new Date();
                long currentTimestamp = currentTime.getTime() / 1000;
                long lastExecutedTimestamp = plugin.lastWikiLookup.get(player.getUniqueId());

                if ((currentTimestamp - 10) <= lastExecutedTimestamp) {
                    long timeRemaining = 10 - (currentTimestamp - lastExecutedTimestamp);
                    player.sendMessage(ChatColor.RED + "Please wait " + timeRemaining + " seconds" +
                            " before searching the Wiki again.");
                    return;
                }
            }

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

            // variables for book response
            List<IChatBaseComponent> pages = new ArrayList<IChatBaseComponent>();
            String bookTitle = null;

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

                    // create heading and ComponentBuilder
                    ComponentBuilder cb = new ComponentBuilder("");
                    cb.underlined(true);
                    cb.append(topic + " Results: \n\n");
                    cb.underlined(false);

                    // add search results
                    for (int i = 0; i < results.size(); i++) {
                        String result = results.get(i);

                        // create result link
                        String cmd = "/wiki " + result;
                        cb.append(ChatColor.BLUE + "" + (i +1) + ". " + result).
                                event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd)).
                                event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder("Look up " + result).create()));
                        cb.append("\n\n");
                    }

                    // create the actual page
                    IChatBaseComponent page = IChatBaseComponent.ChatSerializer.a(
                            ComponentSerializer.toString(cb.create()));
                    pages.add(page);

                    bookTitle = "Search Results: " + topic;
                } else {
                    // check if there is content
                    Elements elements = doc.select("div#mw-content-text > p");
                    if (elements.size() > 0) {
                        String description = elements.get(0).text();

                        // regex to remove references ([1]) from descriptions.
                        description = description.replaceAll("\\[\\d+\\]", "");

                        // create topic title with link
                        ComponentBuilder cb = new ComponentBuilder("");
                        cb.color(ChatColor.BLUE);
                        cb.bold(true);
                        cb.underlined(true);
                        String url = "http://minecraft.gamepedia.com/" + topicUnderscore;
                        cb.append(topic).event(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ComponentBuilder(url).create()));
                        cb.append("\n\n");

                        cb.reset();
                        cb.append(description);

                        // create the actual page
                        IChatBaseComponent page = IChatBaseComponent.ChatSerializer.a(
                                ComponentSerializer.toString(cb.create()));
                        pages.add(page);

                        bookTitle = topic;

                        // only prevent them from using the command if actual content was returned
                        Date currentTime = new Date();
                        long currentTimestamp = currentTime.getTime() / 1000;
                        plugin.lastWikiLookup.put(player.getUniqueId(), currentTimestamp);
                    } else {
                        String msg = ChatColor.RED + "Unable to find any results for that topic.";
                        player.sendMessage(msg);
                    }
                }


                // create the book and show it to the player
                ItemStack book = Utils.newBook(bookTitle, pages);
                Utils.openBook(player, book);
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "The Minecraft Wiki could not accessed. Check " +
                        "your internet connection");
            }

        }
    }
}
