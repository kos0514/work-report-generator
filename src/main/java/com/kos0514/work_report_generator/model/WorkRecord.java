package com.kos0514.work_report_generator.model;

import com.kos0514.work_report_generator.model.value.TimeOfDay;
import com.kos0514.work_report_generator.model.value.WorkDuration;
import lombok.Value;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 作業記録を表す値オブジェクト
 */
@Value
public class WorkRecord {
    LocalDate date;          // 日付
    TimeOfDay startTime;     // 開始時刻
    TimeOfDay endTime;       // 終了時刻
    WorkDuration breakTime;  // 休憩時間
    String workContent;      // 作業内容

    /**
     * 文字列から作業記録を生成します
     *
     * @param date 日付
     * @param startTimeStr 開始時刻（H:mm形式）
     * @param endTimeStr 終了時刻（H:mm形式）
     * @param breakTimeStr 休憩時間（H:mm形式）
     * @param workContent 作業内容
     * @return WorkRecordインスタンス
     */
    public static WorkRecord of(
            LocalDate date,
            String startTimeStr,
            String endTimeStr,
            String breakTimeStr,
            String workContent
    ) {
        Objects.requireNonNull(date, "日付は必須です");
        TimeOfDay startTime = TimeOfDay.of(startTimeStr);
        TimeOfDay endTime = TimeOfDay.of(endTimeStr);
        WorkDuration breakTime = WorkDuration.of(breakTimeStr);
        Objects.requireNonNull(workContent, "作業内容は必須です");

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
