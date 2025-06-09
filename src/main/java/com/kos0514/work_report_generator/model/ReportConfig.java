package com.kos0514.work_report_generator.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 作業報告書の設定を管理する型付きプロパティクラス
 */
@Data
@Component
@ConfigurationProperties(prefix = "work-report")
@Validated
public class ReportConfig {

    /**
     * テンプレートファイルのパス
     */
    @NotBlank(message = "テンプレートファイルのパスは必須です")
    private String templateFile;

    /**
     * 出力ディレクトリのパス
     */
    @NotBlank(message = "出力ディレクトリのパスは必須です")
    private String outputDir;

    /**
     * CSVファイルのディレクトリパス
     */
    @NotBlank(message = "CSVディレクトリのパスは必須です")
    private String csvDir;

    /**
     * 祝日データファイルのパス
     */
    @NotBlank(message = "祝日データファイルのパスは必須です")
    private String holidaysFile;
}
