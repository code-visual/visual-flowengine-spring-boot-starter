# Visual FlowEngine — 数据模型

> 最后更新：2026-03-24

---

## 1. 流程定义模型

### 1.1 `WorkflowMetadata`
**文件路径：** `src/main/java/io/github/code/visual/model/WorkflowMetadata.java`
**实现接口：** `java.io.Serializable`

流程的顶层元数据，包含流程的所有节点定义。

| 字段 | 类型 | 说明 |
|------|------|------|
| `workflowId` | `Integer` | 主键，由存储层自动分配（InMemory 从 10001 开始）|
| `workflowName` | `String` | 流程名称，`findByName` 按此查找 |
| `workflowParameters` | `List<WorkflowParameters>` | 声明式入参列表（名称+类型，供前端展示）|
| `workflowPurpose` | `String` | 流程用途说明 |
| `remark` | `String` | 备注信息 |
| `revision` | `Integer` | 版本号，每次 `save` 时自动 +1 |
| `createdAt` | `Date` | 创建时间，由存储层设置 |
| `updatedAt` | `Date` | 最后更新时间，由存储层设置 |
| `scriptMetadata` | `ScriptMetadata` | 流程节点树的根节点（通常为 `Start` 类型）|

**废弃字段别名（向后兼容）：**
- `getCreateTime()` / `setCreateTime()` → 映射到 `createdAt`
- `getUpdateTime()` / `setUpdateTime()` → 映射到 `updatedAt`

---

### 1.2 `WorkflowParameters`
**文件路径：** `src/main/java/io/github/code/visual/model/WorkflowParameters.java`
**实现接口：** `java.io.Serializable`

流程入参声明，仅用于前端展示（引擎不依赖此字段执行，实际入参通过 `inputVariables` Map 传入）。

| 字段 | 类型 | 说明 |
|------|------|------|
| `parameterName` | `String` | 参数名，对应 Groovy Binding 中的变量名 |
| `parameterType` | `String` | 参数类型描述（如 `"Integer"`、`"String"`）|

---

## 2. 节点模型

### 2.1 `ScriptMetadata`
**文件路径：** `src/main/java/io/github/code/visual/model/ScriptMetadata.java`
**实现接口：** `java.io.Serializable`

流程节点定义，构成树型结构（每个节点持有子节点列表）。

| 字段 | 类型 | 说明 |
|------|------|------|
| `scriptId` | `String` | 节点唯一 ID（前端生成，如 `"1"`, `"2"` 等；Start 节点固定为 `"1"`）|
| `scriptName` | `String` | 节点名称（展示用）|
| `scriptText` | `String` | Groovy 脚本代码（Start/End 节点为空字符串）|
| `scriptType` | `ScriptType` | 节点类型枚举 |
| `scriptDesc` | `String` | 节点描述（可选）|
| `children` | `List<ScriptMetadata>` | 子节点列表（有序）；叶节点为 `null` 或空列表 |

**树结构示例：**
```json
{
  "scriptId": "1",
  "scriptName": "Start",
  "scriptType": "Start",
  "scriptText": "",
  "children": [
    {
      "scriptId": "2",
      "scriptName": "判断年龄",
      "scriptType": "Condition",
      "scriptText": "age >= 18",
      "children": [
        {
          "scriptId": "3",
          "scriptName": "成年处理",
          "scriptType": "Script",
          "scriptText": "binding.setVariable('result', 'adult')",
          "children": [
            {
              "scriptId": "4",
              "scriptName": "End",
              "scriptType": "End",
              "scriptText": "",
              "children": null
            }
          ]
        }
      ]
    },
    {
      "scriptId": "5",
      "scriptName": "未成年处理",
      "scriptType": "Condition",
      "scriptText": "age < 18",
      "children": [
        {
          "scriptId": "6",
          "scriptName": "End",
          "scriptType": "End",
          "scriptText": "",
          "children": null
        }
      ]
    }
  ]
}
```

---

## 3. 执行上下文模型

### 3.1 `WorkflowTaskLog`
**文件路径：** `src/main/java/io/github/code/visual/model/WorkflowTaskLog.java`
**实现接口：** `java.io.Serializable`

单个节点的执行日志，包含执行前后变量快照。

| 字段 | 类型 | 说明 |
|------|------|------|
| `scriptId` | `String` | 对应节点的 scriptId |
| `scriptName` | `String` | 对应节点的名称 |
| `scriptType` | `ScriptType` | 对应节点的类型 |
| `beforeRunBinding` | `Object` | 执行前 Binding 快照（JSON 字符串，Jackson 序列化；非可序列化对象降级为 toString）|
| `afterRunBinding` | `Object` | 执行后 Binding 快照（同上）|
| `scriptRunStatus` | `ScriptRunStatus` | 执行结果状态 |
| `scriptRunResult` | `Object` | 脚本最后一行表达式的返回值（仅 Script/Condition/Rule 节点有效）|
| `scriptRunTime` | `LocalDateTime` | 执行开始时间 |
| `scriptRunError` | `String` | 完整异常堆栈（仅 Error 状态时有值）|
| `durationMs` | `Long` | 执行耗时（毫秒）；Start/End 节点为 0 |

**日志按层次分组：**

`execute` 方法返回 `Map<Integer, List<WorkflowTaskLog>>`：
- 第 1 层（key=1）：Start 节点
- 第 2 层（key=2）：Start 的直接子节点
- 第 n 层（key=n）：树中第 n 层节点

同一层次若有多个节点（如多个 Condition 兄弟），全部收集在同一层级的 List 中（按执行顺序）。

---

