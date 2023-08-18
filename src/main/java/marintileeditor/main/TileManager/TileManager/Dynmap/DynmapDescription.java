package marintileeditor.main.TileManager.TileManager.Dynmap;

// Обработано
public class DynmapDescription {

    // Основные переменные
    private String name;
    private String[] description;

    public DynmapDescription(String name, String[] description) {
        this.name = name;
        this.description = description;
    }

    // Геттеры
    public String getName() {
        return name;
    }
    public String[] getDescriptionsStrings() {
        return this.description;
    }
}
