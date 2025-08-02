package com.kos0514.work_report_generator.service.send_file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kos0514.work_report_generator.service.TestUserInputService;
import com.kos0514.work_report_generator.service.config.ConfigService;
import com.kos0514.work_report_generator.service.file.ZipService;
import com.kos0514.work_report_generator.service.mail.MailTemplateService;
import com.kos0514.work_report_generator.service.report.ReportService;
import com.kos0514.work_report_generator.service.report.ReportService.CsvFileInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SendExcelFileServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
class SendExcelFileServiceTest {

    @Mock
    private ReportService reportService;

    @Mock
    private ZipService zipService;

    @Mock
    private ConfigService configService;

    @Mock
    private MailTemplateService mailTemplateService;

    private SendExcelFileService sendExcelFileService;

    @BeforeEach
    void setUp() {
        // テスト用のUserInputService実装
        TestUserInputService testUserInputService = new TestUserInputService()
                .withResponse("送信先ディレクトリを入力してください: ", "/test/output");

        // テスト対象のサービスを作成
        sendExcelFileService = new SendExcelFileService(
            reportService, 
            zipService, 
            configService, 
            mailTemplateService,
                testUserInputService
        );
    }

    @Nested
    class SendExcelFile {

        @Test
        void fileNameSpecified_sendsSpecifiedFile() throws Exception {
            // Arrange
            var fileName = "user_202507_作業報告書.xls";
            var yearMonth = "202507";
            var outputDir = "/test/output";
            var password = "password123";
            
            when(configService.getSendDirectory()).thenReturn("/test/send");
            when(reportService.extractYearMonthFromFileName(fileName)).thenReturn(yearMonth);
            when(reportService.getOutputDir()).thenReturn(outputDir);
            when(zipService.createPasswordProtectedZip(anyString(), anyString())).thenReturn(password);
            
            // Act
            var result = sendExcelFileService.sendExcelFile(fileName);
            
            // Assert
            verify(reportService).extractYearMonthFromFileName(fileName);
            verify(reportService).getOutputDir();
            verify(zipService).createPasswordProtectedZip(anyString(), anyString());
            verify(zipService).savePasswordToFile(eq(yearMonth), eq(password));
            verify(mailTemplateService).generateAndSaveMailTemplates(eq(yearMonth), eq(password));
            
            assertThat(result).isNotNull();
            assertThat(result).contains("ファイルを送信しました");
            assertThat(result).contains(fileName);
            assertThat(result).contains(password);
        }
        
        @Test
        void emptyFileName_usesLatestFile() throws Exception {
            // Arrange
            var csvFileInfo = new CsvFileInfo("test.csv", "202507");
            var fileName = "user_202507_作業報告書.xls";
            var yearMonth = "202507";
            var outputDir = "/test/output";
            var password = "password123";
            
            when(configService.getSendDirectory()).thenReturn("/test/send");
            when(reportService.findLatestCsvFile()).thenReturn(csvFileInfo);
            when(reportService.findExcelFilesByYearMonth(yearMonth)).thenReturn(List.of(fileName));
            when(reportService.extractYearMonthFromFileName(fileName)).thenReturn(yearMonth);
            when(reportService.getOutputDir()).thenReturn(outputDir);
            when(zipService.createPasswordProtectedZip(anyString(), anyString())).thenReturn(password);
            
            // Act
            var result = sendExcelFileService.sendExcelFile("");
            
            // Assert
            verify(reportService).findLatestCsvFile();
            verify(reportService).findExcelFilesByYearMonth(yearMonth);
            verify(reportService).extractYearMonthFromFileName(fileName);
            verify(reportService).getOutputDir();
            verify(zipService).createPasswordProtectedZip(anyString(), anyString());
            verify(zipService).savePasswordToFile(eq(yearMonth), eq(password));
            verify(mailTemplateService).generateAndSaveMailTemplates(eq(yearMonth), eq(password));
            
            assertThat(result).isNotNull();
            assertThat(result).contains("ファイルを送信しました");
            assertThat(result).contains(fileName);
            assertThat(result).contains(password);
        }
        
