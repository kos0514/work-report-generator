# 作業報告書管理システム仕様書（Gradle版）

## 1. プロジェクト概要

### 1.1 目的
- Spring Boot + Spring Shell による CLI ベース作業報告書管理システム
- CSVファイルによる簡単な読み書き機能
- シンプルなコマンドによるファイル作成・更新

### 1.2 対象環境
- **Javaバージョン**: Java 21
- **フレームワーク**: Spring Boot 3.x + Spring Shell
- **ビルドツール**: Gradle
- **実行方式**: Spring Boot起動 → CLI操作
- **対象ファイル形式**: .xls（Excel 97-2003形式）

## 2. システム構成

### 2.1 プロジェクト構成
```

├── src/main/java/com/kos0514/work_report_generator/
│   ├── WorkReportGeneratorApplication.java  # Spring Boot メインクラス
│   ├── command/
│   │   └── WorkReportCommands.java         # CLI コマンド定義
│   ├── service/
│   │   ├── ReportService.java              # 報告書操作サービス
│   │   ├── ExcelService.java               # Excel読み書きサービス
│   │   ├── CsvService.java                 # CSV読み書きサービス
│   │   └── HolidayService.java             # 祝日判定サービス
│   ├── config/
│   │   └── AppConfig.java                  # アプリケーション設定
│   ├── model/
│   │   ├── WorkRecord.java                 # 作業記録モデル
│   │   ├── Holiday.java                    # 祝日モデル
│   │   └── ReportConfig.java               # 報告書設定
│   └── util/
│       └── DateUtil.java                   # 日付ユーティリティ
├── src/main/resources/
│   ├── application.yml                     # Spring Boot設定
│   └── config/
│       └── syukujitsu.csv                  # 祝日データ（内閣府提供）
├── local-data/                            # Git管理外
│   ├── templates/
│   │   └── 作業報告書 I社フォーマット.xls
│   ├── output/                            # 生成ファイル
│   └── csv/                               # CSV入力ファイル
├── .gitignore                             # local-data/ を除外
├── build.gradle
├── settings.gradle
└── README.md
```

## 3. CLI コマンド仕様

### 3.1 アプリケーション起動
```bash
# Spring Boot アプリケーション起動
./gradlew bootRun

# 起動後、CLIプロンプトが表示される
shell:>
```

### 3.2 基本コマンド

#### 3.2.1 初期ファイル作成
```bash
shell:> create-file --month 2025/06 --user "田中太郎" --client "株式会社サンプル"
```
**処理内容**:
- テンプレートファイルをコピー
- ファイル名: `田中太郎_202506_作業報告書.xls`
- 基本情報（B7: 対象月、C4: クライアント、L4: ユーザー名）を設定
- 勤務時間は空白のまま（CSV更新で入力）

#### 3.2.2 CSV更新
```bash
shell:> update-file --file "田中太郎_202506_作業報告書.xls" --csv "work_data.csv"
```
**処理内容**:
- 指定されたExcelファイルをCSVデータで更新
- 日付をキーとして該当行を特定・更新（開始時刻、終了時刻、休憩時間、作業内容）

#### 3.2.3 ヘルプ表示
```bash
shell:> help
```

## 4. 設定ファイル

### 4.1 Spring Boot設定（application.yml）
```yaml
spring:
  application:
    name: work-report-system
  shell:
    interactive:
      enabled: true
    script:
      enabled: false

work-report:
  template-file: ./local-data/templates/作業報告書 I社フォーマット.xls
  output-dir: ./local-data/output
  csv-dir: ./local-data/csv
  holidays-file: classpath:config/syukujitsu.csv

logging:
  level:
    com.workreport: INFO
```

### 4.2 祝日データ（syukujitsu.csv）
内閣府提供の祝日CSVファイルをそのまま使用：
```csv
国民の祝日・休日月日,国民の祝日・休日名称
1955/1/1,元日
1955/1/15,成人の日
2025/1/1,元日
2025/1/13,成人の日
2025/2/11,建国記念の日
2025/2/23,天皇誕生日
...
```
**エンコーディング**: Shift_JIS

### 4.3 Spring Boot設定（application.yml）

## 5. CSV仕様

