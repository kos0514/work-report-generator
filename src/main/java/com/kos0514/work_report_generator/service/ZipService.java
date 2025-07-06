package com.kos0514.work_report_generator.service;

import com.kos0514.work_report_generator.util.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *  ZIPファイルの作成と管理を行うサービスクラス
 */
@Service
@RequiredArgsConstructor
public class ZipService {

  private static final Logger logger = LoggerFactory.getLogger(ZipService.class);

  private final ConfigService configService;

  /**
   * ファイルをパスワード付きZIPファイルに圧縮します パスワードは内部で生成され、戻り値として返されます
   *
   * @param sourceFilePath 圧縮するファイルのパス
   * @param zipFilePath 作成するZIPファイルのパス
   * @return 生成されたパスワード
   * @throws IOException 圧縮処理に失敗した場合
   */
  public String createPasswordProtectedZip(String sourceFilePath, String zipFilePath)
      throws IOException {
    // パスワード生成
    String password = generatePassword();

    // 親ディレクトリが存在しない場合は作成
    Path zipFileParent = Paths.get(zipFilePath).getParent();
    if (zipFileParent != null && !Files.exists(zipFileParent)) {
      Files.createDirectories(zipFileParent);
    }

    // ZIPファイル作成
    ZipParameters zipParameters = new ZipParameters();
    zipParameters.setEncryptFiles(true);
    zipParameters.setEncryptionMethod(EncryptionMethod.AES);
    zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
    zipParameters.setCompressionLevel(CompressionLevel.NORMAL);

    // 文字化け対策
    try (ZipFile zipFile = new ZipFile(zipFilePath, password.toCharArray())) {
      // UTF-8エンコーディングを使用
      zipFile.setCharset(StandardCharsets.UTF_8);
      zipFile.addFile(new File(sourceFilePath), zipParameters);
    }

    logger.info("パスワード付きZIPファイルを作成しました: {}", zipFilePath);
    return password;
  }

  /**
   * 指定されたルールに従ってランダムなパスワードを生成します 英字大文字小文字、数字、記号なし、8文字
   *
   * @return 生成されたパスワード
   */
  private String generatePassword() {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();

    // 8文字のパスワードを生成
    for (int i = 0; i < Constants.Password.LENGTH; i++) {
      int index = random.nextInt(Constants.Password.CHARS.length());
      sb.append(Constants.Password.CHARS.charAt(index));
    }

    return sb.toString();
  }

  /**
   * パスワードをファイルに保存します
   *
   * @param yearMonth 年月（yyyymm形式）
   * @param password パスワード
   * @throws IOException ファイル書き込みに失敗した場合
   */
  public void savePasswordToFile(String yearMonth, String password) throws IOException {
    // 送信先ディレクトリを取得
    String sendDir = configService.getSendDirectory();
    if (sendDir == null || sendDir.isEmpty()) {
      throw new IllegalStateException("送信先ディレクトリが設定されていません。先に設定してください。");
    }

    // yearMonthから年を抽出
    String year = yearMonth.substring(0, 4);

    // パスワード保存先ディレクトリ作成
    String passwordDirPath =
        Paths.get(sendDir, Constants.Files.WORK_DIR, year, yearMonth).toString();
    Path passwordDir = Paths.get(passwordDirPath);
    if (!Files.exists(passwordDir)) {
      Files.createDirectories(passwordDir);
    }

    // パスワードをファイルに保存
    Path passwordFile = Paths.get(passwordDirPath, Constants.Files.PASSWORD_FILE_NAME);
    Files.writeString(
        passwordFile,
        password,
        Files.exists(passwordFile)
            ? StandardOpenOption.TRUNCATE_EXISTING
            : StandardOpenOption.CREATE);

    logger.info("パスワードをファイルに保存しました: {}", passwordFile);
  }
}
