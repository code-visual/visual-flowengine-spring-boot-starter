# Visual FlowEngine — 配置与集成指南

> 最后更新：2026-03-24

---

## 1. Maven 依赖引入

在目标项目的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>io.github.code-visual</groupId>
    <artifactId>visual-flowengine-spring-boot-starter</artifactId>
    <version>1.2.6</version>
</dependency>
```

**最低要求：**
- Java 11+
- Spring Boot 2.x 或 3.x（均支持）
- Web 环境（`spring-boot-starter-web` 已引入）

---

## 2. 配置属性

所有配置前缀为 `visual.flow`，对应类 `VisualFlowProperties`（`src/main/java/io/github/code/visual/config/VisualFlowProperties.java`）。

### 2.1 基础开关

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `visual.flow.base-path` | `String` | `/visualflow` | 所有内置 API 和 UI 的路径前缀 |
| `visual.flow.enable-api` | `boolean` | `true` | 是否启用内置 REST API（`WorkflowController`）|
| `visual.flow.enable-ui` | `boolean` | `true` | 是否启用可视化 Web UI（`VisualFlowIndexController`）|
| `visual.flow.enable-ast` | `boolean` | `true` | 是否启用 Groovy AST 安全检查 |
| `visual.flow.enable-cache-source` | `boolean` | `true` | 是否缓存已编译的 Groovy 脚本类 |
| `visual.flow.script-timeout-seconds` | `int` | `30` | 单节点脚本执行超时（秒），0 表示不超时 |

### 2.2 API 路径覆盖（可选）

以下属性均可单独覆盖，不设置时自动从 `base-path` 推导：

| 属性 | 默认值（推导规则）|
|------|----------------|
| `visual.flow.workflows-api-path` | `{basePath}/api/workflows` |
| `visual.flow.execute-api-path` | `{basePath}/api/workflows/execute` |
| `visual.flow.debug-api-path` | `{basePath}/api/workflows/debug` |
| `visual.flow.debug-stream-api-path` | `{basePath}/api/workflows/debug/stream` |
| `visual.flow.compile-api-path` | `{basePath}/api/script/compile` |

> **注意：** `config-api-path`（`{basePath}/api/config`）不支持覆盖，始终由 `basePath` 推导。

---

## 3. 最简配置示例

`application.yml`（无任何自定义，全用默认值）：

```yaml
# 无需任何配置，开箱即用
# 访问 http://localhost:8080/visualflow/index.html 打开 Web UI
```

---

## 4. 常用配置场景

### 4.1 修改访问路径前缀

```yaml
visual:
  flow:
    base-path: /flow-designer
```

访问 UI：`http://localhost:8080/flow-designer/index.html`

---

### 4.2 带 context-path 的 Spring Boot 应用

```yaml
server:
  servlet:
    context-path: /myapp

visual:
  flow:
    base-path: /flow
```

访问 UI：`http://localhost:8080/myapp/flow/index.html`

`VisualFlowIndexController` 会自动检测 context-path 并修正静态资源路径及注入的 `window.__VISUAL_FLOW_CONFIG__`。

---

### 4.3 关闭 Web UI，仅保留 API

```yaml
visual:
  flow:
    enable-ui: false
```

---

### 4.4 关闭内置 API（仅用 Java API）

```yaml
visual:
  flow:
    enable-api: false
```

---

### 4.5 关闭 Groovy 脚本缓存（开发调试时）

```yaml
visual:
  flow:
    enable-cache-source: false
```

缓存关闭时，每次执行都重新编译脚本（性能略低，但可以在不重启的情况下更新脚本）。

---

### 4.6 调整脚本超时

```yaml
visual:
  flow:
    script-timeout-seconds: 10   # 10 秒超时
    # script-timeout-seconds: 0  # 不超时
```

---

## 5. 自定义持久化存储

默认 `InMemoryWorkflowRepository` 重启后数据丢失，生产环境需替换：

```java
@Configuration
public class MyFlowConfig {

    @Bean
    public WorkflowRepository workflowRepository(WorkflowDao dao) {
        return new WorkflowRepository() {
            @Override
            public void save(WorkflowMetadata metadata) {
                dao.save(metadata); // 序列化 metadata 存 DB
            }

            @Override
            public void deleteById(Integer workflowId) {
                dao.deleteById(workflowId);
            }

            @Override
            public WorkflowMetadata findById(Integer workflowId) {
                return dao.findById(workflowId);
            }

            @Override
            public WorkflowMetadata findByName(String name) {
                return dao.findByName(name);
            }

            @Override
            public List<WorkflowIdAndName> findAll() {
                return dao.findAllIdAndName();
            }
        };
    }
}
```

