package marintileeditor.main.TileManager.TileManager.Dynmap;

import com.fasterxml.uuid.Generators;
import library.Hexagon;
import library.HexagonComponents.HexagonSide;
import library.HexagonalGrid;
import library.Point;
import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.MarinBayUtil;
import marintileeditor.main.TileManager.TileManager.API.SMassiveAPI;
import marintileeditor.main.TileManager.TileManager.City.City;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

// Обработано
public class DynmapDrawer {
    public DynmapDrawer() {
    }

    // Рисует гексагоны
    public void drawFigure(Hexagon[] hexagons, String color, DynmapDescription description) {
        if (hexagons.length == 0) {
            return;
        }
        HexagonalGrid grid = hexagons[0].getGrid();
        HexagonSide[] sides = grid.getBounds(hexagons);
        this.draw(hexagons, color, description);
        this.draw(sides, description, color);
    }

    // Множество функций, которые дружно работают над построением гексагонов и их линий
    public void draw(Hexagon[] hexagons, String color, DynmapDescription description) {
        for (Hexagon hexagon : hexagons) {
            this.draw(hexagon, color, description);
        }
    }
    public void draw(HexagonSide side, DynmapDescription description, String color) {
        Point[] points = side.getEnds();
        drawLine(points, description, color);
    }
    public void draw(HexagonSide[] sides, DynmapDescription description, String color) {
        for (HexagonSide side : sides) {
            this.draw(side, description, color);
        }
    }
    public void draw(Hexagon hexagon, String color, DynmapDescription description) {
        Point[] points = hexagon.getVertexPositions();
        double[] x = new double[points.length];
        double[] z = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            x[i] = points[i].getX();
            z[i] = points[i].getY();
        }

        String id = Generators.timeBasedGenerator().generate().toString();
        MarkerSet marker = Main.getDynmap().getMarkerAPI().getMarkerSet("marin");
        AreaMarker am = marker.createAreaMarker(id, description.getName(), true, "world", x, z, false);
        // Проверяем первый символ в string
        // Кстати, символы в джае пишутся только в одинарных кавычках, а стринги в двойных
        if (color.charAt(0) == '#') {
            // substring возвращает только часть данной строки, конкретно здесь из формата #FFFF00 строка
            // превращается в FFFF00
            color = color.substring(1);
        }

        // Меняем цвета области
        // radix 16 это обычная система счисления, то есть если напишешь 11, то он и вернет 11, а вот если
        // менять radix, начинается жопа
        am.setFillStyle(0.4D, Integer.parseInt(color.toUpperCase(),16));
        am.setLineStyle(0, 0.05D, Integer.parseInt(color.toUpperCase(), 16));
        am.setBoostFlag(false);
        this.setHexagonDescription(id, description);
    }
    public void draw(Point point, String icon, DynmapDescription description) {
        String id = Generators.timeBasedGenerator().generate().toString();

        Location locOfCapital = new Location(Bukkit.getWorld("world"), point.getX(), 64, point.getY());
        Point centerOfCapitalHexagon = getHexagonCenter(locOfCapital);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmarker add id:" + id + " " + description.getName() + " set:marin x:" + centerOfCapitalHexagon.getX() + " y:64 z:" + centerOfCapitalHexagon.getY() + " icon:" + icon + " world:world");
        this.setMarkerDescription(id, description);
    }

    // Получает точку центра гексагона
    public Point getHexagonCenter(Location locOfCapital) {
        Hexagon hexOfCapital = Main.grid.getHexagon(locOfCapital);

        Point[] points = hexOfCapital.getVertexPositions();
        double[] x = new double[points.length];
        double[] z = new double[points.length];

        double centerX = 0;
        double centerZ = 0;

        for (int i = 0; i < points.length; i++) {
            x[i] = points[i].getX();
            z[i] = points[i].getY();
        }
        for (double xPoint : x) {
            centerX += xPoint;
        }
        for (double zPoint : z) {
            centerZ += zPoint;
        }
        return new Point(centerX / x.length, centerZ / z.length);
    }

    // Рисует обводку гексагонов
    public void drawLine(Point[] points, DynmapDescription description, String color) {
        double[] x = new double[points.length];
        double[] y = new double[points.length];
        double[] z = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            x[i] = points[i].getX();
            y[i] = 64;
            z[i] = points[i].getY();
        }

        if (color.charAt(0) == '#') {
            // substring возвращает только часть данной строки, конкретно здесь из формата #FFFF00 строка
            // превращается в FFFF00
            color = color.substring(1);
        }

        String id = Generators.timeBasedGenerator().generate().toString();
        MarkerSet m = Main.getDynmap().getMarkerAPI().getMarkerSet("marin");
        PolyLineMarker p = m.createPolyLineMarker(id, description.getName(), true, "world", x, y, z, false);
        p.setLineStyle(2, 1D, Integer.parseInt(color.toUpperCase(), 16));
        this.setSideDescription(id,description);
    }

    // Рисует все города
    public void drawAllCities() {
        City[] cities = Main.getCityMap().getCities();
        if (cities.length == 0) {
            return;
        }
        for (int i = 0; i < cities.length; i++) {
            cities[i].draw();
        }
    }

    // Рисует все казармы
    public void drawAllBarracks() {
        Barrack[] barracks = MarinBayUtil.getAllBarracks().toArray(new Barrack[0]);
        if (barracks.length == 0) {
            return;
        }
        for (int i = 0; i < barracks.length; i++) {
            barracks[i].draw();
        }
    }

    // Меняет описание маркера на указанное
    public void setMarkerDescription(String id, DynmapDescription description) {
        String descriptionString = this.generateDescriptionLine(description.getDescriptionsStrings());
        DynmapAPI dynmap = Main.getDynmap();
        MarkerSet m = dynmap.getMarkerAPI().getMarkerSet("marin");
        Marker mm = m.findMarker(id);
        mm.setDescription(descriptionString);
        return;
    }

    // Меняет описание гексагона на указанное
    public void setHexagonDescription(String id, DynmapDescription description) {
        String descrpString = this.generateDescriptionLine(description.getDescriptionsStrings());
        DynmapAPI dynmap = Main.getDynmap();
        MarkerSet m = dynmap.getMarkerAPI().getMarkerSet("marin");
        AreaMarker am = m.findAreaMarker(id);
        am.setDescription(descrpString);
        return;
    }

    // Меняет описание линии на указанное
    public void setSideDescription(String id, DynmapDescription description) {
        String descriptionString = this.generateDescriptionLine(description.getDescriptionsStrings());
        DynmapAPI dynmap = Main.getDynmap();
        MarkerSet m = dynmap.getMarkerAPI().getMarkerSet("marin");
        PolyLineMarker p = m.findPolyLineMarker(id);
        p.setDescription(descriptionString);
        return;
    }

    // Очищаем динмап, удаляя все точки
    public static void clearDynmap() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmarker deleteset id:marin world:world");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmarker addset Государства id:marin world:world");
    }

    // Перезагрузка динмапа
    public static void redrawDynmap() {
        DynmapDrawer.clearDynmap();
        long time0 = System.currentTimeMillis();
        DynmapDrawer drawer = new DynmapDrawer();
        drawer.drawAllCities();
        drawer.drawAllBarracks();
        long time1 = System.currentTimeMillis();

        long time = time1 - time0;
    }

    // Создаём строчку описания
    private String generateDescriptionLine(String[] strings) {
        return SMassiveAPI.toString(strings, "<br>");
    }
}
