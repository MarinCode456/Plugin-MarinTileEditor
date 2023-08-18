package marintileeditor.main.TileManager.MaryManager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

// Обработано
public class SchematicPaster {

    // Тех. часть
    public static Logger log = Bukkit.getLogger();
    public static World world = Bukkit.getWorld("world");

    // Ставит схематик
    public static boolean placeSchematic(String name, BlockVector3 bv3, int direction, boolean ignoreAir) {
        File schem = new File(Main.getInstance().getDataFolder() + "/schematics/" + name);
        ClipboardFormat format = ClipboardFormats.findByFile(schem);
        Clipboard clipboard;

        try {
            ClipboardReader reader = format.getReader(new FileInputStream(schem));
            clipboard = reader.read();
            log.info(ChatColor.AQUA + "Я успешно загрузил файл с именем " + name);
        } catch (IOException e) {
            log.info(ChatColor.RED + "Ошибка при загрузке файла с именем - " + name);
            return false;
        }

        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), -1);
        ClipboardHolder holder = new ClipboardHolder(clipboard);
        AffineTransform transform = new AffineTransform();

        switch (direction) {
            case 1:
                transform = transform.rotateY(180);
                transform = transform.rotateX(0);
                transform = transform.rotateZ(0);
                break;
            case 2:
                // Именно direction 2 это идеальное состояние для схематика
                transform = transform.rotateY(0);
                transform = transform.rotateX(0);
                transform = transform.rotateZ(0);
                break;
            case 3:
                transform = transform.rotateY(90);
                transform = transform.rotateX(0);
                transform = transform.rotateZ(0);
                break;
            case 4:
                transform = transform.rotateY(270);
                transform = transform.rotateX(0);
                transform = transform.rotateZ(0);
                break;
        }
        holder.setTransform(holder.getTransform().combine(transform));
        Operation operation = holder.createPaste(session).to(bv3).ignoreAirBlocks(ignoreAir).build();

        try {
            Operations.complete(operation);
            session.close();
        } catch (WorldEditException e) {
            return false;
        }

        return true;
    }
}
