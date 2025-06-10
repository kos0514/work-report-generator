package com.kos0514.work_report_generator.service;

import com.kos0514.work_report_generator.model.WorkRecord;
import com.kos0514.work_report_generator.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 作業報告書の作成と更新に関するビジネスロジックを提供するサービスクラス
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    // I社フォーマット用定数（ハードコード）
    private static final String TARGET_MONTH_CELL = "B7";
    private static final String CLIENT_NAME_CELL = "C4";
    private static final String USER_NAME_CELL = "L4";
    private static final String START_TIME_COLUMN = "F";
    private static final String END_TIME_COLUMN = "G";
    private static final String BREAK_TIME_COLUMN = "H";
    private static final String WORK_CONTENT_COLUMN = "J";

    private final ExcelService excelService;
    private final CsvService csvService;
    private final HolidayService holidayService;

    @Value("${work-report.template-file}")
    private String templateFile;

    @Value("${work-report.output-dir}")
    private String outputDir;

    @Value("${work-report.csv-dir}")
    private String csvDir;

    /**
     * 新規報告書作成（ファイル名: user_yyyymm_作業報告書.xls）
     */
    public String createReport(String month, String user, String client) {
        try {
            // テンプレートファイルの存在確認
            File templateFileObj = new File(templateFile);
            if (!templateFileObj.exists()) {
                throw new IOException("テンプレートファイルが見つかりません: " + templateFile + 
                    "\n`local-data/templates/` ディレクトリに作業報告書のテンプレートファイル（`作業報告書 I社フォーマット.xls`）を配置してください。");
            }

            // 1. ファイル名設定 (user_202506_作業報告書.xls)
            String fileNameMonth = DateUtil.getFileNameMonth(month);
            String fileName = user + "_" + fileNameMonth + "_作業報告書.xls";
            String outputPath = Paths.get(outputDir, fileName).toString();

            // 2. テンプレートファイルをコピー
            excelService.copyFile(templateFile, outputPath);

            // 3. 基本項目設定
            HSSFWorkbook workbook = excelService.loadWorkbook(outputPath);
            HSSFSheet sheet = workbook.getSheetAt(0);

            // 基本情報設定（B7: 対象月、C4: クライアント、L4: ユーザー名）
            // B7の日付形式をyyyy/mm/ddに変更
            String[] parts = month.split("/");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[0]);
                int monthValue = Integer.parseInt(parts[1]);
                String formattedDate = String.format("%04d/%02d/01", year, monthValue);
                excelService.setCellValue(sheet, TARGET_MONTH_CELL, formattedDate);
            } else {
                excelService.setCellValue(sheet, TARGET_MONTH_CELL, month);
            }
            excelService.setCellValue(sheet, CLIENT_NAME_CELL, client);
            excelService.setCellValue(sheet, USER_NAME_CELL, user);

            // 平日（土日祝以外）に開始時刻、終了時刻、休憩時間を自動設定
            setDefaultWorkTimeForWeekdays(sheet, month);

            // 4. ファイル保存
            excelService.saveWorkbook(workbook, outputPath);
            workbook.close();

            logger.info("ファイル作成完了: {}", fileName);
            return fileName;

        } catch (IOException e) {
            throw new UncheckedIOException("報告書の作成に失敗しました", e);
        } catch (Exception e) {
            throw new IllegalStateException("報告書の作成に失敗しました", e);
        }
    }

    /**
     * CSV からの更新（日付,開始時刻,終了時刻,休憩時間,作業内容）
     */
    public int updateFromCsv(String fileName, String csvFile) {
        try {
            // 1. CSVファイル読み込み
            String csvPath = Paths.get(csvDir, csvFile).toString();
            List<WorkRecord> records = csvService.readCsv(csvPath);

            // 2. Excelファイル読み込み
            String excelPath = Paths.get(outputDir, fileName).toString();
            HSSFWorkbook workbook = excelService.loadWorkbook(excelPath);
            HSSFSheet sheet = workbook.getSheetAt(0);

            int updatedCount = 0;

            // 3. 各レコードを処理
            for (WorkRecord record : records) {
                // 日付をキーとして該当行特定
                int rowIndex = excelService.findRowByDate(sheet, record.getDate());

                if (rowIndex >= 0) {
                    // 4. 開始時刻・終了時刻・休憩時間・作業内容更新
                    String startTimeCell = START_TIME_COLUMN + rowIndex;
                    String endTimeCell = END_TIME_COLUMN + rowIndex;
                    String breakTimeCell = BREAK_TIME_COLUMN + rowIndex;
                    String workContentCell = WORK_CONTENT_COLUMN + rowIndex;

                    excelService.setCellValue(sheet, startTimeCell, record.getStartTimeString());
                    excelService.setCellValue(sheet, endTimeCell, record.getEndTimeString());
                    excelService.setCellValue(sheet, breakTimeCell, record.getBreakTimeString());
                    excelService.setCellValue(sheet, workContentCell, record.getWorkContent());

                    updatedCount++;
                } else {
                    logger.warn("該当日なし: {} ({})", record.getDate(), record.getDate().getDayOfWeek());
                }
            }

            // 5. ファイル保存
            excelService.saveWorkbook(workbook, excelPath);
            workbook.close();

            logger.info("CSV更新完了: {}件更新", updatedCount);
            return updatedCount;

        } catch (IOException e) {
            throw new UncheckedIOException("CSVからの更新に失敗しました", e);
        } catch (Exception e) {
            throw new IllegalStateException("CSVからの更新に失敗しました", e);
        }
    }

    /**
     * 平日（土日祝日以外）に開始時刻、終了時刻、休憩時間を自動設定
     *
     * @param sheet 対象のシート
     * @param monthStr 対象月（yyyy/MM形式）
     */
    private void setDefaultWorkTimeForWeekdays(HSSFSheet sheet, String monthStr) {
        try {
            // 月の解析
            String[] parts = monthStr.split("/");
            if (parts.length != 2) {
                logger.warn("月形式が不正です: {}", monthStr);
                return;
            }

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            // 対象月の日数を取得
            YearMonth yearMonth = YearMonth.of(year, month);
            int daysInMonth = yearMonth.lengthOfMonth();

            // デフォルト値
            String defaultStartTime = "09:00";
            String defaultEndTime = "18:00";
            String defaultBreakTime = "1:00";

            // 各日に対して処理
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);

                // 平日（土日祝日以外）かチェック
                if (holidayService.isWorkday(date)) {
                    // 行インデックスを取得
                    int rowIndex = excelService.findRowByDate(sheet, date);

                    if (rowIndex >= 0) {
                        // セル位置を計算
                        String startTimeCell = START_TIME_COLUMN + rowIndex;
                        String endTimeCell = END_TIME_COLUMN + rowIndex;
                        String breakTimeCell = BREAK_TIME_COLUMN + rowIndex;

                        // 値を設定
                        excelService.setCellValue(sheet, startTimeCell, defaultStartTime);
                        excelService.setCellValue(sheet, endTimeCell, defaultEndTime);
                        excelService.setCellValue(sheet, breakTimeCell, defaultBreakTime);

                        logger.debug("平日のデフォルト時間を設定: {} ({}) - 開始: {}, 終了: {}, 休憩: {}", 
                            date, date.getDayOfWeek(), defaultStartTime, defaultEndTime, defaultBreakTime);
                    } else {
                        logger.warn("該当日の行が見つかりません: {} ({})", date, date.getDayOfWeek());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("平日のデフォルト時間設定中にエラーが発生しました: {}", e.getMessage());
        }
    }

    /**
     * 月の出勤日（平日）に対応するCSVファイルを作成します
     * 
     * @param month 対象月（yyyy/MM形式）
     * @return 作成したCSVファイル名
     */
    public String createCsvFile(String month) {
        try {
            // 1. 月の解析
            String[] parts = month.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("月形式が不正です: " + month);
            }

            int year = Integer.parseInt(parts[0]);
            int monthValue = Integer.parseInt(parts[1]);

            // 2. ファイル名設定 (yyyymm_work_data.csv)
            String fileNameMonth = DateUtil.getFileNameMonth(month);
            String fileName = fileNameMonth + "_work_data.csv";
            String csvPath = Paths.get(csvDir, fileName).toString();

            // CSVディレクトリが存在しない場合は作成
            File csvDirFile = new File(csvDir);
            if (!csvDirFile.exists()) {
                csvDirFile.mkdirs();
            }

            // 3. 対象月の日数を取得
            YearMonth yearMonth = YearMonth.of(year, monthValue);
            int daysInMonth = yearMonth.lengthOfMonth();

            // 4. 出勤日（平日）のWorkRecordリストを作成
            List<WorkRecord> records = new ArrayList<>();

            // デフォルト値
            String defaultStartTime = "09:00";
            String defaultEndTime = "18:00";
            String defaultBreakTime = "1:00";
            String emptyWorkContent = "";

            // 各日に対して処理
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, monthValue, day);

                // 平日（土日祝日以外）かチェック
                if (holidayService.isWorkday(date)) {
                    // WorkRecordオブジェクトを作成
                    WorkRecord record = WorkRecord.of(
                        date,
                        defaultStartTime,
                        defaultEndTime,
                        defaultBreakTime,
                        emptyWorkContent
                    );
                    records.add(record);

                    logger.debug("出勤日のレコードを追加: {} ({})", date, date.getDayOfWeek());
                }
            }

            // 5. CSVファイル作成
            csvService.writeCsv(records, csvPath);

            logger.info("CSVファイル作成完了: {}", fileName);
            return fileName;

        } catch (Exception e) {
            throw new IllegalStateException("CSVファイルの作成に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * ヘルプメッセージ表示
     */
    public String getHelpMessage() {
        return """
                【作業報告書管理システム】

                利用可能なコマンド:

                1. create-file --month <月> --user <ユーザー名> --client <クライアント名>
                   新規報告書ファイルを作成します
                   例: create-file --month 2025/06 --user "田中太郎" --client "株式会社サンプル"

                2. update-file --file <ファイル名> --csv <CSVファイル名>
                   CSVファイルで報告書を更新します
                   例: update-file --file "田中太郎_202506_作業報告書.xls" --csv "work_data.csv"

                3. help
                   このヘルプを表示します

                CSVファイル形式:
                日付,開始時刻,終了時刻,休憩時間,作業内容
                2025/06/02,09:30,17:45,1:00,システム設計書作成
                2025/06/03,10:00,18:30,1:00,プログラム実装

                注意事項:
                - CSVファイルは " + csvDir + " に配置してください
                - 生成ファイルは " + outputDir + " に保存されます
                - テンプレートファイル: " + templateFile + "
                """;
    }
}
