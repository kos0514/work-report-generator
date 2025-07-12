package com.kos0514.work_report_generator.model.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link WorkDuration}のテストクラス
 */
class WorkDurationTest {

    @Nested
    @DisplayName("of メソッドのテスト")
    class Of {

        @Test
        @DisplayName("1桁の時間（1:30）でインスタンスが生成できること")
        void singleDigitHour_returnsInstance() {
            // Arrange
            var durationStr = "1:30";

            // Act
            var workDuration = WorkDuration.of(durationStr);

            // Assert
            assertThat(workDuration).isNotNull();
            assertThat(workDuration.format()).isEqualTo("1:30");
            assertThat(workDuration.getValue().toMinutes()).isEqualTo(90); // 1時間30分 = 90分
        }


        @Test
        @DisplayName("1時間を超える休憩時間（2:30）でインスタンスが生成できること")
        void overOneHour_returnsInstance() {
            // Arrange
            var durationStr = "2:30";

            // Act
            var workDuration = WorkDuration.of(durationStr);

            // Assert
            assertThat(workDuration).isNotNull();
            assertThat(workDuration.format()).isEqualTo("2:30");
            assertThat(workDuration.getValue().toMinutes()).isEqualTo(150); // 2時間30分 = 150分
        }

        @Nested
        @DisplayName("境界値テスト")
        class BoundaryValues {
            @Test
            @DisplayName("最小値（0:00）でインスタンスが生成できること")
            void minValue_returnsInstance() {
                // Arrange
                var durationStr = "0:00";

                // Act
                var workDuration = WorkDuration.of(durationStr);

                // Assert
                assertThat(workDuration).isNotNull();
                assertThat(workDuration.format()).isEqualTo("0:00");
                assertThat(workDuration.getValue().toMinutes()).isZero();
            }

            @Test
            @DisplayName("時間が0で分が0以外の場合（0:30）でインスタンスが生成できること")
            void zeroHourWithMinutes_returnsInstance() {
                // Arrange
                var durationStr = "0:30";

                // Act
                var workDuration = WorkDuration.of(durationStr);

                // Assert
                assertThat(workDuration).isNotNull();
                assertThat(workDuration.format()).isEqualTo("0:30");
                assertThat(workDuration.getValue().toMinutes()).isEqualTo(30);
            }

            @Test
            @DisplayName("最大値（3:00）でインスタンスが生成できること")
            void maxValue_returnsInstance() {
                // Arrange
                var durationStr = "3:00";

                // Act
                var workDuration = WorkDuration.of(durationStr);

                // Assert
                assertThat(workDuration).isNotNull();
                assertThat(workDuration.format()).isEqualTo("3:00");
                assertThat(workDuration.getValue().toMinutes()).isEqualTo(180); // 3時間 = 180分
            }

            @Test
            @DisplayName("最大値に近い値（2:59）でインスタンスが生成できること")
            void nearMaxValue_returnsInstance() {
                // Arrange
                var durationStr = "2:59";

                // Act
                var workDuration = WorkDuration.of(durationStr);

                // Assert
                assertThat(workDuration).isNotNull();
                assertThat(workDuration.format()).isEqualTo("2:59");
                assertThat(workDuration.getValue().toMinutes()).isEqualTo(179); // 2時間59分 = 179分
            }
        }

        @Nested
        @DisplayName("異常系テスト")
        class ErrorCases {
            @ParameterizedTest
            @CsvSource({
                "4:00",
                "3:01",
                "999:59"
            })
            @DisplayName("3時間を超える値の場合にIllegalArgumentExceptionがスローされること")
            void overThreeHours_throwsIllegalArgumentException(String durationStr) {
                // Act & Assert
                assertThatThrownBy(() -> WorkDuration.of(durationStr))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("時間は3時間以下である必要があります")
                        .hasMessageContaining("だったら休んでください");
            }

            @Test
            @DisplayName("時間文字列がnullの場合にNullPointerExceptionがスローされること")
            void nullDurationString_throwsNullPointerException() {
                // Arrange
                String durationStr = null;

                // Act & Assert
                assertThatThrownBy(() -> WorkDuration.of(durationStr))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessageContaining("時間文字列は必須です");
            }

            @ParameterizedTest
            @CsvSource({
                "abc",
                "1:60",
                "1:99",
                "1:5",
                "1-30",
                ":",
                "1:",
                ":30",
                "-1:30",
                "a:30",
                "1:a0"
            })
            @DisplayName("時間形式が不正な場合にIllegalArgumentExceptionがスローされること")
            void invalidDurationFormat_throwsIllegalArgumentException(String durationStr) {
                // Act & Assert
                assertThatThrownBy(() -> WorkDuration.of(durationStr))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("時間形式が不正です");
            }
        }

        @Test
        @DisplayName("前後に空白がある場合でもインスタンスが生成できること")
        void withWhitespace_returnsInstance() {
            // Arrange
            var durationStr = " 2:15 ";

            // Act
            var workDuration = WorkDuration.of(durationStr);

            // Assert
            assertThat(workDuration).isNotNull();
            assertThat(workDuration.format()).isEqualTo("2:15");
            assertThat(workDuration.getValue().toMinutes()).isEqualTo(135); // 2時間15分 = 135分
        }
    }

    @Nested
    @DisplayName("format メソッドのテスト")
    class Format {

        @Test
        @DisplayName("1桁の時間がそのまま返されること")
        void singleDigitHour_returnsFormattedString() {
            // Arrange
            var workDuration = WorkDuration.of("1:30");

            // Act
            var formattedDuration = workDuration.format();

            // Assert
            assertThat(formattedDuration).isEqualTo("1:30");
        }


        @Test
        @DisplayName("分が1桁の場合でも2桁で返されること")
        void singleDigitMinute_returnsFormattedString() {
            // Arrange
            var workDuration = WorkDuration.of("1:05");

            // Act
            var formattedDuration = workDuration.format();

            // Assert
            assertThat(formattedDuration).isEqualTo("1:05");
        }

        @Test
        @DisplayName("時間が0の場合に0:XXの形式で返されること")
        void zeroHour_returnsFormattedString() {
            // Arrange
            var workDuration = WorkDuration.of("0:30");

            // Act
            var formattedDuration = workDuration.format();

            // Assert
            assertThat(formattedDuration).isEqualTo("0:30");
        }

        @Test
        @DisplayName("分が0の場合にX:00の形式で返されること")
        void zeroMinute_returnsFormattedString() {
            // Arrange
            var workDuration = WorkDuration.of("2:00");

            // Act
            var formattedDuration = workDuration.format();

            // Assert
            assertThat(formattedDuration).isEqualTo("2:00");
        }

        @Test
        @DisplayName("時間も分も0の場合に0:00の形式で返されること")
        void zeroHourAndMinute_returnsFormattedString() {
            // Arrange
            var workDuration = WorkDuration.of("0:00");

            // Act
            var formattedDuration = workDuration.format();

            // Assert
            assertThat(formattedDuration).isEqualTo("0:00");
        }

        @Test
        @DisplayName("最大値（3:00）の場合に正しくフォーマットされること")
        void maxValue_returnsFormattedString() {
            // Arrange
            var workDuration = WorkDuration.of("3:00");

            // Act
            var formattedDuration = workDuration.format();

            // Assert
            assertThat(formattedDuration).isEqualTo("3:00");
        }
    }
}
