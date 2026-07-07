# OpenAPI CRUD Skill - AI 执行手册（中文）

本文件是面向 AI 编码助手的可执行手册，用于在本仓库中按统一标准实现 CRUD 资源。

## 1) 角色与目标

你需要基于 OpenAPI-first 流程实现一个新的 CRUD 资源，并满足生产可用要求：

- OpenAPI 契约更新
- 生成 DTO/API 边界代码
- 轻量 Adapter 层（仅编排与权限）
- 基于通用 CRUD 基座的 Service 实现
- RBAC + ownership 权限控制
- DB 写入审计可追踪
- 集成测试与鉴权测试覆盖

## 2) 仓库锚点（必须遵循）

- OpenAPI 规范：`src/main/resources/swagger/server/sample.yaml`
- 通用 CRUD 基座：`src/main/java/com/xuxiaoye/api/client/CRUDDbClient.java`
- DB 查询/异常基座：`src/main/java/com/xuxiaoye/api/client/BaseDbClient.java`
- 统一响应壳：`src/main/java/com/xuxiaoye/api/resp/AppResponse.java`
- 全局异常处理：`src/main/java/com/xuxiaoye/api/common/exceptions/GlobalExceptionHandler.java`
- 安全过滤器：`src/main/java/com/xuxiaoye/api/interceptors/JWTAuthenticationFilter.java`
- 权限实现：`src/main/java/com/xuxiaoye/api/services/PermissionServiceImpl.java`
- 审计拦截器：`src/main/java/com/xuxiaoye/api/interceptors/TableAuditLogInterceptor.java`
- Service 装配：`src/main/java/com/xuxiaoye/api/conf/ServiceConfig.java`
- Adapter 装配：`src/main/java/com/xuxiaoye/api/conf/AdapterConfig.java`

## 3) 必填输入契约（来自用户/任务）

编码前先收集以下输入：

```yaml
resourceName: string
resourceNamePlural: string
entityIdType: string
apiBasePath: string
fields:
  - name: string
    type: string
    required: boolean
    constraints: string
validationRules:
  - string
permissions:
  create: string
  read: string
  search: string
  update: string
  delete: string
  export: string
  import: string
ownershipEnabled: boolean
features:
  search: boolean
  pagination: boolean
  sorting: boolean
  import: boolean
  export: boolean
```

如果关键输入缺失，先提出聚焦问题，再开始实现。

## 4) 不可违背规则

- 先契约，后实现。
- 不要手写应由 OpenAPI 生成的 API DTO。
- Adapter 层保持轻量，只做接入、权限、响应转换。
- Service 层优先复用 `CRUDDbClient`，仅覆盖业务差异化 Hook。
- DTO 与 DB Entity 映射必须显式、可审查。
- 错误处理遵循仓库统一的 `AppResponse/Exception` 语义。
- 测试至少覆盖：成功路径、校验失败、鉴权失败。

## 5) 执行流程（A-I）

### Step A - OpenAPI 契约

1. 在 `sample.yaml` 增加/调整资源路径。
2. 定义 create/update/read/search 的 schema。
3. 补充成功与错误示例。
4. `operationId` 命名风格与现有代码一致。

### Step B - 生成边界代码

1. 执行编译/生成流程。
2. 确认生成的 DTO 与 API 接口已刷新。

### Step C - 数据层

1. 新增 `DBEntity<IdType>` 子类。
2. 新增 `BaseMapper<Entity>` 子类。
3. 新增 `ServiceImpl<Mapper, Entity>` DB Service。

### Step D - 映射层

1. 新增 DTO <-> Entity 映射器。
2. 提供 list 批量转换方法。

### Step E - 业务层

1. 新增 service interface，沿用项目 `Service<...>` 风格。
2. 通过 `CRUDDbClient<...>` 实现 service。
3. 仅覆盖必要 Hook：
   - `validate(...)`
   - `buildQuery(...)`
   - import 行映射
   - export 行写入
   - merge/patch 差异逻辑

### Step F - Adapter 层

1. 实现生成的 delegate。
2. 按端点添加 `@PreAuthorize`。
3. 统一将 `AppResponse` 转为 `ResponseEntity`。

### Step G - 配置装配

1. 按现有风格更新 `ServiceConfig`。
2. 按现有风格更新 `AdapterConfig`。

