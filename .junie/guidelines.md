# Java OOPプロジェクト - ガイドライン

## プロンプトへの指示
日本語で回答してください。

## コーディング規約

### 1. オブジェクト指向設計原則
- **カプセル化**: 適切なgetterとsetterを持つプライベートフィールドの使用
- **継承**: 共通機能に継承を使用
- **ポリモーフィズム**: 柔軟な設計のためのインターフェースと抽象クラスの使用
- **抽象化**: 明確に定義されたインターフェースの背後に実装の詳細を隠す

### 2. 値オブジェクト
- ドメインコンセプト（例：`SoulName`、`Age`）には不変の値オブジェクトを使用
- コンストラクタまたはファクトリーメソッドで入力を検証
- 適切なequals/hashCode/toStringメソッドの実装（適宜Lombokを使用）

### 3. デザインパターン
- **ファクトリーパターン**: 複雑なオブジェクトの作成に使用（例：`TransmigratorFactory`、`PlayableStatusesFactory`）
- **ビルダーパターン**: オプションパラメータが多いオブジェクトに使用
- **ストラテジーパターン**: 交換可能なアルゴリズムにストラテジーインターフェースを使用
- **リポジトリパターン**: データアクセス抽象化にリポジトリを使用

### 4. コード構成
- 関連する機能をパッケージにグループ化
- 説明的なクラスとメソッド名を使用
- Javaの命名規則に従う（メソッド/変数にはキャメルケース、クラスにはパスカルケース）

### 5. ドキュメント
- すべての公開クラスとメソッドをJavadocでドキュメント化
- パラメータの説明と戻り値の説明を含める
- スローされる可能性のある例外をドキュメント化

## データベースガイドライン

### 1. MyBatis統合
- データベースアクセスにMyBatisを使用
- マッパーは`mapper`パッケージで定義
- ボイラープレートコードにはMyBatis Generatorを使用

### 2. エンティティクラス
- エンティティクラスは`entity`パッケージに保持
- 生成されたエンティティは`entity.generated`パッケージに配置
- 生成されたコードを直接変更しない

## ゲームメカニクス

### 1. 転生プロセス
1. 基本情報（名前、年齢）の収集
2. 世界の選択
3. じゃんけんで利用可能な種族レア度を決定
4. 種族の選択
5. 年齢、種族、ランダム要素に基づくキャラクターステータスの生成

### 2. 種族レア度システム
- **STANDARD（一般）**: デフォルトで利用可能
- **UNIQUE（レア）**: じゃんけん1回勝利で選択可能
- **LEGENDARY（スーパーレア）**: じゃんけん2回連続勝利で選択可能
- **SECRET（ウルトラレア）**: じゃんけん3回連続勝利で選択可能

## 開発ワークフロー

### 1. 新機能の追加
1. ドメインモデルと値オブジェクトを定義
2. ビジネスロジックを持つサービスクラスを実装
3. 必要に応じてファクトリークラスを作成
4. 必要に応じてデータベーススキーマとマッパーを更新
5. 適切なテストを追加

### 2. テスト
- すべてのビジネスロジックに対してユニットテストを作成
- テストにはJUnitを使用
- 適切な場合は依存関係をモック化

## ベストプラクティス

1. **不変性**: 可能な限り不変オブジェクトを優先
2. **検証**: 入力を早期に検証し、明確なエラーメッセージを提供
3. **関心の分離**: クラスを単一の責任に集中させる
4. **依存性注入**: 依存関係の管理にSpringのDIコンテナを使用
5. **例外処理**: 適切な例外タイプを使用し、適切なレベルで例外を処理

## 技術スタック
- Java 21
- Spring Boot 3.4.4
- MyBatis
- MySQL
- Lombok

# Spring Boot ベストプラクティス

このプロジェクトで適用するSpring Bootのベストプラクティスを以下に示します。

## 1. コンストラクタインジェクションの優先使用

フィールドやセッターインジェクションではなく、コンストラクタインジェクションを優先します。

```java
@Service
@RequiredArgsConstructor  // Lombokを使用してコンストラクタを自動生成
public class TransmigrationService {

    // finalフィールドで必須依存関係を宣言
    private final TransmigratorFactory transmigratorFactory;
    private final SelectWorldService selectWorldService;
    private final SelectRaceService selectRaceService;

    // 単一コンストラクタの場合、@Autowiredアノテーションは不要
}
```

- 必須の依存関係はすべて`final`フィールドとして宣言し、コンストラクタ経由で注入する
- Springは単一のコンストラクタを自動検出するため、`@Autowired`アノテーションは不要
- 本番コードではフィールド/セッターインジェクションを避ける

