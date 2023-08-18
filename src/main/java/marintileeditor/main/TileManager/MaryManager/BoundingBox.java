package marintileeditor.main.TileManager.MaryManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.logging.Logger;

// Обработано
public class BoundingBox {

	// Тех. часть и переменные коробки
	private final int x1, y1, z1, x2, y2, z2;
	private Logger log = Bukkit.getLogger();

	public BoundingBox(int x1, int y1, int z1, int x2, int y2, int z2) {
		// Правильно сортирует все координаты
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.z1 = Math.min(z1, z2);
		this.x2 = Math.max(x2, x1);
		this.y2 = Math.max(y2, y1);
		this.z2 = Math.max(z2, z1);
	}

	// Проверяет содержит ли коробка данные координаты
	public boolean contains(int x, int y, int z) {
		return (x >= x1 && x <= x2) && (y >= y1 && y <= y2) && (z >= z1 && z <= z2);
	}
	public boolean contains(Block block) {
		return contains(block.getX(), block.getY(), block.getZ());
	}
}
