# 前端 2.11 迁移说明

日期：2026-06-03

## 结论

前端必须纳入 2.11 迁移范围。

之前说“前端暂不提交”，不是说前端不迁移，也不是说前端源码不重要。准确含义是：当前前端工作区剩下的 3 个未提交 diff 主要是本机运行配置和生成文件，不应该当作业务功能迁移成果提交。

前端业务恢复本身已经在 `jetlinks-ui-vue-2.1.1` 的 `lsx-ui-2.1.1-recovery` 分支上形成了提交，它们才是后续迁到 2.11 的前端基线。

## 当前前端基线

仓库：

```text
F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1
```

分支：

```text
lsx-ui-2.1.1-recovery
```

当前已提交的关键恢复提交：

```text
e0234b59 feat: complete customer device recovery
ebd55c97 feat: restore customer device jobs
7c3041c2 feat: restore customer reweb frontend
124c6d0b docs: record customer page recovery
32dc73d3 feat: restore customer management pages
a9c62141 docs: record lsx ui recovery progress
86169dd1 feat: add lsx backend api wrappers
40ae9959 docs: plan lsx ui 2.1.1 recovery
```

这些提交说明前端不是空白状态。客户、客户设备、设备任务、REWEB 相关页面和 API wrapper 已经作为 2.1.1 恢复基线保留下来。

## 为什么当前前端 diff 不建议直接提交

当前前端工作区还有这些未提交文件：

```text
M public/js/liveplayer-lib.min.js
M src/auto-imports.d.ts
M vite.config.ts
```

它们的性质不同：

| 文件 | 当前变化 | 是否作为迁移提交 | 原因 |
| --- | --- | --- | --- |
| `vite.config.ts` | 代理改到 `127.0.0.1:8848`，允许 `agent.wugee.net.cn` | 不建议直接提交 | 这是本机联调配置，不是业务功能。后续应改成 `.env.local` 或 2.11 独立开发配置 |
| `public/js/liveplayer-lib.min.js` | 压缩产物 1 行变化 | 不提交 | 这是生成/第三方压缩文件，当前没有证据说明它是 LSX 私有功能 |
| `src/auto-imports.d.ts` | 没有实际 diff，只有换行提示 | 不提交 | 这是自动生成文件/换行噪声 |

因此，不提交的是“当前残留 diff”，不是“不提交前端恢复成果”。

## 2.11 前端迁移目标

2.11 前端迁移不是把 2.1.1 前端目录整包复制过去，而是按功能模块迁移：

1. 客户管理页面
2. 客户设备列表、导入、导出、批量更新、定位、同步状态
3. 客户设备详情扩展字段
4. 设备任务页面和执行日志
5. REWEB 按钮、功能调用、打开 `*.reweb.wugee.net.cn`
6. 告警和场景中旧 jar 已确认的接口补充
7. 物联网卡页面只作为待确认项，不能当作完整功能直接迁移

## 推荐迁移路线

### 阶段 1：冻结 2.1.1 前端恢复基线

目标是保护现在已经恢复出来的前端证据。

执行内容：

- 保留 `lsx-ui-2.1.1-recovery` 分支。
- 确认已提交内容能构建。
- 不把 `vite.config.ts` 的本机代理、`liveplayer-lib.min.js`、`auto-imports.d.ts` 作为业务提交。
- 如需记录本机运行方式，写到文档或 `.env.local.example`，不要污染功能提交。

验证命令：

```powershell
git -C F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1 status --short --branch
pnpm.cmd --dir F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1 run build
```

### 阶段 2：建立独立 2.11 前端迁移分支

目标是在 2.11 前端上重接 LSX 页面，不破坏当前 2.1.1 试水环境。

建议：

```text
新分支：lsx-ui-migration-2.11
来源：现有 2.11 前端分支
开发端口：15173 或其它不与 5173 冲突的端口
后端目标：2.11 迁移后端端口，例如 18848
```

不要直接在 `jetlinks-ui-vue-2.1.1` 上升级到 2.11。这个目录应该继续作为旧前端恢复证据。

### 阶段 3：先迁 API，再迁页面

先在 2.11 前端中按 2.11 的请求封装风格重建 API：

- `/customer/**`
- `/customer/device/**`
- `/deviceJob/**`
- `/device/instance/{deviceId}/function/reweb`
- 告警、场景中确认缺失的接口

再迁页面：

- 先迁客户设备详情，因为它能和真实设备 `869624060285951` 闭环验证。
- 再迁客户设备列表、客户管理、设备任务。
- 最后迁告警、场景、物联网卡待确认部分。

### 阶段 4：每个模块单独验收和提交

每个前端模块都应该单独提交，提交粒度建议：

```text
feat(ui): migrate customer api wrappers to 2.11
feat(ui): migrate customer device detail
feat(ui): migrate reweb action
feat(ui): migrate customer device list
feat(ui): migrate device jobs
```

每个提交至少验证：

- `pnpm run build` 通过。
- 页面能打开。
- API 请求能打到 2.11 后端。
- 与后端字段一致。
- 对真实测试设备 `869624060285951` 能看到状态或字段变化。

## 2.11 迁移时需要重点对照的前端证据

旧编译前端：

```text
F:\project\other\jetlinks\html
F:\project\other\jetlinks\html\assets
```

2.1.1 恢复前端：

```text
F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1
```

前端恢复文档：

```text
F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1\docs\reconstruction\lsx-ui-2.1.1-recovery-plan.md
F:\project\other\jetlinks\jetlinks-ui-vue-2.1.1\docs\reconstruction\reweb-frontend-recovery-summary.md
```

前后端接口校验文档：

```text
F:\project\other\jetlinks\jetlinks-community\docs\reconstruction\frontend-backend-verification.md
```

## 迁移判断标准

一个前端模块只有满足以下条件，才算迁移到 2.11：

1. 旧编译前端或 2.1.1 恢复源码证明该功能存在。
2. 2.11 前端有对应页面、路由、API wrapper。
3. 2.11 后端有对应接口和字段。
4. 构建通过。
5. 页面真实调用 2.11 后端成功。
6. 对测试设备 `869624060285951` 能完成至少一条端到端验证。

只复制页面代码但后端接口不通，不算完成迁移。
