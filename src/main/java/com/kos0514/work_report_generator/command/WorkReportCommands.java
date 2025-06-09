package com.kos0514.work_report_generator.command;

import com.kos0514.work_report_generator.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;

/**
 * 作業報告書管理システムのCLIコマンドを定義するクラス
 */
@Component
@ShellComponent
@RequiredArgsConstructor
public class WorkReportCommands {

    private final ReportService reportService;

    @ShellMethod(value = "新規報告書ファイルを作成", key = "create-file")
    public String createFile(
        @ShellOption("--month") String month,      // 2025/06 形式
        @ShellOption("--user") String user,       // ユーザー名
        @ShellOption("--client") String client    // クライアント名
    ) {
        try {
            String fileName = reportService.createReport(month, user, client);
            return "ファイル作成完了: " + fileName;
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }

    @ShellMethod(value = "CSVファイルで報告書を更新", key = "update-file")
    public String updateFile(
        @ShellOption("--file") String fileName,   // 更新対象ファイル
        @ShellOption("--csv") String csvFile      // CSVファイル名
    ) {
        try {
            int updatedRows = reportService.updateFromCsv(fileName, csvFile);
            return "更新完了: " + updatedRows + " 件";
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }

    @ShellMethod(value = "コマンド一覧とヘルプを表示", key = "help")
    public String showHelp() {
        return reportService.getHelpMessage();
    }
}
