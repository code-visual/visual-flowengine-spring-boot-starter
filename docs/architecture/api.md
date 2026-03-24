# Visual FlowEngine — 公开 API 参考

> 最后更新：2026-03-24

---

## 1. 内置 REST API

所有路径默认前缀为 `/visualflow`，可通过 `visual.flow.base-path` 修改。
所有接口均由 `WorkflowController` 提供，受 `visual.flow.enable-api=true`（默认开启）控制。

---

### 1.1 配置信息接口

#### `GET {basePath}/api/config`

返回前端所需的所有 API 路径配置，前端 `window.__VISUAL_FLOW_CONFIG__` 的服务端对应版本。

**响应示例：**
```json
{
  "basePath": "/visualflow",
  "workflowsApiPath": "/visualflow/api/workflows",
  "executeApiPath": "/visualflow/api/workflows/execute",
  "debugApiPath": "/visualflow/api/workflows/debug",
  "debugStreamApiPath": "/visualflow/api/workflows/debug/stream",
  "compileApiPath": "/visualflow/api/script/compile"
}
```

---

### 1.2 工作流 CRUD

#### `GET {basePath}/api/workflows`

列出所有工作流（轻量列表，仅 id + name）。

**响应：** `List<WorkflowIdAndName>`
```json
[
  {"workflowId": 10001, "workflowName": "订单审批流程"},
  {"workflowId": 10002, "workflowName": "风控评分流程"}
]
```

---

#### `POST {basePath}/api/workflows`

创建新工作流。引擎自动创建初始 `Start` 节点。

**请求体：** `WorkflowMetadata`（不含 workflowId）
```json
{
  "workflowName": "新流程",
  "workflowPurpose": "描述用途",
  "remark": "备注",
  "workflowParameters": [
    {"parameterName": "age", "parameterType": "Integer"}
  ]
}
```

**响应：** `WorkflowMetadata`（含自动生成的 workflowId、revision=1、createdAt）

---

#### `GET {basePath}/api/workflows/{id}`

获取工作流完整定义（含完整 `scriptMetadata` 树）。

**响应：** `WorkflowMetadata`（完整）

---

#### `PUT {basePath}/api/workflows/{id}`

更新工作流定义。`revision` 自动 +1。

**请求体：** `WorkflowMetadata`（含完整 `scriptMetadata` 树）

**响应：** 更新后的 `WorkflowMetadata`

---

#### `DELETE {basePath}/api/workflows/{id}`

删除工作流。

**响应：** 204 No Content

---

### 1.3 工作流执行

#### `POST {basePath}/api/workflows/execute`

按 ID 执行已保存的工作流。

**请求体：**
```json
{
  "workflowId": 10001,
  "inputVariables": {
    "age": 25,
    "score": 90
  }
}
```

**响应：** `Map<Integer, List<WorkflowTaskLog>>`
- Key：节点层次（第 1 层 = Start，第 2 层 = Start 的子节点，以此类推）
- Value：该层级执行的节点日志列表

```json
{
  "1": [
    {
      "scriptId": "1",
      "scriptName": "Start",
      "scriptType": "Start",
      "beforeRunBinding": "{\"age\":25,\"score\":90}",
      "afterRunBinding": "{\"age\":25,\"score\":90}",
      "scriptRunStatus": "Start",
      "scriptRunResult": null,
      "scriptRunTime": "2026-03-24T10:00:00",
      "durationMs": 0,
      "scriptRunError": null
    }
  ],
  "2": [...]
}
```

---

### 1.4 调试执行

#### `POST {basePath}/api/workflows/debug`

不依赖已保存流程，直接传入 `ScriptMetadata` 树执行（前端调试专用）。支持断点。

**请求体：** `DebugRequest`
```json
{
  "scriptMetadata": {
    "scriptId": "1",
    "scriptName": "Start",
    "scriptType": "Start",
    "scriptText": "",
    "children": [
      {
        "scriptId": "2",
        "scriptName": "计算等级",
        "scriptType": "Script",
        "scriptText": "def level = score > 80 ? 'A' : 'B'\nbinding.setVariable('level', level)",
        "children": [
          {
            "scriptId": "3",
            "scriptName": "End",
            "scriptType": "End",
            "scriptText": "",
            "children": null
          }
        ]
      }
    ]
  },
  "inputValues": {"score": 90},
  "breakpointNodeId": null
}
```

