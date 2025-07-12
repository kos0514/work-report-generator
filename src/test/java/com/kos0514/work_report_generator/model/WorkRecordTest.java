package com.kos0514.work_report_generator.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link WorkRecord}のテストクラス
 */
class WorkRecordTest {

    // 共通のテストデータを定数として定義
    private static final LocalDate VALID_DATE = LocalDate.of(2023, 7, 1);
    private static final String VALID_START_TIME = "9:30";
    private static final String VALID_END_TIME = "18:00";
    private static final String VALID_BREAK_TIME = "1:00";
    private static final String VALID_WORK_CONTENT = "プログラミング";

    @Nested
    @DisplayName("of メソッドのテスト")
    class Of {

        @Test
        @DisplayName("有効なパラメータでインスタンスが生成できること")
        void validParameters_returnsInstance() {
            // Act
            var workRecord = WorkRecord.of(
                VALID_DATE, 
                VALID_START_TIME, 
                VALID_END_TIME, 
                VALID_BREAK_TIME, 
                VALID_WORK_CONTENT
            );

            // Assert
            assertThat(workRecord).isNotNull();
            assertThat(workRecord.getDate()).isEqualTo(VALID_DATE);
            assertThat(workRecord.getStartTimeString()).isEqualTo("09:30");
            assertThat(workRecord.getEndTimeString()).isEqualTo("18:00");
            assertThat(workRecord.getBreakTimeString()).isEqualTo("1:00");
            assertThat(workRecord.getWorkContent()).isEqualTo(VALID_WORK_CONTENT);
        }

        @Test
        @DisplayName("日付がnullの場合にNullPointerExceptionがスローされること")
        void nullDate_throwsNullPointerException() {
            // Act & Assert
            assertThatThrownBy(() -> WorkRecord.of(
                null, 
                VALID_START_TIME, 
                VALID_END_TIME, 
                VALID_BREAK_TIME, 
                VALID_WORK_CONTENT
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("日付は必須です");
        }

        @Test
        @DisplayName("作業内容がnullの場合にNullPointerExceptionがスローされること")
        void nullWorkContent_throwsNullPointerException() {
            // Act & Assert
            assertThatThrownBy(() -> WorkRecord.of(
                VALID_DATE, 
                VALID_START_TIME, 
                VALID_END_TIME, 
                VALID_BREAK_TIME, 
                null
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("作業内容は必須です");
        }

        @ParameterizedTest
        @CsvSource({
            "invalid",
            "25:00",
            "9:60",
            "9:5",
            "9-30",
            ":"
        })
        @DisplayName("開始時刻の形式が不正な場合にIllegalArgumentExceptionがスローされること")
        void invalidStartTime_throwsIllegalArgumentException(String startTimeStr) {
            // Act & Assert
            assertThatThrownBy(() -> WorkRecord.of(
                VALID_DATE, 
                startTimeStr, 
                VALID_END_TIME, 
                VALID_BREAK_TIME, 
                VALID_WORK_CONTENT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("時刻形式が不正です");
        }

        @ParameterizedTest
        @CsvSource({
            "invalid",
            "25:00",
            "9:60",
            "9:5",
            "9-30",
            ":"
        })
        @DisplayName("終了時刻の形式が不正な場合にIllegalArgumentExceptionがスローされること")
        void invalidEndTime_throwsIllegalArgumentException(String endTimeStr) {
            // Act & Assert
            assertThatThrownBy(() -> WorkRecord.of(
                VALID_DATE, 
                VALID_START_TIME, 
                endTimeStr, 
                VALID_BREAK_TIME, 
                VALID_WORK_CONTENT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("時刻形式が不正です");
        }

        @ParameterizedTest
        @CsvSource({
            "invalid",
            "1:5",
            "1-30",
            ":",
            "1:",
            ":30"
        })
        @DisplayName("休憩時間の形式が不正な場合にIllegalArgumentExceptionがスローされること")
        void invalidBreakTimeFormat_throwsIllegalArgumentException(String breakTimeStr) {
            // Act & Assert
            assertThatThrownBy(() -> WorkRecord.of(
                VALID_DATE, 
                VALID_START_TIME, 
                VALID_END_TIME, 
                breakTimeStr, 
                VALID_WORK_CONTENT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("時間形式が不正です");
        }

        @Test
        @DisplayName("終了時刻が開始時刻より前の場合にIllegalArgumentExceptionがスローされること")
        void endTimeBeforeStartTime_throwsIllegalArgumentException() {
            // Act & Assert
            assertThatThrownBy(() -> WorkRecord.of(
                VALID_DATE, 
                "10:00", 
                "9:00", 
                VALID_BREAK_TIME, 
                VALID_WORK_CONTENT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("終了時刻は開始時刻より後である必要があります");
        }

        @Test
        @DisplayName("休憩時間が実労働時間より長い場合にIllegalArgumentExceptionがスローされること")
        void breakTimeLongerThanWorkDuration_throwsIllegalArgumentException() {
            // Act & Assert
            assertThatThrownBy(() -> WorkRecord.of(
                VALID_DATE, 
                "9:00", 
                "10:00", 
                "2:00", 
                VALID_WORK_CONTENT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("休憩時間は実労働時間より短い必要があります");
        }
    }

    @Nested
    @DisplayName("getStartTimeString メソッドのテスト")
    class GetStartTimeString {

        @ParameterizedTest
        @CsvSource({
            "9:30, 09:30",
            "9:05, 09:05",
            "12:30, 12:30"
        })
        @DisplayName("フォーマットされた開始時刻文字列が返されること")
        void returnsFormattedStartTime(String input, String expected) {
            // Arrange
            var workRecord = WorkRecord.of(
                VALID_DATE,
                input,
                VALID_END_TIME,
                VALID_BREAK_TIME,
                VALID_WORK_CONTENT
            );

            // Act
            var startTimeString = workRecord.getStartTimeString();

            // Assert
            assertThat(startTimeString).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getEndTimeString メソッドのテスト")
    class GetEndTimeString {

        @ParameterizedTest
        @CsvSource({
            "9:59, 09:59",
            "18:00, 18:00"
        })
        @DisplayName("フォーマットされた終了時刻文字列が返されること")
        void returnsFormattedEndTime(String input, String expected) {
            // Arrange
            var workRecord = WorkRecord.of(
                VALID_DATE,
                "9:30",
                input,
                "0:00", // 休憩時間を0:00に設定
                VALID_WORK_CONTENT
            );

            // Act
            var endTimeString = workRecord.getEndTimeString();

            // Assert
            assertThat(endTimeString).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getBreakTimeString メソッドのテスト")
    class GetBreakTimeString {

        @ParameterizedTest
        @CsvSource({
            "1:00, 1:00",
            "0:30, 0:30",
            "2:05, 2:05"
        })
        @DisplayName("フォーマットされた休憩時間文字列が返されること")
        void returnsFormattedBreakTime(String input, String expected) {
            // Arrange
            var workRecord = WorkRecord.of(
                VALID_DATE,
                VALID_START_TIME,
                VALID_END_TIME,
                input,
                VALID_WORK_CONTENT
            );

            // Act
            var breakTimeString = workRecord.getBreakTimeString();

            // Assert
            assertThat(breakTimeString).isEqualTo(expected);
        }
    }
}