## 2. Springコンポーネントでのpackage-privateの優先使用

可能な限り、コントローラー、リクエスト処理メソッド、`@Configuration`クラス、`@Bean`メソッドをpackage-private（デフォルト）可視性で宣言します。

```java
@Service
@RequiredArgsConstructor
class TransmigrationService {  // publicではなくpackage-private

    void startTransmigrationProcess() {  // package-private
        // 実装
    }
}

@Repository  
class WorldRepository {  // package-private

    List<World> getAvailableWorlds() {  // package-private
        return List.of(
            new FantasyWorld(),
            new MagicTechWorld(),
            new CultivationWorld(),
            new FullDiveGameWorld()
        );
    }
}
```

## 3. 型付きプロパティでの設定管理

共通のプレフィックスを持つアプリケーション固有の設定プロパティは、`@ConfigurationProperties`クラスにまとめてバリデーションアノテーションと組み合わせて使用します。

```java
@ConfigurationProperties(prefix = "transmigration")
@Validated
public record TransmigrationProperties(

    @NotNull
    @Valid
    Database database,

    @NotNull
    @Valid  
    Game game
) {

    public record Database(
        @NotBlank
        String url,

        @NotBlank
        String username,

        @NotBlank
        String password
    ) {}

    public record Game(
        @Min(1)
        @Max(10)
        int maxRockPaperScissorsRounds,

        @NotNull
        @Size(min = 1)
        List<String> availableWorlds
    ) {}
}
```

```yaml
# application.yml
transmigration:
  database:
    url: jdbc:mysql://localhost:3306/transmigration
    username: user
    password: password
  game:
    max-rock-paper-scissors-rounds: 3
    available-worlds:
      - fantasy
      - magic-tech
      - cultivation
      - full-dive-game
```

- 異なる環境への設定プロパティの受け渡しには、プロファイルではなく環境変数を優先使用
- 設定が無効な場合はアプリケーションの早期失敗を実現

## 4. 明確なトランザクション境界の定義

各サービス層メソッドをトランザクション単位として定義します。

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // クラスレベルでreadOnlyを設定
public class TransmigrationService {

    private final TransmigratorFactory transmigratorFactory;

    // 読み取り専用メソッド（クラスレベルの設定を継承）
    public Transmigrator findTransmigratorById(SoulId soulId) {
        return transmigratorRepository.findById(soulId)
            .orElseThrow(() -> new TransmigratorNotFoundException(soulId));
    }

    // データ変更メソッドには明示的に@Transactionalを設定
    @Transactional
    public Transmigrator createTransmigrator(SoulName soulName, Age age, World world, Race race) {
        var transmigrator = transmigratorFactory.create(soulName, age, world, race);
        return transmigratorRepository.save(transmigrator);
    }
}
```

- 読み取り専用メソッドには`@Transactional(readOnly = true)`を使用
- データ変更メソッドには`@Transactional`を使用
- 各トランザクション内のコードを必要最小限のスコープに制限

## 5. Web層と永続化層の分離

コントローラーでエンティティを直接レスポンスとして公開しません。

```java
// ❌ 悪い例 - エンティティを直接公開
@RestController
class TransmigratorController {

    @GetMapping("/transmigrators/{id}")
    public Transmigrator getTransmigrator(@PathVariable String id) {
        return transmigratorService.findById(SoulId.of(id));
    }
}

// ✅ 良い例 - 専用のDTOレコードを使用
@RestController
class TransmigratorController {

    @GetMapping("/transmigrators/{id}")
    public TransmigratorResponse getTransmigrator(@PathVariable String id) {
        var transmigrator = transmigratorService.findById(SoulId.of(id));
        return TransmigratorResponse.from(transmigrator);
    }
}

// リクエスト/レスポンス用のDTOレコード
public record TransmigratorRequest(
    @NotBlank String name,
    @Min(1) @Max(120) int age,
    @NotBlank String worldId,
    @NotBlank String raceId
) {}