### 5.1 CSV入力フォーマット
```csv
日付,開始時刻,終了時刻,休憩時間,作業内容
2025/06/02,09:30,17:45,1:00,システム設計書作成
2025/06/03,10:00,18:30,1:00,プログラム実装
2025/06/04,09:00,17:00,1:00,テスト実行
```

## 6. 実装クラス

### 6.1 WorkReportCommands.java
```java
@Component
@ShellComponent
public class WorkReportCommands {

    @Autowired
    private ReportService reportService;

    @ShellMethod(value = "新規報告書ファイルを作成", key = "create-file")
    public String createFile(
        @ShellOption("--month") String month,      // 2025/06 形式
        @ShellOption("--user") String user,       // ユーザー名
        @ShellOption("--client") String client    // クライアント名
    ) {
        try {
            String fileName = reportService.createReport(month, user, client);
            return "ファイル作成完了: " + fileName;
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }

    @ShellMethod(value = "CSVファイルで報告書を更新", key = "update-file")
    public String updateFile(
        @ShellOption("--file") String fileName,   // 更新対象ファイル
        @ShellOption("--csv") String csvFile      // CSVファイル名
    ) {
        try {
            int updatedRows = reportService.updateFromCsv(fileName, csvFile);
            return "更新完了: " + updatedRows + " 件";
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }

    @ShellMethod(value = "コマンド一覧とヘルプを表示", key = "help")
    public String showHelp() {
        return reportService.getHelpMessage();
    }
}
```

### 6.2 ReportService.java
```java
@Service
public class ReportService {
    
    // I社フォーマット用定数（ハードコード）
    private static final String TARGET_MONTH_CELL = "B7";
    private static final String CLIENT_NAME_CELL = "C4";
    private static final String USER_NAME_CELL = "L4";
    private static final int WORK_START_ROW = 8;
    private static final int WORK_END_ROW = 38;
    private static final String DATE_COLUMN = "B";
    private static final String START_TIME_COLUMN = "D";
    private static final String END_TIME_COLUMN = "E";
    private static final String BREAK_TIME_COLUMN = "F";
    private static final String WORK_CONTENT_COLUMN = "G";

    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private CsvService csvService;
    
    @Autowired
    private HolidayService holidayService;

    @Value("${work-report.template-file}")
    private String templateFile;

    @Value("${work-report.output-dir}")
    private String outputDir;

    // 新規報告書作成（ファイル名: user_yyyymm_作業報告書.xls）
    public String createReport(String month, String user, String client) {
        // 1. テンプレートファイルをコピー
        // 2. ファイル名設定 (user_202506_作業報告書.xls)
        // 3. 基本項目設定 (B7, C4, L4)
        // 4. 勤務時間は空白のまま
        // 5. ファイル保存
    }

    // CSV からの更新（日付,開始時刻,終了時刻,休憩時間,作業内容）
    public int updateFromCsv(String fileName, String csvFile) {
        // 1. CSVファイル読み込み
        // 2. Excelファイル読み込み
        // 3. 日付をキーとして該当行特定
        // 4. 開始時刻・終了時刻・休憩時間・作業内容更新
        // 5. ファイル保存
    }

    // ヘルプメッセージ表示
    public String getHelpMessage() {
        // 使用方法とCSVフォーマット例を返す
    }
}
```

### 6.3 HolidayService.java
```java
@Service
public class HolidayService {
    
    @Value("${work-report.holidays-file}")
    private String holidaysFile;
    
    private List<Holiday> holidays;
    
    @PostConstruct
    public void loadHolidays() {
        // syukujitsu.csv を Shift_JIS エンコーディングで読み込み
        // 1955年〜の全祝日データを読み込み
    }
    
    // 指定日が祝日かチェック
    public boolean isHoliday(LocalDate date) {
        return holidays.stream()
            .anyMatch(h -> h.getDate().equals(date));
    }
    
    // 指定日が平日かチェック（土日祝除く）
    public boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return !isHoliday(date) && 
               dayOfWeek != DayOfWeek.SATURDAY && 
               dayOfWeek != DayOfWeek.SUNDAY;
    }
}
```

### 6.4 Holiday.java
```java
@Data
@AllArgsConstructor
public class Holiday {
    private LocalDate date;        // 祝日月日
    private String name;           // 祝日名称
}
```

