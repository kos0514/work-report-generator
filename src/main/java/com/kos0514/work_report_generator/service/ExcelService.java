package com.kos0514.work_report_generator.service;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Excelファイルの操作に関するサービスクラス
 * <p>
 * このクラスはExcelファイルの読み書きや、セルの操作、日付からの行検索などの
 * 機能を提供します。
 * </p>
 */
@Service
public class ExcelService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d");

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
     * 指定された日付に対応する行番号を特定します
     * 
     * @param sheet 対象のシート
     * @param date 検索する日付
     * @return 見つかった場合は行番号（0ベース）、見つからない場合は-1
     */
    public int findRowByDate(HSSFSheet sheet, LocalDate date) {
        int targetDay = date.getDayOfMonth();

        // 作業データ行範囲（8-38行目）で検索
        for (int rowNum = 7; rowNum <= 37; rowNum++) { // 0-based indexing
            HSSFRow row = sheet.getRow(rowNum);
            if (row != null) {
                HSSFCell dateCell = row.getCell(1); // B列 (0-based indexing)
                if (dateCell != null && dateCell.getCellType() == CellType.NUMERIC) {
                    int cellValue = (int) dateCell.getNumericCellValue();
                    if (cellValue == targetDay) {
                        return rowNum;
                    }
                }
            }
        }

        return -1; // 見つからない場合
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

            return new int[]{rowNum, colNum};
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