public record TransmigratorResponse(
    String soulId,
    String name,
    int age,
    String worldName,
    String raceName,
    PlayableStatusesResponse statuses
) {
    public static TransmigratorResponse from(Transmigrator transmigrator) {
        return new TransmigratorResponse(
            transmigrator.getSoulId().getId().toString(),
            transmigrator.getSoulName().getName(),
            transmigrator.getAge().getValue(),
            transmigrator.getWorld().getName(),
            transmigrator.getRace().getJapaneseName(),
            PlayableStatusesResponse.from(transmigrator.getPlayableStatuses())
        );
    }
}
```

- 明示的なリクエスト/レスポンスレコード（DTO）クラスを定義
- リクエストレコードにJakarta Validationアノテーションを適用して入力ルールを強制

## 6. REST API設計原則の遵守

```java
@RestController
@RequestMapping("/api/v1")  // バージョン管理されたリソース指向URL
class TransmigratorController {

    // コレクションリソース
    @GetMapping("/transmigrators")
    public ResponseEntity<PagedResponse<TransmigratorResponse>> getTransmigrators(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pagedResult = transmigratorService.findAll(page, size);
        return ResponseEntity.ok(PagedResponse.from(pagedResult));
    }

    // 個別リソース
    @GetMapping("/transmigrators/{id}")
    public ResponseEntity<TransmigratorResponse> getTransmigrator(@PathVariable String id) {
        var transmigrator = transmigratorService.findById(SoulId.of(id));
        return ResponseEntity.ok(TransmigratorResponse.from(transmigrator));
    }

    // サブリソース
    @GetMapping("/transmigrators/{id}/statuses")
    public ResponseEntity<PlayableStatusesResponse> getTransmigratorStatuses(@PathVariable String id) {
        var statuses = transmigratorService.getStatuses(SoulId.of(id));
        return ResponseEntity.ok(PlayableStatusesResponse.from(statuses));
    }

    // 作成
    @PostMapping("/transmigrators")
    public ResponseEntity<TransmigratorResponse> createTransmigrator(@Valid @RequestBody TransmigratorRequest request) {
        var transmigrator = transmigratorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(TransmigratorResponse.from(transmigrator));
    }
}
```

- **バージョン管理されたリソース指向URL**: エンドポイントを`/api/v{version}/resources`として構造化
- **コレクションとサブリソースの一貫したパターン**: URL規則を統一（例：投稿コレクションは`/posts`、特定投稿のコメントは`/posts/{slug}/comments`）
- **ResponseEntityでの明示的なHTTPステータスコード**: 正しいステータス（200 OK、201 Created、404 Not Foundなど）をレスポンスボディと共に返す
- 無制限のアイテム数を含む可能性があるコレクションリソースにはページネーションを使用
- 将来の拡張を可能にするため、JSONペイロードはトップレベルのデータ構造としてJSONオブジェクトを使用
- JSONプロパティ名にはsnake_caseまたはcamelCaseを一貫して使用

## 7. ビジネス操作でのコマンドオブジェクトの使用

入力データをラップする専用のコマンドレコードを作成します。

```java
// コマンドレコードの定義
public record CreateTransmigratorCommand(
    @NotBlank String soulName,
    @Min(1) @Max(120) int age,
    @NotBlank String worldId,
    @NotBlank String raceId
) {}

public record UpdateTransmigratorStatusCommand(
    @NotNull SoulId soulId,
    @Valid PlayableStatusesCommand statuses
) {}

// サービスメソッドでコマンドを受け取る
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransmigrationService {

    @Transactional
    public Transmigrator createTransmigrator(CreateTransmigratorCommand command) {
        var soulName = SoulName.of(command.soulName());
        var age = Age.of(command.age());
        var world = worldRepository.findById(command.worldId())
            .orElseThrow(() -> new WorldNotFoundException(command.worldId()));
        var race = raceMapper.selectByPrimaryKey(command.raceId())
            .orElseThrow(() -> new RaceNotFoundException(command.raceId()));

        return transmigratorFactory.create(soulName, age, world, race);
    }
}
```

## 8. 例外処理の一元化

`@RestControllerAdvice`を使用したグローバル例外ハンドラーで一貫性のあるエラーレスポンスを返します。

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TransmigratorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransmigratorNotFound(TransmigratorNotFoundException ex) {
        log.warn("Transmigrator not found: {}", ex.getMessage());

        var errorResponse = new ErrorResponse(
            "TRANSMIGRATOR_NOT_FOUND",
            ex.getMessage(),
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        var errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            Instant.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error occurred", ex);

        var errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "予期しないエラーが発生しました",
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

public record ErrorResponse(
    String code,
    String message,
    Instant timestamp
) {}
```

- `@ExceptionHandler`メソッドを使用して特定の例外を処理
- 一貫したエラーレスポンスを返す
- ProblemDetails レスポンスフォーマット（[RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)）の使用を検討

## 9. Actuatorエンドポイントの適切な公開

