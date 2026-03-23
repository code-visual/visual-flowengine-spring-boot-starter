# Visual Flow 首页可打开但静态资源 404 问题分析

## 问题现象

访问下面的地址时，首页 HTML 可以正常返回：

`http://localhost:8081/feature-flag/visualflow/index.html`

但是页面继续加载静态资源时出现 404，例如：

- `/visualflow/assets/logo-BGicvN48.jpeg`
- `/visualflow/assets/TreeChart-BZqzVIEE.js`
- `/visualflow/assets/TreeChart-nPlAcHF5.css`
- `/visualflow/assets/monaco-mIzVRKHB.css`
- `/visualflow/assets/monaco-nTlCWY6K.js`

浏览器报错的本质是：

- 首页请求路径带了应用上下文前缀 `/feature-flag`
- 后续静态资源请求却丢掉了 `/feature-flag`
- 因此浏览器访问到了错误地址 `http://localhost:8081/visualflow/...`
- 实际正确地址应为 `http://localhost:8081/feature-flag/visualflow/...`

## 根因结论

`visual-flowengine-spring-boot-starter:1.2.3` 已发布产物中的前端入口 `index.html` 使用了写死的绝对路径：

- `/visualflow/favicon.ico`
- `/visualflow/assets/index-CTXPycBg.js`
- `/visualflow/assets/monaco-nTlCWY6K.js`
- `/visualflow/assets/monaco-mIzVRKHB.css`

这类以 `/` 开头的绝对路径会从站点根路径开始解析，不会自动带上 Spring Boot 应用的 `context-path`。  
所以当应用不是部署在根路径，而是部署在 `/feature-flag` 下时，首页虽然能通过 `/feature-flag/visualflow/index.html` 打开，但页面内引用的资源仍会请求到 `/visualflow/...`，从而导致 404。

## 已确认的证据

### 1. 已发布 starter 包内的 `index.html` 使用了绝对路径

从本地 Maven 仓库中的 jar 包读取到的内容如下：

- jar 包路径：
  `D:/repo/io/github/code-visual/visual-flowengine-spring-boot-starter/1.2.3/visual-flowengine-spring-boot-starter-1.2.3.jar`

- 其中包含资源目录：
  `META-INF/resources/visualflow/`

- 其中入口文件内容为：

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <link rel="icon" href="/visualflow/favicon.ico"/>
    <title>Visual Flow Engine</title>
  <script type="module" crossorigin src="/visualflow/assets/index-CTXPycBg.js"></script>
  <link rel="modulepreload" crossorigin href="/visualflow/assets/monaco-nTlCWY6K.js">
  <link rel="stylesheet" crossorigin href="/visualflow/assets/monaco-mIzVRKHB.css">
  <link rel="stylesheet" crossorigin href="/visualflow/assets/index-B_ocPwZU.css">
