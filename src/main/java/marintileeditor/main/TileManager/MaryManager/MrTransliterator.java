package marintileeditor.main.TileManager.MaryManager;

import org.apache.commons.lang.StringUtils;

// Обработано
public class MrTransliterator {

    // Переводит русский текст в английский
    public static String rusToEng(String text){
        String[] abcRus = {"а","б","в","г","д","е","ё","ж","з","и","й","к","л","м","н","о","п","р","с","т","у","ф","х","ц","ч","ш","щ","ъ","ы","ь","э","ю","я"};
        String[] abcEng = {"a","b","v","g","d","e","jo","zh","z","i","i","k","l","m","n","o","p","r","s","t","u","f","h","ts","ch","sh","sch","","","","e","ju","ja"};
        return StringUtils.replaceEach(text, abcRus, abcEng);
    }

    // Проверяет подходит ли текст
    public static boolean isValid(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!Character.UnicodeBlock.of(name.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)
            || name.charAt(i) == '-') {
                if (!(name.charAt(i) == ' ')) {
                    return false;
                }
            }
        }
        return true;
    }
}
