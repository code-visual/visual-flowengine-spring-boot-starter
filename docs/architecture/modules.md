# Visual FlowEngine — 模块详解

> 最后更新：2026-04-10

---

## 1. 工作流引擎核心（`workflow` 包）

### 1.1 `WorkflowEngine`
**文件路径：** `src/main/java/io/github/code/visual/workflow/WorkflowEngine.java`

核心类，用户直接注入使用。

#### 构造函数
```java
// 向后兼容构造（旧版 listener）
public WorkflowEngine(
    CompilerConfiguration compilerConfig,
    WorkflowRepository repository,
    VisualFlowProperties properties,
    List<WorkflowExecutionListener> listeners)

// 新版构造（since 1.3.0，同时支持旧版和新版 listener）
public WorkflowEngine(
    CompilerConfiguration compilerConfig,
    WorkflowRepository repository,
    VisualFlowProperties properties,
    List<WorkflowExecutionListener> listeners,
    List<WorkflowExecutionEventListener> eventListeners)
```
构造时创建：
- `GroovyClassLoader`（持有编译器配置，含安全沙箱）
- 单线程 `ScheduledExecutorService`（daemon 线程名 `script-timeout`，用于脚本超时中断）

#### 公开 API 方法

| 方法签名 | 说明 |
|---------|------|
| `execute(Integer workflowId, Map inputVariables)` | 按 ID 执行工作流，返回按层次分组的执行日志 |
| `execute(String workflowName, Map inputVariables)` | 按名称执行工作流 |
| `debug(DebugRequest debugRequest)` | 调试执行（不触发 Listener 回调） |
| `debug(DebugRequest debugRequest, Consumer<WorkflowTaskLog> onNodeComplete)` | 调试执行 + SSE 实时回调，支持断点 |
| `compileScript(String code)` | 编译 Groovy 脚本，返回错误/警告列表（不执行）|
| `createWorkflow(WorkflowMetadata metadata)` | 创建工作流（初始化 Start 节点）|
| `getWorkflow(Integer workflowId)` | 按 ID 查询工作流定义 |
| `updateWorkflow(WorkflowMetadata metadata)` | 更新工作流定义 |
| `deleteWorkflow(Integer workflowId)` | 删除工作流 |
| `validateStructure(ScriptMetadata node)` | 结构校验，返回错误消息列表（空 = 合法）|

#### 内部核心方法

| 方法 | 说明 |
|------|------|
| `doExecute(ScriptMetadata, Map, Consumer, String)` | 执行入口，先结构校验（仅 Start 根节点），再调用 `recursiveAndExecute` |
| `recursiveAndExecute(...)` | 核心递归方法，switch 节点类型分发处理逻辑 |
| `recurseChildren(...)` | 遍历子节点，处理 `CONDITION_HIT` 中断逻辑 |
| `executeAndRecurse(...)` | Script/Rule 通用：执行脚本 + 处理信号 + 递归子节点 |
| `logScriptExecution(...)` | 执行脚本，记录前后 Binding 快照、耗时、异常 |
| `logTerminalNode(...)` | 记录 Start/End 节点（无脚本，只记录 Binding 快照）|
| `executeScript(ScriptMetadata, Binding)` | 实际执行 Groovy 脚本，MD5 缓存 + 超时中断 |
| `snapshotBinding(Binding)` | 快照当前 Binding 变量为 JSON 字符串（Jackson，失败降级为 toString）|
| `extractErrorMessage(Throwable)` | 提取完整异常堆栈字符串 |
| `validateStructure / doValidateStructure` | 树结构合法性递归校验 |
| `notifyListeners(...)` | 遍历所有 `WorkflowExecutionListener`，异常不抛出只打日志 |
| `notifyEventListeners(...)` | 构建 `WorkflowExecutionEvent` 并通知所有 `WorkflowExecutionEventListener` |
| `executeAndNotify(metadata, inputVariables)` | 内部统一执行入口：计时 → `doExecute` → `notifyListeners` + `notifyEventListeners`（在 finally 中执行，保证即使执行异常也会回调）|

#### 脚本超时机制
当 `scriptTimeoutSeconds > 0` 时：
1. 调用 `timeoutScheduler.schedule(currentThread::interrupt, timeout, TimeUnit.SECONDS)` 设置定时器
2. 脚本完成后 `interruptor.cancel(false)` 取消定时器
3. 捕获 `InterruptedException` 后检查标志，若超时则抛出 `RuntimeException("Script execution timed out")`
4. `finally` 中调用 `Thread.interrupted()` 清除中断标志，避免影响后续节点

---

### 1.2 `WorkflowRepository`（接口）
**文件路径：** `src/main/java/io/github/code/visual/workflow/WorkflowRepository.java`

