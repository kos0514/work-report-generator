package com.kos0514.work_report_generator.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kos0514.work_report_generator.service.report.ReportService;
import com.kos0514.work_report_generator.service.send_file.SendExcelFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link WorkReportCommands}のテストクラス
 */
@ExtendWith(MockitoExtension.class)
class WorkReportCommandsTest {

    @Mock
    private ReportService reportService;

    @Mock
    private SendExcelFileService sendExcelFileService;

    @InjectMocks
    private WorkReportCommands workReportCommands;

    @Nested
    @DisplayName("createFile メソッドのテスト")
    class CreateFile {

        @Test
        @DisplayName("正常系：ファイルが正常に作成されること")
        void validParameters_returnsSuccessMessage() {
            // Arrange
            String month = "2025/06";
            String user = "テストユーザー";
            String client = "テスト会社";
            String excelFileName = "テストユーザー_202506_作業報告書.xls";
            String csvFileName = "202506_work_data.csv";

            when(reportService.createReport(month, user, client)).thenReturn(excelFileName);
            when(reportService.createCsvFile(month)).thenReturn(csvFileName);

            // Act
            String result = workReportCommands.createFile(month, user, client);

            // Assert
            assertThat(result).contains("ファイル作成完了");
            assertThat(result).contains(excelFileName);
            assertThat(result).contains(csvFileName);
            verify(reportService).createReport(month, user, client);
            verify(reportService).createCsvFile(month);
        }

        @Test
        @DisplayName("異常系：例外が発生した場合にエラーメッセージが返されること")
        void exceptionThrown_returnsErrorMessage() {
            // Arrange
            String month = "2025/06";
            String user = "テストユーザー";
            String client = "テスト会社";
            String errorMessage = "ファイル作成中にエラーが発生しました";

            when(reportService.createReport(month, user, client)).thenThrow(new RuntimeException(errorMessage));

            // Act
            String result = workReportCommands.createFile(month, user, client);

            // Assert
            assertThat(result).contains("エラー");
            assertThat(result).contains(errorMessage);
            verify(reportService).createReport(month, user, client);
        }
    }

    @Nested
    @DisplayName("updateFile メソッドのテスト")
    class UpdateFile {

        @Test
        @DisplayName("正常系：ファイルが正常に更新されること")
        void validParameters_returnsSuccessMessage() {
            // Arrange
            String fileName = "テストユーザー_202506_作業報告書.xls";
            String csvFile = "202506_work_data.csv";
            int updatedRows = 10;

            when(reportService.updateFromCsv(fileName, csvFile)).thenReturn(updatedRows);

            // Act
            String result = workReportCommands.updateFile(fileName, csvFile);

            // Assert
            assertThat(result).contains("更新完了");
            assertThat(result).contains(String.valueOf(updatedRows));
            verify(reportService).updateFromCsv(fileName, csvFile);
        }

        @Test
        @DisplayName("異常系：例外が発生した場合にエラーメッセージが返されること")
        void exceptionThrown_returnsErrorMessage() {
            // Arrange
            String fileName = "テストユーザー_202506_作業報告書.xls";
            String csvFile = "202506_work_data.csv";
            String errorMessage = "ファイル更新中にエラーが発生しました";

            when(reportService.updateFromCsv(fileName, csvFile)).thenThrow(new RuntimeException(errorMessage));

            // Act
            String result = workReportCommands.updateFile(fileName, csvFile);

            // Assert
            assertThat(result).contains("エラー");
            assertThat(result).contains(errorMessage);
            verify(reportService).updateFromCsv(fileName, csvFile);
        }
    }

    @Nested
    @DisplayName("saveLatestCsv メソッドのテスト")
    class SaveLatestCsv {

