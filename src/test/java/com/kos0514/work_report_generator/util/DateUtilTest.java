package com.kos0514.work_report_generator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("DateUtil クラスのテスト")
class DateUtilTest {

    @Nested
    @DisplayName("parseDate メソッドのテスト")
    class ParseDate {

        @Test
        @DisplayName("正常系：正しい日付形式の文字列をLocalDateに変換できること")
        void validDateString_returnsLocalDate() {
            // Arrange
            var dateStr = "2023/6/15";
            var expected = LocalDate.of(2023, 6, 15);

            // Act
            var result = DateUtil.parseDate(dateStr);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("異常系：不正な日付形式の文字列で例外がスローされること")
        void invalidDateString_throwsException() {
            // Arrange
            var invalidDateStr = "2023-06-15";

            // Act & Assert
            assertThatThrownBy(() -> DateUtil.parseDate(invalidDateStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("日付形式が正しくありません");
        }
    }

    @Nested
    @DisplayName("parseMonth メソッドのテスト")
    class ParseMonth {

        @ParameterizedTest
        @CsvSource({
            "2023/06, 2023/06",
            "2023/6, 2023/06",
            "2023/12, 2023/12",
            "2023/1, 2023/01"
        })
        @DisplayName("正常系：様々な月形式の文字列を正規化できること")
        void validMonthString_returnsNormalizedMonth(String input, String expected) {
            // Act
            var result = DateUtil.parseMonth(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("異常系：不正な月形式の文字列で例外がスローされること")
        void invalidMonthString_throwsException() {
            // Arrange
            var invalidMonthStr = "2023-06";

            // Act & Assert
            assertThatThrownBy(() -> DateUtil.parseMonth(invalidMonthStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("月形式が正しくありません");
        }

        @Test
        @DisplayName("異常系：数値に変換できない月形式の文字列で例外がスローされること")
        void nonNumericMonthString_throwsException() {
            // Arrange
            var nonNumericMonthStr = "2023/XX";

            // Act & Assert
            assertThatThrownBy(() -> DateUtil.parseMonth(nonNumericMonthStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("月形式が正しくありません");
        }
    }

    @Nested
    @DisplayName("getFileNameMonth メソッドのテスト")
    class GetFileNameMonth {

        @ParameterizedTest
        @CsvSource({
            "2023/06, 202306",
            "2023/6, 202306",
            "2023/12, 202312",
            "2023/1, 202301"
        })
        @DisplayName("正常系：様々な月形式の文字列からファイル名用の月文字列を生成できること")
        void validMonthString_returnsFileNameMonth(String input, String expected) {
            // Act
            var result = DateUtil.getFileNameMonth(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("異常系：不正な月形式の文字列で例外がスローされること")
        void invalidMonthString_throwsException() {
            // Arrange
            var invalidMonthStr = "2023-06";

            // Act & Assert
            assertThatThrownBy(() -> DateUtil.getFileNameMonth(invalidMonthStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("月形式が正しくありません");
        }
    }

    @Nested
    @DisplayName("formatDate メソッドのテスト")
    class FormatDate {

        @Test
        @DisplayName("正常系：LocalDateをCSV形式の日付文字列に変換できること")
        void validDate_returnsFormattedString() {
            // Arrange
            var date = LocalDate.of(2023, 6, 15);
            var expected = "2023/6/15";

            // Act
            var result = DateUtil.formatDate(date);

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }
}