存储 SPI，用户实现此接口以接入自己的数据库。

```java
void save(WorkflowMetadata workflowMetadata);       // 新增（id=null）or 更新（id非null）
void deleteById(Integer workflowId);
WorkflowMetadata findById(Integer workflowId);
WorkflowMetadata findByName(String workflowName);
List<WorkflowIdAndName> findAll();
```

---

### 1.3 `InMemoryWorkflowRepository`
**文件路径：** `src/main/java/io/github/code/visual/workflow/InMemoryWorkflowRepository.java`

默认实现，**重启后数据丢失**。

- 使用 `ConcurrentHashMap<Integer, WorkflowMetadata>` 存储
- 使用 `AtomicInteger`（初始值 10000）自增生成 workflowId
- `save`：新建时自动分配 id、revision=1；更新时 revision+1，保留 createdAt

---

### 1.4 `WorkflowExecutionListener`（接口）
**文件路径：** `src/main/java/io/github/code/visual/workflow/WorkflowExecutionListener.java`

旧版执行完成回调。实现为 Spring Bean 后自动被注入 `WorkflowEngine`。

```java
void onExecutionComplete(
    Integer workflowId,
    Integer revision,
    Map<Integer, List<WorkflowTaskLog>> logs);
```

> **注意**：新代码建议使用 `WorkflowExecutionEventListener`（1.3.0+），它提供更完整的上下文。

---

### 1.4b `WorkflowExecutionEventListener`（接口，since 1.3.0）
**文件路径：** `src/main/java/io/github/code/visual/workflow/WorkflowExecutionEventListener.java`

增强版执行完成回调，提供完整执行上下文（工作流名称、成功/失败状态、耗时、错误信息）。实现为 Spring Bean 后自动被注入 `WorkflowEngine`。

```java
void onExecutionComplete(WorkflowExecutionEvent event);
```

**使用方式：**
```java
@Component
public class MyListener implements WorkflowExecutionEventListener {
    @Override
    public void onExecutionComplete(WorkflowExecutionEvent event) {
        log.info("Workflow {} finished in {}ms, success={}",
            event.getWorkflowName(), event.getDurationMs(), event.isSuccess());
    }
}
```

> **注意**：回调为同步调用，实现应快速返回，耗时操作请提交异步线程池。

---

### 1.4c `WorkflowExecutionEvent`（值对象，since 1.3.0）
**文件路径：** `src/main/java/io/github/code/visual/workflow/WorkflowExecutionEvent.java`

不可变事件对象，通过 Builder 构造：

| 字段 | 类型 | 说明 |
|------|------|------|
| `workflowId` | `Integer` | 工作流 ID |
| `workflowName` | `String` | 工作流名称 |
| `revision` | `Integer` | 工作流版本号 |
| `success` | `boolean` | 执行是否成功（任一节点 Error 状态则为 false）|
| `durationMs` | `long` | 总耗时（毫秒）|
| `executedAt` | `LocalDateTime` | 执行时刻 |
| `errorMessage` | `String` | 失败原因（null 表示成功）|
| `logs` | `Map<Integer, List<WorkflowTaskLog>>` | 完整执行日志（按层次分组）|

---

### 1.5 `ExecutionSignal`（包级枚举）
**文件路径：** `src/main/java/io/github/code/visual/workflow/ExecutionSignal.java`

递归遍历控制信号，仅在 `workflow` 包内使用：

| 值 | 含义 |
|----|------|
| `CONTINUE` | 正常，继续执行下一兄弟节点 |
| `CONDITION_HIT` | Condition 节点命中，父节点停止遍历后续兄弟 |
| `STOP` | 错误或断点，立即停止整棵子树 |

---

## 2. 节点类型与执行逻辑

### 2.1 节点类型枚举 `ScriptType`
**文件路径：** `src/main/java/io/github/code/visual/model/ScriptType.java`

| 枚举值 | 含义 | 执行逻辑 |
|--------|------|---------|
| `Start` | 流程起点（只能有一个，必须为根） | 记录 Binding 快照，不执行脚本，递归子节点 |
| `Script` | 普通 Groovy 脚本节点 | 执行脚本，更新 Binding，成功则递归子节点 |
| `Condition` | 条件分支节点（必须返回 Boolean） | 执行脚本，若 true 递归子节点并发出 CONDITION_HIT；若非 Boolean 报错 |
| `Rule` | 规则引擎节点（自定义 DSL） | 通过 `RuleEngine.parser` + `RuleEngine.execute` 执行，将决策结果写入 Binding `decision_rule` 变量 |
| `Fork` | 并行分支起点（定义但暂未实现） | 当前 default 分支，直接 CONTINUE |
| `Join` | 并行分支汇合点（定义但暂未实现） | 当前 default 分支，直接 CONTINUE |
| `End` | 流程终点（叶节点必须为此类型） | 记录 Binding 快照，不执行脚本，结束当前分支 |

