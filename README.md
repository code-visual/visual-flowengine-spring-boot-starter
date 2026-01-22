# visual-flowengine-spring-boot-starter

![Visual Flow Engine Logo](https://s11.ax1x.com/2024/02/20/pFtp2U1.png)

一个开箱即用的 Spring Boot Starter，提供可视化流程编排 UI、默认 REST API、Groovy 脚本执行与轻量规则引擎，便于在业务系统中快速接入流程与规则能力。

## 快速体验

- 在线体验：[https://www.ikuning.com/visualFlow-ui.html](https://www.ikuning.com/visualFlow-ui.html)
- 项目交流：V **LeviJava**

## 主要特性

- 内置可视化 UI，访问一个路径即可使用流程编排界面
- 默认提供流程管理与执行 REST API，可按需关闭或自定义路径
- Groovy 脚本驱动流程节点，支持条件节点与规则节点
- 默认内存仓库，支持替换为自定义持久化实现
- Groovy 安全限制与 AST 校验，降低不可信脚本风险

## 快速开始

1. 引入依赖（Maven）

```xml
<dependency>
  <groupId>io.github.code-visual</groupId>
  <artifactId>visual-flowengine-spring-boot-starter</artifactId>
  <version>1.0.8</version>
</dependency>
```

1. 确保你的应用是 Web 应用（需引入 `spring-boot-starter-web`），然后启动应用
1. 打开默认 UI 地址：`http://localhost:8080/visualFlow-ui.html`

## 配置项

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `visual.flow.enableDefaultApi` | `true` | 是否启用默认 REST API |
| `visual.flow.enableWebUIPath` | `true` | 是否启用 UI 入口路径 |
| `visual.flow.webUIPath` | `/visualFlow-ui.html` | UI 入口路径 |
| `visual.flow.enableCacheSource` | `true` | 是否缓存 Groovy 编译结果 |
| `visual.flow.executeWorkflowApiPath` | `/api/engine/workflow/execute` | 执行流程 |
| `visual.flow.debugWorkflowApiPath` | `/api/engine/workflow/debug` | 调试流程 |
| `visual.flow.createWorkflowApiPath` | `/api/engine/workflow` | 创建流程 |
| `visual.flow.deleteWorkflowApiPath` | `/api/engine/workflow` | 删除流程 |
| `visual.flow.updateWorkflowApiPath` | `/api/engine/workflow` | 更新流程 |
| `visual.flow.listWorkflowsApiPath` | `/api/engine/workflowList` | 流程列表 |
| `visual.flow.compileScriptApiPath` | `/api/engine/groovyScript/compile` | Groovy 编译诊断 |
| `visual.flow.getWorkflowMetadataApiPath` | `/api/engine/workflow` | 获取流程详情 |

示例配置：

```yaml
visual:
  flow:
    webUIPath: /visualFlow-ui.html
    enableDefaultApi: true
    enableWebUIPath: true
    enableCacheSource: true
```

## 默认 API 说明

- `POST /api/engine/workflow/execute`：执行流程，参数 `workflowId`
- `POST /api/engine/workflow/debug`：调试流程（传入脚本与入参）
- `POST /api/engine/workflow`：创建流程
- `PUT /api/engine/workflow`：更新流程
- `DELETE /api/engine/workflow`：删除流程
- `GET /api/engine/workflowList`：获取流程列表
- `GET /api/engine/workflow`：获取流程详情，参数 `workflowId`
- `POST /api/engine/groovyScript/compile`：Groovy 语法诊断

## 流程结构说明

流程的核心是 `WorkflowMetadata`，其中 `scriptMetadata` 是一个树形结构节点：

```json
{
  "workflowName": "demo",
  "workflowPurpose": "示例流程",
  "scriptMetadata": {
    "scriptId": "1",
    "scriptName": "Start",
    "scriptType": "Start",
    "scriptText": "",
    "children": [
      {
        "scriptId": "2",
        "scriptName": "检查额度",
        "scriptType": "Condition",
        "scriptText": "return amount > 100",
        "children": []
      }
    ]
  }
}
```

节点类型以 `ScriptType` 为准，常用类型：`Start`、`Script`、`Condition`、`Rule`、`End`。

## 规则引擎脚本示例

规则节点脚本可以使用如下 DSL：

```groovy
decision_rule("HighAmount") {
    when { amount > 100 }
    then { binding.setVariable("tag", "high") }
}
```

规则执行结果会写入 `decision_rule` 变量中（列表形式）。

## 自定义持久化

默认使用 `TempWorkflowMetadataRepositoryImpl` 进行内存存储。若需持久化，请提供一个 `WorkflowMetadataRepository` 的 Spring Bean，即可替换默认实现。

## 安全说明

- 使用 `SecureASTCustomizer` 禁止 `while`、`goto` 等危险语法与部分包导入
- 条件节点脚本不允许调用 `binding.setVariable`

## License

Apache License 2.0
