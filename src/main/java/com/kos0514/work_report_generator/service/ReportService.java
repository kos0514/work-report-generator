package com.kos0514.work_report_generator.service;

import com.kos0514.work_report_generator.model.WorkRecord;
import com.kos0514.work_report_generator.util.DateUtil;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    // 日付検証用の定数
    private static final int MIN_VALID_YEAR = 1900;
    private static final int MAX_VALID_YEAR = 2100;
    private static final int MIN_VALID_MONTH = 1;
    private static final int MAX_VALID_MONTH = 12;
    private static final String DATE_FORMAT_PATTERN = "^\\d{4}/(?:0?[1-9]|1[0-2])$";

    private final ExcelService excelService;
    private final CsvService csvService;
    private final HolidayService holidayService;

    @Value("${work-report.template-file}")
    private String templateFile;

    /**
     * -- GETTER --
     *  出力ディレクトリのパスを取得します
     *
     */
    @Getter
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
            // B7にDate型の日付を設定
            Date targetDate = parseTargetDate(month);
            excelService.setCellDateValue(sheet, TARGET_MONTH_CELL, targetDate);
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
     * CSVファイルに含まれない日付の行はクリアされます
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

            // 3. 対象月を取得（ファイル名から抽出）
            // ファイル名形式: user_yyyymm_作業報告書.xls
            String yearMonth = null;
            String[] parts = fileName.split("_");
            if (parts.length >= 2) {
                yearMonth = parts[1];
                if (yearMonth.length() == 6) {
                    yearMonth = yearMonth.substring(0, 4) + "/" + yearMonth.substring(4, 6);
                }
            }

            if (yearMonth == null) {
                logger.warn("ファイル名から対象月を抽出できませんでした: {}", fileName);
                return 0;
            }

            // 4. CSVファイルに含まれる日付のリストを作成
            List<LocalDate> csvDates = records.stream()
                .map(WorkRecord::getDate)
                .toList();

            // 5. 対象月の全ての平日（出勤日）を取得
            List<LocalDate> workdays = getWorkdaysOfMonth(yearMonth);

            int updatedCount = 0;
            int clearedCount = 0;

            // 6. 各レコードを処理
            for (WorkRecord record : records) {
                // 日付をキーとして該当行特定
                int rowIndex = excelService.findRowByDate(sheet, record.getDate());

                if (rowIndex >= 0) {
                    // 7. 開始時刻・終了時刻・休憩時間・作業内容更新
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

            // 8. CSVファイルに含まれない平日の行をクリア
            for (LocalDate workday : workdays) {
                if (!csvDates.contains(workday)) {
                    int rowIndex = excelService.findRowByDate(sheet, workday);
                    if (rowIndex >= 0) {
                        // 9. 開始時刻・終了時刻・休憩時間・作業内容をクリア
                        String startTimeCell = START_TIME_COLUMN + rowIndex;
                        String endTimeCell = END_TIME_COLUMN + rowIndex;
                        String breakTimeCell = BREAK_TIME_COLUMN + rowIndex;
                        String workContentCell = WORK_CONTENT_COLUMN + rowIndex;

                        // 新しいclearCellメソッドを使用してセルをクリア
                        excelService.clearCell(sheet, startTimeCell);
                        excelService.clearCell(sheet, endTimeCell);
                        excelService.clearCell(sheet, breakTimeCell);
                        excelService.clearCell(sheet, workContentCell);

                        clearedCount++;
                        logger.debug("CSVに含まれない日付の行をクリア: {} ({})", workday, workday.getDayOfWeek());
                    }
                }
            }

            // 10. すべての計算式を再評価
            logger.info("計算式を再評価します");
            HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

            // 11. ファイル保存
            excelService.saveWorkbook(workbook, excelPath);
            workbook.close();

            logger.info("CSV更新完了: {}件更新, {}件クリア", updatedCount, clearedCount);
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
     * 対象月の全ての平日（出勤日）を取得します
     * 
     * @param monthStr 対象月（yyyy/MM形式）
     * @return 平日（出勤日）のリスト
     */
    private List<LocalDate> getWorkdaysOfMonth(String monthStr) {
        List<LocalDate> workdays = new ArrayList<>();

        try {
            // 月の解析
            String[] parts = monthStr.split("/");
            if (parts.length != 2) {
                logger.warn("月形式が不正です: {}", monthStr);
                return workdays;
            }

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            // 対象月の日数を取得
            YearMonth yearMonth = YearMonth.of(year, month);
            int daysInMonth = yearMonth.lengthOfMonth();

            // 各日に対して処理
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);

                // 平日（土日祝日以外）かチェック
                if (holidayService.isWorkday(date)) {
                    workdays.add(date);
                }
            }
        } catch (Exception e) {
            logger.error("平日リスト取得中にエラーが発生しました: {}", e.getMessage());
        }

        return workdays;
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
     * csvフォルダから最新の日付（yyyymm形式）のファイルを見つけます
     * 
     * @return 最新のCSVファイル情報（ファイル名とyyyymm形式の日付）
     * @throws IllegalStateException CSVファイルが見つからない場合
     */
    public CsvFileInfo findLatestCsvFile() {
        try {
            // CSVファイルの命名規則: yyyymm_work_data.csv
            Pattern pattern = Pattern.compile("(\\d{6})_work_data\\.csv");

            // csvフォルダ内のファイルを検索
            Path csvDirPath = Paths.get(csvDir);

            Optional<CsvFileInfo> latestCsvFile;
            try (Stream<Path> paths = Files.list(csvDirPath)) {
                latestCsvFile = paths
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        Matcher matcher = pattern.matcher(fileName);
                        if (matcher.matches()) {
                            String yearMonth = matcher.group(1);
                            return new CsvFileInfo(fileName, yearMonth);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .max(Comparator.comparing(CsvFileInfo::yearMonth));
            }

            if (latestCsvFile.isPresent()) {
                logger.info("最新のCSVファイルを見つけました: {}", latestCsvFile.get().fileName());
                return latestCsvFile.get();
            } else {
                throw new IllegalStateException("CSVファイルが見つかりません: " + csvDir);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("CSVファイルの検索中にエラーが発生しました", e);
        }
    }

    /**
     * 指定した年月（yyyymm形式）を含むExcelファイルを見つけます
     * 
     * @param yearMonth 年月（yyyymm形式）
     * @return 該当するExcelファイルのリスト
     */
    public List<String> findExcelFilesByYearMonth(String yearMonth) {
        try {
            // Excelファイルの命名規則: user_yyyymm_作業報告書.xls
            Pattern pattern = Pattern.compile("(.+)_" + yearMonth + "_作業報告書\\.xls");

            // outputフォルダ内のファイルを検索
            Path outputDirPath = Paths.get(outputDir);

            List<String> matchingFiles = new ArrayList<>();
            try (Stream<Path> paths = Files.list(outputDirPath)) {
                paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .forEach(fileName -> {
                        Matcher matcher = pattern.matcher(fileName);
                        if (matcher.matches()) {
                            matchingFiles.add(fileName);
                        }
                    });
            }

            logger.info("{}年月のExcelファイルを{}件見つけました", yearMonth, matchingFiles.size());
            return matchingFiles;
        } catch (IOException e) {
            throw new UncheckedIOException("Excelファイルの検索中にエラーが発生しました", e);
        }
    }

    /**
     * 最新のCSVファイルを対応するExcelファイルに適用します
     * 
     * @return 更新されたファイル数
     */
    public int saveLatestCsvToExcel() {
        try {
            // 1. 最新のCSVファイルを見つける
            CsvFileInfo latestCsvFile = findLatestCsvFile();
            String yearMonth = latestCsvFile.yearMonth();
            String csvFileName = latestCsvFile.fileName();

            // 2. 同じ年月のExcelファイルを見つける
            List<String> excelFiles = findExcelFilesByYearMonth(yearMonth);

            if (excelFiles.isEmpty()) {
                logger.warn("{}年月のExcelファイルが見つかりません", yearMonth);
                return 0;
            }

            // 3. 各Excelファイルを更新
            int updatedFiles = 0;
            for (String excelFileName : excelFiles) {
                try {
                    int updatedRows = updateFromCsv(excelFileName, csvFileName);
                    logger.info("ファイル更新完了: {} ({}行更新)", excelFileName, updatedRows);
                    updatedFiles++;
                } catch (Exception e) {
                    logger.error("ファイル更新中にエラーが発生しました: {}", excelFileName, e);
                }
            }

            return updatedFiles;
        } catch (Exception e) {
            logger.error("保存処理中にエラーが発生しました", e);
            throw e;
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

                3. save
                   最新のCSVファイルを対応するExcelファイルに適用します
                   例: save

                4. send [--file <ファイル名>]
                   Excelファイルをパスワード付きZIPにして送信します
                   例: send
                   例: send --file "田中太郎_202506_作業報告書.xls"

                5. help
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

    /**
     * 対象月文字列（yyyy/mm形式）を解析し、対応する日付（Date型）を返します。
     * 無効な入力の場合は当月の1日を返します。
     *
     * @param month 対象月文字列（yyyy/mm形式）
     * @return 対象月の1日（Date型）
     */
    private Date parseTargetDate(String month) {
        Date targetDate = null;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        // yyyy/mm形式かどうかを正規表現で検証
        boolean validFormat = false;

        if (month != null && month.matches(DATE_FORMAT_PATTERN)) {
            try {
                String[] parts = month.split("/");
                int year = Integer.parseInt(parts[0]);
                int monthValue = Integer.parseInt(parts[1]);

                // 年と月の値が有効な範囲内かを検証
                if (year >= MIN_VALID_YEAR && year <= MAX_VALID_YEAR &&
                    monthValue >= MIN_VALID_MONTH && monthValue <= MAX_VALID_MONTH) {
                    // 月の値（1-12）に対応するCalendarの月定数を取得
                    int calendarMonth = switch (monthValue) {
                        case 1 -> Calendar.JANUARY;
                        case 2 -> Calendar.FEBRUARY;
                        case 3 -> Calendar.MARCH;
                        case 4 -> Calendar.APRIL;
                        case 5 -> Calendar.MAY;
                        case 6 -> Calendar.JUNE;
                        case 7 -> Calendar.JULY;
                        case 8 -> Calendar.AUGUST;
                        case 9 -> Calendar.SEPTEMBER;
                        case 10 -> Calendar.OCTOBER;
                        case 11 -> Calendar.NOVEMBER;
                        case 12 -> Calendar.DECEMBER;
                        default -> Calendar.JANUARY; // デフォルト値
                    };

                    calendar.set(year, calendarMonth, 1); // 月の1日を設定
                    targetDate = calendar.getTime();
                    validFormat = true;

                    logger.debug("有効な日付形式を処理: {}", month);
                } else {
                    logger.warn("無効な年または月の値: {}", month);
                }
            } catch (NumberFormatException e) {
                logger.warn("数値への変換に失敗: {}", month, e);
            }
        } else {
            logger.warn("無効な日付形式: {}", month);
        }

        // 無効な入力の場合は当月の1日を設定
        if (!validFormat) {
            calendar.setTime(new Date()); // 現在の日付を設定
            calendar.set(Calendar.DAY_OF_MONTH, 1); // 当月の1日
            targetDate = calendar.getTime();
            logger.info("無効な入力のため当月の1日を設定します");
        }

        return targetDate;
    }

    /**
     * CSVファイル情報を保持するレコードクラス
     */
    public record CsvFileInfo(String fileName, String yearMonth) {}

    /**
     * ファイル名から年月を抽出します（user_yyyymm_作業報告書.xls）
     *
     * @param fileName ファイル名
     * @return 年月（yyyymm形式）、抽出できない場合はnull
     */
    public String extractYearMonthFromFileName(String fileName) {
        // ReportService.updateFromCsv と同じ方法で年月を抽出
        String[] parts = fileName.split("_");
        if (parts.length >= 2) {
            String yearMonth = parts[1];
            if (yearMonth.length() == 6 && yearMonth.matches("\\d{6}")) {
                return yearMonth;
            }
        }
        return null;
    }
}
