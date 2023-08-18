package marintileeditor.main.TileManager.TileManager.Database;

import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// Обработано
public class Database {
    // Основные переменные
    private File configFile;
    private FileConfiguration cfg;
    private String fileName;

    // Все базы данных
    public static Database STAT_DATABASE;
    public static Database ID_LINK_DATABASE;
    public static Database BUILDINGS_DATABASE;
    public static Database COLOR_CHANGE_DATABASE;
    public static Database GENERALS_DATABASE;
    public static Database INVITE_DATABASE;

    // Тех. часть
    Logger log = Bukkit.getLogger();

    // Устанавливаем базы данных
    public static void setUp() {
        STAT_DATABASE = new Database("database.yml");
        ID_LINK_DATABASE = new Database("il_database.yml");
        BUILDINGS_DATABASE = new Database("buildings.yml");
        COLOR_CHANGE_DATABASE = new Database("color_change.yml");
        GENERALS_DATABASE = new Database("generals.yml");
        INVITE_DATABASE = new Database("invite.yml");
    }

    // Создание базы данных
    public Database(String fileName) {
        this.configFile = new File(Main.getInstance().getDataFolder(), fileName);
        this.fileName = fileName;
        this.cfg = YamlConfiguration.loadConfiguration(this.configFile);

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                this.cfg = YamlConfiguration.loadConfiguration(this.configFile);
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[MarinTileEditor] Файл " + fileName + " успешно создан!");
            }
            catch (IOException i) {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[MarinTileEditor] Произошла ошибка при создании файла " + fileName);
            }
        }
    }

    // Перезагрузка конкретной базы данных
    public void reload() {
        this.configFile = new File(Main.getInstance().getDataFolder(), this.fileName);
        this.cfg = YamlConfiguration.loadConfiguration(configFile);
    }

    // Запись в базу данных
    public void write(String path, String str) {
        this.cfg.set(path, str);
        this.save();
    }

    // Вписываем новый гексагон во владения страны
    public void writeNewHexagon(String path, String hex) {
        String str = null;
        if (this.cfg.getString(path) != null) {
            str = this.cfg.getString(path) + " " + hex;
        } else {
            str = hex;
        }
        this.cfg.set(path, str);
        this.save();
    }

    // Вписывает новый город в государство
    public void writeNewCity(String path, String city) {
        String str;
        if (this.cfg.getString(path) != null && !(this.cfg.getString(path).equals(""))) {
            str = this.cfg.getString(path) + " " + city;
        } else {
            str = city;
        }
        this.cfg.set(path, str);
        this.save();
    }

    // Вписывает новое государство в строку в каких государствах человек был
    public void writeNewStateWhereBe(String path, String state) {
        // state указываем так - Албания
        // path - marinbay.player.don_yagon.wherebe
        String str;
        if (this.cfg.getString(path) != null) {
            str = this.cfg.getString(path) + ", " + state;
        } else {
            str = state;
        }
        this.cfg.set(path, str);
        this.save();
    }

    // Вписывает нового человека в citisens
    public void writePlayerToState(String path, String playerName) {
        // path - путь до citizens .citizens
        String str = this.cfg.getString(path);
        List<String> citizens = new ArrayList<>();
        citizens.addAll(Arrays.asList(str.split(", ")));
        citizens.add(playerName);

        String newCitizens = String.join(", ", citizens);
        this.cfg.set(path, newCitizens);
        this.save();
    }

    // Удаляет игрока из строки игроков citizens и убирает строку state
    // Также как и статус + timeInvite
    public void removePlayer(String path, String playerName) {
        // path - путь до citizens .citizens
        String str = this.cfg.getString(path);
        List<String> citizens = new ArrayList<>();
        citizens.addAll(Arrays.asList(str.split(", ")));
        citizens.remove(playerName);

        String newCitizens = String.join(", ", citizens);
        this.cfg.set(path, newCitizens);
        this.cfg.set("marinbay.player." + playerName.toLowerCase() + ".state", null);
        this.cfg.set("marinbay.player." + playerName.toLowerCase() + ".status", null);
        this.cfg.set("marinbay.player." + playerName.toLowerCase() + ".timeInvite", null);
        this.save();
    }

    // Добавляет элемент в сборище элементов
    // path - путь до сборища элементов
    public void addOneElement(String path, String element) {
        String str;
        if (this.cfg.getString(path) != null && !(this.cfg.getString(path).equals(""))) {
            str = this.cfg.getString(path) + ", " + element;
        } else {
            str = element;
        }
        this.cfg.set(path, str);
        this.save();
    }

    // Перемещает элемент, который уже есть в списке, в начало этого списка
    public void castlingElement(String path, String element) {
        // castling - рокировка

        // Удаляем элемент из списка
        removeOneElement(path, element, ", ");

        ArrayList<String> newElements = new ArrayList<>();
        newElements.add(element);
        newElements.addAll(Arrays.asList(sliceElements(path, ", ")));

        String endElements = String.join(", ", newElements);
        write(path, endElements);
    }

    // Возвращает список объектов с указанным регексом
    public String[] sliceElements(String path, String regex) {
        String elements = this.cfg.getString(path);
        if (elements == null) {return new String[0];}
        return elements.split(regex);
    }

    // Удаляет один элемент из сборища элементов
    // path - путь до сборища элементов
    public void removeOneElement(String path, String element, String regex) {
        String str = this.cfg.getString(path);
        List<String> elements = new ArrayList<>();
        elements.addAll(Arrays.asList(str.split(regex)));
        elements.remove(element);

        String newElements;
        if (elements.size() == 1) {
            newElements = elements.get(0);
        } else {
            newElements = String.join(regex, elements);
        }
        this.cfg.set(path, newElements);
        this.save();
    }

    // Удаляет указанный путь
    public void remove(String path) {
        this.cfg.set(path, null);
        this.save();
    }

    // Проверяет установлено ли значение
    public boolean isSet(String path) {
        return this.cfg.isSet(path);
    }

    // Получаем значение из базы данных
    public String getString(String path) {
        return this.cfg.getString(path);
    }

    // Получаем список значений
    public String[] getStringList(String path) {
        ConfigurationSection configurationSection = this.cfg.getConfigurationSection(path);
        if (configurationSection == null) {
            return new String[0];
        }
        return this.cfg.getConfigurationSection(path).getKeys(false).toArray(new String[0]);
    }

    // Сохраняет базу данных
    public void save() {
        try {
            cfg.save(configFile);
        }
        catch (IOException i) {
            Bukkit.getConsoleSender().sendMessage("[MarinTileEditor] Произошла ошибка в процессе сохранения файла " + fileName);
        }
    }
}
