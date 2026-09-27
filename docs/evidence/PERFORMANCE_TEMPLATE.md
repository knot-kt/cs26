# Performance Evidence / 性能证据

Copy this file to `docs/evidence/runs/performance-<timestamp>.md` after running `scripts/collect-performance.sh`. Do not mark a result as passing without the raw files from the same run.

运行 `scripts/collect-performance.sh` 后，将本文件复制到 `docs/evidence/runs/performance-<timestamp>.md`。没有同一次运行产生的原始文件时，不填写通过。

## Scope / 范围

- Requirement / 需求: `QUALITY-01`
- Commit or tag / 提交或标签:
- Device model / 设备型号:
- Android version / Android 版本:
- Build type / 构建类型: `debug` or `release`
- Sample count / 样本数:
- Tool / 工具: `adb am start -W`, `dumpsys meminfo`, `dumpsys gfxinfo framestats`

## Raw outputs / 原始输出

- Environment / 环境: `environment.txt`
- Startup / 启动: `startup.txt`
- Memory / 内存: `meminfo.txt`
- Frames / 帧: `gfxinfo-framestats.txt`

## Result / 结果

- Expected / 预期:
- Observed / 实际:
- Result / 结果: `pass | fail | blocked`
- Notes / 备注:
