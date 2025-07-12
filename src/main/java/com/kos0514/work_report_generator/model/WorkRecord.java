package com.kos0514.work_report_generator.model;

import com.kos0514.work_report_generator.model.value.TimeOfDay;
import com.kos0514.work_report_generator.model.value.WorkDuration;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Value;

/**
 * 作業記録を表す値オブジェクト
 */
@Value
public class WorkRecord {
  /** 日付 */
  LocalDate date;

  /** 開始時刻 */
  TimeOfDay startTime;

  /** 終了時刻 */
  TimeOfDay endTime;

  /** 休憩時間 */
  WorkDuration breakTime;

  /** 作業内容 */
  String workContent;

  /**
   * 文字列から作業記録を生成します
   *
   * @param date 日付
   * @param startTimeStr 開始時刻（H:mm形式）
   * @param endTimeStr 終了時刻（H:mm形式）
   * @param breakTimeStr 休憩時間（H:mm形式）
   * @param workContent 作業内容
   * @return WorkRecordインスタンス
   * @throws NullPointerException 日付または作業内容がnullの場合
   * @throws IllegalArgumentException 時刻形式が不正な場合、終了時刻が開始時刻より前の場合、または休憩時間が実労働時間より長い場合
   */
  public static WorkRecord of(
      LocalDate date,
      String startTimeStr,
      String endTimeStr,
      String breakTimeStr,
      String workContent) {
    Objects.requireNonNull(date, "日付は必須です");
    TimeOfDay startTime = TimeOfDay.of(startTimeStr);
    TimeOfDay endTime = TimeOfDay.of(endTimeStr);
    WorkDuration breakTime = WorkDuration.of(breakTimeStr);
    Objects.requireNonNull(workContent, "作業内容は必須です");

    // 終了時刻が開始時刻より後であることを確認
    if (endTime.getValue().isBefore(startTime.getValue())) {
      throw new IllegalArgumentException("終了時刻は開始時刻より後である必要があります: " + startTimeStr + " -> " + endTimeStr);
    }

    // 休憩時間が実労働時間より短いことを確認
    Duration workDuration = Duration.between(startTime.getValue(), endTime.getValue());
    if (breakTime.getValue().compareTo(workDuration) > 0) {
      throw new IllegalArgumentException("休憩時間は実労働時間より短い必要があります: " + breakTimeStr);
    }

    return new WorkRecord(date, startTime, endTime, breakTime, workContent);
  }

  /**
   * 開始時刻を文字列形式で返します
   *
   * @return HH:mm形式の開始時刻
   */
  public String getStartTimeString() {
    return startTime.format();
  }

  /**
   * 終了時刻を文字列形式で返します
   *
   * @return HH:mm形式の終了時刻
   */
  public String getEndTimeString() {
    return endTime.format();
  }

  /**
   * 休憩時間を文字列形式で返します
   *
   * @return H:mm形式の休憩時間
   */
  public String getBreakTimeString() {
    return breakTime.format();
  }
}
