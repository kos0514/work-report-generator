package com.kos0514.work_report_generator.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("ConsoleUserInputService クラスのテスト")
@ExtendWith(MockitoExtension.class)
class ConsoleUserInputServiceTest {

    private InputStream originalSystemIn;
    private Scanner scanner;
    private ConsoleUserInputService service;

    @BeforeEach
    void setUp() {
        // 元のSystem.inを保存
        originalSystemIn = System.in;
        // サービスインスタンスを作成
        service = new ConsoleUserInputService();
    }

    @AfterEach
    void tearDown() {
        // テスト後にSystem.inを元に戻す
        System.setIn(originalSystemIn);
        // scannerが存在する場合はクローズ
        if (scanner != null) {
            scanner.close();
            scanner = null;
        }
    }

    @Nested
    @DisplayName("readLine メソッドのテスト")
    class ReadLine {

        @Test
        @DisplayName("正常系：ユーザー入力を取得できること")
        void validInput_returnsUserInput() {
            // Arrange
            String expectedInput = "test input";
            ByteArrayInputStream testIn = new ByteArrayInputStream(expectedInput.getBytes());
            System.setIn(testIn);

            // Scannerを生成
            scanner = new Scanner(System.in);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", scanner);

            // Act
            String result = service.readLine("Enter input: ");

            // Assert
            assertThat(result).isEqualTo(expectedInput);
        }

        @Test
        @DisplayName("正常系：入力の前後の空白が削除されること")
        void inputWithWhitespace_returnsTrimmedInput() {
            // Arrange
            String inputWithWhitespace = "  test input with whitespace  ";
            String expectedTrimmedInput = "test input with whitespace";
            ByteArrayInputStream testIn = new ByteArrayInputStream(inputWithWhitespace.getBytes());
            System.setIn(testIn);

            // Scannerを生成
            scanner = new Scanner(System.in);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", scanner);

            // Act
            String result = service.readLine("Enter input: ");

            // Assert
            assertThat(result).isEqualTo(expectedTrimmedInput);
        }
    }

    @Nested
    @DisplayName("readYesNo メソッドのテスト")
    class ReadYesNo {

        @Test
        @DisplayName("正常系：'y'の入力でtrueを返すこと")
        void inputY_returnsTrue() {
            // Arrange
            String input = "y";
            ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(testIn);

            // Scannerを生成
            scanner = new Scanner(System.in);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", scanner);

            // Act
            boolean result = service.readYesNo("Yes or No? ");

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("正常系：'yes'の入力でtrueを返すこと")
        void inputYes_returnsTrue() {
            // Arrange
            String input = "yes";
            ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(testIn);

            // Scannerを生成
            scanner = new Scanner(System.in);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", scanner);

            // Act
            boolean result = service.readYesNo("Yes or No? ");

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("正常系：'n'の入力でfalseを返すこと")
        void inputN_returnsFalse() {
            // Arrange
            String input = "n";
            ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(testIn);

            // Scannerを生成
            scanner = new Scanner(System.in);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", scanner);

            // Act
            boolean result = service.readYesNo("Yes or No? ");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("正常系：'no'の入力でfalseを返すこと")
        void inputNo_returnsFalse() {
            // Arrange
            String input = "no";
            ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(testIn);

            // Scannerを生成
            scanner = new Scanner(System.in);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", scanner);

            // Act
            boolean result = service.readYesNo("Yes or No? ");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("正常系：その他の入力でfalseを返すこと")
        void otherInput_returnsFalse() {
            // Arrange
            String input = "other";
            ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(testIn);

            // Scannerを生成
            scanner = new Scanner(System.in);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", scanner);

            // Act
            boolean result = service.readYesNo("Yes or No? ");

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("closeScanner メソッドのテスト")
    class CloseScanner {

        @Test
        @DisplayName("正常系：scannerがクローズされること")
        void closesScanner() {
            // Arrange
            Scanner mockScanner = mock(Scanner.class);
            // privateフィールドのscannerを置き換える
            ReflectionTestUtils.setField(service, "scanner", mockScanner);

            // Act
            service.closeScanner();

            // Assert
            verify(mockScanner).close();
        }
    }
}
