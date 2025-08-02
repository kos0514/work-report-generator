package com.kos0514.work_report_generator.service.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 設定ファイルの読み込みと保存を行うインターフェース
 */
public interface ConfigLoader {
    
    /**
     * 設定ファイルを読み込みます
     *
     * @param configPath 設定ファイルのパス
     * @return 読み込まれたプロパティ
     * @throws IOException 設定ファイルの読み込みに失敗した場合
     */
    Properties loadConfig(Path configPath) throws IOException;
    
    /**
     * テンプレートファイルからInputStreamを取得します
     *
     * @param templatePath テンプレートファイルのパス
     * @return テンプレートファイルのInputStream
     */
    InputStream getTemplateStream(String templatePath);
    
    /**
     * 設定ファイルを保存します
     *
     * @param configPath 設定ファイルのパス
     * @param properties 保存するプロパティ
     * @throws IOException 設定ファイルの保存に失敗した場合
     */
    void saveConfig(Path configPath, Properties properties) throws IOException;
    
    /**
     * 設定ファイルが存在するかを確認します
     *
     * @param configPath 設定ファイルのパス
     * @return 設定ファイルが存在する場合はtrue、存在しない場合はfalse
     */
    boolean configExists(Path configPath);
}