### 6.5 ExcelService.java
```java
@Service
public class ExcelService {
    
    // Excelファイル読み込み
    public HSSFWorkbook loadWorkbook(String filePath)
    
    // セルに値設定
    public void setCellValue(HSSFSheet sheet, String cellPosition, String value)
    
    // 日付から行番号特定
    public int findRowByDate(HSSFSheet sheet, LocalDate date)
    
    // ワークブック保存
    public void saveWorkbook(HSSFWorkbook workbook, String filePath)
}
```

### 6.6 CsvService.java
```java
@Service
public class CsvService {
    
    // CSVファイル読み込み（日付,開始時刻,終了時刻,休憩時間,作業内容）
    public List<WorkRecord> readCsv(String csvFilePath)
    
    // 祝日CSVファイル読み込み（Shift_JIS対応）
    public List<Holiday> readHolidayCsv(String csvFilePath)
}
```

### 6.7 WorkRecord.java
```java
@Data
@AllArgsConstructor
public class WorkRecord {
    private LocalDate date;        // 日付
    private String startTime;      // 開始時刻
    private String endTime;        // 終了時刻
    private String breakTime;      // 休憩時間
    private String workContent;    // 作業内容
}
```

## 7. Gradle設定（build.gradle）

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.1.2'
    id 'io.spring.dependency-management' version '1.1.2'
}

group = 'com.workreport'
version = '1.0.0'
sourceCompatibility = '21'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter
    implementation 'org.springframework.boot:spring-boot-starter'
    
    // Spring Shell
    implementation 'org.springframework.shell:spring-shell-starter:3.1.2'
    
    // Apache POI for Excel
    implementation 'org.apache.poi:poi:5.2.4'
    implementation 'org.apache.poi:poi-scratchpad:5.2.4'
    
    // CSV処理
    implementation 'com.opencsv:opencsv:5.8'
    
    // JSON処理
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

// 起動タスク設定
bootRun {
    standardInput = System.in
}
```

## 8. 使用フロー

### 8.1 月次報告書作成フロー
```bash
# 1. アプリケーション起動
./gradlew bootRun

# 2. 新規ファイル作成
shell:> create-file --month 2025/06 --user "田中太郎" --client "株式会社サンプル"
ファイル作成完了: 田中太郎_202506_作業報告書.xls

# 3. CSVファイルを作成・編集（外部エディタで）
# フォーマット: 日付,開始時刻,終了時刻,休憩時間,作業内容

# 4. CSV更新
shell:> update-file --file "田中太郎_202506_作業報告書.xls" --csv "work_data.csv"
更新完了: 15 件

# 5. 完了！ファイルは local-data/output/ に保存済み
```

## 9. エラーハンドリング

### 9.1 想定エラーと対応
- **テンプレートファイルなし**: 適切なエラーメッセージ表示
- **CSV形式エラー**: 行番号付きエラー表示
- **日付不一致**: 該当しない日付をスキップ・ログ出力
- **ファイル権限エラー**: 権限問題の解決方法提示
- **祝日データ読み込みエラー**: Shift_JISエンコーディング問題対応

### 9.2 ログ出力例
```
2025/06/09 14:30:15 [INFO] 祝日データ読み込み完了: 1050件
2025/06/09 14:30:15 [INFO] ファイル作成開始: 田中太郎_202506_作業報告書.xls
2025/06/09 14:30:16 [INFO] 基本項目設定完了（勤務時間は空白）
2025/06/09 14:30:16 [INFO] ファイル作成完了
2025/06/09 14:35:20 [INFO] CSV更新開始: work_data.csv
2025/06/09 14:35:21 [WARN] 該当日なし: 2025/06/15 (土曜日)
2025/06/09 14:35:21 [INFO] CSV更新完了: 15件更新
```

## 10. 特徴・利点

### 10.1 シンプルな操作
- Gradleによる簡単ビルド・実行
- **3つのコマンドだけ**: `create-file`, `update-file`, `help`
- ファイル名形式: user_yyyymm_作業報告書.xls

### 10.2 祝日対応
- 内閣府提供の公式祝日データ使用
- 1955年〜現在までの全祝日に対応
- Shift_JISエンコーディング対応

### 10.3 ファイル管理
- テンプレートファイルはローカル管理
- 生成ファイルもローカル保存
- Git管理対象外で機密性確保