### Step H - 测试覆盖

1. CRUD + search 集成测试。
2. 角色/权限放行与拒绝测试。
3. 校验失败、not-found 等负向用例。

### Step I - 质量门禁

1. 确认生成物与源码同步。
2. 本地测试通过。
3. 覆盖率门禁满足阈值策略。

## 6) AI 输出格式（强制）

完成时按以下顺序输出：

1. `Checklist`（A-I 是否完成）
2. `Changed files`（每个文件一行原因）
3. `Risk notes`
4. `Verification commands`
5. `Next options`（2-3 条编号建议）

## 7) 验证命令模板

```bash
mvn clean compile
mvn test
mvn jacoco:check
```

## 8) 可直接粘贴的 Prompt

```text
请严格遵循 docs/SKILL_OPENAPI_CRUD_AI_ZH.md。
在本仓库中按 OpenAPI-first 流程实现一个新资源。

输入：
- resourceName: <RESOURCE>
- resourceNamePlural: <RESOURCE_PLURAL>
- entityIdType: <ID_TYPE>
- apiBasePath: <API_BASE_PATH>
- fields: <FIELDS>
- validationRules: <RULES>
- permissions: <PERMISSIONS>
- ownershipEnabled: <true|false>
- features: <FEATURE_FLAGS>

要求：
- 先改 OpenAPI 契约，再实现代码。
- 复用 CRUDDbClient，Adapter 保持轻量。
- 响应与异常遵循统一策略。
- 增加集成、鉴权、负向测试。
- 输出 checklist、改动文件、风险与验证命令。
```

## 9) 完成定义（DoD）

满足以下全部条件才算完成：

- OpenAPI 契约与生成物保持一致
- 端点行为符合权限模型
- 校验与异常路径有测试覆盖
- 未在生成边界外重复定义 DTO
- Service 逻辑主要体现为基座 Hook 覆写
- 提供可执行的构建/测试/覆盖率验证命令

## 10) 示例输入与期望输出

### 示例输入（Student）

```yaml
resourceName: Student
resourceNamePlural: Students
entityIdType: String
apiBasePath: /api/v1/students
fields:
  - name: id
    type: string
    required: false
    constraints: 服务端生成
  - name: name
    type: string
    required: true
    constraints: 非空, maxLength=100
  - name: age
    type: integer
    required: true
    constraints: min=1, max=150
  - name: height
    type: number
    required: false
    constraints: min=30, max=300
validationRules:
  - create/update 时 name 必填
  - age 必须在 1 到 150 之间
permissions:
  create: student:create
  read: student:get
  search: student:search
  update: student:update
  delete: student:delete
  export: student:export
  import: student:import
ownershipEnabled: true
features:
  search: true
  pagination: true
  sorting: true
  import: true
  export: true
```

### 期望 AI 输出风格（简版）

```text
Checklist
- [x] Step A OpenAPI 契约已更新
- [x] Step B 生成边界已刷新
- [x] Step C 数据层已新增
- [x] Step D 映射层已新增
- [x] Step E Service 已基于 CRUDDbClient 实现
- [x] Step F Adapter 与 @PreAuthorize 已完成
- [x] Step G Bean 装配已更新
- [x] Step H 测试已补齐
- [x] Step I 质量检查已执行

Changed files
- src/main/resources/swagger/server/sample.yaml (新增 Student 路径与 schema)
- src/main/java/.../StudentServiceImpl.java (校验与查询 Hook)
- src/main/java/.../StudentAdapter.java (delegate 与权限注解)
- src/test/java/.../StudentAdapterTest.java (CRUD/search 集成测试)
- src/test/java/.../StudentAdapterAuthTest.java (权限矩阵)

Risk notes
- AuthorizationDeniedException 建议映射为 403。
- import 列顺序必须与 mapper 保持一致。

Verification commands
mvn clean compile
mvn test
mvn jacoco:check

Next options
1) 追加 curl 合约示例。
2) 增加 import/export 夹具文件。
3) 增加 ownership 专项用例。
```

### 常见失败模式（避免）

- 在 Adapter 写业务逻辑，导致分层污染
- 手写 OpenAPI 已生成的 DTO
- 绕过 `AppResponse` 造成状态码语义不一致
- 缺少 401/403 负向鉴权测试
- 开启 import/export 后未覆盖对应测试路径