必要不可欠なactuatorエンドポイント（`/health`、`/info`、`/metrics`など）のみを認証なしで公開し、その他のエンドポイントは保護します。

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
  security:
    enabled: true
```

## 10. ResourceBundleでの国際化

ユーザー向けのテキスト（ラベル、プロンプト、メッセージなど）は、コードに埋め込まずResourceBundleで外部化します。

```java
@Component
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String getTransmigrationWelcomeMessage() {
        return getMessage("transmigration.welcome.message");
    }

    public String getWorldSelectionPrompt() {
        return getMessage("world.selection.prompt");
    }
}
```

```properties
# messages.properties
transmigration.welcome.message=異世界転生トランスミッションサービスへようこそ！
world.selection.prompt=転生先の世界を選択してください
race.selection.prompt=種族を選択してください

# messages_en.properties  
transmigration.welcome.message=Welcome to the Isekai Transmigration Service!
world.selection.prompt=Please select your destination world
race.selection.prompt=Please select your race
```

## 11. 統合テストでのTestcontainersの使用

本番環境を模倣するため、統合テストで実際のサービス（データベース、メッセージブローカーなど）を起動します。

```java
@SpringBootTest
@Testcontainers
class TransmigrationServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("transmigration_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void shouldCreateTransmigratorSuccessfully() {
        // テスト実装
    }
}
```

## 12. 統合テストでのランダムポートの使用

統合テスト作成時は、ポート競合を避けるためランダムな利用可能ポートでアプリケーションを起動します。

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransmigratorControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnTransmigratorById() {
        var url = "http://localhost:" + port + "/api/v1/transmigrators/123";
        var response = restTemplate.getForEntity(url, TransmigratorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## 13. ロギング

### 適切なロギングフレームワークの使用

アプリケーションログには`System.out.println()`を使用せず、SLF4J（または互換性のある抽象化）と選択したバックエンド（Logback、Log4j2など）を使用します。

```java
@Service
@RequiredArgsConstructor
@Slf4j  // Lombokのロギングアノテーション
public class TransmigrationService {

    public void startTransmigrationProcess() {
        log.info("転生プロセスを開始します");

        try {
            // ビジネスロジック
            log.debug("転生者の基本情報収集が完了しました");
        } catch (Exception e) {
            log.error("転生プロセス中にエラーが発生しました", e);
            throw e;
        }

        log.info("転生プロセスが正常に完了しました");
    }
}
```

### 機密データの保護

ログ出力に認証情報、個人情報、その他の機密情報が表示されないよう保証します。

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TransmigrationService {

    public Transmigrator createTransmigrator(CreateTransmigratorCommand command) {
        // ❌ 悪い例 - 機密情報がログに出力される可能性
        log.info("Creating transmigrator with command: {}", command);

        // ✅ 良い例 - 機密情報を除外
        log.info("Creating transmigrator for soul name: {} at age: {}", 
            command.soulName(), command.age());

        // 実装
    }
}
```

### 高コストなログ呼び出しの保護

`DEBUG`や`TRACE`レベルで詳細なメッセージを構築する際、特にメソッド呼び出しや複雑な文字列連結を含む場合は、レベルチェックやサプライヤーでラップします。

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayableStatusesFactory {

    public PlayableStatuses create(Age age, SoulId soulId, RaceStatusModifier raceStatusModifier) {

        // レベルチェックを使用した方法
        if (log.isDebugEnabled()) {
            log.debug("Creating statuses for age: {}, soul: {}, race modifier: {}", 
                age.getValue(), soulId.getId(), computeExpensiveModifierDetails(raceStatusModifier));
        }

        // ラムダ式/サプライヤーを使用した方法
        log.atDebug()
            .setMessage("Detailed status calculation: {}")
            .addArgument(() -> computeExpensiveStatusDetails(age, soulId, raceStatusModifier))
            .log();

        // 実装
    }

    private String computeExpensiveModifierDetails(RaceStatusModifier modifier) {
        // 高コストな計算
        return "expensive calculation result";
    }
}

## 追加実装方針

### 変数宣言でのvarの使用

右辺の式から型推論できる場合は`var`を使用することを推奨します。

