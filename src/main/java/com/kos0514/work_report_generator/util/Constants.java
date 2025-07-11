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
    public static final String MAIL_DIR = "mail";
    public static final String SEND_CONFIG_FILE = "send-config.properties";


    // テンプレートファイル
   public static final String SEND_CONFIG_TEMPLATE_PATH = "/templates/send-config.template.properties";

    // メールテンプレートファイル
    public static final String MAIL_TEMPLATE_FILE = "mail_template.txt";

    // 出力ファイル名
    public static final String MAIL_COMBINED_FILE = "mail_content.txt";
    // 以下は互換性のために残しています
    public static final String MAIL_SUBJECT_FILE = "mail_subject.txt";
    public static final String MAIL_BODY_FILE = "mail_body.txt";
    public static final String PASSWORD_MAIL_BODY_FILE = "password_mail_body.txt";

    // テンプレート区切り文字
    public static final String TEMPLATE_DELIMITER = "--------";
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