> **提示：** `WorkflowMetadata` 实现了 `Serializable`，`scriptMetadata` 字段为树形嵌套 JSON，推荐存为 JSON 字符串（`TEXT`/`JSON` 类型字段），用 Jackson 序列化/反序列化。

---

## 6. 注册执行日志监听器

```java
@Component
public class ExecutionAuditListener implements WorkflowExecutionListener {

    @Autowired
    private ExecutionLogRepository logRepo;

    @Override
    public void onExecutionComplete(Integer workflowId, Integer revision,
                                    Map<Integer, List<WorkflowTaskLog>> logs) {
        // 仅在 execute() 方法触发，debug() 不触发
        ExecutionRecord record = new ExecutionRecord();
        record.setWorkflowId(workflowId);
        record.setRevision(revision);
        record.setExecutedAt(new Date());
        record.setLogs(toJson(logs));
        logRepo.save(record);
    }
}
```

---

## 7. 自定义 Groovy 安全策略

覆盖 `SecureASTCustomizer` Bean（`@ConditionalOnMissingBean`）：

```java
@Bean
public SecureASTCustomizer secureASTCustomizer() {
    SecureASTCustomizer secure = new SecureASTCustomizer();
    secure.setClosuresAllowed(true);
    // 在默认黑名单基础上增加限制
    secure.setDisallowedTokens(Arrays.asList(
        Types.KEYWORD_WHILE,
        Types.KEYWORD_GOTO,
        Types.KEYWORD_FOR      // 也禁止 for 循环
    ));
    return secure;
}
```

或完全替换 `CompilerConfiguration`：

```java
@Bean
public CompilerConfiguration compilerConfiguration(SecureASTCustomizer secure) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.addCompilationCustomizers(secure);
    config.setSourceEncoding("UTF-8");
    config.setTargetBytecode(CompilerConfiguration.JDK11);
    return config;
}
```

---

## 8. Spring Boot 自动装配机制

### SB3（Spring Boot 3.x）
通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册：
```
io.github.code.visual.config.VisualFlowEngineAutoConfiguration
```

### SB2（Spring Boot 2.x）
同时支持 SB2 的 `spring.factories`（由 Spring Boot 的向后兼容机制处理）。

### 自动装配条件
- `@ConditionalOnWebApplication`：仅 Web 应用才生效
- `@ConditionalOnMissingBean`：所有核心 Bean 均可被用户覆盖

### 注册的核心 Bean

| Bean 类型 | 条件 | 说明 |
|-----------|------|------|
| `WebMvcConfigurer` | `enable-ui=true` | 静态资源路径映射 |
| `SecureASTCustomizer` | 无自定义 Bean 时 | 默认安全配置 |
| `CompilerConfiguration` | 无自定义 Bean 时 | Groovy 编译器配置 |
| `WorkflowRepository` | 无自定义 Bean 时 | 内存存储 |
| `WorkflowEngine` | 无自定义 Bean 时 | 核心引擎 |
| `WorkflowController` | `enable-api=true` | REST API |
| `VisualFlowIndexController` | `enable-ui=true` | 前端入口 |
| `SpringContext` | 通过 `@ComponentScan` | 静态 ApplicationContext 访问 |

---

## 9. 典型集成示例（完整）

```java
// 1. 引入依赖（pom.xml）
// 2. 无需任何 @EnableXxx 注解
// 3. 直接注入使用

@RestController
@RequestMapping("/my-business")
public class MyBusinessController {

    @Autowired
    private WorkflowEngine workflowEngine;

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody ProcessRequest req) {
        Map<String, Object> inputVars = new HashMap<>();
        inputVars.put("orderId", req.getOrderId());
        inputVars.put("amount", req.getAmount());
        inputVars.put("userId", req.getUserId());

        try {
            Map<Integer, List<WorkflowTaskLog>> logs =
                workflowEngine.execute("订单处理流程", inputVars);

            // 从执行结果中提取最终变量值
            // 找到 End 节点的 afterRunBinding
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

---

## 10. 注意事项

1. **不要在 Condition 节点中修改 Binding**：AST 检查会在编译期阻止 `binding.setVariable()` 调用。
2. **Condition 节点必须返回 Boolean**：最后一行表达式必须是 `true`/`false`，否则运行时报错。
3. **所有叶节点必须是 End 类型**：`validateStructure` 在每次执行前校验此规则（仅对 `execute` 方法，`debug` 方法也会校验当 root 为 Start 时）。
4. **生产环境替换 `InMemoryWorkflowRepository`**：内存实现重启丢失所有流程定义。
5. **`while` 和 `goto` 被禁止**：防止无限循环。需要循环时使用 Groovy 的 `for`/`each`。
6. **禁止使用 `System`、`Runtime`、`Class`**：安全沙箱在 AST 级别阻止这些类的使用。
