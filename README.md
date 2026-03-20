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
- 调试支持 SSE 流式推送，逐节点实时反馈执行结果
- 默认内存仓库，支持替换为自定义持久化实现
- Groovy 安全限制与 AST 校验，降低不可信脚本风险

## 快速开始

1. 引入依赖（Maven）

```xml
<dependency>
  <groupId>io.github.code-visual</groupId>
  <artifactId>visual-flowengine-spring-boot-starter</artifactId>
  <version>1.2.0</version>
</dependency>
```

2. 确保你的应用是 Web 应用（需引入 `spring-boot-starter-web`），然后启动应用
3. 打开默认 UI 地址：`http://localhost:8080/visualflow/index.html`

## 配置项

所有配置均以 `visual.flow` 为前缀：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `basePath` | `/visualflow` | 所有端点的基础路径（UI + API） |
| `enableApi` | `true` | 是否启用内置 REST API |
| `enableUi` | `true` | 是否启用 Web UI |
| `enableAST` | `true` | 是否启用 Groovy AST 安全校验 |
| `enableCacheSource` | `true` | 是否缓存 Groovy 编译结果 |

API 路径默认从 `basePath` 派生，也可单独覆盖：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `workflowsApiPath` | `{basePath}/api/workflows` | 流程 CRUD |
| `executeApiPath` | `{basePath}/api/workflows/execute` | 执行流程 |
| `debugApiPath` | `{basePath}/api/workflows/debug` | 调试流程 |
| `compileApiPath` | `{basePath}/api/script/compile` | Groovy 编译诊断 |

示例配置：

```yaml
visual:
  flow:
    basePath: /visualflow
    enableApi: true
    enableUi: true
    enableCacheSource: true
```

## API 说明

所有路径默认以 `/visualflow` 为前缀（可通过 `basePath` 修改）：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/config` | 获取前端配置 |
| `GET` | `/api/workflows` | 获取流程列表 |
| `POST` | `/api/workflows` | 创建流程 |
| `GET` | `/api/workflows/{id}` | 获取流程详情 |
| `PUT` | `/api/workflows/{id}` | 更新流程 |
| `DELETE` | `/api/workflows/{id}` | 删除流程 |
| `POST` | `/api/workflows/execute` | 执行流程 |
| `POST` | `/api/workflows/debug` | 调试流程（同步返回） |
| `POST` | `/api/workflows/debug/stream` | 调试流程（SSE 流式推送） |
| `POST` | `/api/script/compile` | Groovy 语法诊断 |

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

默认使用内存仓库存储流程。若需持久化，提供一个 `WorkflowRepository` 的 Spring Bean 即可替换默认实现。

## 安全说明

- 使用 `SecureASTCustomizer` 禁止 `while`、`goto` 等危险语法与部分包导入
- 条件节点脚本不允许调用 `binding.setVariable`

## License

Apache License 2.0