```java
@Service
@RequiredArgsConstructor
public class TransmigrationService {

    public void startTransmigrationProcess() {
        // ✅ 良い例 - 型推論可能な場合はvarを使用
        var scanner = new Scanner(System.in);
        var soulName = collectSoulName(scanner);
        var age = collectAge(scanner);
        var selectedWorld = selectWorldService.selectWorld(scanner);
        var selectedRace = selectRaceService.selectRace(scanner);

        // ✅ 型推論できない場合は明示的な型宣言
        Optional<Race> raceOptional = raceMapper.selectByPrimaryKey(raceId);
        List<World> availableWorlds = worldRepository.getAvailableWorlds();

        var transmigrator = transmigratorFactory.create(soulName, age, selectedWorld, selectedRace);
        executeTransmigration(transmigrator);
    }

    private SoulName collectSoulName(Scanner scanner) {
        SoulName soulName = null;
        while (soulName == null) {
            try {
                var name = scanner.nextLine();  // 型推論可能
                soulName = SoulName.of(name);
            } catch (IllegalArgumentException e) {
                warn(e.getMessage());
            }
        }
        return soulName;
    }
}
```

**使用基準:**
- 右辺から型が明確に推論できる場合は`var`を使用
- 型推論できない場合や、明示的な型宣言が可読性を向上させる場合は従来の型宣言を使用
- コレクションの初期化やファクトリーメソッドの呼び出しでは`var`が特に効果的

### コードフォーマット

すべてのファイルの末尾に改行コードを含めることを徹底します。

```java
// ファイルの最後に必ず改行を含める
public class Example {
    // クラス内容
}
// ← この改行が必要
```

これにより、Gitでの差分表示やテキストエディタでの表示が適切に行われます。

# Java ユニットテスト生成ガイドライン

## 基本要件
- **フレームワーク**: JUnit 5 と Mockito を使用すること
- **Java バージョン**: Java 21
- **プロジェクト環境**: Lombok、Gradle を使用
- **Spring Boot バージョン**: 3.4.4

## テスト構造と命名規則

### クラス命名規則
- テストクラス名は `[テスト対象クラス名]Test` とすること
- 例: `UserService` のテストクラスは `UserServiceTest`

### メソッド命名規則と階層構造
- テスト対象メソッドごとに`@Nested`クラスを作成し、階層構造にすること
- 外側のクラス名は`[テスト対象メソッド名]`とすること
- 内側のテストメソッド名は`[テスト条件]_[期待される結果]`とすること

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    class FindById {

        @Test
        void userExists_returnsUser() {
            // テストコード
        }

        @Test
        void userNotFound_throwsException() {
            // テストコード
        }
    }

    @Nested
    class SaveUser {

        @Test
        void validUser_savesSuccessfully() {
            // テストコード
        }

        @Test
        void duplicateEmail_throwsException() {
            // テストコード
        }
    }
}
```

### Spring統合テストでのモックアノテーション
Spring Boot 3.4.0以降では、以下のアノテーションを使用すること：

#### 純粋なMockitoテスト（Spring Context不要）
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {

    @Mock  // 通常のMockitoの@Mockは継続使用可能
    private Repository repository;

    @Spy   // 通常のMockitoの@Spyも継続使用可能
    private EmailService emailService;

    @InjectMocks
    private Service service;
}
```

#### Spring統合テスト（Spring Context必要）
```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
class IntegrationTest {

    @MockitoBean  // Spring Boot 3.4.0以降の新しいアノテーション
    private UserRepository userRepository;

    @MockitoSpyBean  // Spring Boot 3.4.0以降の新しいアノテーション
    private EmailService emailService;

    @Autowired  // Spring統合テストでは@Autowiredを使用
    private UserService userService;
}
```

**重要：** Spring Boot 3.4.0では `@MockBean` と `@SpyBean` が非推奨となり、Spring Framework 6.2の `@MockitoBean` と `@MockitoSpyBean` が推奨されています。

## スタブとモックの定義と使い分け

### 定義

#### **スタブ（Stub）**
- **定義**: テスト対象が依存するオブジェクトの代替品で、**事前に決められた値を返すだけ**
- **目的**: テスト対象に必要な入力データを提供する
- **検証**: **戻り値のみ**を検証（呼び出されたかどうかは関心なし）

#### **モック（Mock）**
- **定義**: テスト対象が依存するオブジェクトの代替品で、**期待される相互作用を検証する**
- **目的**: テスト対象が依存オブジェクトと正しく相互作用しているかを検証
- **検証**: **メソッド呼び出し（回数、引数、順序など）**を検証

### 使い分けの基準

