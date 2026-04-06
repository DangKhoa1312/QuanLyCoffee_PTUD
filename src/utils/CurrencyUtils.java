package utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    /** Format: 45,000 d */
    public static String format(double amount) {
        return VND.format((long) amount) + " d";
    }

    /** Format khong ky hieu: 45,000 */
    public static String formatNoUnit(double amount) {
        return VND.format((long) amount);
    }
}
