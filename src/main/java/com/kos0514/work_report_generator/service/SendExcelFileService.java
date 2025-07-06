package com.kos0514.work_report_generator.service;

import com.kos0514.work_report_generator.util.Constants;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Excelファイルの送信処理を行うサービスクラス
 */
@Service
@RequiredArgsConstructor
public class SendExcelFileService {

  private static final Logger logger = LoggerFactory.getLogger(SendExcelFileService.class);

  private final ReportService reportService;
  private final ZipService zipService;
  private final ConfigService configService;

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
        System.out.print("送信先ディレクトリを入力してください: ");
        Scanner scanner = new Scanner(System.in);
        String newDir = scanner.nextLine().trim();
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

      logger.info("ファイルを送信しました: {}", zipFilePath);

      return "ファイルを送信しました:\n"
          + "- 元ファイル: "
          + fileName
          + "\n"
          + "- ZIP: "
          + zipFilePath
          + "\n"
          + "- パスワード: "
          + password
          + "\n"
          + "- パスワード保存先: "
          + Paths.get(
              sendDir,
              Constants.Files.WORK_DIR,
              yearMonth.substring(0, 4),
              yearMonth,
              Constants.Files.PASSWORD_FILE_NAME);
    } catch (Exception e) {
      logger.error("ファイル送信中にエラーが発生しました", e);
      return "エラー: " + e.getMessage();
    }
  }
}
