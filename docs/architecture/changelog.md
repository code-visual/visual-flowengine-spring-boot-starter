# Visual FlowEngine — 变更日志

> 最后更新：2026-04-10
> 数据来源：`git log --oneline --all`

---

## 版本 1.2.8（当前版本）

> **注意**：下方 1.3.0 新特性在未升级版本号的情况下合入，实际 pom.xml 仍为 1.2.8

**HEAD Commit：** `92cf7654e56e1f079557ce08023dd27e6fba6a83`

| Commit | 说明 |
|--------|------|
| `92cf765` | **feat**: 新增 `WorkflowExecutionEvent` + `WorkflowExecutionEventListener` 回调机制（for Hub 集成）；`WorkflowEngine` 新增 `executeAndNotify()` 统一执行入口，在 finally 中同时触发旧版和新版回调；`AutoConfiguration` 支持注入多个 `WorkflowExecutionEventListener` Bean |

**之前 HEAD Commit：** `fec396650ad16ec8422a51f8e2f3bc2f4099e475`

| Commit | 说明 |
|--------|------|
| `fec3966` | **fix**: 修正脚本超时检测逻辑（条件取反 Bug），版本升至 1.2.8 |
| `1b6c8da` | **fix**: 修复全屏退出图标不可见问题，以相对路径重建前端（v1.2.7）|

---

## 版本 1.2.6

**HEAD Commit：** `d36a56670c549c228b653ab8fd3dfb24b9c775af`

| Commit | 说明 |
|--------|------|
| `d36a566` | chore: 前端重建 - snippet 面板跟随模态框拖动 |
| `e8d9023` | chore: 前端重建 - 结构校验仅从 Start 节点触发 |
| `09ed858` | **fix**: 仅当 root 为 Start 类型时执行结构校验，修复调试子树报错问题 |
| `9e80fbd` | chore: 前端重建 - 热力图错误颜色、模态框拖拽、toast 持续时长 |
| `6a7637c` | chore: 前端重建 - toast 按类型设置持续时长 |
| `9b22a6d` | feat: 校验叶节点必须为 End 类型，前端重建 |
| `9971db8` | chore: 前端重建 - 校验按钮移至停靠栏 |
| `09998f8` | **1.2.6 正式发布**: Condition if/else-if 执行逻辑、结构校验、所有中文消息英文化、前端重建 |
| `be68b2c` | i18n: 将 WorkflowEngine 中所有中文消息翻译为英文 |
| `a109da1` | feat: 新增 `ExecutionSignal` 枚举，实现工作流节点结构校验 |

---

## 版本 1.2.3

| Commit | 说明 |
|--------|------|
| `3db1ae7` | **1.2.3**: 编译功能完善、静态分析、前端变量监视面板可展开查看完整值 |
| `461650e` | 优化: `Diagnostic` 增加 severity 字段，`CompilationUnit` 收集 warnings，前端资源更新 |
| `9f84102` | feat: 断点调试——执行到 `breakpointNodeId` 后停止递归 |

---

## 版本 1.2.1

| Commit | 说明 |
|--------|------|
| `0369ecf` | **1.2.1**: Condition 节点返回值校验、前端 anchor 选择器修复 |
| `8b6786d` | 优化: Condition 节点返回值类型校验，非 Boolean 时报错并提示实际类型 |

---

## 版本 1.2.0

| Commit | 说明 |
|--------|------|
| `5727f6a` | **1.2.0**: 改用 `groovy-all` 依赖，新增 `durationMs` 耗时字段，完整异常堆栈，移除 favicon 映射和全局异常处理器 |
| `0bcb325` | 前端 1.2.0: 全局搜索、路径回放、节点模板、批量选择、变量依赖、Diff 对比、Dark Mode |
| `3434f24` | 优化: Binding 快照始终输出合法 JSON，新增单节点脚本执行超时（默认 30 秒） |
| `ceb41d1` | docs: README 更新为当前代码实际配置和 API 路径 |
| `62e8c71` | chore: 移除误提交的 .dmp 文件，添加到 .gitignore |
| `09afa81` | **修复**: SSE 端点去除双重序列化，增加客户端断开保护 |
| `e26da63` | feat: 新增 Debug SSE 流式端点，支持逐节点实时推送执行结果 |
| `fab43cb` | **重构**: AST 安全检查从 Groovy 改写为 Java，解决跨 Groovy 版本兼容问题 |
| `38e0c7e` | feat: 版本升级至 1.2.0，调整依赖 |

---

## 版本 1.1.1

| Commit | 说明 |
|--------|------|
| `60d7c78` | **1.1.1**: 添加 favicon 并映射到根路径 |

---

## 版本 1.1.0

| Commit | 说明 |
|--------|------|
| `81e7ab0` | **1.1.0**: 修复前端 mock 模式在生产构建中未关闭的问题 |

---

## 版本 1.0.9

| Commit | 说明 |
|--------|------|
| `9fcb413` | **1.0.9**: 升级依赖版本，重新打包前端修复静态资源路径 |
| `adecfc1` | 1.0.9 依赖调整 |

---

## 架构重构（1.0.9 之前）

| Commit | 说明 |
|--------|------|
| `d7c0bc6` | **重构**: starter 架构重新设计 |
| `c66daf3` | refactor: 优化工作流执行引擎 |
| `d15c081` | docs: 重写 README |

---

## 版本 1.0.8

| Commit | 说明 |
|--------|------|
| `5912579` | **1.0.8**: 后端增加日志接口回调（`WorkflowExecutionListener`），前端体验优化 |
| `f35456e` | 1.0.8: 前端体验优化 |
| `5f0703d` | 增加日志回调 + 代码格式化 |
| `5b1a52c` | 优化前端体验：模态框可以拖动 |
| `b091428` | 新增 `enableCacheSource` 配置项 |

---

## 版本 1.0.5 ~ 1.0.6

| Commit | 说明 |
|--------|------|
| `bf81af6` | **1.0.5**: 深度增强 AST 安全性校验、优化菜单栏、增加官网跳转、画布自适应窗口大小 |
| `d55e27a` | 深度增强安全性校验 |
| `73ffc81` | 能序列化的内容才能被返回；增加安全性校验；增加接口默认参数名（Spring Boot 3 兼容）|
| `2bc9103` | 用 AST 去判断条件节点 |
| `4c95fe3` | 条件节点不能赋值（AST 禁止 `binding.setVariable()`）|
| `df41ad2` | 项目结构优化，解析脚本提供缓存选择 |
| `6660ca8` | 增加编译缓存 |

---

## 早期版本（初始化 ~ 1.0.x）

| Commit | 说明 |
|--------|------|
| `e689718` | 更新 API path 逻辑，静态资源动态加载前缀 |
| `4af80f6` | 静态编译需要跳过动态 DSL |
| `c82b775` | 使用递归实现脚本执行 |
| `45c7174` | 内存持久化，脚本代码由使用者自行持久化 |
| `80f7618` | 修复 SPI 机制不生效问题（路径放错）|
| `87edb2f` | 找到生效机制，禁止 while 循环后会报错 |
| `89d14ff` | groovy-sandbox 不能用在 Web 环境（改用 SecureASTCustomizer）|
| `7e55ed9` | init：项目初始化 |

---

## 提交统计概览

- 最早提交：初始化（`7e55ed9`）
- 最新提交：`fec3966`（2026-03-28 超时检测 Bug 修复，版本 1.2.8）
- 总 commit 数：200+
- 活跃开发阶段：2023 年 Q3 ~ 至今
- 主要开发者：levi (levi.lideng@gmail.com)
