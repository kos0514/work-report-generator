package com.kos0514.work_report_generator.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * CSV形式の日付文字列をLocalDateに変換
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, CSV_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日付形式が正しくありません: " + dateStr + " (期待形式: yyyy/M/d)");
        }
    }

    /**
     * 月形式の文字列（yyyy/MM）をパース
     */
    public static String parseMonth(String monthStr) {
        try {
            // yyyy/MM形式をyyyy/Mに正規化
            String[] parts = monthStr.split("/");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                return String.format("%04d/%02d", year, month);
            }
            throw new IllegalArgumentException("月形式が正しくありません: " + monthStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("月形式が正しくありません: " + monthStr + " (期待形式: yyyy/MM)");
        }
    }

    /**
     * ファイル名用の月文字列を生成（yyyyMM形式）
     */
    public static String getFileNameMonth(String monthStr) {
        String normalized = parseMonth(monthStr);
        return normalized.replace("/", "");
    }

    /**
     * LocalDateをCSV形式の日付文字列に変換
     * 
     * @param date 変換する日付
     * @return yyyy/M/d形式の日付文字列
     */
    public static String formatDate(LocalDate date) {
        return date.format(CSV_DATE_FORMAT);
    }
}
