package com.kos0514.work_report_generator.service.send_file;

import com.kos0514.work_report_generator.service.config.ConfigService;
import com.kos0514.work_report_generator.service.config.UserInputService;
import com.kos0514.work_report_generator.service.file.ZipService;
import com.kos0514.work_report_generator.service.mail.MailTemplateService;
import com.kos0514.work_report_generator.service.report.ReportService;
import com.kos0514.work_report_generator.util.Constants;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Excelファイルの送信処理を行うサービスクラス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SendExcelFileService {

  private final ReportService reportService;
  private final ZipService zipService;
  private final ConfigService configService;
  private final MailTemplateService mailTemplateService;
  private final UserInputService userInputService;

  /**
   * Excelファイルをパスワード付きZIPにして送信します
   *
   * @param fileName 送信するExcelファイル名（空の場合は最新のファイルを使用）
   * @return 処理結果メッセージ
   */
  public String sendExcelFile(String fileName) {
    try {
      // 送信先ディレクトリの確認
      String sendDir = configService.getSendDirectory();
      if (sendDir == null || sendDir.isEmpty()) {
        // 送信先ディレクトリが設定されていない場合は設定を促す
        String newDir = userInputService.readLine("送信先ディレクトリを入力してください: ");
        configService.setSendDirectory(newDir);
        sendDir = newDir;
      }

      // ファイル名が指定されていない場合は最新のExcelファイルを使用
      if (fileName.isEmpty()) {
        // 最新のCSVファイルから年月を取得
        ReportService.CsvFileInfo latestCsvFile = reportService.findLatestCsvFile();
        String yearMonth = latestCsvFile.yearMonth();

        // 同じ年月のExcelファイルを検索
        List<String> excelFiles = reportService.findExcelFilesByYearMonth(yearMonth);
        if (excelFiles.isEmpty()) {
          return "送信可能なExcelファイルが見つかりません";
        }
        fileName = excelFiles.getFirst();
      }

      // ファイル名から年月を抽出（user_yyyymm_作業報告書.xls）
      String yearMonth = reportService.extractYearMonthFromFileName(fileName);
      if (yearMonth == null) {
        return "ファイル名から年月を抽出できません: " + fileName;
      }

      // Excelファイルのパスを取得
      String excelFilePath = Paths.get(reportService.getOutputDir(), fileName).toString();

      // ZIPファイル名の生成
      String zipFileName =
          fileName.replaceFirst(
              "\\" + Constants.Files.EXCEL_EXTENSION + "$", Constants.Files.ZIP_EXTENSION);
      String zipFilePath = Paths.get(sendDir, zipFileName).toString();

      // パスワード付きZIPファイルの作成（パスワードは内部で生成される）
      String password = zipService.createPasswordProtectedZip(excelFilePath, zipFilePath);

      // パスワードをファイルに保存
      zipService.savePasswordToFile(yearMonth, password);

      // メール文面を生成して保存
      mailTemplateService.generateAndSaveMailTemplates(yearMonth, password);

      log.info("ファイルを送信しました: {}", zipFilePath);

      StringBuilder resultMessage = new StringBuilder();
      resultMessage.append("ファイルを送信しました:\n");
      resultMessage.append("- 元ファイル: ").append(fileName).append("\n");
      resultMessage.append("- ZIP: ").append(zipFilePath).append("\n");
      resultMessage.append("- パスワード: ").append(password).append("\n");

      String mailDirPath = Paths.get(
          sendDir,
          Constants.Files.WORK_DIR,
          yearMonth.substring(0, 4),
          yearMonth).toString();

      resultMessage.append("- メール文面保存先: ").append(mailDirPath).append("\n");

      return resultMessage.toString();
    } catch (Exception e) {
      log.error("ファイル送信中にエラーが発生しました", e);
      return "エラー: " + e.getMessage();
    }
  }
}