### 3.2 `DebugRequest`
**文件路径：** `src/main/java/io/github/code/visual/model/DebugRequest.java`
**实现接口：** `java.io.Serializable`

调试接口的请求体。

| 字段 | 类型 | 说明 |
|------|------|------|
| `scriptMetadata` | `ScriptMetadata` | 流程节点树根节点（不依赖已保存流程）|
| `inputValues` | `Map<String, Object>` | 初始输入变量，注入 Groovy Binding |
| `breakpointNodeId` | `String` | 断点节点 ID；到达该节点执行完毕后停止递归 |

---

### 3.3 `ScriptRequest`
**文件路径：** `src/main/java/io/github/code/visual/model/ScriptRequest.java`
**实现接口：** `java.io.Serializable`

脚本编译接口的请求体。

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | `String` | 待编译的 Groovy 脚本代码 |
| `inputValues` | `Map<String, Object>` | 预设变量（当前编译校验接口不使用此字段，为扩展预留）|

---

### 3.4 Groovy `Binding`（运行时上下文）

Groovy 原生类 `groovy.lang.Binding`，不在模型包中定义，但是执行过程中的核心数据载体。

- 通过 `new Binding(inputVariables)` 初始化，将 `inputVariables` Map 的所有键值注入
- 所有节点共享同一个 `Binding` 实例
- 脚本通过变量名直接读写（`score`、`age` 等）
- 显式写入：`binding.setVariable("key", value)`
- 读取：`binding.getVariable("key")` 或直接用变量名

---

### 3.5 `WorkflowIdAndName`
**文件路径：** `src/main/java/io/github/code/visual/model/WorkflowIdAndName.java`
**实现接口：** `java.io.Serializable`

列表接口的轻量 DTO，避免传输完整 `scriptMetadata` 树。

| 字段 | 类型 | 说明 |
|------|------|------|
| `workflowId` | `Integer` | 流程 ID |
| `workflowName` | `String` | 流程名称 |

提供有参构造器 `WorkflowIdAndName(Integer workflowId, String workflowName)` 和无参构造器。

---

## 4. 枚举与常量

### 4.1 `ScriptType`
**文件路径：** `src/main/java/io/github/code/visual/model/ScriptType.java`

| 枚举值 | 用途 |
|--------|------|
| `Start` | 流程起点，仅可作为根节点 |
| `Script` | 普通脚本节点（执行 Groovy，可修改 Binding）|
| `Condition` | 条件节点（必须返回 Boolean，不可修改 Binding）|
| `Rule` | 规则引擎节点（DSL 定义规则组）|
| `Fork` | 并行分叉（定义，未实现）|
| `Join` | 并行汇合（定义，未实现）|
| `End` | 流程终点，必须为叶节点 |

---

### 4.2 `ScriptRunStatus`
**文件路径：** `src/main/java/io/github/code/visual/model/ScriptRunStatus.java`

| 枚举值 | 含义 |
|--------|------|
| `Start` | Start 节点已记录（特殊状态，表示流程开始）|
| `End` | End 节点已记录（特殊状态，表示分支结束）|
| `Success` | 脚本正常执行完成 |
| `Error` | 脚本执行异常（含超时、类型错误、运行时错误）|

---

### 4.3 `ExecutionSignal`（包级私有）
**文件路径：** `src/main/java/io/github/code/visual/workflow/ExecutionSignal.java`

仅在 `workflow` 包内使用，不对外暴露：

| 枚举值 | 含义 |
|--------|------|
| `CONTINUE` | 继续执行下一兄弟节点 |
| `CONDITION_HIT` | Condition 命中，父节点中断兄弟遍历 |
| `STOP` | 错误或断点，停止整棵子树 |

---

### 4.4 `Diagnostic` 常量
**文件路径：** `src/main/java/io/github/code/visual/model/Diagnostic.java`

```java
public static final int SEVERITY_HINT    = 1;   // 提示
public static final int SEVERITY_INFO    = 2;   // 信息
public static final int SEVERITY_WARNING = 4;   // 警告
public static final int SEVERITY_ERROR   = 8;   // 错误（默认）
```

这些值与 Monaco Editor 的 `MarkerSeverity` 枚举值对齐，前端可直接使用。

---

### 4.5 `Diagnostic` 模型字段
**文件路径：** `src/main/java/io/github/code/visual/model/Diagnostic.java`
**实现接口：** `java.io.Serializable`

| 字段 | 类型 | 说明 |
|------|------|------|
| `startLineNumber` | `int` | 错误起始行号（从 1 开始）|
| `startColumn` | `int` | 错误起始列号（从 1 开始）|
| `endLineNumber` | `int` | 错误结束行号 |
| `endColumn` | `int` | 错误结束列号 |
| `message` | `String` | 错误/警告消息文本 |
| `severity` | `int` | 严重级别（默认 `SEVERITY_ERROR=8`）|

---

## 5. 规则引擎数据模型

### 5.1 `Rule`（Groovy 类）
**文件路径：** `src/main/groovy/io/github/code/visual/ruleengine/Rule.groovy`

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 规则名称，命中时追加到 `decision_rule` 变量 |
| `when` | `Closure` | 条件闭包，接收 Binding，返回 boolean |
| `then` | `Closure` | 动作闭包，接收 Binding，修改变量 |

**DSL 语法：**
```groovy
decision_rule("规则名称") {
    when { /* 条件表达式，可访问 Binding 变量 */ }
    then { /* 动作，修改 binding 变量 */ }
}
```

执行后，Binding 中 `decision_rule` 变量为命中的所有规则名 List（未命中时为 `["miss"]`）。
