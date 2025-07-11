package com.kos0514.work_report_generator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** ファイル操作を集約するサービスクラス ディレクトリ作成やファイル読み書きの重複コードを削減します */
@Service
public class FileService {

  private static final Logger logger = LoggerFactory.getLogger(FileService.class);
  private static final String WORK_DIR_FORMAT = "%s/%s/%s/%s";

  /**
   * ディレクトリが存在しない場合は作成します
   *
   * @param dirPath 作成するディレクトリのパス
   * @return 作成されたディレクトリのパス
   * @throws IOException ディレクトリ作成に失敗した場合
   */
  public Path createDirectoryIfNotExists(Path dirPath) throws IOException {
    if (!Files.exists(dirPath)) {
      Files.createDirectories(dirPath);
      logger.debug("ディレクトリを作成しました: {}", dirPath);
    }
    return dirPath;
  }

  /**
   * ファイルにテキストを書き込みます ファイルが存在する場合は上書き、存在しない場合は新規作成します
   *
   * @param filePath 書き込み先ファイルのパス
   * @param content 書き込む内容
   * @throws IOException ファイル書き込みに失敗した場合
   */
  public void writeStringToFile(Path filePath, String content) throws IOException {
    Files.writeString(
        filePath,
        content,
        Files.exists(filePath) ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE);
    logger.debug("ファイルに書き込みました: {}", filePath);
  }

  /**
   * ファイルからテキストを読み込みます
   *
   * @param filePath 読み込むファイルのパス
   * @return ファイルの内容
   * @throws IOException ファイル読み込みに失敗した場合
   */
  public String readStringFromFile(Path filePath) throws IOException {
    if (!Files.exists(filePath)) {
      throw new IOException("ファイルが見つかりません: " + filePath);
    }
    return Files.readString(filePath);
  }

  /**
   * ファイルが存在するかチェックします
   *
   * @param path チェックするパス
   * @return ファイルが存在する場合はtrue
   */
  public boolean exists(Path path) {
    return Files.exists(path);
  }

  /**
   * 年月に対応する作業ディレクトリを作成します
   *
   * @param sendDir 送信先ディレクトリ
   * @param workDirName 作業ディレクトリ名（Constants.Files.WORK_DIR）
   * @param year 年（yyyy形式）
   * @param yearMonth 年月（yyyymm形式）
   * @return 作成されたディレクトリのパス
   * @throws IOException ディレクトリ作成に失敗した場合
   */
  public Path createWorkDirectoryForYearMonth(String sendDir, String workDirName, String year, String yearMonth) throws IOException {
    if (sendDir == null || sendDir.isEmpty()) {
      throw new IllegalArgumentException("送信先ディレクトリが指定されていません");
    }

    String dirPath = String.format(WORK_DIR_FORMAT, sendDir, workDirName, year, yearMonth);
    Path path = Paths.get(dirPath);
    return createDirectoryIfNotExists(path);
  }
}
