package marintileeditor.main.TileManager.TileManager.City;

import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDescription;
import marintileeditor.main.TileManager.TileManager.Player.MarinPlayer;

// Обработано
public class CityInfo {

    // Основные переменные
    private String name;
    private MarinPlayer general;
    private String residents;
    private String maximumCitizens;

    public CityInfo(City city) {
        this.name = city.getName();
        this.residents = city.getResidentsString();
        this.general = city.getGeneral();
        this.maximumCitizens = city.getMaxCitizens();
    }

    // Создаём описание для плашки информация государства
    public DynmapDescription toDynmapFormat() {
        if (maximumCitizens == null) {
            String[] strings = new String[3];
            strings[0] = "<center><strong>"+this.name+"</strong></center>";
            strings[1] = "Великий государь: " + this.general.getName()+"";
            strings[2] = "Жители: " + residents;
            return new DynmapDescription(this.name, strings);
        }
        String[] strings = new String[4];
        strings[0] = "<center><strong>"+this.name+"</strong></center>";
        strings[1] = "Великий государь: " + this.general.getName()+"";
        strings[2] = "Макс. Жителей: " + maximumCitizens;
        strings[3] = "Жители: " + residents;
        return new DynmapDescription(this.name, strings);
    }
}