        @Test
        void emptyFileName_noExcelFilesFound_returnsErrorMessage() throws Exception {
            // Arrange
            var csvFileInfo = new CsvFileInfo("test.csv", "202507");
            var yearMonth = "202507";
            
            when(configService.getSendDirectory()).thenReturn("/test/send");
            when(reportService.findLatestCsvFile()).thenReturn(csvFileInfo);
            when(reportService.findExcelFilesByYearMonth(yearMonth)).thenReturn(Collections.emptyList());
            
            // Act
            var result = sendExcelFileService.sendExcelFile("");
            
            // Assert
            verify(reportService).findLatestCsvFile();
            verify(reportService).findExcelFilesByYearMonth(yearMonth);
            verify(zipService, never()).createPasswordProtectedZip(anyString(), anyString());
            verify(zipService, never()).savePasswordToFile(anyString(), anyString());
            verify(mailTemplateService, never()).generateAndSaveMailTemplates(anyString(), anyString());
            
            assertThat(result).isEqualTo("送信可能なExcelファイルが見つかりません");
        }
        
        @Test
        void sendDirectoryNotSet_promptsForDirectory() throws Exception {
            // Arrange
            var fileName = "user_202507_作業報告書.xls";
            var yearMonth = "202507";
            var outputDir = "/test/output";
            var password = "password123";
            
            when(configService.getSendDirectory()).thenReturn(null); // 送信先ディレクトリが未設定
            when(reportService.extractYearMonthFromFileName(fileName)).thenReturn(yearMonth);
            when(reportService.getOutputDir()).thenReturn(outputDir);
            when(zipService.createPasswordProtectedZip(anyString(), anyString())).thenReturn(password);
            
            // Act
            var result = sendExcelFileService.sendExcelFile(fileName);
            
            // Assert
            verify(configService).setSendDirectory("/test/output"); // テスト用の入力が使用されたことを確認
            verify(reportService).extractYearMonthFromFileName(fileName);
            verify(reportService).getOutputDir();
            verify(zipService).createPasswordProtectedZip(anyString(), anyString());
            verify(zipService).savePasswordToFile(eq(yearMonth), eq(password));
            verify(mailTemplateService).generateAndSaveMailTemplates(eq(yearMonth), eq(password));
            
            assertThat(result).isNotNull();
            assertThat(result).contains("ファイルを送信しました");
            assertThat(result).contains(fileName);
            assertThat(result).contains(password);
        }
        
        @Test
        void cannotExtractYearMonth_returnsErrorMessage() throws Exception {
            // Arrange
            var fileName = "invalid_filename.xls";
            
            when(configService.getSendDirectory()).thenReturn("/test/send");
            when(reportService.extractYearMonthFromFileName(fileName)).thenReturn(null);
            
            // Act
            var result = sendExcelFileService.sendExcelFile(fileName);
            
            // Assert
            verify(reportService).extractYearMonthFromFileName(fileName);
            verify(zipService, never()).createPasswordProtectedZip(anyString(), anyString());
            verify(zipService, never()).savePasswordToFile(anyString(), anyString());
            verify(mailTemplateService, never()).generateAndSaveMailTemplates(anyString(), anyString());
            
            assertThat(result).contains("ファイル名から年月を抽出できません");
            assertThat(result).contains(fileName);
        }
        
        @Test
        void exceptionThrown_returnsErrorMessage() throws Exception {
            // Arrange
            var fileName = "user_202507_作業報告書.xls";
            var yearMonth = "202507";
            var outputDir = "/test/output";
            var exceptionMessage = "テスト用例外";
            
            when(configService.getSendDirectory()).thenReturn("/test/send");
            when(reportService.extractYearMonthFromFileName(fileName)).thenReturn(yearMonth);
            when(reportService.getOutputDir()).thenReturn(outputDir);
            doThrow(new IOException(exceptionMessage)).when(zipService).createPasswordProtectedZip(anyString(), anyString());
            
            // Act
            var result = sendExcelFileService.sendExcelFile(fileName);
            
            // Assert
            verify(reportService).extractYearMonthFromFileName(fileName);
            verify(reportService).getOutputDir();
            verify(zipService).createPasswordProtectedZip(anyString(), anyString());
            verify(zipService, never()).savePasswordToFile(anyString(), anyString());
            verify(mailTemplateService, never()).generateAndSaveMailTemplates(anyString(), anyString());
            
            assertThat(result).contains("エラー");
            assertThat(result).contains(exceptionMessage);
        }
    }
}