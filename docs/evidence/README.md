# Acceptance Evidence / 验收证据

Create one Markdown record per acceptance slice. Do not record a pass without a command, environment, and observed result.

每个验收切片建立一个 Markdown 记录。没有命令、环境和实际结果时，不填写“通过”。

Use this format / 使用以下格式：

```text
Requirement / 需求: CHAT-03
Build / 构建: commit or tag
Environment / 环境: device, Android version, server URL
Steps / 步骤: numbered reproducible actions
Expected / 预期: observable behavior
Observed / 实际: output, screenshot path, or log reference
Result / 结果: pass | fail | blocked
```

Performance records must include device model, OS, build type, sample count, tool, and raw result path. Keep before/after measurements together.

性能记录必须包含设备型号、系统、构建类型、样本数、工具和原始结果路径，并把优化前后数据放在同一记录中。