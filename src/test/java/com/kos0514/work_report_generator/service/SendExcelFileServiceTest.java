package com.kos0514.work_report_generator.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kos0514.work_report_generator.service.config.ConfigService;
import com.kos0514.work_report_generator.service.file.ZipService;
import com.kos0514.work_report_generator.service.mail.MailTemplateService;
import com.kos0514.work_report_generator.service.report.ReportService;
import com.kos0514.work_report_generator.service.send_file.SendExcelFileService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SendExcelFileServiceのテストクラス
 * UserInputServiceのモック実装を使用してテスト容易性を示します
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

    @Test
    void testSendExcelFile_WithUserInput() throws Exception {
        // モックの設定
        when(configService.getSendDirectory()).thenReturn(null); // 送信先ディレクトリが未設定

        // テスト用のCSVファイル情報を作成
        ReportService.CsvFileInfo csvFileInfo = new ReportService.CsvFileInfo("test.csv", "202507");
        when(reportService.findLatestCsvFile()).thenReturn(csvFileInfo);

        // テスト用のExcelファイルリストを作成
        when(reportService.findExcelFilesByYearMonth("202507")).thenReturn(List.of("user_202507_作業報告書.xls"));
        when(reportService.extractYearMonthFromFileName("user_202507_作業報告書.xls")).thenReturn("202507");
        when(reportService.getOutputDir()).thenReturn("/test/output");

        // ZIPファイル作成のモック
        when(zipService.createPasswordProtectedZip(anyString(), anyString())).thenReturn("password123");

        // メソッド実行
        String result = sendExcelFileService.sendExcelFile("");

        // 検証
        verify(configService).setSendDirectory("/test/output"); // テスト用の入力が使用されたことを確認
        verify(zipService).createPasswordProtectedZip(any(), any());
        verify(zipService).savePasswordToFile(anyString(), anyString());
        verify(mailTemplateService).generateAndSaveMailTemplates(anyString(), anyString());

        // 結果の検証
        assertTrue(result.contains("ファイルを送信しました"));
    }
}
