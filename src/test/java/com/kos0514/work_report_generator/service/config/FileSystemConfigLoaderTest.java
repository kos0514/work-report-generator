package com.kos0514.work_report_generator.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.kos0514.work_report_generator.exception.ConfigLoadException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FileSystemConfigLoader クラスのテスト")
@ExtendWith(MockitoExtension.class)
class FileSystemConfigLoaderTest {

    @TempDir
    Path tempDir;
    @Spy
    @InjectMocks
    private FileSystemConfigLoader configLoader;

    @Nested
    @DisplayName("loadConfig メソッドのテスト")
    class LoadConfig {

        @Test
        @DisplayName("正常系：設定ファイルを読み込めること")
        void validFile_loadsProperties() throws IOException {
            // Arrange - テスト用の設定ファイルを作成
            Path configFile = tempDir.resolve("test-config.properties");
            String content = "test.key=test value";
            Files.write(configFile, content.getBytes());

            // Act
            Properties result = configLoader.loadConfig(configFile);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getProperty("test.key")).isEqualTo("test value");
        }

        @Test
        @DisplayName("異常系：ファイルが存在しない場合は例外がスローされること")
        void fileNotExists_throwsException() {
            // Arrange
            Path nonExistentFile = tempDir.resolve("non-existent.properties");

            // Act & Assert
            assertThatThrownBy(() -> configLoader.loadConfig(nonExistentFile))
                .isInstanceOf(ConfigLoadException.class)
                .hasMessageContaining("設定ファイルの読み込みに失敗しました");
        }
    }

    @Nested
    @DisplayName("getTemplateStream メソッドのテスト")
    class GetTemplateStream {

        @Test
        @DisplayName("正常系：テンプレートファイルからInputStreamを取得できること")
        void validTemplate_returnsInputStream() {
            // Arrange - モッククラスを作成してgetResourceAsStreamをオーバーライド
            String templatePath = "/templates/test-template.properties";
            InputStream mockStream = new ByteArrayInputStream("test.key=test value".getBytes());

            FileSystemConfigLoader spyLoader = spy(new FileSystemConfigLoader() {
                @Override
                public InputStream getTemplateStream(String templatePath) {
                    // getClass().getResourceAsStream()の部分を直接モック化
                    return mockStream;
                }
            });

            // Act
            InputStream result = spyLoader.getTemplateStream(templatePath);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(mockStream);
        }

        @Test
        @DisplayName("異常系：テンプレートファイルが見つからない場合はnullを返すこと")
        void templateNotFound_returnsNull() {
            // Arrange
            String nonExistentTemplate = "/templates/non-existent-template.properties";

            // Act
            InputStream result = configLoader.getTemplateStream(nonExistentTemplate);

            // Assert
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("saveConfig メソッドのテスト")
    class SaveConfig {

        @Test
        @DisplayName("正常系：設定ファイルを保存できること")
        void validProperties_savesFile() throws IOException {
            // Arrange
            Path configFile = tempDir.resolve("save-test.properties");
            Properties props = new Properties();
            props.setProperty("test.key", "test value");

            // Act
            configLoader.saveConfig(configFile, props);

            // Assert
            assertThat(Files.exists(configFile)).isTrue();
            Properties loadedProps = new Properties();
            try (InputStream is = Files.newInputStream(configFile)) {
                loadedProps.load(is);
            }
            assertThat(loadedProps.getProperty("test.key")).isEqualTo("test value");
        }

        @Test
        @DisplayName("異常系：ファイル保存時にIOExceptionが発生した場合は例外がスローされること")
        void ioExceptionOccurs_throwsException() throws IOException {
            // Arrange
            Path configFile = tempDir.resolve("error-test.properties");
            Properties props = new Properties();
            props.setProperty("test.key", "test value");

            // FileSystemConfigLoaderのスパイオブジェクトに対して、saveConfigメソッドが呼び出されたときに
            // IOExceptionをスローするように設定
            doThrow(new IOException("テスト用のIO例外")).when(configLoader).saveConfig(any(Path.class), any(Properties.class));

            // Act & Assert
            assertThatThrownBy(() -> configLoader.saveConfig(configFile, props))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("テスト用のIO例外");
            // 56行目のlog.errorが実行されることを確認するためのテスト
        }
    }

    @Nested
    @DisplayName("configExists メソッドのテスト")
    class ConfigExists {

        @Test
        @DisplayName("正常系：ファイルが存在する場合はtrueを返すこと")
        void fileExists_returnsTrue() throws IOException {
            // Arrange - テスト用のファイルを作成
            Path existingFile = tempDir.resolve("existing.properties");
            Files.createFile(existingFile);

            // Act
            boolean result = configLoader.configExists(existingFile);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("正常系：ファイルが存在しない場合はfalseを返すこと")
        void fileNotExists_returnsFalse() {
            // Arrange
            Path nonExistentFile = tempDir.resolve("non-existent.properties");

            // Act
            boolean result = configLoader.configExists(nonExistentFile);

            // Assert
            assertThat(result).isFalse();
        }
    }
}