# 贡献指南 / Contributing

## 工作方式 / Workflow

从 `main` 创建短期分支，通过 PR 合并。分支名使用 `feat/<feature>`、`fix/<feature>`、`docs/<topic>` 或 `chore/<topic>`。PR 必须包含需求编号、验证命令和结果；Android UI 变化附截图或录屏。

Create a short-lived branch from `main` and merge through a pull request. Use `feat/<feature>`, `fix/<feature>`, `docs/<topic>`, or `chore/<topic>`. Every PR must include the requirement ID and validation results; attach screenshots or a recording for Android UI changes.

## 开发边界 / Development Boundary

先在 CS26 验证真实功能。只有稳定、可独立测试且确实复用的能力才提取到 Knot。不要让产品功能依赖 Knot 的浮动分支。

Validate real behavior in CS26 first. Extract only stable, independently testable capabilities that are truly reusable into Knot. Do not make product features depend on a floating Knot branch.

## 密钥 / Secrets

本地开发使用 fake/local adapter 和 `.env.example`。真实短信、OSS、ntfy、数据库和 Android signing secrets 使用 GitHub Environments 或本地安全存储，不入库。

Use fake/local adapters and `.env.example` for local development. Keep real SMS, OSS, ntfy, database, and Android signing secrets in GitHub Environments or secure local storage; never commit them.
