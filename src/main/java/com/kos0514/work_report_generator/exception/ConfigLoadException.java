package com.kos0514.work_report_generator.exception;

/**
 * 設定ファイルの読み込みに失敗した場合にスローされる例外
 */
public class ConfigLoadException extends RuntimeException {

    /**
     * 指定されたメッセージで新しい例外を構築します
     *
     * @param message 例外メッセージ
     */
    public ConfigLoadException(String message) {
        super(message);
    }

    /**
     * 指定されたメッセージと原因で新しい例外を構築します
     *
     * @param message 例外メッセージ
     * @param cause 原因となった例外
     */
    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}