### 2.2 结构校验规则（`validateStructure`）

在调用 `doExecute` 前（仅当根节点为 `Start` 类型时）自动执行：
1. **叶节点必须为 End 类型**：叶节点（无子节点）若非 `End` 则报错
2. **Condition 兄弟节点必须全为 Condition**：Condition 节点不可与 Script 等节点并列
3. **End 节点不可有兄弟节点**：`End` 出现时 children 列表只能有它一个

---

## 3. 流程执行生命周期

```
execute(workflowId, inputVars)
    └─ findById / findByName         # 查 WorkflowMetadata
    └─ doExecute(scriptMetadata, inputVars)
          └─ validateStructure()     # 仅 root=Start 时执行
          └─ new Binding(inputVars)
          └─ recursiveAndExecute(Start, binding, logMap={}, level=1)
                ├─ logTerminalNode(Start)   # 记录快照，status=Start
                └─ recurseChildren()
                      └─ recursiveAndExecute(Script_A, ..., level=2)
                            └─ logScriptExecution()
                                  ├─ snapshotBinding (before)
                                  ├─ executeScript()   # Groovy 编译/执行
                                  └─ snapshotBinding (after)
                      └─ recursiveAndExecute(Condition_B, ..., level=2)
                            └─ logScriptExecution()  # 必须返回 Boolean
                            └─ if true: recurseChildren() → CONDITION_HIT
                            └─ CONDITION_HIT → break（跳过后续兄弟 Condition）
                      └─ recursiveAndExecute(End, ..., level=2)
                            └─ logTerminalNode(End)
    └─ executeAndNotify(metadata, inputVariables)     # since 1.3.0 统一入口
          ├─ doExecute(scriptMetadata, inputVariables)
          └─ [finally]
                ├─ notifyListeners(workflowId, revision, logs)           # 旧版回调
                └─ notifyEventListeners(metadata, logs, success, durationMs, errorMessage) # 新版回调
    return Map<Integer(level), List<WorkflowTaskLog>>
```

---

## 4. 表达式求值（Groovy Binding 共享机制）

所有节点共享同一个 `groovy.lang.Binding` 对象。Groovy 脚本通过直接读写变量名访问 Binding：

```groovy
// Script 节点：读取输入变量，写入新变量
def result = age > 18 ? "adult" : "minor"
binding.setVariable("category", result)

// Condition 节点：必须返回 Boolean，不得修改 Binding
age > 18   // 最后一行表达式即为返回值
```

`snapshotBinding` 在每个节点执行前后各调用一次，将 `binding.getVariables()` 序列化为 JSON 字符串存入 `WorkflowTaskLog.beforeRunBinding` / `afterRunBinding`。

---

## 5. 规则引擎模块（`ruleengine` 包）

### 5.1 `Rule`（Groovy 类）
**文件路径：** `src/main/groovy/io/github/code/visual/ruleengine/Rule.groovy`

简单数据类：
```groovy
class Rule {
    String name
    Closure when    // 条件判断，返回 boolean
    Closure then    // 动作执行，修改 Binding
}
```

### 5.2 `RuleEngine`（Groovy 类）
**文件路径：** `src/main/groovy/io/github/code/visual/ruleengine/RuleEngine.groovy`

**`parser(ScriptMetadata rulesDefinition)`**：
1. 创建临时 Binding，注入 `decision_rule` 闭包（DSL 关键字）
2. 调用 `WorkflowEngine.executeScript` 执行规则脚本，闭包内部构造 `Rule` 对象并收集到 `localRules`
3. 返回 `List<Rule>`

**`execute(List<Rule> rules, Binding inputData)`**：
1. `rules.findAll { rule -> rule.when(inputData) }` 找出所有匹配规则
2. 若无匹配：向 Binding 的 `decision_rule` 变量追加 `"miss"`
3. 若有匹配：逐个执行 `rule.then(inputData)`，将规则名追加到 `decision_rule`
4. 返回命中规则名列表字符串

**Rule DSL 示例：**
```groovy
decision_rule("高价值客户") {
    when { score > 80 }
    then { binding.setVariable("level", "VIP") }
}
decision_rule("普通客户") {
    when { score <= 80 }
    then { binding.setVariable("level", "normal") }
}
```

---

## 6. AST 安全检查模块（`ast` 包）

### 6.1 `GroovyASTCodeParse`
**文件路径：** `src/main/java/io/github/code/visual/ast/GroovyASTCodeParse.java`

实现 `ASTTransformation`，通过 SPI（`META-INF/services/org.codehaus.groovy.transform.ASTTransformation`）注册，在所有 Groovy 脚本编译 `SEMANTIC_ANALYSIS` 阶段自动触发。

