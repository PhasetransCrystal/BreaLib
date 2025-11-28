package com.phasetranscrystal.brealib.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class FormattingUtil {

    private static final NumberFormat NUMBER_FORMAT;
    public static final DecimalFormat DECIMAL_FORMAT_0F;
    public static final DecimalFormat DECIMAL_FORMAT_1F;
    public static final DecimalFormat DECIMAL_FORMAT_2F;
    public static final DecimalFormat DECIMAL_FORMAT_SIC;
    public static final DecimalFormat DECIMAL_FORMAT_SIC_2F;
    private static final int SMALL_DOWN_NUMBER_BASE = 8320;
    private static final int SMALL_UP_NUMBER_BASE = 8304;
    private static final int SMALL_UP_NUMBER_ONE = 185;
    private static final int SMALL_UP_NUMBER_TWO = 178;
    private static final int SMALL_UP_NUMBER_THREE = 179;
    private static final int NUMBER_BASE = 48;

    public static String toSmallUpNumbers(String string) {
        return checkNumbers(string, 8304);
    }

    public static String toSmallDownNumbers(String string) {
        return checkNumbers(string, 8320);
    }

    private static @NotNull String checkNumbers(String string, int startIndex) {
        char[] charArray = string.toCharArray();

        for (int i = 0; i < charArray.length; ++i) {
            int relativeIndex = charArray[i] - 48;
            if (relativeIndex >= 0 && relativeIndex <= 9) {
                if (startIndex == 8304) {
                    if (relativeIndex == 1) {
                        charArray[i] = 185;
                        continue;
                    }

                    if (relativeIndex == 2) {
                        charArray[i] = 178;
                        continue;
                    }

                    if (relativeIndex == 3) {
                        charArray[i] = 179;
                        continue;
                    }
                }

                int newChar = startIndex + relativeIndex;
                charArray[i] = (char) newChar;
            }
        }

        return new String(charArray);
    }

    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < string.length(); ++i) {
            char curChar = string.charAt(i);
            result.append(Character.toLowerCase(curChar));
            if (i == string.length() - 1) {
                break;
            }

            char nextChar = string.charAt(i + 1);
            if (curChar != '_' && nextChar != '_') {
                boolean nextIsUpper = Character.isUpperCase(nextChar);
                if ((!Character.isUpperCase(curChar) || !nextIsUpper) && (nextIsUpper || Character.isDigit(curChar) ^ Character.isDigit(nextChar))) {
                    result.append('_');
                }
            }
        }

        return result.toString();
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "7.0.0")
    @ApiStatus.Obsolete(since = "7.0.0")
    public static String toLowerCaseUnder(String string) {
        return toLowerCaseUnderscore(string);
    }

    public static boolean hasUpperCase(String string) {
        for (int i = 0; i < string.length(); ++i) {
            char ch = string.charAt(i);
            if (Character.isUpperCase(ch)) {
                return true;
            }
        }

        return false;
    }

    public static String toEnglishName(Object internalName) {
        return (String) Arrays.stream(internalName.toString().toLowerCase(Locale.ROOT).split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public static String toRomanNumeral(int number) {
        return "I".repeat(number).replace("IIIII", "V").replace("IIII", "IV").replace("VV", "X").replace("VIV", "IX").replace("XXXXX", "L").replace("XXXX", "XL").replace("LL", "C").replace("LXL", "XC").replace("CCCCC", "D").replace("CCCC", "CD").replace("DD", "M").replace("DCD", "CM");
    }

    public static String lowerUnderscoreToUpperCamel(String string) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < string.length(); ++i) {
            if (string.charAt(i) != '_') {
                if (i != 0 && string.charAt(i - 1) != '_') {
                    result.append(string.charAt(i));
                } else {
                    result.append(Character.toUpperCase(string.charAt(i)));
                }
            }
        }

        return result.toString();
    }

    public static String formatPercent(double number) {
        return String.format("%,.2f", number);
    }

    public static String formatNumbers(int number) {
        return NUMBER_FORMAT.format((long) number);
    }

    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(Object number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumberReadable(long number) {
        return formatNumberReadable(number, false);
    }

    public static String formatNumberReadable(long number, boolean milli) {
        return formatNumberReadable((double) number, milli, DECIMAL_FORMAT_1F, (String) null);
    }

    public static String formatNumberReadable2F(double number, boolean milli) {
        return formatNumberReadable(number, milli, DECIMAL_FORMAT_2F, (String) null);
    }

    public static String formatNumberReadable(double number, boolean milli, NumberFormat fmt, @Nullable String unit) {
        StringBuilder sb = new StringBuilder();
        if (number < (double) 0.0F) {
            number = -number;
            sb.append('-');
        }

        if (milli && number >= (double) 1000.0F) {
            milli = false;
            number /= (double) 1000.0F;
        }

        int exp = 0;
        if (number >= (double) 1000.0F) {
            exp = (int) Math.log10(number) / 3;
            if (exp > 7) {
                exp = 7;
            }

            if (exp > 0) {
                number /= Math.pow((double) 1000.0F, (double) exp);
            }
        }

        sb.append(fmt.format(number));
        if (exp > 0) {
            sb.append("kMGTPEZ".charAt(exp - 1));
        } else if (milli && number != (double) 0.0F) {
            sb.append('m');
        }

        if (unit != null) {
            sb.append(unit);
        }

        return sb.toString();
    }

    public static String formatNumberOrSic(BigInteger number, BigInteger threshold) {
        return number.compareTo(threshold) > 0 ? DECIMAL_FORMAT_SIC_2F.format(number) : formatNumbers(number);
    }

    public static String formatBuckets(long mB) {
        return formatNumberReadable((double) mB, true, DECIMAL_FORMAT_2F, "B");
    }

    public static @NotNull String formatNumber2Places(float number) {
        return DECIMAL_FORMAT_2F.format((double) number);
    }

    public static @NotNull String formatNumber2Places(double number) {
        return DECIMAL_FORMAT_2F.format(number);
    }

    public static Component formatPercentage2Places(String langKey, float percentage) {
        return Component.translatable(langKey, new Object[] { formatNumber2Places(percentage) }).withStyle(ChatFormatting.YELLOW);
    }

    static {
        NUMBER_FORMAT = NumberFormat.getInstance(Locale.ROOT);
        DECIMAL_FORMAT_0F = new DecimalFormat(",###");
        DECIMAL_FORMAT_1F = new DecimalFormat("#,##0.#");
        DECIMAL_FORMAT_2F = new DecimalFormat("#,##0.##");
        DECIMAL_FORMAT_SIC = new DecimalFormat("0E00");
        DECIMAL_FORMAT_SIC_2F = new DecimalFormat("0.00E00");
    }
}
