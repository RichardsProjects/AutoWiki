package net.richardsprojects.autowiki;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaBook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

/**
 * A class with utility methods for the plugin.
 *
 * @author RichardB122
 * @version 4/22/17
 */
public class Utils {

    public static ItemStack newBook(String title, String author, List<IChatBaseComponent> pageList)
    {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        List<IChatBaseComponent> pages;

        // get the pages
        try {
            pages = (List<IChatBaseComponent>) CraftMetaBook.class.getDeclaredField("pages").get(bookMeta);
            pages.addAll(pageList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // book meta
        bookMeta.setTitle(title);
        bookMeta.setAuthor(author);
        book.setItemMeta(bookMeta);

        return book;
    }

}