**断点用法：** 设置 `breakpointNodeId` 为某个节点的 `scriptId`，执行到该节点后立即停止。

**响应：** 同 `execute` 接口，`Map<Integer, List<WorkflowTaskLog>>`

---

#### `POST {basePath}/api/workflows/debug/stream`

SSE 流式调试，每个节点执行完立即推送，无需等待全部完成。

- `Content-Type: text/event-stream`
- 超时时间：60 秒

**请求体：** 同 `/debug`（`DebugRequest`）

**SSE 事件格式：**
```
event: node
data: { ...WorkflowTaskLog JSON... }

event: node
data: { ...WorkflowTaskLog JSON... }

event: done
data:
```

**客户端示例（JavaScript）：**
```javascript
const evtSource = new EventSource(/* 需先 POST，实际实现为 fetch+ReadableStream */);
// 或使用 fetch + ReadableStream 解析 SSE
```

---

### 1.5 脚本编译校验

#### `POST {basePath}/api/script/compile`

编译 Groovy 代码，返回语法错误/警告列表，不执行脚本。供前端代码编辑器实时校验。

**请求体：** `ScriptRequest`
```json
{
  "code": "def x = 1\nx + 1"
}
```

**响应：** `List<Diagnostic>`
```json
[
  {
    "startLineNumber": 2,
    "startColumn": 1,
    "endLineNumber": 2,
    "endColumn": 5,
    "message": "unexpected token",
    "severity": 8
  }
]
```

`severity` 值（对应 Monaco Editor `MarkerSeverity`）：
| 值 | 含义 |
|----|------|
| 1 | Hint |
| 2 | Info |
| 4 | Warning |
| 8 | Error |

空列表表示脚本无问题。

---

## 2. 前端 Web UI

### `GET {basePath}/index.html`

由 `VisualFlowIndexController` 服务，动态注入 `basePath` 和配置。

访问前端 UI：`http://localhost:8080{contextPath}{basePath}/index.html`

默认地址：`http://localhost:8080/visualflow/index.html`

---

## 3. Java 编程 API

直接注入 `WorkflowEngine` Bean 在代码中调用：

```java
@Autowired
private WorkflowEngine workflowEngine;

// 按 ID 执行
Map<Integer, List<WorkflowTaskLog>> logs =
    workflowEngine.execute(10001, Map.of("age", 25, "score", 90));

// 按名称执行
Map<Integer, List<WorkflowTaskLog>> logs =
    workflowEngine.execute("订单审批流程", inputVars);

// 编译校验（不执行）
List<Diagnostic> diagnostics = workflowEngine.compileScript("def x = 1\n x + 1");

// 结构校验
List<String> errors = workflowEngine.validateStructure(scriptMetadata);
```

---

## 4. 扩展接口

### 4.1 自定义存储（`WorkflowRepository`）

```java
@Bean
public WorkflowRepository workflowRepository(YourDao dao) {
    return new YourWorkflowRepository(dao);
}
```

实现五个方法：`save`、`deleteById`、`findById`、`findByName`、`findAll`。

### 4.2 执行监听（`WorkflowExecutionListener`）

```java
@Component
public class AuditLogListener implements WorkflowExecutionListener {
    @Override
    public void onExecutionComplete(Integer workflowId, Integer revision,
                                    Map<Integer, List<WorkflowTaskLog>> logs) {
        // 保存执行记录到数据库
    }
}
```

可注册多个 Listener，全部按顺序调用，单个异常不影响其他 Listener。

### 4.3 自定义安全策略（`SecureASTCustomizer` / `CompilerConfiguration`）

```java
@Bean
public SecureASTCustomizer mySecureASTCustomizer() {
    // 完全替换默认安全配置
    return new SecureASTCustomizer();
}

@Bean
public CompilerConfiguration myCompilerConfiguration(SecureASTCustomizer secure) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.addCompilationCustomizers(secure);
    return config;
}
```

`@ConditionalOnMissingBean` 保证用户自定义 Bean 优先。

---

## 5. 配置属性

详见 [config-deploy.md](config-deploy.md) 中的完整属性列表。
