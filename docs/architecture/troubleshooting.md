# Visual FlowEngine — 故障排查与已知修复

> 最后更新：2026-03-24

---

## 1. 已知修复记录（来自 Git 历史）

### FIX-001：结构校验仅在 root=Start 时执行
**Commit：** `09ed858` `fix: only validate structure when root is Start, rebuild frontend with pre-debug validation`

**问题：** 调试接口（`/debug`）直接传入子树（非 Start 根节点）时，`validateStructure` 被错误触发，导致正常的子树调试报"leaf must be End"错误。

**修复：** `doExecute` 中增加判断：
```java
List<String> validationErrors = script.getScriptType() == ScriptType.Start
        ? validateStructure(script) : Collections.emptyList();
```
只有当传入节点为 `Start` 类型时才执行结构校验。

---

### FIX-002：SSE 端点双重序列化
**Commit：** `09afa81` `修复: SSE端点去除双重序列化，增加客户端断开保护`

**问题：** SSE 端点对 `WorkflowTaskLog` 先手动序列化为字符串，再 `.data(String)` 传给 SseEmitter，导致前端收到双重转义的 JSON。

**修复：** 改为直接 `.data(log)`（Object），让 Spring 的 Jackson 自动序列化：
```java
emitter.send(SseEmitter.event().name("node").data(log));
```
同时增加 `AtomicBoolean aborted` 标志保护客户端断开后的写入。

---

### FIX-003：Condition 节点 BUG
**Commit：** `eb721f3` `条件节点 BUG`

**问题：** 早期版本中，多个 Condition 兄弟节点全部执行，未实现 if/else-if 语义（命中第一个后跳过其余）。

**修复（v1.2.6）：** 引入 `ExecutionSignal.CONDITION_HIT`，在 `recurseChildren` 中：
```java
if (signal == ExecutionSignal.CONDITION_HIT) break;
```
确保第一个命中的 Condition 执行后立即中断兄弟遍历。

---

### FIX-004：AST 安全检查跨 Groovy 版本兼容问题
**Commit：** `fab43cb` `重构: AST安全检查从Groovy改写为Java，解决跨Groovy版本兼容问题`

**问题：** 原始 AST 安全检查用 Groovy 脚本实现，在不同 Groovy 版本（尤其是 Groovy 4.x）下编译行为不一致，导致安全检查失效。

**修复：** 将 `GroovyShellVisitor` 改用纯 Java 实现（继承 `ClassCodeVisitorSupport`），消除版本依赖。

---

### FIX-005：前端 mock 模式生产环境未关闭
**Commit：** `81e7ab0` `1.1.0: 修复前端 mock 模式在生产构建中未关闭的问题`

**问题：** 前端开发时开启了 mock 数据模式，未在生产构建时关闭，导致前端不请求真实 API，直接返回 mock 数据。

**修复：** 前端构建时关闭 mock 开关，重新打包静态资源。

---

### FIX-006：SpringBoot 3.x 参数名注入问题
**Commit：** `73ffc81` `增加接口默认参数名。不然在springboot3 中会报错`

**问题：** Spring Boot 3.x 不再自动推导方法参数名（需要 `-parameters` 编译选项），`@PathVariable` 等注解在不显式指定 name 时报错。

**修复：** 所有 `@PathVariable`、`@RequestParam` 等注解显式指定参数名：
```java
@GetMapping(".../{id}")
public WorkflowMetadata getWorkflow(@PathVariable("id") Integer id)
```

---

### FIX-007：Binding 快照序列化失败
**Commit：** `3434f24` `优化: Binding快照始终输出合法JSON`

**问题：** Binding 中包含不可 JSON 序列化的对象（如自定义 Groovy 对象）时，`snapshotBinding` 抛出异常，导致整个节点执行失败。

**修复：** 增加降级逻辑，对每个变量尝试 Jackson 序列化，失败则使用 `toString()` 替代：
```java
try {
    OBJECT_MAPPER.writeValueAsString(v);
    safeMap.put(String.valueOf(k), v);
} catch (Exception ex) {
    safeMap.put(String.valueOf(k), String.valueOf(v));
}
```

---

### FIX-008：相对路径问题
**Commit：** `a52987b` `解决相对路径的问题`

**问题：** 前端静态资源路径硬编码为 `/visualflow/assets/`，当应用部署在非根路径（有 context-path）时静态资源 404。

**修复：** `VisualFlowIndexController` 动态替换路径，`VisualFlowEngineAutoConfiguration` 动态注册资源处理器：
```java
html = html.replace("\"/visualflow/assets/", "\"" + fullBasePath + "/assets/");
```

---

### FIX-009：Groovy 脚本缓存相关 BUG
**Commit：** `0c6da48` `BUG`

**问题：** `GroovyClassLoader` 缓存键设计不当，修改脚本内容后仍使用旧缓存。