</head>
<body>
<div id="root"></div>
</body>
</html>
```

这已经足够说明 404 不是后端 Controller 或鉴权拦截导致的，而是前端打包产物中的资源前缀错误。

### 2. starter 包内资源文件实际上存在

jar 中实际已经包含这些文件，例如：

- `META-INF/resources/visualflow/assets/logo-BGicvN48.jpeg`
- `META-INF/resources/visualflow/assets/TreeChart-BZqzVIEE.js`
- `META-INF/resources/visualflow/assets/TreeChart-nPlAcHF5.css`
- `META-INF/resources/visualflow/assets/monaco-mIzVRKHB.css`
- `META-INF/resources/visualflow/assets/monaco-nTlCWY6K.js`

说明问题不是“文件没打进去”，而是“浏览器请求错了地址”。

### 3. 当前前端源码已经考虑了 context-path，但发布产物和源码不一致

当前前端源码目录：

`E:/code/visual-flowengine-front`

已检查到以下配置：

- `vite.config.ts`
  - `base: env.VITE_BASE_PATH || '/'`

- `.env.production`
  - `VITE_BASE_PATH=./`

这意味着如果按当前源码的 production 配置构建，理论上应该生成相对路径资源引用，而不是 `/visualflow/...` 这种绝对路径。

同时，运行时 API 配置代码也已经考虑了 context-path：

- `src/network/config.ts`
  - 会从 `window.location.pathname` 中识别 `/visualflow`
  - 当页面地址是 `/feature-flag/visualflow/index.html` 时，会推导出 base path 为 `/feature-flag/visualflow`

因此可以判断：

- API 路径这块源码已经做了兼容
- 但 `1.2.3` 发布包中的静态资源入口文件不是按这套正确配置产出的

## 高概率原因

`visual-flowengine-spring-boot-starter:1.2.3` 的发布包很可能存在以下其中一种情况：

1. 发布时使用了旧版本前端代码，旧代码把 Vite `base` 配成了 `/visualflow/`
2. 发布时使用了错误的环境变量，导致 `VITE_BASE_PATH` 不是 `./`
3. 发布时没有按当前源码的标准 production 构建流程生成最终静态文件
4. 发布包中的前端资源与当前 `E:/code/visual-flowengine-front` 代码并非同一版

## 本地前端源码复核结果

已对当前前端源码目录执行检查：

`E:/code/visual-flowengine-front`

确认到以下事实：

### 1. 当前源码的生产环境构建配置是正确方向

- `vite.config.ts`
  - `base: env.VITE_BASE_PATH || '/'`

- `.env.production`
  - `VITE_BASE_PATH=./`

这意味着生产构建时，Vite 应当输出相对路径资源引用，而不是 `/visualflow/...` 绝对路径。

### 2. 当前源码本地构建产物已验证通过

已在本地执行：

`npm run build`

构建成功后，`dist/index.html` 的实际内容为：

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <link rel="icon" href="./favicon.ico"/>
    <title>Visual Flow Engine</title>
  <script type="module" crossorigin src="./assets/index-DU18OYry.js"></script>
  <link rel="modulepreload" crossorigin href="./assets/monaco-CQTYtDci.js">
  <link rel="stylesheet" crossorigin href="./assets/monaco-jjPWvMqx.css">
  <link rel="stylesheet" crossorigin href="./assets/index-B_ocPwZU.css">
</head>
<body>
<div id="root"></div>
</body>
</html>
```

这说明：

- 当前前端源码打包后已经不会生成 `/visualflow/assets/...`
- 当前前端源码本身并不是 404 的直接来源
- 问题出在 `1.2.3` starter 发布物与当前源码构建结果不一致

### 3. 前端仓库自己的变更记录也说明这个问题本来已经修过

`E:/code/visual-flowengine-front/CHANGELOG.md` 中已经明确记录：

- 旧配置：`VITE_BASE_PATH=/visualflow/`
- 新配置：`VITE_BASE_PATH=./`
- 目标：修复 starter 在 `context-path` 场景下的静态资源与 API 404 问题

因此当前现象高度说明：

- 要么 `1.2.3` 发版时没有使用这份前端源码重新构建
- 要么 starter 打包时仍然拷贝了旧的静态资源
- 要么 `1.2.3` 的 jar 来自错误分支或历史构建产物

## 为什么首页能开但资源会 404

这是两个不同阶段：

1. Spring Boot 返回 `index.html`
   - 请求地址是 `/feature-flag/visualflow/index.html`
   - 服务端可以正常找到 `META-INF/resources/visualflow/index.html`

2. 浏览器解析 `index.html` 并继续请求里面的 JS/CSS/图片
   - 由于 HTML 里写的是 `/visualflow/assets/...`
   - 浏览器会从站点根路径请求资源
   - 不会自动补上 `/feature-flag`
   - 所以请求落到错误地址，出现 404

## 与项目当前配置的关系

当前项目中的：

`web/src/main/resources/application-local.yml`

包含：

- `synnex.security.authorization.ignored.additional: /visualflow/**`

这只是安全放行配置，只决定是否跳过鉴权，不决定静态资源 URL 如何生成。  
因此它不是本次 404 的根因。

## 修复建议

### 方案 1：修复前端构建并重新发布 starter

这是根治方案。

需要确认发布 `visual-flowengine-spring-boot-starter` 时：

