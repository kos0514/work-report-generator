package com.kos0514.work_report_generator.model.value;

import java.time.Duration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;

/**
 * 作業時間や休憩時間を表す値オブジェクト H:mm形式の時間を扱います
 */
@Value
public class WorkDuration {

  private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+):(\\d{2})");

  Duration value;

  private WorkDuration(Duration value) {
    this.value = Objects.requireNonNull(value, "時間は必須です");
  }

  /**
   * 文字列から時間を生成します
   *
   * @param durationStr H:mm形式の時間文字列（例: "1:30"）
   * @return WorkDurationインスタンス
   * @throws IllegalArgumentException 時間形式が不正な場合
   */
  public static WorkDuration of(String durationStr) {
    Objects.requireNonNull(durationStr, "時間文字列は必須です");

    Matcher matcher = DURATION_PATTERN.matcher(durationStr.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("時間形式が不正です: " + durationStr + " (期待形式: H:mm)");
    }

    int hours = Integer.parseInt(matcher.group(1));
    int minutes = Integer.parseInt(matcher.group(2));

    if (minutes >= 60) {
      throw new IllegalArgumentException("分は0-59の範囲で指定してください: " + durationStr);
    }

    Duration duration = Duration.ofHours(hours).plusMinutes(minutes);
    return new WorkDuration(duration);
  }

  /**
   * フォーマットされた時間文字列を返します
   *
   * @return H:mm形式の時間文字列（例: "1:30"）
   */
  public String format() {
    long totalMinutes = value.toMinutes();
    long hours = totalMinutes / 60;
    long minutes = totalMinutes % 60;

    return String.format("%d:%02d", hours, minutes);
  }
}
