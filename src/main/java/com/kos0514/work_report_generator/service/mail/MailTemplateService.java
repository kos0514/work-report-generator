package com.kos0514.work_report_generator.service.mail;

import com.kos0514.work_report_generator.service.config.ConfigService;
import com.kos0514.work_report_generator.service.file.FileService;
import com.kos0514.work_report_generator.service.report.HolidayService;
import com.kos0514.work_report_generator.util.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * メールテンプレートを管理するサービスクラス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailTemplateService {
    private static final String LOCAL_DATA_DIR = "./local-data";
    private static final String MAIL_TEMPLATE_DIR = LOCAL_DATA_DIR + "/" + Constants.Files.MAIL_DIR;
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)", Locale.JAPANESE);

    private final HolidayService holidayService;
    private final ConfigService configService;
    private final FileService fileService;

    /**
     * メール文面を生成して保存します
     *
     * @param yearMonth 年月（yyyymm形式）
     * @param password ZIPファイルのパスワード
     * @throws IOException テンプレートファイルの読み込みや保存に失敗した場合
     */
    public void generateAndSaveMailTemplates(String yearMonth, String password) throws IOException {
        // テンプレートファイルを読み込む
        List<String> templates = readTemplateFile();

        if (templates.size() < 3) {
            throw new IOException("テンプレートファイルの形式が不正です。3つのセクションが必要です。");
        }

        // 年月を変換
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyyMM"));
        String formattedYearMonth = ym.format(YEAR_MONTH_FORMATTER);

        // 翌月の第2営業日を計算
        LocalDate deadline = calculateSecondBusinessDayOfNextMonth(ym);
        String formattedDeadline = deadline.format(DATE_FORMATTER);

        // テンプレート内の変数を置換
        String subject = templates.get(0).replace("${yearMonth}", formattedYearMonth);
        String body = templates.get(1)
                .replace("${yearMonth}", formattedYearMonth)
                .replace("${deadline}", formattedDeadline);
        String passwordBody = templates.get(2).replace("${password}", password);

        // 生成したメール文面を保存
        saveMailTemplates(yearMonth, subject, body, passwordBody);
    }

    /**
     * テンプレートファイルを読み込みます
     *
     * @return テンプレートのリスト（件名、本文、パスワードメール本文の順）
     * @throws IOException ファイルの読み込みに失敗した場合
     */
    private List<String> readTemplateFile() throws IOException {
        Path templateDir = Paths.get(MAIL_TEMPLATE_DIR);
        fileService.createDirectoryIfNotExists(templateDir);

        Path templateFile = Paths.get(MAIL_TEMPLATE_DIR, Constants.Files.MAIL_TEMPLATE_FILE);

        // テンプレートファイルが存在しない場合、リソースからコピーする
        if (!fileService.exists(templateFile)) {
            log.info("メールテンプレートファイルが見つかりません。デフォルトテンプレートを作成します: {}", templateFile);
            try (InputStream templateStream = getClass().getResourceAsStream(Constants.Files.MAIL_TEMPLATE_TEMPLATE_PATH)) {
                if (templateStream != null) {
                    String templateContent = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);
                    fileService.writeStringToFile(templateFile, templateContent);
                    log.info("デフォルトメールテンプレートを作成しました: {}", templateFile);
                } else {
                    throw new IOException("デフォルトメールテンプレートが見つかりません: " + Constants.Files.MAIL_TEMPLATE_TEMPLATE_PATH);
                }
            }
        }

        String content = fileService.readStringFromFile(templateFile);
        String[] sections = content.split(Constants.Files.TEMPLATE_DELIMITER);

        List<String> templates = new ArrayList<>();
        for (String section : sections) {
            templates.add(section.trim());
        }

        return templates;
    }

    /**
     * 翌月の第2営業日を計算します
     *
     * @param yearMonth 基準となる年月
     * @return 翌月の第2営業日
     */
    private LocalDate calculateSecondBusinessDayOfNextMonth(YearMonth yearMonth) {
        // 翌月の初日
        YearMonth nextMonth = yearMonth.plusMonths(1);
        LocalDate date = nextMonth.atDay(1);

        int businessDayCount = 0;
        while (businessDayCount < 2) {
            if (holidayService.isWorkday(date)) {
                businessDayCount++;
            }
            if (businessDayCount < 2) {
                date = date.plusDays(1);
            }
        }

        return date;
    }

    /**
     * メール文面をファイルに保存します
     *
     * @param yearMonth 年月（yyyymm形式）
     * @param subject メール件名
     * @param body メール本文
     * @param passwordBody パスワードメールの本文
     * @throws IOException ファイル書き込みに失敗した場合
     */
    private void saveMailTemplates(String yearMonth, String subject, String body, String passwordBody) throws IOException {
        // 送信先ディレクトリを取得
        String sendDir = configService.getSendDirectory();
        if (sendDir == null || sendDir.isEmpty()) {
            throw new IllegalStateException("送信先ディレクトリが設定されていません。先に設定してください。");
        }

        // yearMonthから年を抽出
        String year = yearMonth.substring(0, 4);

        // メール保存先ディレクトリ作成
        Path mailDir = fileService.createWorkDirectoryForYearMonth(sendDir, Constants.Files.WORK_DIR, year, yearMonth);
        String mailDirPath = mailDir.toString();

        // 全てのメール文面を1つのファイルに保存
        String combinedContent = subject + "\n" + 
                                Constants.Files.TEMPLATE_DELIMITER + "\n" + 
                                body + "\n" + 
                                Constants.Files.TEMPLATE_DELIMITER + "\n" + 
                                passwordBody;

        Path combinedFile = Paths.get(mailDirPath, Constants.Files.MAIL_COMBINED_FILE);
        fileService.writeStringToFile(combinedFile, combinedContent);

        log.info("メール文面をファイルに保存しました: {}", mailDirPath);
    }
}