两项检查：
1. **Condition 节点禁止修改 Binding**：脚本文件名以 `Condition` 开头时，检查是否存在 `binding.setVariable(...)` 调用，发现则抛 RuntimeException
2. **触发 `GroovyShellVisitor`**：对所有 ClassNode 调用 `visitContents(new GroovyShellVisitor(source))`

### 6.2 `GroovyShellVisitor`
**文件路径：** `src/main/java/io/github/code/visual/ast/GroovyShellVisitor.java`

继承 `ClassCodeVisitorSupport`，访问所有方法/方法调用/构造调用：

| 检查项 | 规则 |
|--------|------|
| 方法名长度 | 单字母方法名（长度=1）抛出 SyntaxException |
| 方法调用对象 | 对象类型为 `java.lang.System`/`Runtime`/`reflect.Method` 时添加编译错误 |
| 构造调用 | 构造类型为上述三类时添加编译错误 |

### 6.3 `SecureExtension.groovy`
**文件路径：** `src/main/resources/groovy/SecureExtension.groovy`

Groovy 类型检查扩展（`@CompileStatic` 兼容），通过钩子方法增强安全性和 DSL 解析：
- `onMethodSelection`：拦截 System/Runtime/Class 方法调用，添加静态类型错误
- `unresolvedVariable`：允许 `when`/`then` 未解析变量（Rule DSL 闭包属性），类型标记为 `CLOSURE_TYPE`
- `methodNotFound`：允许 `decision_rule(...)` 动态方法调用，返回 `CLOSURE_TYPE`
- `unresolvedProperty`/`unresolvedAttribute`：标记为已处理（不报错）

---

## 7. Web 层（`web` 包）

### 7.1 `WorkflowController`
**文件路径：** `src/main/java/io/github/code/visual/web/WorkflowController.java`

`@ConditionalOnProperty(name="visual.flow.enable-api", havingValue="true", matchIfMissing=true)`

所有路径以 `${visual.flow.basePath:/visualflow}` 为前缀：

| HTTP 方法 | 路径 | 功能 |
|-----------|------|------|
| GET | `/api/config` | 返回前端所需 API 路径配置 |
| GET | `/api/workflows` | 列出所有工作流（id+name）|
| POST | `/api/workflows` | 创建工作流 |
| GET | `/api/workflows/{id}` | 获取工作流详情 |
| PUT | `/api/workflows/{id}` | 更新工作流 |
| DELETE | `/api/workflows/{id}` | 删除工作流 |
| POST | `/api/workflows/execute` | 执行工作流（请求体：`workflowId` + `inputVariables`）|
| POST | `/api/workflows/debug` | 调试执行（请求体：`DebugRequest`）|
| POST | `/api/workflows/debug/stream` | SSE 调试流（每个节点完成推 `event:node`，完成推 `event:done`）|
| POST | `/api/script/compile` | 脚本编译校验（返回 `List<Diagnostic>`）|

SSE 端点使用 `CachedThreadPool` + `AtomicBoolean` 做客户端断开保护（`onCompletion`/`onTimeout`/`onError` 设置 `aborted=true`，回调中检查）。

### 7.2 `VisualFlowIndexController`
**文件路径：** `src/main/java/io/github/code/visual/web/VisualFlowIndexController.java`

`@ConditionalOnProperty(name="visual.flow.enable-ui", havingValue="true", matchIfMissing=true)`

- 服务路径：`{basePath}/index.html`
- 读取 `META-INF/resources/visualflow/index.html`
- 使用 `ServletUriComponentsBuilder.fromCurrentContextPath()` 获取 context-path（兼容 SB2/SB3，无 javax/jakarta 直接引用）
- 替换 HTML 中的 `/visualflow/assets/` 为 `{contextPath}{basePath}/assets/`
- 注入 `window.__VISUAL_FLOW_CONFIG__` 脚本块到 `</head>` 前

---

## 8. 工具类（`utils` 包）

### 8.1 `CommonUtils`
**文件路径：** `src/main/java/io/github/code/visual/utils/CommonUtils.java`

静态工具：将 Groovy `Message` 列表转换为 `List<Diagnostic>`，处理三种 Message 子类型：
- `SyntaxErrorMessage` → 带行列号的 Diagnostic
- `WarningMessage` → severity=WARNING 的 Diagnostic
- `ExceptionMessage` → 带堆栈行号的 Diagnostic

### 8.2 `SpringContext`
**文件路径：** `src/main/java/io/github/code/visual/utils/SpringContext.java`

`ApplicationContextAware` 静态工具，供 Groovy 代码（`RuleEngine`）在 Spring 容器外获取 Bean：
```java
SpringContext.getBean(WorkflowEngine.class)
```
