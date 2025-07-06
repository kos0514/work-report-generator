package com.kos0514.work_report_generator.model.value;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import lombok.Value;

/**
 * 時刻を表す値オブジェクト HH:mm形式の時刻を扱います
 */
@Value
public class TimeOfDay {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("H:mm");
  private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  LocalTime value;

  private TimeOfDay(LocalTime value) {
    this.value = Objects.requireNonNull(value, "時刻は必須です");
  }

  /**
   * 文字列から時刻を生成します
   *
   * @param timeStr HH:mm形式の時刻文字列（例: "9:30"）
   * @return TimeOfDayインスタンス
   * @throws IllegalArgumentException 時刻形式が不正な場合
   */
  public static TimeOfDay of(String timeStr) {
    Objects.requireNonNull(timeStr, "時刻文字列は必須です");
    try {
      LocalTime time = LocalTime.parse(timeStr.trim(), FORMATTER);
      return new TimeOfDay(time);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("時刻形式が不正です: " + timeStr + " (期待形式: H:mm)", e);
    }
  }

  /**
   * フォーマットされた時刻文字列を返します
   *
   * @return HH:mm形式の時刻文字列（例: "09:30"）
   */
  public String format() {
    return value.format(OUTPUT_FORMATTER);
  }
}
