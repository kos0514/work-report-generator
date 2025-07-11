package com.kos0514.work_report_generator.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import java.util.Scanner;

/**
 * コンソールからのユーザー入力を処理するUserInputServiceの実装
 */
@Service
public class ConsoleUserInputService implements UserInputService {

    // 単一のScannerインスタンスを使用してSystem.inを閉じないようにする
    private final Scanner scanner = new Scanner(System.in);

    /**
     * ユーザーからテキスト入力を取得します
     *
     * @param prompt 表示するプロンプト
     * @return ユーザーが入力したテキスト
     */
    @Override
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * ユーザーからYes/No入力を取得します
     *
     * @param prompt 表示するプロンプト
     * @return ユーザーがYesを選択した場合はtrue、Noを選択した場合はfalse
     */
    @Override
    public boolean readYesNo(String prompt) {
        System.out.print(prompt);
        String answer = scanner.nextLine().trim().toLowerCase();
        return answer.equals("y") || answer.equals("yes");
    }

    /**
     * アプリケーション終了時にScannerをクローズ
     */
    @PreDestroy
    public void closeScanner() {
        scanner.close();
    }
}