```java
// スタブとして使用（戻り値のみ関心）
@Test
void findById_userExists_returnsUser() {
    // Arrange - スタブの設定
    var userId = 1L;
    var expectedUser = createTestUser(userId, "TestUser");
    when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

    // Act
    var actualUser = userService.findById(userId);

    // Assert - 戻り値のみ検証（相互作用は検証しない）
    assertThat(actualUser.getName()).isEqualTo("TestUser");
}

// モックとして使用（相互作用も検証）
@Test
void createUser_validUser_savesAndSendsNotification() {
    // Arrange - スタブとモックの設定
    var newUser = createTestUser(null, "NewUser");
    var savedUser = createTestUser(1L, "NewUser");
    when(userRepository.save(any(User.class))).thenReturn(savedUser); // スタブ

    // Act
    userService.createUser(newUser);

    // Assert - 戻り値の検証
    assertThat(savedUser.getId()).isEqualTo(1L);

    // Assert - 相互作用の検証（モック）
    verify(userRepository).save(any(User.class));
    verify(notificationService).sendWelcomeNotification(savedUser.getEmail());
}
```

### Mockitoでの実装

**重要**: Mockitoの`@Mock`アノテーションは「テストダブル」を作成し、用途に応じてスタブまたはモックとして使用できます。

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock  // テストダブルを作成
    private UserRepository userRepository;

    @Mock  // テストダブルを作成
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;
}
```

## テスト記述ガイドライン

### 変数宣言の原則
- **基本方針**: 右辺の式から型推論できる場合は`var`を使用すること
- **例外**: 型推論できない場合は明示的な型宣言を使用

```java
@Test
void userExists_returnsUser() {
    // Arrange - varを使用（型推論可能）
    var userId = 1L;
    var expectedUser = createTestUser(userId, "TestUser");

    // 型推論できない場合は明示的型宣言
    Optional<User> userOptional = Optional.of(expectedUser);
    when(userRepository.findById(userId)).thenReturn(userOptional);

    // Act
    var actualUser = userService.findById(userId);

    // Assert
    assertThat(actualUser).isNotNull();
    assertThat(actualUser.getId()).isEqualTo(userId);
}
```

### テストケースの順序
- **基本方針**: 正常系のテストケースを先に全て記述し、その後に異常系のテストケースを記述すること
- **目的**: テストの可読性と理解しやすさを向上させる

```java
@Nested
class FindById {
    // 正常系のテストケース（先に記述）
    @Test
    void userExists_returnsUser() {
        // 正常系のテスト実装
    }

    @Test
    void userWithSpecialPermissions_returnsEnhancedUser() {
        // 正常系のテスト実装
    }

    // 異常系のテストケース（後に記述）
    @Test
    void userNotFound_throwsException() {
        // 異常系のテスト実装
    }

    @Test
    void invalidUserId_throwsException() {
        // 異常系のテスト実装
    }
}
```

### テストの基本構造 (AAA パターン)
各テストは以下の3つのセクションを明確に区分すること:
- **Arrange** (準備): テストデータとモックの設定
- **Act** (実行): テスト対象メソッドの呼び出し
- **Assert** (検証): 結果の検証

```java
@Nested
class FindUserById {

    @Test
    void userExists_returnsUser() {
        // Arrange
        var userId = 1L;
        var expectedUser = createTestUser(userId, "TestUser");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        var actualUser = userService.findById(userId);

        // Assert
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getId()).isEqualTo(userId);
        assertThat(actualUser.getName()).isEqualTo("TestUser");
    }
}
```

### スタブとモックの設定と検証

#### スタブの基本設定
```java
// 戻り値の設定（スタブ）
when(repository.findById(anyLong())).thenReturn(Optional.of(expectedEntity));

// void メソッドの場合（スタブ）
doNothing().when(emailService).sendEmail(anyString());

// 例外をスローする場合（スタブ）
when(repository.save(any())).thenThrow(new DataIntegrityViolationException("制約違反"));
```

#### モックの検証
```java
// メソッド呼び出しの検証（モック）
verify(repository, times(1)).save(any(User.class));
verify(emailService, never()).sendEmail(anyString());

// 引数の詳細検証（モック）
verify(repository).save(argThat(user -> 
    user.getName().equals("TestUser") && user.getAge() == 25));

// 呼び出し順序の検証（モック）
InOrder inOrder = inOrder(repository, emailService);
inOrder.verify(repository).save(any(User.class));
inOrder.verify(emailService).sendEmail(anyString());
```

#### スタブとモックの組み合わせ使用
```java
@Test
void processUser_validUser_savesAndNotifies() {
    // Arrange
    var user = createTestUser(null, "TestUser");
    var savedUser = createTestUser(1L, "TestUser");

    // スタブの設定（戻り値を提供）
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(emailValidator.isValid(anyString())).thenReturn(true);

    // Act
    userService.processUser(user);

    // Assert - 戻り値の検証
    // （この例では戻り値がvoidなので省略）

    // Assert - 相互作用の検証（モック）
    verify(userRepository).save(any(User.class));
    verify(notificationService).sendWelcomeEmail(savedUser.getEmail());
    verify(auditService).logUserCreation(savedUser.getId());
}
```

### 例外テスト
```java
@Nested
class FindById {

