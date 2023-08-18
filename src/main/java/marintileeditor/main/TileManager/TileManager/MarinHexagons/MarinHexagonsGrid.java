package marintileeditor.main.TileManager.TileManager.MarinHexagons;

import library.*;
import marintileeditor.main.TileManager.TileManager.Player.MarinPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// Обработано
public class MarinHexagonsGrid extends HexagonalGrid {
    public static final MarinHexagonsGrid CLASSIC_100 = new MarinHexagonsGrid(Orientation.FLAT, new Point(0,0), new Point(50,50), new Morton64(2, 16));

    // Создаём сборище гексагонов
    public MarinHexagonsGrid (Orientation orientation, Point point, Point size, Morton64 mort) {
        super(orientation, point, size, mort);
    }

    // Получаем гексагон по разным переменным
    public Hexagon getHexagon(Player p) {
        Location loc = p.getLocation();
        return this.getHexagon(loc);
    }
    public Hexagon getHexagon(MarinPlayer marinPlayer) {
        Player p = marinPlayer.getPlayer();
        return this.getHexagon(p);
    }
    public Hexagon getHexagon(Location loc) {
        Point point = new Point(loc.getX(), loc.getZ());
        return this.getHexagon(point);
    }
}
