package com.kos0514.work_report_generator.service;

/**
 * ユーザー入力を処理するサービスのインターフェース
 * テスト容易性を向上させるために、System.inへの直接アクセスを抽象化します
 */
public interface UserInputService {

    /**
     * ユーザーからテキスト入力を取得します
     *
     * @param prompt 表示するプロンプト
     * @return ユーザーが入力したテキスト
     */
    String readLine(String prompt);

    /**
     * ユーザーからYes/No入力を取得します
     *
     * @param prompt 表示するプロンプト
     * @return ユーザーがYesを選択した場合はtrue、Noを選択した場合はfalse
     */
    boolean readYesNo(String prompt);
}