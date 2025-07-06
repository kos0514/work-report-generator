package com.kos0514.work_report_generator.util;

/**
 * アプリケーション全体で使用する定数を定義するクラス
 */
public final class Constants {

  private Constants() {
    // インスタンス化防止
  }

  /** ファイル関連の定数 */
  public static final class Files {
    // ファイル拡張子
    public static final String EXCEL_EXTENSION = ".xls";
    public static final String ZIP_EXTENSION = ".zip";
    public static final String PASSWORD_FILE_NAME = "password.txt";

    // ディレクトリ構造
    public static final String WORK_DIR = "work";
    public static final String CONFIG_DIR = "config";
    public static final String SEND_CONFIG_FILE = "send-config.properties";
  }

  /** パスワード生成関連の定数 */
  public static final class Password {
    public static final String CHARS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final int LENGTH = 8;
  }

  /** 設定ファイルのプロパティキー */
  public static final class ConfigKeys {
    public static final String SEND_DIRECTORY = "send.directory";
  }
}
