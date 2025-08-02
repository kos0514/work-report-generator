package com.kos0514.work_report_generator.service.config;

import com.kos0514.work_report_generator.exception.ConfigLoadException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ファイルシステムを使用して設定ファイルの読み込みと保存を行う実装クラス
 */
@Component
@Slf4j
public class FileSystemConfigLoader implements ConfigLoader {

    @Override
    public Properties loadConfig(Path configPath) throws ConfigLoadException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath.toString())) {
            props.load(fis);
            log.debug("設定ファイルを読み込みました: {}", configPath);
        } catch (IOException e) {
            throw new ConfigLoadException("設定ファイルの読み込みに失敗しました: " + e.getMessage(), e);
        }
        return props;
    }

    @Override
    public InputStream getTemplateStream(String templatePath) {
        InputStream templateStream = getClass().getResourceAsStream(templatePath);
        if (templateStream == null) {
            log.warn("テンプレートファイルが見つかりません: {}", templatePath);
        } else {
            log.debug("テンプレートファイルを読み込みました: {}", templatePath);
        }
        return templateStream;
    }

    @Override
    public void saveConfig(Path configPath, Properties properties) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(configPath.toString())) {
            properties.store(fos, "送信設定");
            log.debug("設定ファイルを保存しました: {}", configPath);
        } catch (IOException e) {
            log.error("設定ファイルの保存に失敗しました: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean configExists(Path configPath) {
        boolean exists = Files.exists(configPath);
        log.debug("設定ファイルの存在確認: {} - {}", configPath, exists ? "存在します" : "存在しません");
        return exists;
    }
}
