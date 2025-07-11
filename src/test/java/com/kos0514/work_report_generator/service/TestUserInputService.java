package com.kos0514.work_report_generator.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * テスト用のUserInputService実装
 * テスト中に事前定義された応答を返すことができます
 */
public class TestUserInputService implements UserInputService {

    // プロンプトに対する応答のマップ
    private final Map<String, String> responses = new HashMap<>();
    
    // デフォルトの応答を生成する関数
    private Function<String, String> defaultResponseFunction = prompt -> "";
    
    /**
     * 特定のプロンプトに対する応答を設定します
     *
     * @param prompt プロンプト
     * @param response 応答
     * @return このインスタンス（メソッドチェーン用）
     */
    public TestUserInputService withResponse(String prompt, String response) {
        responses.put(prompt, response);
        return this;
    }
    
    /**
     * Yes/No質問に対する応答を設定します
     *
     * @param prompt プロンプト
     * @param yesResponse trueの場合は"y"、falseの場合は"n"を返す
     * @return このインスタンス（メソッドチェーン用）
     */
    public TestUserInputService withYesNoResponse(String prompt, boolean yesResponse) {
        responses.put(prompt, yesResponse ? "y" : "n");
        return this;
    }
    
    /**
     * デフォルトの応答生成関数を設定します
     *
     * @param defaultResponseFunction プロンプトを受け取り応答を返す関数
     * @return このインスタンス（メソッドチェーン用）
     */
    public TestUserInputService withDefaultResponseFunction(Function<String, String> defaultResponseFunction) {
        this.defaultResponseFunction = defaultResponseFunction;
        return this;
    }

    @Override
    public String readLine(String prompt) {
        return responses.getOrDefault(prompt, defaultResponseFunction.apply(prompt));
    }

    @Override
    public boolean readYesNo(String prompt) {
        String response = responses.getOrDefault(prompt, defaultResponseFunction.apply(prompt));
        return response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes");
    }
}