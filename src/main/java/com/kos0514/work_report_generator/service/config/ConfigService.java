package com.kos0514.work_report_generator.service.config;

import com.kos0514.work_report_generator.util.Constants;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *  設定ファイルの読み書きを行うサービスクラス
 */
@Service
public class ConfigService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
  private static final String LOCAL_DATA_DIR = "./local-data";
  private static final String CONFIG_PATH = LOCAL_DATA_DIR + "/" + Constants.Files.CONFIG_DIR;
  private static final String SEND_CONFIG_PATH =
      CONFIG_PATH + "/" + Constants.Files.SEND_CONFIG_FILE;

  /**
   * 送信先ディレクトリの設定を取得します
   *
   * @return 送信先ディレクトリのパス、設定がない場合はnull
   */
  public String getSendDirectory() {
    Properties props = loadSendConfig();
    return props.getProperty(Constants.ConfigKeys.SEND_DIRECTORY);
  }

  /**
   * 送信先ディレクトリの設定を保存します
   *
   * @param directory 送信先ディレクトリのパス
   * @throws IOException 設定ファイルの保存に失敗した場合
   */
  public void setSendDirectory(String directory) throws IOException {
    Properties props = loadSendConfig();
    props.setProperty(Constants.ConfigKeys.SEND_DIRECTORY, directory);
    saveSendConfig(props);
    logger.info("送信先ディレクトリを設定しました: {}", directory);
  }

  /**
   * 送信設定ファイルを読み込みます
   *
   * @return 読み込まれたプロパティ
   */
  private Properties loadSendConfig() {
    Properties props = new Properties();

    try {
      Path configFile = Paths.get(SEND_CONFIG_PATH);
      if (Files.exists(configFile)) {
        try (FileInputStream fis = new FileInputStream(SEND_CONFIG_PATH)) {
          props.load(fis);
          logger.debug("設定ファイルを読み込みました: {}", SEND_CONFIG_PATH);
          return props;
        }
      }

      // テンプレートファイルから初期設定を作成
      try (InputStream templateStream = getClass().getResourceAsStream(Constants.Files.SEND_CONFIG_TEMPLATE_PATH)) {
        if (templateStream != null) {
          props.load(templateStream);
          logger.info("テンプレートから設定ファイルを作成します: {}", SEND_CONFIG_PATH);
          // 設定ファイルを保存
          saveSendConfig(props);
        } else {
          // テンプレートが見つからない場合は例外をスロー
          throw new IOException("テンプレートファイルが見つかりません: " + Constants.Files.SEND_CONFIG_TEMPLATE_PATH);
        }
      }
    } catch (IOException e) {
      logger.error("設定ファイルの読み込みに失敗しました: {}", e.getMessage());
    }

    return props;
  }

  /**
   * 送信設定ファイルを保存します
   *
   * @param props 保存するプロパティ
   * @throws IOException 設定ファイルの保存に失敗した場合
   */
  private void saveSendConfig(Properties props) throws IOException {
    // 設定ファイルを保存
    try (FileOutputStream fos = new FileOutputStream(SEND_CONFIG_PATH)) {
      props.store(fos, "送信設定");
      logger.debug("設定ファイルを保存しました: {}", SEND_CONFIG_PATH);
    }
  }
}
