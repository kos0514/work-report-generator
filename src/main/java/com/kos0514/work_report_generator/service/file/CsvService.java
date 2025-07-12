package com.kos0514.work_report_generator.service.file;

import com.kos0514.work_report_generator.model.Holiday;
import com.kos0514.work_report_generator.model.WorkRecord;
import com.kos0514.work_report_generator.util.DateUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * CSVファイルの読み込みに関するサービスクラス
 *
 * <p>このクラスは作業記録CSVファイルや祝日データCSVファイルの読み込み機能を提供します。
 */
@Service
@Slf4j
public class CsvService {

  /**
   * 作業記録CSVファイルを読み込み、WorkRecordオブジェクトのリストを返します
   *
   * <p>CSVファイルの形式: 日付,開始時刻,終了時刻,休憩時間,作業内容
   *
   * <p>例: 2025/06/02,09:30,17:45,1:00,システム設計書作成
   *
   * @param csvFilePath 読み込むCSVファイルのパス
   * @return WorkRecordオブジェクトのリスト
   * @throws UncheckedIOException ファイルの読み込みに失敗した場合
   * @throws IllegalArgumentException CSVの形式が不正な場合
   */
  public List<WorkRecord> readCsv(String csvFilePath) {
    List<WorkRecord> records = new ArrayList<>();

    try (CSVReader reader =
        new CSVReader(
            new InputStreamReader(new FileInputStream(csvFilePath), StandardCharsets.UTF_8))) {
      String[] line;
      boolean isHeader = true;

      while ((line = reader.readNext()) != null) {
        // ヘッダー行をスキップ
        if (isHeader) {
          isHeader = false;
          continue;
        }

        if (line.length >= 5) {
          try {
            LocalDate date = DateUtil.parseDate(line[0].trim());
            String startTime = line[1].trim();
            String endTime = line[2].trim();
            String breakTime = line[3].trim();
            String workContent = line[4].trim();

            records.add(WorkRecord.of(date, startTime, endTime, breakTime, workContent));
          } catch (Exception e) {
            log.warn("CSV行の処理中にエラーが発生しました: {} - {}", String.join(",", line), e.getMessage());
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("CSVファイルの読み込み中にエラーが発生しました: " + csvFilePath, e);
    } catch (Exception e) {
      throw new IllegalArgumentException("CSVデータの処理中にエラーが発生しました: " + csvFilePath, e);
    }

    return records;
  }

  /** 祝日CSVファイル読み込み（Shift_JIS対応） */
  public List<Holiday> readHolidayCsv(String csvFilePath) {
    List<Holiday> holidays = new ArrayList<>();

    try (CSVReader reader =
        new CSVReader(
            new InputStreamReader(
                new FileInputStream(csvFilePath), Charset.forName("Shift_JIS")))) {
      String[] line;
      boolean isHeader = true;

      while ((line = reader.readNext()) != null) {
        // ヘッダー行をスキップ
        if (isHeader) {
          isHeader = false;
          continue;
        }

        if (line.length >= 2) {
          try {
            LocalDate date = DateUtil.parseDate(line[0].trim());
            String name = line[1].trim();
            holidays.add(Holiday.of(date, name));
          } catch (Exception e) {
            log.warn("祝日CSV行の処理中にエラーが発生しました: {} - {}", String.join(",", line), e.getMessage());
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("祝日CSVファイルの読み込み中にエラーが発生しました: " + csvFilePath, e);
    } catch (Exception e) {
      throw new IllegalArgumentException("祝日データの処理中にエラーが発生しました: " + csvFilePath, e);
    }

    return holidays;
  }

  /**
   * 作業記録CSVファイルを作成します
   *
   * <p>CSVファイルの形式: 日付,開始時刻,終了時刻,休憩時間,作業内容
   *
   * <p>例: 2025/06/02,09:00,18:00,1:00,
   *
   * @param records 作業記録のリスト
   * @param csvFilePath 作成するCSVファイルのパス
   * @throws UncheckedIOException ファイルの書き込みに失敗した場合
   */
  public void writeCsv(List<WorkRecord> records, String csvFilePath) {
    try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath, StandardCharsets.UTF_8))) {
      // ヘッダー行を書き込み
      writer.writeNext(new String[] {"日付", "開始時刻", "終了時刻", "休憩時間", "作業内容"});

      // 各レコードを書き込み
      for (WorkRecord record : records) {
        String[] line =
            new String[] {
              DateUtil.formatDate(record.getDate()),
              record.getStartTimeString(),
              record.getEndTimeString(),
              record.getBreakTimeString(),
              record.getWorkContent()
            };
        writer.writeNext(line);
      }

      log.info("CSVファイルを作成しました: {}", csvFilePath);
    } catch (IOException e) {
      throw new UncheckedIOException("CSVファイルの作成中にエラーが発生しました: " + csvFilePath, e);
    }
  }
}