**修复：** 使用脚本内容 MD5 作为文件名的一部分，确保内容变化时缓存键不同：
```java
filename = scriptType + "_Id_" + scriptId + "_MD5_" + md5(scriptText) + ".groovy";
```

---

### FIX-010：Condition 节点不能赋值
**Commit：** `4c95fe3` `条件节点不能赋值`

**问题：** Condition 节点修改了 Binding 变量，影响了其他兄弟 Condition 节点的判断结果（Condition 节点全部共享同一 Binding，若第一个 Condition 修改了变量，后续 Condition 的判断依据就被污染）。

**修复：** 在 AST 转换层（`GroovyASTCodeParse`）禁止 `Condition_*` 文件名的脚本调用 `binding.setVariable()`。

---

## 2. 常见问题与排查

### Q1：访问 `/visualflow/index.html` 返回 404

**排查步骤：**
1. 确认 `visual.flow.enable-ui=true`（默认为 true）
2. 确认 `spring-boot-starter-web` 已引入（`@ConditionalOnWebApplication`）
3. 确认无其他 Spring Security 或拦截器阻拦请求
4. 若有 context-path，访问 `http://host:port/{contextPath}/visualflow/index.html`
5. 检查 jar 包内是否包含 `META-INF/resources/visualflow/index.html`：`jar tf your-starter.jar | grep visualflow`

---

### Q2：API 请求返回 404，但 UI 正常

**排查步骤：**
1. 确认 `visual.flow.enable-api=true`（默认为 true）
2. 确认请求路径前缀匹配 `visual.flow.base-path`（默认 `/visualflow`）
3. 若自定义了 `base-path`，确认前端 UI 的 API 配置也同步更新（通过 `/api/config` 接口检查）

---

### Q3：Condition 节点执行报错 "must return Boolean"

**原因：** Condition 节点的脚本最后一行表达式不是 `Boolean` 类型。

**正确写法：**
```groovy
// 正确：直接返回布尔表达式
age >= 18

// 正确：最后一行是布尔值
def result = age >= 18
result

// 错误：最后一行是赋值语句（Groovy 赋值表达式的返回值不是 Boolean）
// 不要这样写：def result = age >= 18（最后一行是赋值，不会返回 Boolean）
```

---

### Q4：脚本超时被中断后后续节点也不执行

**原因：** 超时时抛出 `RuntimeException`，`recursiveAndExecute` 捕获后返回 `STOP` 信号，停止整棵子树。

**解决：** 优化脚本逻辑减少执行时间，或调大 `visual.flow.script-timeout-seconds`。

---

### Q5：使用 `while` 循环报编译错误

**原因：** `SecureASTCustomizer` 默认禁止 `while` 关键字和 `WhileStatement`。

**解决方案（二选一）：**
1. 改用 `for` 循环或 Groovy 集合方法（`each`、`collect` 等）
2. 自定义 `SecureASTCustomizer` Bean 移除 `while` 限制

---

### Q6：`java.lang.System` 使用报错

**原因：** AST 安全检查禁止使用 `System`、`Runtime`、`Class`。

**解决：** 替换为允许的等价操作：
- `System.currentTimeMillis()` → `new Date().time`（Groovy）
- `System.out.println()` → `println()`（Groovy 内置）

---

### Q7：流程重启后丢失（InMemory 存储）

**原因：** 默认 `InMemoryWorkflowRepository` 是内存存储，重启后清空。

**解决：** 实现自定义 `WorkflowRepository` 并注册为 Spring Bean，参见 [config-deploy.md](config-deploy.md) 第 5 节。

---

### Q8：SSE 调试流长时间无响应

**排查：**
1. 确认请求超时设置（SseEmitter 超时为 60 秒）
2. 检查脚本是否陷入死循环（`while` 已被禁止，但 `for` 大范围循环仍可能）
3. 查看服务端日志中是否有 `Script execution timed out` 信息
4. 检查客户端是否正确处理 SSE 事件（event 类型为 `node` 和 `done`）

---

### Q9：执行后 `WorkflowTaskLog` 的 `beforeRunBinding`/`afterRunBinding` 为 `{}`

**原因：** Binding 中的对象无法被 Jackson 序列化，且 `toString()` 也失败。

**解决：** 确保传入 Binding 的对象都是可序列化的（实现 `Serializable` 或标准 POJO）。

---

### Q10：`WorkflowExecutionListener` 未被调用

**原因：** `onExecutionComplete` 只在 `execute()` 方法（按 ID 或名称执行）中触发，调试接口 `debug()` 不触发 Listener。

---

## 3. 日志排查

开启 DEBUG 日志查看执行细节：

```yaml
logging:
  level:
    io.github.code.visual: DEBUG
```

关键日志位置：
- `WorkflowEngine.logScriptExecution`：脚本执行错误（level=ERROR，含完整堆栈）
- `WorkflowEngine.notifyListeners`：Listener 异常（level=ERROR）
- `WorkflowEngine.snapshotBinding`：Binding 序列化失败（level=WARN）
