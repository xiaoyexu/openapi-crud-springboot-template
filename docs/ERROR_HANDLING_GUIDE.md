# Error Handling Guide

## 1. 目标
统一本项目错误处理策略，减少混用，提升可读性、可维护性和排障效率。

## 2. 总原则
1. **业务可预期失败**：优先使用 `AppResponse.failWithStatus(...)` 返回。
2. **系统不可恢复失败**：抛出异常，由全局异常处理器统一兜底。
3. **Service 间调用**：优先使用 `ifOk()` / `ifOkElse()` 链式风格，避免重复 `isOk()` 样板代码。
4. **日志分级**：业务失败 `warn/info`；系统异常 `error`（必须带异常栈）。

## 3. 分层约定

### 3.1 Service 层
- 业务校验失败（参数缺失、状态冲突、资源不存在等）
  - 返回 `AppResponse.failWithStatus(AppStatus.xxx(...))`
- 多步骤依赖调用
  - 使用 `ifOk()` 进行链式变换
- 技术故障（数据库连接、IO、序列化、框架异常）
  - 不要降级为普通业务失败；保留异常语义（由统一层处理）

### 3.2 BaseDbClient / 基础调用包装
- `handleDbCall(...)` 负责统一收口异常与事务行为。
- 建议策略：
  - 业务失败：返回 `AppResponse`，默认不回滚
  - 系统异常：记录 `error` + 回滚 + 返回统一失败状态

### 3.3 Adapter/Controller 层
- 不承载业务逻辑，仅做协议转换。
- `AppResponse` 统一映射 HTTP 响应。
- 未处理异常由 `GlobalExceptionHandler` 转换为标准错误响应。

## 4. 事务约定
- 业务失败默认不回滚（除非业务明确要求原子失败）。
- 系统异常默认回滚。

## 5. 错误码约定
- 每个业务失败应有稳定 `code`（面向程序）。
- `message` 用于人类可读（可国际化/可调整）。
- 不建议仅依赖 message 做前端分支。

## 6. 推荐与反例

### 6.1 推荐（链式）
```java
return this.searchInternal(request, pagination)
        .ifOk(this.getMapper()::mapPagedToPresent);
```

### 6.2 推荐（业务失败显式返回）
```java
if (StringUtils.isBlank(user.getAccountName())) {
    return AppResponse.failWithStatus(AppStatus.badRequest("Missing Account Name"));
}
```

### 6.3 反例（同场景混用两套机制）
```java
if (badCondition) {
    throw new RuntimeException("bad");
}
return AppResponse.failWithStatus(AppStatus.badRequest("bad"));
```

## 7. PR 评审清单
- [ ] 该失败是否属于业务可预期分支？若是，是否使用 `AppResponse.failWithStatus`。
- [ ] 该失败是否属于系统故障？若是，是否保留异常语义并记录堆栈。
- [ ] 是否避免了同一逻辑里无必要的“异常 + AppResponse”混用。
- [ ] Service 间串联是否优先使用 `ifOk()` / `ifOkElse()`。
- [ ] 错误 `code` 是否稳定、可被客户端消费。

## 8. 质量评分 Rubric（100 分）

| 维度 | 分值 | 评估点 |
|---|---:|---|
| 分层职责与边界 | 25 | 业务失败显式返回、系统故障异常兜底是否清晰 |
| 一致性与可读性 | 20 | 同类场景是否统一风格，样板代码是否可控 |
| 可观测性 | 20 | 是否有日志分级，系统异常是否保留堆栈 |
| 错误语义与契约 | 20 | 是否有稳定错误码，message 是否清晰 |
| 测试覆盖 | 15 | 业务失败、系统异常、边界映射是否覆盖 |

评级建议：
- 90-100：成熟
- 75-89：可用且健康
- 60-74：可运行但演进风险较高
- <60：需要优先治理

## 9. 落地顺序（建议）
1. 先统一新增代码遵循本规范，不强行一次性改全量存量。
2. 优先改高频路径：认证、导入导出、跨 Service 编排。
3. 每个迭代清理 1-2 个典型模块，逐步收敛历史风格。