- Vite 的 `base` 最终为 `./`，或其他可正确兼容 context-path 的相对路径
- 打包产出的 `dist/index.html` 中资源引用不再是 `/visualflow/assets/...`
- 重新发布新的 starter 版本，并替换项目中的 `1.2.3`

验证标准：

- 新 jar 中 `META-INF/resources/visualflow/index.html` 的资源链接应类似：
  - `./assets/index-xxxx.js`
  - `./assets/index-xxxx.css`

## 推荐的重新打包流程

下面流程的目标是生成“可在 `/feature-flag/visualflow/index.html` 下正常运行”的 starter 静态资源。

### 第 1 步：在前端项目中重新构建正式产物

前端源码目录：

`E:/code/visual-flowengine-front`

执行：

```bash
npm install
npm run build
```

本次排查已验证 `npm run build` 可以成功，并且构建后的 `dist/index.html` 已经是相对路径。

### 第 2 步：确认构建结果必须满足下面标准

构建后的：

`E:/code/visual-flowengine-front/dist/index.html`

必须满足：

- `favicon` 使用 `./favicon.ico`
- 主 JS 使用 `./assets/...`
- CSS 使用 `./assets/...`
- 不能再出现 `/visualflow/assets/...`

如出现绝对路径，则说明构建环境变量不对，不应继续打包 starter。

### 第 3 步：将前端 dist 覆盖到 starter 的资源目录

starter 工程中应有类似资源目录：

`src/main/resources/META-INF/resources/visualflow/`

需要把前端 `dist` 下内容覆盖进去，通常包括：

- `index.html`
- `favicon.ico`
- `assets/`

注意是“复制 dist 目录下的内容进入 `visualflow/`”，而不是把整个 `dist` 目录多嵌一层。

正确目标结构应类似：

```text
src/main/resources/META-INF/resources/visualflow/index.html
src/main/resources/META-INF/resources/visualflow/favicon.ico
src/main/resources/META-INF/resources/visualflow/assets/...
```

### 第 4 步：重新打 starter jar

在 starter 工程目录执行 Maven 打包，例如：

```bash
mvn clean install
```

如果发布到私服，再按你们现有流程执行 deploy。

### 第 5 步：发布前先检查 jar 内的 index.html

打包完成后，不要直接发布，先检查 jar 内：

- `META-INF/resources/visualflow/index.html`

必须仍然是：

- `./favicon.ico`
- `./assets/...`

而不能退回成：

- `/visualflow/favicon.ico`
- `/visualflow/assets/...`

如果 jar 内再次出现绝对路径，说明 starter 发布流程中仍有旧静态文件覆盖新文件的问题。

## 对 starter 发布方的直接结论

如果要一句话说明给发布方，可以直接用下面这段：