    @Test
    void userNotFound_throwsException() {
        // Arrange
        var nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(
            UserNotFoundException.class, 
            () -> userService.findById(nonExistentId)
        );

        assertThat(exception.getMessage()).contains("User not found with id: " + nonExistentId);
    }
}
```

### パラメータ化テスト
```java
@Nested
class ValidateUserId {

    @ParameterizedTest
    @CsvSource({
        "1, true, 'Valid user'",
        "0, false, 'Invalid ID'",
        "-1, false, 'Negative ID'"
    })
    void withDifferentInputs_returnsExpectedResult(
            long userId, boolean expectedValid, String description) {
        // Act
        var result = userService.isValidUserId(userId);

        // Assert
        assertThat(result).as(description).isEqualTo(expectedValid);
    }
}
```

## 高度なテストダブル技術

### 1. 引数キャプチャ（モック機能）
```java
@Nested
class SaveUser {

    @Test
    void validUser_savesWithCorrectData() {
        // Arrange
        var inputUser = createTestUser(null, "NewUser");
        var userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        userService.createUser(inputUser);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        var capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getName()).isEqualTo("NewUser");
        assertThat(capturedUser.getCreatedDate()).isNotNull();
    }
}
```

### 2. スタブの連続呼び出し
```java
@Test
void failsThenSucceeds_eventuallySaves() {
    // Arrange - 連続したスタブの設定
    var user = createTestUser(1L, "TestUser");
    when(repository.save(any(User.class)))
        .thenThrow(new DataAccessException("一時的エラー"))
        .thenThrow(new DataAccessException("再度エラー"))
        .thenReturn(user);

    // Act
    var result = userService.saveWithRetry(user);

    // Assert - 戻り値の検証
    assertThat(result).isNotNull();

    // Assert - 相互作用の検証（モック）
    verify(repository, times(3)).save(any(User.class));
}
```

### 3. 部分モック（Spy）の使用
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Spy
    private UserService userServiceSpy;

    @Nested
    class ComplexMethod {

        @Test
        void callsInternalMethod_verifyInteraction() {
            // Arrange
            doReturn(true).when(userServiceSpy).validateUser(any());

            // Act
            userServiceSpy.processUser(createTestUser());

            // Assert
            verify(userServiceSpy).validateUser(any());
        }
    }
}
```

## テストデータとヘルパーメソッド

### テストデータファクトリ
```java
class UserTestDataFactory {

    public static User createTestUser() {
        return createTestUser(1L, "DefaultUser");
    }

    public static User createTestUser(Long id, String name) {
        return User.builder()
            .id(id)
            .name(name)
            .email(name.toLowerCase() + "@example.com")
            .age(25)
            .createdDate(LocalDateTime.now())
            .build();
    }

    public static List<User> createUserList(int count) {
        return IntStream.range(1, count + 1)
            .mapToObj(i -> createTestUser((long) i, "User" + i))
            .collect(Collectors.toList());
    }
}
```

## アサーション推奨事項

### AssertJの使用を推奨
```java
// 基本的なアサーション
assertThat(actualUser).isNotNull();
assertThat(actualUser.getName()).isEqualTo("ExpectedName");

// コレクションのアサーション
assertThat(userList)
    .hasSize(3)
    .extracting(User::getName)
    .containsExactly("User1", "User2", "User3");

// 例外のアサーション
assertThatThrownBy(() -> userService.findById(-1L))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("Invalid ID");

// 条件付きアサーション
assertThat(user).satisfies(u -> {
    assertThat(u.getName()).isNotBlank();
    assertThat(u.getAge()).isBetween(0, 150);
    assertThat(u.getEmail()).contains("@");
});
```

