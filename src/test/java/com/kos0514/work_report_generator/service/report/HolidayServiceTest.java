package com.kos0514.work_report_generator.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.kos0514.work_report_generator.model.Holiday;
import com.kos0514.work_report_generator.service.file.CsvService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private CsvService csvService;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource mockResource;

    @InjectMocks
    private HolidayService holidayService;

    @BeforeEach
    void setUp() {
        // holidaysFileプロパティを設定
        ReflectionTestUtils.setField(holidayService, "holidaysFile", "classpath:config/syukujitsu.csv");
    }

    @Nested
    class LoadHolidays {

        @Test
        @DisplayName("祝日データが正常に読み込まれること")
        void successfullyLoadsHolidays() throws IOException {
            // Arrange
            var holidays = List.of(
                    Holiday.of(LocalDate.of(2025, 1, 1), "元日"),
                    Holiday.of(LocalDate.of(2025, 2, 11), "建国記念の日")
            );

            when(resourceLoader.getResource(anyString())).thenReturn(mockResource);
            when(mockResource.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("日付,祝日名称\n2025/01/01,元日\n2025/02/11,建国記念の日".getBytes()));
            when(csvService.readHolidayCsv(any())).thenReturn(holidays);

            // Act
            holidayService.loadHolidays();

            // Assert
            verify(resourceLoader).getResource("classpath:config/syukujitsu.csv");
            verify(csvService).readHolidayCsv(any());
        }

        @Test
        @DisplayName("リソースの読み込みに失敗した場合、UncheckedIOExceptionがスローされること")
        void throwsExceptionWhenResourceLoadingFails() throws IOException {
            // Arrange
            when(resourceLoader.getResource(anyString())).thenReturn(mockResource);
            when(mockResource.getInputStream()).thenThrow(new IOException("リソース読み込みエラー"));

            // Act & Assert
            assertThatThrownBy(() -> holidayService.loadHolidays())
                    .isInstanceOf(UncheckedIOException.class)
                    .hasMessageContaining("祝日データの読み込みに失敗しました");
        }

        @Test
        @DisplayName("CSVの処理中にエラーが発生した場合、空のリストで継続すること")
        void continuesWithEmptyListWhenCsvProcessingFails() throws IOException {
            // Arrange
            when(resourceLoader.getResource(anyString())).thenReturn(mockResource);
            when(mockResource.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("invalid data".getBytes()));
            when(csvService.readHolidayCsv(any())).thenThrow(new IllegalArgumentException("CSVフォーマットエラー"));

            // Act
            holidayService.loadHolidays();

            // Assert - 例外がスローされずに処理が継続すること
            verify(resourceLoader).getResource("classpath:config/syukujitsu.csv");
            verify(csvService).readHolidayCsv(any());
        }
    }

    @Nested
    class IsHoliday {

        @BeforeEach
        void setUp() {
            // テスト用の祝日データを設定
            var holidays = List.of(
                    Holiday.of(LocalDate.of(2025, 1, 1), "元日"),
                    Holiday.of(LocalDate.of(2025, 2, 11), "建国記念の日"),
                    Holiday.of(LocalDate.of(2025, 5, 5), "こどもの日")
            );
            ReflectionTestUtils.setField(holidayService, "holidays", holidays);
        }

        @Test
        @DisplayName("祝日の場合、trueを返すこと")
        void returnsTrueForHoliday() {
            // Arrange
            var holidayDate = LocalDate.of(2025, 1, 1);

            // Act
            var result = holidayService.isHoliday(holidayDate);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("祝日でない場合、falseを返すこと")
        void returnsFalseForNonHoliday() {
            // Arrange
            var nonHolidayDate = LocalDate.of(2025, 1, 2);

            // Act
            var result = holidayService.isHoliday(nonHolidayDate);

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    class IsWorkday {

        @BeforeEach
        void setUp() {
            // テスト用の祝日データを設定
            var holidays = List.of(
                    Holiday.of(LocalDate.of(2025, 1, 1), "元日"),
                    Holiday.of(LocalDate.of(2025, 2, 11), "建国記念の日"),
                    Holiday.of(LocalDate.of(2025, 5, 5), "こどもの日")
            );
            ReflectionTestUtils.setField(holidayService, "holidays", holidays);
        }

        @ParameterizedTest
        @CsvSource({
                "2025-01-06, true, '平日（月曜日）'",
                "2025-01-01, false, '祝日（元日）'",
                "2025-01-04, false, '土曜日'",
                "2025-01-05, false, '日曜日'"
        })
        @DisplayName("日付に応じて平日かどうかを正しく判定すること")
        void correctlyIdentifiesWorkdays(LocalDate date, boolean expected, String description) {
            // Act
            var result = holidayService.isWorkday(date);

            // Assert
            assertThat(result).as(description).isEqualTo(expected);
        }
    }
}