`visual-flowengine-spring-boot-starter:1.2.3` 包内的前端静态资源不是按当前 `E:/code/visual-flowengine-front` 的生产构建结果打进去的。当前源码本地 `npm run build` 后生成的 `dist/index.html` 已经是 `./assets/...` 相对路径，能够兼容 `/feature-flag` 这类 context-path；但已发布 jar 中仍是 `/visualflow/assets/...` 绝对路径，说明 starter 发布流程拷入了旧产物或使用了错误构建输入。应重新用当前源码构建前端 dist，覆盖到 `META-INF/resources/visualflow/` 后再重新打包发布。`

## 最终结论

本次问题的直接根因是：

`visual-flowengine-spring-boot-starter:1.2.3` 内置前端产物使用了写死的绝对静态资源路径 `/visualflow/...`，导致应用部署在 `/feature-flag` 这类非根上下文路径下时，浏览器请求资源地址错误并出现 404。

当前 `E:/code/visual-flowengine-front` 中的源码已经表现出“准备兼容 context-path”的实现思路，但该思路没有体现在已发布的 `1.2.3` jar 产物中。  
因此需要优先核对 starter 的实际发布流程和前端构建输入，重新产出正确的静态资源包。

## 附加问题记录：流式调试下条件节点错误状态丢失

### 问题现象

当工作流中的 `Condition` 节点脚本没有返回 `true` 或 `false`，而是返回了：

- `null`
- 非 `Boolean` 类型
- 或者脚本最后一个表达式不是布尔值

后端执行逻辑会把该节点判定为错误，并中断后续执行。  
但是在前端“流式调试”界面中，用户可能看不到该条件节点最终被标记为 `Error`，界面上看起来像是节点正常执行过，只是流程没有继续向下走。

### 后端实际执行逻辑

`Condition` 节点执行代码位于：

`D:/project/visual-flowengine-spring-boot-starter/src/main/java/io/github/code/visual/workflow/WorkflowEngine.java`

核心逻辑如下：

```java
case Condition: {
    WorkflowTaskLog condLog = logScriptExecution(script, binding, logList, this::executeScript);
    if (onNodeComplete != null) onNodeComplete.accept(condLog);
    if (condLog.getScriptRunStatus() == ScriptRunStatus.Error) {
        return false;
    }
    Object result = condLog.getScriptRunResult();
    if (!(result instanceof Boolean)) {
        condLog.setScriptRunStatus(ScriptRunStatus.Error);
        condLog.setScriptRunError("Condition node must return a Boolean ...");
        return false;
    }
    if ((Boolean) result) {
        return recurseChildren(...);
    }
    return true;
}
```

这说明：

- 返回 `true` 时，会继续执行子节点
- 返回 `false` 时，不执行子节点，但不算错误
- 返回非 `Boolean` 或 `null` 时，会把 `condLog` 改成 `Error`，并终止后续执行

### 为什么流式调试看不到

问题在于 `onNodeComplete.accept(condLog)` 的调用时机过早。

当前顺序是：

1. 先执行脚本，生成 `condLog`
2. 立即通过 `onNodeComplete` 把这条日志推给 SSE
3. 然后才校验 `scriptRunResult` 是否为 `Boolean`
4. 如果不是 `Boolean`，再把同一个 `condLog` 改成 `Error`

但是这时 SSE 已经把旧状态发给前端了，后端没有再次发送“修正后的条件节点日志”。

流式接口位于：

`D:/project/visual-flowengine-spring-boot-starter/src/main/java/io/github/code/visual/web/WorkflowController.java`

相关逻辑为：

```java
engine.debug(debugRequest, log -> {
    if (aborted.get()) return;
    emitter.send(SseEmitter.event()
            .name("node")
            .data(log));
});
```

也就是说，`onNodeComplete` 的作用就是把每个节点执行结果实时发给前端。

### 前端为什么不会自动纠正

前端流式调试代码位于：

`E:/code/visual-flowengine-front/src/network/api.ts`

当前逻辑只是把每条 SSE `node` 事件直接加入数组：

```typescript
const log: WorkflowTaskLog = JSON.parse(data);
allLogs.push(log);
onNode(log);
```

并没有做：

- 同一 `scriptId` 的日志覆盖更新
- 流式结束后再补拉一次最终 debug 结果

因此一旦后端先发送了“旧状态”，前端最终展示的就是旧状态。

### 影响

这会导致：

- 后端真实执行结果与前端流式展示不一致
- 条件节点返回非布尔值时，前端可能看不到明确的 `Error`
- 用户只能观察到“流程没往下走”，但界面上缺少直接错误提示

### 建议修复

优先建议修后端发送时机。

方案 1：后端修复，推荐

- 将 `Condition` 分支中的 `onNodeComplete.accept(condLog)` 挪到布尔类型校验之后
- 确保发给前端的是最终状态，而不是中间状态

方案 2：前端兜底

- 流式执行结束后，再补调一次非流式 `/api/workflows/debug`
- 用最终日志覆盖当前界面展示

这个方案可以缓解显示问题，但不是根因修复。

### 一句话结论

当前“流式调试看不到条件节点最终错误状态”的根因是：  
`WorkflowEngine` 在 `Condition` 节点布尔校验之前就通过 `onNodeComplete` 把日志推送给了前端，而前端又没有对同一节点做二次覆盖更新，导致界面停留在旧状态。
