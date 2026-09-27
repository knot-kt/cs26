# Pixel 6 Debug Baseline / Pixel 6 调试基线

- Requirement / 需求: `QUALITY-01`
- Commit / 提交: `fb24b52`
- Environment / 环境: Pixel 6, Android 16, local server through `adb reverse tcp:8080 tcp:8080`
- Build type / 构建类型: debug
- Sample count / 样本数: one cold start capture
- Tool / 工具: `scripts/collect-performance.sh`

## Steps / 步骤

1. Install `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
2. Start the local Ktor server on port 8080.
3. Run `scripts/collect-performance.sh docs/evidence/runs/performance-20260927` with the authorized Pixel 6 connected.

## Observed / 实际

- Cold start: `TotalTime: 654 ms`.
- Memory: `TOTAL PSS: 113389 kB`.
- Frame sample: 5 frames, 2 janky frames (40%); this is a startup baseline, not a steady-state threshold result.

Raw outputs / 原始输出: `environment.txt`, `startup.txt`, `meminfo.txt`, `gfxinfo-framestats.txt` in this directory.

Result / 结果: `pass` for evidence capture; performance optimization remains open.