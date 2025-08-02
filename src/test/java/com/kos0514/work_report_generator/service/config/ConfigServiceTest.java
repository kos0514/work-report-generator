package com.kos0514.work_report_generator.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kos0514.work_report_generator.exception.ConfigLoadException;
import com.kos0514.work_report_generator.util.Constants;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ConfigService クラスのテスト")
@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    /**
     * テスト用のディレクトリパス定数
     */
    private static final String TEST_SEND_DIRECTORY = "/test/send/directory";
    private static final String NEW_SEND_DIRECTORY = "/new/send/directory";
    private static final String SEND_CONFIG_PATH = "./local-data/config/send-config.properties";

    @Mock
    private ConfigLoader configLoader;

    @InjectMocks
    private ConfigService configService;

    @Nested
    @DisplayName("getSendDirectory メソッドのテスト")
    class GetSendDirectory {

        @Test
        @DisplayName("正常系：設定ファイルから送信先ディレクトリを取得できること")
        void configExists_returnsSendDirectory() throws IOException {
            // Arrange - テスト用のプロパティを設定
            Properties props = new Properties();
            props.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, TEST_SEND_DIRECTORY);

            Path configPath = Paths.get(SEND_CONFIG_PATH);
            when(configLoader.configExists(configPath)).thenReturn(true);
            when(configLoader.loadConfig(configPath)).thenReturn(props);

            // Act - テスト対象メソッドを実行
            String result = configService.getSendDirectory();

            // Assert - 結果を検証
            assertThat(result).isEqualTo(TEST_SEND_DIRECTORY);
            verify(configLoader).configExists(configPath);
            verify(configLoader).loadConfig(configPath);
        }

        @Test
        @DisplayName("正常系：設定ファイルが存在しない場合はテンプレートから作成されること")
        void configNotExists_createsFromTemplate() throws IOException {
            // Arrange - テスト用のプロパティを設定
            Properties props = new Properties();
            props.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, "");

            Path configPath = Paths.get(SEND_CONFIG_PATH);
            when(configLoader.configExists(configPath)).thenReturn(false);

            // テンプレートの内容をモック
            String templateContent = "send.directory=";
            InputStream templateStream = new ByteArrayInputStream(templateContent.getBytes());
            when(configLoader.getTemplateStream(Constants.Files.SEND_CONFIG_TEMPLATE_PATH)).thenReturn(templateStream);

            // Act - テスト対象メソッドを実行
            String result = configService.getSendDirectory();

            // Assert - 結果を検証
            assertThat(result).isEmpty(); // テンプレートでは値が空
            verify(configLoader).configExists(configPath);
            verify(configLoader).getTemplateStream(Constants.Files.SEND_CONFIG_TEMPLATE_PATH);
            verify(configLoader).saveConfig(eq(configPath), any(Properties.class));
        }

        @Test
        @DisplayName("異常系：テンプレートファイルが見つからない場合は例外がスローされること")
        void templateNotFound_throwsException() {
            // Arrange - テスト用のプロパティを設定
            Path configPath = Paths.get(SEND_CONFIG_PATH);
            when(configLoader.configExists(configPath)).thenReturn(false);
            when(configLoader.getTemplateStream(Constants.Files.SEND_CONFIG_TEMPLATE_PATH)).thenReturn(null);

            // Act & Assert - 例外がスローされることを検証
            assertThatThrownBy(() -> configService.getSendDirectory())
                .isInstanceOf(ConfigLoadException.class)
                .hasMessageContaining("テンプレートファイルが見つかりません");
        }

        @Test
        @DisplayName("異常系：設定ファイルの読み込みに失敗した場合は空のプロパティが返されること")
        void loadError_returnsEmptyProperty() throws IOException {
            // Arrange - 例外をスローするようにモック化
            Path configPath = Paths.get(SEND_CONFIG_PATH);
            when(configLoader.configExists(configPath)).thenReturn(true);
            when(configLoader.loadConfig(configPath)).thenThrow(new IOException("Failed to load config"));

            // Act - テスト対象メソッドを実行
            String result = configService.getSendDirectory();

            // Assert - 結果を検証
            assertThat(result).isNull(); // 設定が取得できないのでnull
        }
    }

    @Nested
    @DisplayName("setSendDirectory メソッドのテスト")
    class SetSendDirectory {

        @Test
        @DisplayName("正常系：送信先ディレクトリを設定できること")
        void validDirectory_setsSendDirectory() throws IOException {
            // Arrange - テスト用のプロパティを設定
            Properties props = new Properties();
            props.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, TEST_SEND_DIRECTORY);

            Path configPath = Paths.get(SEND_CONFIG_PATH);
            when(configLoader.configExists(configPath)).thenReturn(true);
            when(configLoader.loadConfig(configPath)).thenReturn(props);

            // Act - テスト対象メソッドを実行
            configService.setSendDirectory(NEW_SEND_DIRECTORY);

            // Assert - 結果を検証
            Properties expectedProps = new Properties();
            expectedProps.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, NEW_SEND_DIRECTORY);
            verify(configLoader).saveConfig(configPath, expectedProps);
        }

        @Test
        @DisplayName("正常系：設定ファイルが存在しない場合は新規作成されること")
        void configNotExists_createsNewFile() throws IOException {
            // Arrange - テスト用のプロパティを設定
            Path configPath = Paths.get(SEND_CONFIG_PATH);
            when(configLoader.configExists(configPath)).thenReturn(false);

            // テンプレートの内容をモック
            Properties templateProps = new Properties();
            templateProps.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, "");
            String templateContent = "send.directory=";
            InputStream templateStream = new ByteArrayInputStream(templateContent.getBytes());
            when(configLoader.getTemplateStream(Constants.Files.SEND_CONFIG_TEMPLATE_PATH)).thenReturn(templateStream);

            // Act - テスト対象メソッドを実行
            configService.setSendDirectory(NEW_SEND_DIRECTORY);

            // Assert - 結果を検証
            Properties expectedProps = new Properties();
            expectedProps.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, NEW_SEND_DIRECTORY);
            verify(configLoader, times(2)).saveConfig(configPath, expectedProps);
        }

        @Test
        @DisplayName("異常系：設定ファイルの保存に失敗した場合は例外がスローされること")
        void saveError_throwsException() throws IOException {
            // Arrange - テスト用のプロパティを設定
            Properties props = new Properties();
            props.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, TEST_SEND_DIRECTORY);

            Path configPath = Paths.get(SEND_CONFIG_PATH);
            when(configLoader.configExists(configPath)).thenReturn(true);
            when(configLoader.loadConfig(configPath)).thenReturn(props);

            // 保存時に例外をスロー
            doThrow(new IOException("Failed to save config")).when(configLoader).saveConfig(any(Path.class), any(Properties.class));

            // Act & Assert - 例外がスローされることを検証
            assertThatThrownBy(() -> configService.setSendDirectory(NEW_SEND_DIRECTORY))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to save config");
        }
    }
}
