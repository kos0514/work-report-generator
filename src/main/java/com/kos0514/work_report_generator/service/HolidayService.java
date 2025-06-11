package com.kos0514.work_report_generator.service;

import com.kos0514.work_report_generator.model.Holiday;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * 祝日データを管理し、日付が祝日かどうかを判定するサービスクラス
 */
@Service
@RequiredArgsConstructor
public class HolidayService {

    private static final Logger logger = LoggerFactory.getLogger(HolidayService.class);
    private final CsvService csvService;
    private final ResourceLoader resourceLoader;
    @Value("${work-report.holidays-file}")
    private String holidaysFile;
    private List<Holiday> holidays;

    @PostConstruct
    public void loadHolidays() {
        try {
            Resource resource = resourceLoader.getResource(holidaysFile);
            // リソースからテンポラリファイルを作成して読み込み
            java.io.File tempFile = java.io.File.createTempFile("syukujitsu", ".csv");
            tempFile.deleteOnExit();

            try (InputStream inputStream = resource.getInputStream();
                 java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                inputStream.transferTo(outputStream);
            }

            holidays = csvService.readHolidayCsv(tempFile.getAbsolutePath());
            logger.info("祝日データ読み込み完了: {}件", holidays.size());
        } catch (IOException e) {
            logger.error("祝日データの読み込みに失敗しました: {}", e.getMessage());
            throw new UncheckedIOException("祝日データの読み込みに失敗しました", e);
        } catch (Exception e) {
            logger.error("祝日データの処理中にエラーが発生しました: {}", e.getMessage());
            holidays = List.of(); // 空のリストで継続
        }
    }

    /**
     * 指定日が祝日かチェック
     */
    public boolean isHoliday(LocalDate date) {
        return holidays.stream()
            .anyMatch(h -> h.getDate().equals(date));
    }

    /**
     * 指定日が平日かチェック（土日祝除く）
     */
    public boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return !isHoliday(date) && 
               dayOfWeek != DayOfWeek.SATURDAY && 
               dayOfWeek != DayOfWeek.SUNDAY;
    }
}
