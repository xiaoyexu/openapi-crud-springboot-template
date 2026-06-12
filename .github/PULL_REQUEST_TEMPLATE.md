## Summary

- What problem does this PR solve?
- Why is this approach chosen?

## Scope

- [ ] Bug fix
- [ ] Refactor
- [ ] Feature
- [ ] Documentation
- [ ] Test only

## Key Changes

- 
- 
- 

## Error Handling Checklist (Required)

Reference: `docs/ERROR_HANDLING_GUIDE.md`

- [ ] Business-expected failures are returned via `AppResponse.failWithStatus(...)`
- [ ] System/infrastructure failures are not silently downgraded to business failures
- [ ] No unnecessary mixing of exception flow and `AppResponse` flow in the same logic path
- [ ] Service chaining uses `ifOk()` / `ifOkElse()` where it improves clarity
- [ ] Error `code` is stable and consumable by clients (not message-only)
- [ ] Logging level is appropriate (`warn/info` for business failures, `error` + stack for system failures)
- [ ] Transaction behavior is correct (business failures do not accidentally rollback; system failures rollback as expected)

## API / Contract Impact

- [ ] No API change
- [ ] Backward-compatible API change
- [ ] Breaking API change

If API changed, describe:

- Endpoint(s):
- Request/Response fields:
- Status/error code changes:

## Testing

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Existing tests pass

Executed locally:

```bash
# example
mvn -q -DskipTests compile
mvn -q test
```

## Risk and Rollback

- Risk level: [Low / Medium / High]
- Main risks:
  - 
- Rollback plan:
  - 

## Reviewer Focus

Please pay extra attention to:

- 
- 

## Checklist Before Merge

- [ ] Docs updated when behavior changed
- [ ] No sensitive data/log leakage introduced
- [ ] Error handling follows `docs/ERROR_HANDLING_GUIDE.md`
- [ ] Naming and code style follow project conventions

