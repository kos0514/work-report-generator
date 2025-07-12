package com.kos0514.work_report_generator.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Holiday}のテストクラス
 */
class HolidayTest {

    @Nested
    @DisplayName("of メソッドのテスト")
    class Of {

        @Test
        @DisplayName("有効な日付と名称でインスタンスが生成できること")
        void validDateAndName_returnsInstance() {
            // Arrange
            var date = LocalDate.of(2023, 1, 1);
            var name = "元日";

            // Act
            var holiday = Holiday.of(date, name);

            // Assert
            assertThat(holiday).isNotNull();
            assertThat(holiday.getDate()).isEqualTo(date);
            assertThat(holiday.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("日付がnullの場合にNullPointerExceptionがスローされること")
        void nullDate_throwsNullPointerException() {
            // Arrange
            LocalDate date = null;
            var name = "元日";

            // Act & Assert
            assertThatThrownBy(() -> Holiday.of(date, name))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("祝日月日は必須です");
        }

        @Test
        @DisplayName("名称がnullの場合にNullPointerExceptionがスローされること")
        void nullName_throwsNullPointerException() {
            // Arrange
            var date = LocalDate.of(2023, 1, 1);
            String name = null;

            // Act & Assert
            assertThatThrownBy(() -> Holiday.of(date, name))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("祝日名称は必須です");
        }

        @Test
        @DisplayName("空文字列の名称でインスタンスが生成できること")
        void emptyName_returnsInstance() {
            // Arrange
            var date = LocalDate.of(2023, 1, 1);
            var name = "";

            // Act
            var holiday = Holiday.of(date, name);

            // Assert
            assertThat(holiday).isNotNull();
            assertThat(holiday.getDate()).isEqualTo(date);
            assertThat(holiday.getName()).isEmpty();
        }

        @Test
        @DisplayName("閏年の2月29日でインスタンスが生成できること")
        void leapYearDate_returnsInstance() {
            // Arrange
            var date = LocalDate.of(2020, 2, 29);
            var name = "閏日";

            // Act
            var holiday = Holiday.of(date, name);

            // Assert
            assertThat(holiday).isNotNull();
            assertThat(holiday.getDate()).isEqualTo(date);
            assertThat(holiday.getName()).isEqualTo(name);
        }
    }
}
