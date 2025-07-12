package com.kos0514.work_report_generator.model;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Value;

/**
 * 祝日を表す値オブジェクト
 */
@Value
public class Holiday {
  /** 祝日月日 */
  LocalDate date;

  /** 祝日名称 */
  String name;

  /**
   * 祝日を生成します
   *
   * @param date 祝日月日
   * @param name 祝日名称
   * @return Holidayインスタンス
   * @throws NullPointerException 日付または名称がnullの場合
   */
  public static Holiday of(LocalDate date, String name) {
    Objects.requireNonNull(date, "祝日月日は必須です");
    Objects.requireNonNull(name, "祝日名称は必須です");

    return new Holiday(date, name);
  }
}
