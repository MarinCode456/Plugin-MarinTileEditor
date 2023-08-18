package marintileeditor.main.TileManager.TileManager.MrPlaceholder;

import library.Hexagon;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class StatePlaceholder extends PlaceholderExpansion {

    // Базы данных
    Database databaseDB = Database.STAT_DATABASE;

    @Override
    public String getIdentifier() {
        return "marintileeditor";
    }
    @Override
    public String getAuthor() {
        return "Rostislav_Kraitov";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if (p == null) { return ""; }

        if (params.equals("territory")) {
            Hexagon hex = Main.grid.getHexagon(p);
            String strHex = hex.toString();

            if (databaseDB.isSet("marinbay.hexagons." + strHex)) {
                String owner = databaseDB.getString("marinbay.hexagons." + strHex + ".owner");
                if (owner != null) {
                    String ownerName = databaseDB.getString("marinbay.state." + owner + ".name");
                    return ownerName;
                }
            }
            return "Отсутствует";
        }

        if (params.equals("state")) {
            String pName = p.getName().toLowerCase();

            if (databaseDB.isSet("marinbay.player." + pName + ".state")) {
                String stateID = databaseDB.getString("marinbay.player." + pName + ".state");
                if (stateID != null) {
                    String stateName = databaseDB.getString("marinbay.state." + stateID + ".name");
                    return stateName;
                }
            }
            return "Отсутствует";
        }

        if (params.equals("rank")) {
            String pName = p.getName().toLowerCase();

            if (databaseDB.isSet("marinbay.player." + pName + ".status")) {
                String pStatus = databaseDB.getString("marinbay.player." + pName + ".status");
                if (pStatus.equals("general")) {
                    return "Государь";
                } else if (pStatus.equals("closer")) {
                    return "Приближённый";
                } else if (pStatus.equals("guber")) {
                    return "Губернатор";
                } else if (pStatus.equals("citizen")) {
                    return "Гражданин";
                }
            }
            return "Отсутствует";
        }

        return "";
    }
}
