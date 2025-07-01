package com.kos0514.work_report_generator.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 * Excelファイルの操作に関するサービスクラス
 *
 * <p>このクラスはExcelファイルの読み書きや、セルの操作、日付からの行検索などの 機能を提供します。
 */
@Service
public class ExcelService {

  /**
   * Excelファイルを読み込み、ワークブックオブジェクトを返します
   *
   * @param filePath 読み込むExcelファイルのパス
   * @return 読み込まれたHSSFWorkbookオブジェクト
   * @throws UncheckedIOException ファイルの読み込みに失敗した場合
   */
  public HSSFWorkbook loadWorkbook(String filePath) {
    try (FileInputStream fis = new FileInputStream(filePath)) {
      return new HSSFWorkbook(fis);
    } catch (IOException e) {
      throw new UncheckedIOException("Excelファイルの読み込みに失敗しました: " + filePath, e);
    }
  }

  /**
   * 指定されたシートの指定されたセル位置に値を設定します
   *
   * @param sheet 対象のシート
   * @param cellPosition セル位置（例: "B7"）
   * @param value 設定する値
   * @throws IllegalArgumentException セル位置の形式が不正な場合
   */
  public void setCellValue(HSSFSheet sheet, String cellPosition, String value) {
    int[] pos = parseCellPosition(cellPosition);
    int row = pos[0];
    int col = pos[1];

    HSSFRow hssfRow = sheet.getRow(row);
    if (hssfRow == null) {
      hssfRow = sheet.createRow(row);
    }

    HSSFCell cell = hssfRow.getCell(col);
    if (cell == null) {
      cell = hssfRow.createCell(col);
    }

    cell.setCellValue(value);
  }

  /**
   * 指定されたシートの指定されたセル位置に日付値を設定します
   *
   * @param sheet 対象のシート
   * @param cellPosition セル位置（例: "B7"）
   * @param date 設定する日付
   * @throws IllegalArgumentException セル位置の形式が不正な場合
   */
  public void setCellDateValue(HSSFSheet sheet, String cellPosition, Date date) {
    int[] pos = parseCellPosition(cellPosition);
    int row = pos[0];
    int col = pos[1];

    HSSFRow hssfRow = sheet.getRow(row);
    if (hssfRow == null) {
      hssfRow = sheet.createRow(row);
    }

    HSSFCell cell = hssfRow.getCell(col);
    if (cell == null) {
      cell = hssfRow.createCell(col);
    }

    cell.setCellValue(date);
  }

  /**
   * 指定された日付に対応する行番号を特定します
   *
   * @param sheet 対象のシート
   * @param date 検索する日付
   * @return 見つかった場合は行番号（0ベース）、見つからない場合は-1
   */
  public int findRowByDate(HSSFSheet sheet, LocalDate date) {
    int targetDay = date.getDayOfMonth();

    // 日付が1-31の範囲内かチェック

    // テンプレートファイルでは、B7に1日が設定され、以降は連続的に日付が設定される
    // B7 = 1日, B8 = 2日, B9 = 3日, ...
    int rowNum = (targetDay - 1) + 7; // 7行目（0ベース）が1日に対応

    // 行番号が有効範囲内（7-37行目）かチェック

    // 計算した行が存在するかチェック
    HSSFRow row = sheet.getRow(rowNum);
    if (row == null) {
      return -1;
    }

    return rowNum;
  }

  /**
   * ワークブックをファイルに保存します
   *
   * @param workbook 保存するワークブック
   * @param filePath 保存先のファイルパス
   * @throws UncheckedIOException ファイルの保存に失敗した場合
   */
  public void saveWorkbook(HSSFWorkbook workbook, String filePath) {
    try (FileOutputStream fos = new FileOutputStream(filePath)) {
      workbook.write(fos);
    } catch (IOException e) {
      throw new UncheckedIOException("Excelファイルの保存に失敗しました: " + filePath, e);
    }
  }

  /**
   * 指定されたセルをクリアします
   *
   * @param sheet 対象のシート
   * @param cellPosition セル位置（例: "B7"）
   */
  public void clearCell(HSSFSheet sheet, String cellPosition) {
    int[] pos = parseCellPosition(cellPosition);
    int row = pos[0];
    int col = pos[1];

    HSSFRow hssfRow = sheet.getRow(row);
    if (hssfRow != null) {
      HSSFCell cell = hssfRow.getCell(col);
      if (cell != null) {
        cell.setBlank();
      }
    }
  }

  /**
   * セル位置文字列を行列番号に変換します
   *
   * @param cellPosition セル位置（例: "B7"）
   * @return 行番号と列番号の配列 [rowNum, colNum]（0ベース）
   * @throws IllegalArgumentException セル位置の形式が不正な場合
   */
  private int[] parseCellPosition(String cellPosition) {
    if (cellPosition == null || cellPosition.length() < 2) {
      throw new IllegalArgumentException("セル位置の形式が不正です: " + cellPosition);
    }

    char colChar = cellPosition.charAt(0);
    if (colChar < 'A' || colChar > 'Z') {
      throw new IllegalArgumentException("列指定が不正です: " + colChar);
    }

    try {
      int rowNum = Integer.parseInt(cellPosition.substring(1)) - 1; // 0-based indexing
      int colNum = colChar - 'A'; // A=0, B=1, C=2, ...

      if (rowNum < 0) {
        throw new IllegalArgumentException("行番号は1以上である必要があります: " + cellPosition);
      }

      return new int[] {rowNum, colNum};
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("行番号の形式が不正です: " + cellPosition, e);
    }
  }

  /**
   * ファイルをコピーします
   *
   * @param sourcePath コピー元のファイルパス
   * @param destPath コピー先のファイルパス
   * @throws UncheckedIOException ファイルのコピーに失敗した場合
   */
  public void copyFile(String sourcePath, String destPath) {
    try (FileInputStream fis = new FileInputStream(sourcePath);
        FileOutputStream fos = new FileOutputStream(destPath)) {
      fis.transferTo(fos);
    } catch (IOException e) {
      throw new UncheckedIOException("ファイルのコピーに失敗しました: " + sourcePath + " -> " + destPath, e);
    }
  }
}
