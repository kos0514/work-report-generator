package com.kos0514.work_report_generator.command;

import com.kos0514.work_report_generator.service.ConfigService;
import com.kos0514.work_report_generator.service.ReportService;
import com.kos0514.work_report_generator.service.SendExcelFileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 作業報告書管理システムのCLIコマンドを定義するクラス */
@Component
@ShellComponent
@RequiredArgsConstructor
public class WorkReportCommands {

  private static final Logger logger = LoggerFactory.getLogger(WorkReportCommands.class);

  private final ReportService reportService;
  private final SendExcelFileService sendExcelFileService;
  private final ConfigService configService;

  /**
   * 設定ファイルが存在することを確認し、存在しない場合は作成します
   */
  private void ensureConfigExists() {
    try {
      configService.ensureConfigExists();
    } catch (IOException e) {
      logger.warn("設定ファイルの確認中にエラーが発生しました: {}", e.getMessage());
    }
  }

  @ShellMethod(value = "新規報告書ファイルを作成", key = "create-file")
  public String createFile(
      @ShellOption("--month") String month, // 2025/06 形式
      @ShellOption("--user") String user, // ユーザー名
      @ShellOption("--client") String client // クライアント名
      ) {
    try {
      // 設定ファイルが存在することを確認
      ensureConfigExists();

      // Excelファイル作成
      String excelFileName = reportService.createReport(month, user, client);

      // CSVファイル作成
      String csvFileName = reportService.createCsvFile(month);

      return "ファイル作成完了:\n" + "- Excel: " + excelFileName + "\n" + "- CSV: " + csvFileName;
    } catch (Exception e) {
      return "エラー: " + e.getMessage();
    }
  }

  @ShellMethod(value = "CSVファイルで報告書を更新", key = "update-file")
  public String updateFile(
      @ShellOption("--file") String fileName, // 更新対象ファイル
      @ShellOption("--csv") String csvFile // CSVファイル名
      ) {
    try {
      // 設定ファイルが存在することを確認
      ensureConfigExists();

      int updatedRows = reportService.updateFromCsv(fileName, csvFile);
      return "更新完了: " + updatedRows + " 件";
    } catch (Exception e) {
      return "エラー: " + e.getMessage();
    }
  }

  @ShellMethod(value = "最新のCSVファイルを対応するExcelファイルに適用", key = "save")
  public String saveLatestCsv() {
    try {
      // 設定ファイルが存在することを確認
      ensureConfigExists();

      int updatedFiles = reportService.saveLatestCsvToExcel();
      if (updatedFiles > 0) {
        return "保存完了: " + updatedFiles + " 件のファイルを更新しました";
      } else {
        return "更新対象のファイルがありませんでした";
      }
    } catch (Exception e) {
      return "エラー: " + e.getMessage();
    }
  }

  @ShellMethod(value = "コマンド一覧とヘルプを表示", key = "help")
  public String showHelp() {
    return reportService.getHelpMessage();
  }

  @ShellMethod(value = "Excelファイルをパスワード付きZIPにして送信", key = "send")
  public String sendExcelFile(
      @ShellOption(value = "--file", help = "送信するExcelファイル", defaultValue = "") String fileName) {
    return sendExcelFileService.sendExcelFile(fileName);
  }
}
