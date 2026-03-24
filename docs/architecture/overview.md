# Visual FlowEngine Spring Boot Starter — 架构概览

> 最后更新：2026-03-24
> HEAD Commit：`d36a56670c549c228b653ab8fd3dfb24b9c775af`

---

## 1. 项目简介

`visual-flowengine-spring-boot-starter` 是一个可嵌入任何 Spring Boot 项目的**可视化工作流引擎**。用户在浏览器中通过拖拽节点构建流程图，流程图保存为树型 JSON 结构，后端引擎按树节点顺序执行 Groovy 脚本，并实时返回每个节点的执行日志。

核心能力：
- 零侵入嵌入（引入一个 jar 即可）
- 可视化 Web UI（前端 React 打包静态资源内嵌 jar）
- Groovy 脚本动态执行（支持热编译、缓存）
- 条件分支（if/else-if 风格 Condition 节点）
- 规则引擎（Rule DSL）
- 断点调试 + SSE 实时推送

---

## 2. 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| Java 编译目标 | Java 11 | `maven.compiler.source=11` |
| Spring Boot 自动装配 | spring-boot-autoconfigure | 2.6.3（provided）|
| Spring WebMVC | spring-webmvc | 5.3.15（provided）|
| Groovy 运行时 | groovy-all (pom) | 4.0.24 |
| JSON 序列化 | jackson-databind | 2.13.5（provided）|
| 日志 | slf4j-api | 1.7.36（provided）|
| Maven 构建 | GMavenPlus Plugin | 3.0.2（混编 Java+Groovy）|
| 发布仓库 | Sonatype Central | central-publishing-maven-plugin 0.6.0 |
| 许可证 | Apache 2.0 | |

Maven 坐标：
```xml
<groupId>io.github.code-visual</groupId>
<artifactId>visual-flowengine-spring-boot-starter</artifactId>
<version>1.2.6</version>
```

---

## 3. 项目结构

```
visual-flowengine-spring-boot-starter/
├── pom.xml
└── src/main/
    ├── java/io/github/code/visual/
    │   ├── ast/
    │   │   ├── GroovyASTCodeParse.java      # AST 转换：Condition 节点安全校验 + GroovyShellVisitor 触发
    │   │   └── GroovyShellVisitor.java      # AST 访问者：禁用危险类方法调用/构造、禁止单字母方法名
    │   ├── config/
    │   │   ├── VisualFlowEngineAutoConfiguration.java  # Spring Boot 自动装配入口
    │   │   └── VisualFlowProperties.java               # @ConfigurationProperties(prefix="visual.flow")
    │   ├── model/
    │   │   ├── DebugRequest.java            # 调试请求体：ScriptMetadata + inputValues + breakpointNodeId
    │   │   ├── Diagnostic.java              # 编译诊断结果（对应 Monaco MarkerSeverity）
    │   │   ├── ScriptMetadata.java          # 节点定义：id/name/text/type/children
    │   │   ├── ScriptRequest.java           # 脚本编译请求体：code + inputValues
    │   │   ├── ScriptRunStatus.java         # 运行状态枚举：Start/End/Success/Error
    │   │   ├── ScriptType.java              # 节点类型枚举：Start/Script/Condition/Rule/Fork/Join/End
    │   │   ├── WorkflowIdAndName.java       # 轻量列表 DTO：workflowId + workflowName
    │   │   ├── WorkflowMetadata.java        # 流程定义主体（含 ScriptMetadata 树）
    │   │   ├── WorkflowParameters.java      # 入参声明：parameterName + parameterType
    │   │   └── WorkflowTaskLog.java         # 单节点执行日志（含前后 Binding 快照）
    │   ├── utils/
    │   │   ├── CommonUtils.java             # Groovy 编译消息 → Diagnostic 转换工具
    │   │   └── SpringContext.java           # ApplicationContextAware 静态工具类
    │   ├── web/
    │   │   ├── VisualFlowIndexController.java  # 服务 index.html，注入动态 basePath
    │   │   └── WorkflowController.java         # 全部内置 REST API（CRUD + 执行 + SSE 调试 + 编译）
    │   └── workflow/
    │       ├── ExecutionSignal.java         # 内部枚举：控制递归遍历信号（CONTINUE/CONDITION_HIT/STOP）
    │       ├── InMemoryWorkflowRepository.java  # 默认内存存储实现（开发/测试用）
    │       ├── WorkflowEngine.java          # 核心引擎：执行/调试/编译/校验/CRUD
    │       ├── WorkflowExecutionListener.java   # 扩展接口：执行结束回调
    │       └── WorkflowRepository.java      # 存储 SPI 接口（用户可替换为数据库实现）
    ├── groovy/io/github/code/visual/
    │   └── ruleengine/
    │       ├── Rule.groovy                  # 规则数据类：name/when(Closure)/then(Closure)
    │       └── RuleEngine.groovy            # 规则引擎：解析 DSL + 批量匹配执行
    └── resources/
        ├── META-INF/
        │   ├── resources/visualflow/        # 前端静态资源（index.html + assets/）
        │   ├── services/
        │   │   └── org.codehaus.groovy.transform.ASTTransformation  # SPI 注册 GroovyASTCodeParse
        │   └── spring/
        │       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports  # SB3 自动装配注册
        └── groovy/
            └── SecureExtension.groovy       # Groovy 类型检查扩展（onMethodSelection 等钩子）
```