        @Test
        @DisplayName("正常系：ファイルが正常に保存されること")
        void filesUpdated_returnsSuccessMessage() {
            // Arrange
            int updatedFiles = 3;

            when(reportService.saveLatestCsvToExcel()).thenReturn(updatedFiles);

            // Act
            String result = workReportCommands.saveLatestCsv();

            // Assert
            assertThat(result).contains("保存完了");
            assertThat(result).contains(String.valueOf(updatedFiles));
            verify(reportService).saveLatestCsvToExcel();
        }

        @Test
        @DisplayName("正常系：更新対象のファイルがない場合に適切なメッセージが返されること")
        void noFilesToUpdate_returnsNoFilesMessage() {
            // Arrange
            int updatedFiles = 0;

            when(reportService.saveLatestCsvToExcel()).thenReturn(updatedFiles);

            // Act
            String result = workReportCommands.saveLatestCsv();

            // Assert
            assertThat(result).contains("更新対象のファイルがありませんでした");
            verify(reportService).saveLatestCsvToExcel();
        }

        @Test
        @DisplayName("異常系：例外が発生した場合にエラーメッセージが返されること")
        void exceptionThrown_returnsErrorMessage() {
            // Arrange
            String errorMessage = "ファイル保存中にエラーが発生しました";

            when(reportService.saveLatestCsvToExcel()).thenThrow(new RuntimeException(errorMessage));

            // Act
            String result = workReportCommands.saveLatestCsv();

            // Assert
            assertThat(result).contains("エラー");
            assertThat(result).contains(errorMessage);
            verify(reportService).saveLatestCsvToExcel();
        }
    }

    @Nested
    @DisplayName("showHelp メソッドのテスト")
    class ShowHelp {

        @Test
        @DisplayName("ヘルプメッセージが正常に表示されること")
        void returnsHelpMessage() {
            // Arrange
            String helpMessage = "コマンド一覧:\n- create-file: 新規報告書ファイルを作成\n- update-file: CSVファイルで報告書を更新";

            when(reportService.getHelpMessage()).thenReturn(helpMessage);

            // Act
            String result = workReportCommands.showHelp();

            // Assert
            assertThat(result).isEqualTo(helpMessage);
            verify(reportService).getHelpMessage();
        }
    }

    @Nested
    @DisplayName("sendExcelFile メソッドのテスト")
    class SendExcelFile {

        @Test
        @DisplayName("正常系：パラメータありでファイルが正常に送信されること")
        void validParameters_returnsSuccessMessage() {
            // Arrange
            String fileName = "テストユーザー_202506_作業報告書.xls";
            String successMessage = "ファイルを送信しました";

            when(sendExcelFileService.sendExcelFile(fileName)).thenReturn(successMessage);

            // Act
            String result = workReportCommands.sendExcelFile(fileName);

            // Assert
            assertThat(result).isEqualTo(successMessage);
            verify(sendExcelFileService).sendExcelFile(fileName);
        }

        @Test
        @DisplayName("正常系：パラメータなしでファイルが正常に送信されること")
        void noParameters_returnsSuccessMessage() {
            // Arrange
            String emptyFileName = "";
            String successMessage = "ファイルを送信しました";

            when(sendExcelFileService.sendExcelFile(emptyFileName)).thenReturn(successMessage);

            // Act
            String result = workReportCommands.sendExcelFile(emptyFileName);

            // Assert
            assertThat(result).isEqualTo(successMessage);
            verify(sendExcelFileService).sendExcelFile(emptyFileName);
        }

        @Test
        @DisplayName("異常系：例外が発生した場合にエラーメッセージが返されること")
        void exceptionThrown_returnsErrorMessage() {
            // Arrange
            String fileName = "テストユーザー_202506_作業報告書.xls";
            String errorMessage = "ファイル送信中にエラーが発生しました";

            when(sendExcelFileService.sendExcelFile(fileName)).thenReturn("エラー: " + errorMessage);

            // Act
            String result = workReportCommands.sendExcelFile(fileName);

            // Assert
            assertThat(result).contains("エラー");
            assertThat(result).contains(errorMessage);
            verify(sendExcelFileService).sendExcelFile(fileName);
        }
    }
}
