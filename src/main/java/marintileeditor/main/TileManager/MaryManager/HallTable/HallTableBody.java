package marintileeditor.main.TileManager.MaryManager.HallTable;

import marintileeditor.main.TileManager.MaryManager.BoundingBox;
import marintileeditor.main.TileManager.MaryManager.MarinBayUtil;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.logging.Logger;

// Обработан
public class HallTableBody {

    // Основные переменные
    private final HallTable ht;
    private final Block sign;
    private BoundingBox bounds;
    private byte signoffset;

    // Базы данных
    private static Database buildingDB = Database.BUILDINGS_DATABASE;

    // Тех. часть
    Logger log = Bukkit.getLogger();

    public HallTableBody(HallTable ht) {
        this.ht = ht;
        this.bounds = new BoundingBox(0, 0, 0, 0, 0, 0);
        sign = ht.getSign();

        // Если столик уже существует, то возьмётся его BoundingBox
        if (buildingDB.isSet("halltables." + ht.getName() + ".bounds")) {
            this.bounds = Main.getInstance().getBounds("halltables." + ht.getName() + ".bounds");
        }
    }

    // Геттеры сеттеры
    public String toString() {
        return ChatColor.GOLD + "Мой хт " + ht.toString();
    }
    public Block getBrokenBlock() {
        return checkHallTable();
    }
    public BoundingBox getBounds() {
        if (buildingDB.isSet("halltables." + ht.getName() + ".bounds")) {
            this.bounds = Main.getInstance().getBounds("halltables." + ht.getName() + ".bounds");
        } else {
            this.bounds = new BoundingBox(0, 500, 0, 0, 499, 0);
        }
        return bounds; }
    public static int getDirection(Block sign) {
        int direction = 0;
        Material type = sign.getRelative(0,0,1).getType();
        if (MarinBayUtil.isLog(type)) {
            direction = 3;
        }

        type = sign.getRelative(0,0,-1).getType();
        if (MarinBayUtil.isLog(type)) {
            if (direction == 0) {
                direction = 4;
            } else {
                return 0;
            }
        }

        type = sign.getRelative(1,0,0).getType();
        if (MarinBayUtil.isLog(type)) {
            if (direction == 0) {
                direction = 1;
            } else {
                return 0;
            }
        }

        type = sign.getRelative(-1,0,0).getType();
        if (MarinBayUtil.isLog(type)) {
            if (direction == 0) {
                direction = 2;
            } else {
                return 0;
            }
        }

        return direction;
    }

    // Проверяет правильно ли ты поставил столик
    public Block checkHallTable() {
        // Получаем направление таблички
        int direction = getDirection(sign);  // 1=x+ 2=x- 3=z+ 4=z-

        // Координаты, по которым проверяем материалы всех блоков
        int startX;
        int startZ;
        int endX;
        int endZ;

        //                    z-1 (direction 4)
        //                       ---------
        // x-1 (direction 2) --- постройка --- x+1 (direction 1)
        //                       ---------
        //                    z+1 (direction 3)
        if (direction == 1) {
            startX = 0;
            startZ = -1;
        } else if (direction == 2) {
            startX = -2;
            startZ = -1;
        } else if (direction == 3) {
            startX = -1;
            startZ = 0;
        } else {
            startX = -1;
            startZ = -2;
        }

        // Конечные точки проверки
        endX = startX + 2;
        endZ = startZ + 2;

        Material type;
        int x = startX;
        int y = -1;
        int z = startZ;

        // Просто болванка, чтобы возвратить её, если создание бочки не получилось
        Block block_bolvan = sign.getRelative(x, y, z);

        // Если в getDirection() получен 0, то следующий блок кода выполнять ненужно, ведь
        // Постройка точно не подходит под стол мэрии
        if (direction == 0) {
            return block_bolvan;
        }

        // Этот цикл проверяет только нижнюю часть стола, где поставлены каменные блоки
        while (y < 0) {
            while (x <= endX) {
                while (z <= endZ) {
                    Block block = sign.getRelative(x, y, z);
                    type = block.getType();

                    if (MarinBayUtil.isSmoothStone(type) || MarinBayUtil.isSmoothHalfStone(type)) {
                        if (MarinBayUtil.isSmoothHalfStone(type)) {
                            // Полублок должен быть обязательно поставлен вверх
                            if (!MarinBayUtil.isHalfBlockRightInvert(block)) {
                                return block;
                            }
                        }
                        z++;
                    } else {
                        return sign.getRelative(x, y, z);
                    }
                }
                z = startZ;
                x++;
            }
            y++;
        }

        // Проверка блоков за табличкой
        // Обязательно нужна бочка, кафедра и бревно за табличкой
        Material mayBeLog;
        Material mayBeLecternOrBarrel;
        Material mayBeBarrelOrLectern;

        // Это для 4 позиции
        if (direction == 1) {
            mayBeLog = sign.getRelative(1, 0, 0).getType();
            mayBeLecternOrBarrel = sign.getRelative(1, 0, 1).getType();
            mayBeBarrelOrLectern = sign.getRelative(1, 0, -1).getType();
        } else if (direction == 2) {
            mayBeLog = sign.getRelative(-1, 0, 0).getType();
            mayBeLecternOrBarrel = sign.getRelative(-1, 0, 1).getType();
            mayBeBarrelOrLectern = sign.getRelative(-1, 0, -1).getType();
        } else if (direction == 3) {
            mayBeLog = sign.getRelative(0, 0, 1).getType();
            mayBeLecternOrBarrel = sign.getRelative(1, 0, 1).getType();
            mayBeBarrelOrLectern = sign.getRelative(-1, 0, 1).getType();
        } else {
            mayBeLog = sign.getRelative(0, 0, -1).getType();
            mayBeLecternOrBarrel = sign.getRelative(1, 0, -1).getType();
            mayBeBarrelOrLectern = sign.getRelative(-1, 0, -1).getType();
        }

        // Справа может быть либо бочка, либо кафедра, как и слева
        // Поэтому переменные названы именно так
        if (!MarinBayUtil.isLog(mayBeLog)) {
            return block_bolvan;
        }
        if (!(MarinBayUtil.isBarrel(mayBeLecternOrBarrel) || MarinBayUtil.isLectern(mayBeLecternOrBarrel))) {
            return block_bolvan;
        }
        if (!(MarinBayUtil.isBarrel(mayBeBarrelOrLectern) || MarinBayUtil.isLectern(mayBeBarrelOrLectern))) {
            return block_bolvan;
        }

        bounds = new BoundingBox(
                sign.getX() + startX,
                sign.getY() + 0,
                sign.getZ() + startZ,
                sign.getX() + endX,
                sign.getY() - 1,
                sign.getZ() + endZ
        );

        // Записываем bound в базу данных
        buildingDB.write("halltables." + ht.getName() + ".bounds.x1", String.valueOf(sign.getX() + startX));
        buildingDB.write("halltables." + ht.getName() + ".bounds.y1", String.valueOf(sign.getY() + 0));
        buildingDB.write("halltables." + ht.getName() + ".bounds.z1", String.valueOf(sign.getZ() + startZ));
        buildingDB.write("halltables." + ht.getName() + ".bounds.x2", String.valueOf(sign.getX() + endX));
        buildingDB.write("halltables." + ht.getName() + ".bounds.y2", String.valueOf(sign.getY() - 1));
        buildingDB.write("halltables." + ht.getName() + ".bounds.z2", String.valueOf(sign.getZ() + endZ));

        // Возвращает null, если все проверки прошли успешно и бочка была создана
        return null;
    }
}