---

## 4. 核心设计模式

### 4.1 树型工作流 DSL

流程图保存为一棵以 `Start` 节点为根的 **有序树**：
- 每个节点（`ScriptMetadata`）包含 `children` 列表
- 引擎用**深度优先递归**（`recursiveAndExecute`）遍历树
- 所有节点共享同一个 Groovy `Binding`（变量作用域）——前一节点写入的变量，后一节点可直接读取

### 4.2 Condition 节点 if/else-if 模式

当父节点的 children 全部为 `Condition` 类型时，视为 if/else-if 分支：
- 引擎逐个执行 Condition 节点
- 第一个返回 `true` 的 Condition 节点执行其子树，并发出 `CONDITION_HIT` 信号
- `CONDITION_HIT` 信号使同级后续 Condition 节点全部跳过（break）

### 4.3 ExecutionSignal 递归控制

`recursiveAndExecute` 方法返回 `ExecutionSignal` 枚举：
- `CONTINUE`：正常继续执行下一兄弟节点
- `CONDITION_HIT`：某个 Condition 命中，跳过后续兄弟
- `STOP`：发生错误或遇到断点，停止整棵树递归

### 4.4 Groovy 安全沙箱（双层）

**层 1：SecureASTCustomizer（编译器级别）**
- 禁止 `while`/`goto` 关键字及 `WhileStatement`
- 禁止 `System`/`Runtime`/`Class` 静态导入
- 禁止 `org.codehaus.groovy.runtime.*` 和 `groovy.json.*` 导入

**层 2：GroovyASTCodeParse + GroovyShellVisitor（AST 转换级别）**
- `GroovyASTCodeParse` 实现 `ASTTransformation`，通过 SPI 在编译 SEMANTIC_ANALYSIS 阶段触发
- 对 `Condition_*` 脚本检查：禁止调用 `binding.setVariable()`（Condition 节点不得修改变量）
- `GroovyShellVisitor` 检查所有类：
  - 禁止 `java.lang.System`/`java.lang.Runtime`/`java.lang.reflect.Method` 的方法调用和构造
  - 禁止定义单字母方法名

**层 3：SecureExtension.groovy（类型检查扩展）**
- `onMethodSelection`：拦截对 System/Runtime/Class 方法的调用
- 为 Rule DSL 动态解析 `decision_rule`、`when`、`then` 变量

### 4.5 Spring Boot Auto-configuration

通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（SB3）注册：
```
io.github.code.visual.config.VisualFlowEngineAutoConfiguration
```

`VisualFlowEngineAutoConfiguration` 定义：
- `@ConditionalOnWebApplication`（仅 Web 环境生效）
- `@EnableConfigurationProperties(VisualFlowProperties.class)`
- `@ConditionalOnMissingBean`：`WorkflowRepository`、`WorkflowEngine`、`SecureASTCustomizer`、`CompilerConfiguration` 均可被用户覆盖

### 4.6 Groovy 类缓存

`WorkflowEngine.executeScript()` 使用 `groovyClassLoader.parseClass(codeSource, enableCacheSource)`：
- 文件名格式：`{ScriptType}_Id_{scriptId}_MD5_{md5(scriptText)}.groovy`
- 相同脚本 MD5 相同 → 文件名相同 → `GroovyClassLoader` 命中缓存，不重新编译

---

## 5. 前端集成机制

前端 React 应用打包后内嵌在 `META-INF/resources/visualflow/` 中：
- `VisualFlowIndexController` 服务 `index.html`，在响应前动态替换静态资源路径（适配 `context-path`）并注入 `window.__VISUAL_FLOW_CONFIG__` 全局配置对象
- `WebMvcConfigurer` 将 `{basePath}/**` 映射到 classpath 静态资源（用于 JS/CSS/Font 等 assets）
- 前端通过读取 `window.__VISUAL_FLOW_CONFIG__` 知道所有 API 路径，无需硬编码

---

## 6. 版本历史关键里程碑

| 版本 | 关键变更 |
|------|---------|
| 1.0.5 | 深度增强 AST 安全校验 |
| 1.0.8 | 新增 `WorkflowExecutionListener` 日志回调 |
| 1.0.9 | 升级依赖，修复静态资源路径 |
| 1.1.0 | 修复前端 mock 模式生产环境未关闭 |
| 1.2.0 | 改用 `groovy-all`，新增 `durationMs`，完整异常堆栈，断点调试 SSE |
| 1.2.1 | Condition 节点返回值类型校验（必须为 Boolean）|
| 1.2.3 | 编译诊断增加 severity，CompilationUnit 收集 warnings |
| 1.2.6 | Condition if/else-if 执行逻辑，结构校验，全面英文化 |
