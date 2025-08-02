package com.kos0514.work_report_generator.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConfigLoadException クラスのテスト")
class ConfigLoadExceptionTest {

    @Test
    @DisplayName("メッセージのみのコンストラクタが正しく機能すること")
    void constructor_withMessage_setsMessage() {
        // Arrange
        String errorMessage = "テスト用エラーメッセージ";
        
        // Act
        ConfigLoadException exception = new ConfigLoadException(errorMessage);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getCause()).isNull();
    }
    
    @Test
    @DisplayName("メッセージと原因のコンストラクタが正しく機能すること")
    void constructor_withMessageAndCause_setsMessageAndCause() {
        // Arrange
        String errorMessage = "テスト用エラーメッセージ";
        Throwable cause = new RuntimeException("原因となる例外");
        
        // Act
        ConfigLoadException exception = new ConfigLoadException(errorMessage, cause);
        
        // Assert
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}