## 実装例：完全なテストクラス

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Nested
    class FindById {

        @Test
        void userExists_returnsUser() {
            // Arrange
            var userId = 1L;
            var expectedUser = UserTestDataFactory.createTestUser(userId, "TestUser");
            when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

            // Act
            var actualUser = userService.findById(userId);

            // Assert
            assertThat(actualUser).isNotNull();
            assertThat(actualUser.getId()).isEqualTo(userId);
            assertThat(actualUser.getName()).isEqualTo("TestUser");
        }

        @Test
        void userNotFound_throwsException() {
            // Arrange
            var nonExistentId = 999L;
            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            var exception = assertThrows(
                UserNotFoundException.class, 
                () -> userService.findById(nonExistentId)
            );

            assertThat(exception.getMessage()).contains("User not found with id: " + nonExistentId);
        }
    }

    @Nested
    class CreateUser {

        @Test
        void validUser_savesAndSendsEmail() {
            // Arrange
            var newUser = UserTestDataFactory.createTestUser(null, "NewUser");
            var savedUser = UserTestDataFactory.createTestUser(1L, "NewUser");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            var result = userService.createUser(newUser);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);

            verify(userRepository).save(any(User.class));
            verify(emailService).sendWelcomeEmail(savedUser.getEmail());
        }

        @Test
        void duplicateEmail_throwsException() {
            // Arrange
            var duplicateUser = UserTestDataFactory.createTestUser(null, "Duplicate");
            when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Email already exists"));

            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(duplicateUser))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("Email already exists");

            verify(emailService, never()).sendWelcomeEmail(anyString());
        }
    }
}
```

## テストカバレッジの要件

- 各クラスの主要なパブリックメソッドには必ずテストを作成すること
- プライベートメソッドは直接テストする必要はない（パブリックメソッドを通じて間接的にテストされる）
- 各メソッドの正常系と異常系の両方をテストすること
- 境界値のテストを含めること
- 分岐条件（if文、switch文）のすべてのパスをカバーすること
- エッジケースと例外ケースを網羅すること
- 複雑なパブリックメソッド（例：`startTransmigrationProcess`）については、すべての異常系シナリオを含む網羅的なテストケースを作成し、完全なカバレッジを確保すること

## テストファイルの配置

### 基本原則
- テストクラスは、テスト対象のクラスと同じパッケージ構造に配置する
- 例: `src/main/java/com/kos0514/oop_in_java_learn/service/TransmigrationService.java` のテストは 
  `src/test/java/com/kos0514/oop_in_java_learn/service/TransmigrationServiceTest.java` に配置する

### 命名規則
- テストクラス名は `[テスト対象クラス名]Test` とする
- 例: `TransmigrationService` のテストクラスは `TransmigrationServiceTest`

### パッケージ宣言
- テストクラスのパッケージ宣言は、ファイルの配置場所と一致させる
- 例: `src/test/java/com/kos0514/oop_in_java_learn/service/` に配置されたファイルは
  `package com.kos0514.oop_in_java_learn.service;` と宣言する

## ベストプラクティス

### 1. テストの独立性
- 各テストは他のテストに依存せず、独立して実行できること
- テスト間で状態を共有しないこと

### 2. テストの可読性
- `@Nested`クラスを使用してテストを論理的にグループ化
- テストメソッド名で何をテストしているかが明確に分かること
- Given-When-Then構造またはAAA構造を明確にすること

### 3. テストの保守性
- 重複コードをヘルパーメソッドに抽出
- テストデータの作成を標準化（ファクトリパターン使用）
- モックの設定を再利用可能にする

### 4. パフォーマンスの考慮
- `@MockitoSettings(strictness = Strictness.STRICT_STUBS)`を使用して未使用スタブを検出
- 重いセットアップは`@BeforeAll`で一度だけ実行
- 必要最小限のテストダブルのみ作成

## トラブルシューティング

### よくある問題と解決策

1. **モックが正しく注入されない**
    - `@ExtendWith(MockitoExtension.class)`が設定されているか確認
    - `@InjectMocks`と`@Mock`の組み合わせが正しいか確認

2. **Spring Boot 3.4.0での非推奨警告**
    - Spring統合テストでは`@MockitoBean`と`@MockitoSpyBean`を使用
    - 純粋なMockitoテストでは従来の`@Mock`と`@Spy`を継続使用

3. **UnfinishedStubbingException**
    - `when()`メソッドチェーンが完了しているか確認
    - 静的メソッドのスタブ化にはMockito Staticが必要

4. **テストが不安定（Flaky Test）**
    - 時間に依存するテストはClockをスタブ化
    - 並行処理のテストは適切な同期機構を使用

5. **スタブとモックの混同**
    - 戻り値のみ必要な場合はスタブとして使用
    - 相互作用の検証が必要な場合はモックとして使用
    - 一つのテストダブルで両方の用途を組み合わせることも可能
