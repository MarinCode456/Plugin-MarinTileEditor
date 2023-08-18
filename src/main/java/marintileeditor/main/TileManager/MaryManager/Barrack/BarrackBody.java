package marintileeditor.main.TileManager.MaryManager.Barrack;

import marintileeditor.main.TileManager.MaryManager.BoundingBox;
import marintileeditor.main.TileManager.MaryManager.MarinBayUtil;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.logging.Logger;

public class BarrackBody {

    // Основные переменные
    private String barrackName;
    private final Barrack barrack;
    private final Block sign;
    private BoundingBox bounds;
    private byte signoffset;

    // Базы данных
    private static Database buildingDB = Database.BUILDINGS_DATABASE;

    // Тех. часть
    Logger log = Bukkit.getLogger();

    public BarrackBody(Barrack barrack) {
        this.barrack = barrack;
        this.bounds = new BoundingBox(0, 0, 0, 0, 0, 0);
        sign = barrack.getSign();
        barrackName = barrack.getName();

        // Если столик уже существует, то возьмётся его BoundingBox
        if (buildingDB.isSet("barracks." + barrack.getName() + ".bounds")) {
            this.bounds = Main.getInstance().getBounds("barracks." + barrack.getName() + ".bounds");
        }
    }

    // Геттеры сеттеры
    public String toString() {
        return ChatColor.GOLD + "Моя казарма " + barrack.toString();
    }
    public Block getBrokenBlock() {
        return checkBarrack();
    }
    public BoundingBox getBounds() {
        if (buildingDB.isSet("barracks." + barrackName + ".bounds")) {
            this.bounds = Main.getInstance().getBounds("barracks." + barrackName + ".bounds");
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
    public Block checkBarrack() {
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
        Material mayBeGrindstoneOrFlecth;
        Material mayBeFlecthOrGrindstone;

        // Это для 4 позиции
        if (direction == 1) {
            mayBeLog = sign.getRelative(1, 0, 0).getType();
            mayBeGrindstoneOrFlecth = sign.getRelative(1, 0, 1).getType();
            mayBeFlecthOrGrindstone = sign.getRelative(1, 0, -1).getType();
        } else if (direction == 2) {
            mayBeLog = sign.getRelative(-1, 0, 0).getType();
            mayBeGrindstoneOrFlecth = sign.getRelative(-1, 0, 1).getType();
            mayBeFlecthOrGrindstone = sign.getRelative(-1, 0, -1).getType();
        } else if (direction == 3) {
            mayBeLog = sign.getRelative(0, 0, 1).getType();
            mayBeGrindstoneOrFlecth = sign.getRelative(1, 0, 1).getType();
            mayBeFlecthOrGrindstone = sign.getRelative(-1, 0, 1).getType();
        } else {
            mayBeLog = sign.getRelative(0, 0, -1).getType();
            mayBeGrindstoneOrFlecth = sign.getRelative(1, 0, -1).getType();
            mayBeFlecthOrGrindstone = sign.getRelative(-1, 0, -1).getType();
        }

        // Справа может быть либо бочка, либо кафедра, как и слева
        // Поэтому переменные названы именно так
        if (!MarinBayUtil.isLog(mayBeLog)) {
            return block_bolvan;
        }
        if (!(MarinBayUtil.isGrindStone(mayBeGrindstoneOrFlecth) || MarinBayUtil.isFletchingTable(mayBeGrindstoneOrFlecth))) {
            return block_bolvan;
        }
        if (!(MarinBayUtil.isGrindStone(mayBeFlecthOrGrindstone) || MarinBayUtil.isFletchingTable(mayBeFlecthOrGrindstone))) {
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
        buildingDB.write("barracks." + barrackName + ".bounds.x1", String.valueOf(sign.getX() + startX));
        buildingDB.write("barracks." + barrackName + ".bounds.y1", String.valueOf(sign.getY() + 0));
        buildingDB.write("barracks." + barrackName + ".bounds.z1", String.valueOf(sign.getZ() + startZ));
        buildingDB.write("barracks." + barrackName + ".bounds.x2", String.valueOf(sign.getX() + endX));
        buildingDB.write("barracks." + barrackName + ".bounds.y2", String.valueOf(sign.getY() - 1));
        buildingDB.write("barracks." + barrackName + ".bounds.z2", String.valueOf(sign.getZ() + endZ));

        // Возвращает null, если все проверки прошли успешно и бочка была создана
        return null;
    }
}
