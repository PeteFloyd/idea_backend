# PR #19 Review 问题修复

## Workflow Steps
- [x] Step 0: Backend selection (claude)
- [x] Step 1: Requirement clarification
- [ ] Step 2: Skip (明确的修复任务)
- [ ] Step 3: Generate dev-plan.md
- [ ] Step 4: Execution
- [ ] Step 5: Coverage validation
- [ ] Step 6: Completion summary

## 待修复问题
1. Entity `@Data` → `@Getter/@Setter` + 手动 equals/hashCode
2. `@ManyToOne` 添加 `fetch = FetchType.LAZY`
3. 异常状态码一致性
4. 添加 `ConstraintViolationException` 处理
