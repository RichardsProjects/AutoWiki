package net.richardsprojects.autowiki;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

import static sun.audio.AudioPlayer.player;

/**
 * A class with utility methods for the plugin.
 *
 * @author RichardB122
 * @version 4/22/17
 */
public class Utils {

    public static ItemStack newBook(String title, List<IChatBaseComponent> pageList)
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
        bookMeta.setAuthor("Minecraft Wiki");
        book.setItemMeta(bookMeta);

        return book;
    }


    public static void openBook(Player p, ItemStack book){
        int slot = p.getInventory().getHeldItemSlot();
        ItemStack old = p.getInventory().getItem(slot);
        p.getInventory().setItem(slot, book);

        // send a custom packet to open the book
        // http://wiki.vg/Plugin_channels#MC.7CBOpen
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
        PacketDataSerializer.a(0); // 0 tells it to open from the "main hand"
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new
                PacketPlayOutCustomPayload("MC|BOpen", packetdataserializer));

        // restore whatever item was originally in that slot
        p.getInventory().setItem(slot, old);
        p.updateInventory();
    }
}
