package com.kos0514.work_report_generator.model.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TimeOfDay}のテストクラス
 */
class TimeOfDayTest {

    @Nested
    @DisplayName("of メソッドのテスト")
    class Of {

        @Test
        @DisplayName("1桁の時間（9:30）でインスタンスが生成できること")
        void singleDigitHour_returnsInstance() {
            // Arrange
            var timeStr = "9:30";

            // Act
            var timeOfDay = TimeOfDay.of(timeStr);

            // Assert
            assertThat(timeOfDay).isNotNull();
            assertThat(timeOfDay.format()).isEqualTo("09:30");
        }

        @Test
        @DisplayName("2桁の時間（12:30）でインスタンスが生成できること")
        void doubleDigitHour_returnsInstance() {
            // Arrange
            var timeStr = "12:30";

            // Act
            var timeOfDay = TimeOfDay.of(timeStr);

            // Assert
            assertThat(timeOfDay).isNotNull();
            assertThat(timeOfDay.format()).isEqualTo("12:30");
        }

        @Test
        @DisplayName("境界値（0:00）でインスタンスが生成できること")
        void minValue_returnsInstance() {
            // Arrange
            var timeStr = "0:00";

            // Act
            var timeOfDay = TimeOfDay.of(timeStr);

            // Assert
            assertThat(timeOfDay).isNotNull();
            assertThat(timeOfDay.format()).isEqualTo("00:00");
        }

        @Test
        @DisplayName("境界値（23:59）でインスタンスが生成できること")
        void maxValue_returnsInstance() {
            // Arrange
            var timeStr = "23:59";

            // Act
            var timeOfDay = TimeOfDay.of(timeStr);

            // Assert
            assertThat(timeOfDay).isNotNull();
            assertThat(timeOfDay.format()).isEqualTo("23:59");
        }

        @Test
        @DisplayName("前後に空白がある場合でもインスタンスが生成できること")
        void withWhitespace_returnsInstance() {
            // Arrange
            var timeStr = " 10:15 ";

            // Act
            var timeOfDay = TimeOfDay.of(timeStr);

            // Assert
            assertThat(timeOfDay).isNotNull();
            assertThat(timeOfDay.format()).isEqualTo("10:15");
        }

        @Test
        @DisplayName("時刻文字列がnullの場合にNullPointerExceptionがスローされること")
        void nullTimeString_throwsNullPointerException() {
            // Arrange
            String timeStr = null;

            // Act & Assert
            assertThatThrownBy(() -> TimeOfDay.of(timeStr))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("時刻文字列は必須です");
        }

        @ParameterizedTest
        @CsvSource({
            "abc",
            "12:60",
            "25:00",
            "9:5",
            "9-30",
            ":"
        })
        @DisplayName("時刻形式が不正な場合にIllegalArgumentExceptionがスローされること")
        void invalidTimeFormat_throwsIllegalArgumentException(String timeStr) {
            // Act & Assert
            assertThatThrownBy(() -> TimeOfDay.of(timeStr))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("時刻形式が不正です");
        }
    }

    @Nested
    @DisplayName("format メソッドのテスト")
    class Format {

        @Test
        @DisplayName("1桁の時間が2桁に変換されて返されること")
        void singleDigitHour_returnsFormattedString() {
            // Arrange
            var timeOfDay = TimeOfDay.of("9:30");

            // Act
            var formattedTime = timeOfDay.format();

            // Assert
            assertThat(formattedTime).isEqualTo("09:30");
        }

        @Test
        @DisplayName("2桁の時間がそのまま返されること")
        void doubleDigitHour_returnsFormattedString() {
            // Arrange
            var timeOfDay = TimeOfDay.of("12:45");

            // Act
            var formattedTime = timeOfDay.format();

            // Assert
            assertThat(formattedTime).isEqualTo("12:45");
        }
    }
}
