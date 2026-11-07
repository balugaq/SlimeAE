# SlimeAE

![](https://cdn.jsdelivr.net/gh/TimetownDev/SlimeAE@master/imgs/SlimeAE.png)

SlimeAE 是一个 Slimefun 附属插件：新增完整的 ME 存储网络——上百种设备与终端、自动合成系统、世界矿石生成，可接入 10 余个其他附属。

原项目地址：[TimetownDev/SlimeAE](https://github.com/TimetownDev/SlimeAE)（本仓库为维护分支）

## 前置

| 插件 | 说明 |
| --- | --- |
| [Slimefun4](https://github.com/SlimefunGuguProject/Slimefun4) | 必需 |
| [鬼斩前置库 GuizhanLibPlugin](https://50l.cc/gzlib) | 必需 |
| [CMILib](https://www.spigotmc.org/resources/cmilib.87610/) | 必需 |
| [GuguSlimefunLib](https://builds.guizhanss.com/TimetownDev/GuguSlimefunLib/master) | 必需 |

### 可联动扩展（可选）

安装后自动接入，缺失时对应功能停用：无尽贪婪（InfinityExpansion）、蓬松机器（FluffyMachines）、网络（Networks / Networks-Changed）、网络拓展（NetworksExpansion）、末地科技（TranscEndence）、更好的粘液书（JustEnoughGuide）、星系（Galactifun）、黑曜石科技（ObsidianExpansion）、异域花园（ExoticGarden）、逻辑工艺（LogiTech）、乱序技艺（FinalTECH-Changed）。

## 命令

主命令 `/ae`（别名 `/SlimeAE`），子命令：

| 子命令 | 说明 |
| --- | --- |
| `/ae help` | 查看帮助 |
| `/ae viewitems` | 查看存储元件中的物品 |
| `/ae uuid` | 获取存储元件的 UUID |
| `/ae applyuuid` | 修改存储元件的 UUID（OP） |
| `/ae cleardata` | 清空存储元件的数据（OP） |
| `/ae savedata` | 保存 AE 数据（OP） |
| `/ae reload` | 重新加载配置文件（OP） |
| `/ae status` | 查看网络/任务状态 |
| `/ae migrate` | 数据库迁移（v2 → v3） |
| `/ae rollback` | 回滚 v3 数据库到 v2 |
| `/ae repair` | 修复数据库 |
| `/ae export` / `/ae import` | 导出/导入数据 |

## 配置

首次启动后生成 `plugins/SlimeAEPlugin/config.yml`，常用项：

- `auto-update`：自动更新（默认开）
- `auto-crafting.*`：自动合成任务数上限、并行度、持久化
- `world-gen.enable-ore-gen`：世界矿石生成开关
- `reskin-machine.*`：ME 材质转换机开关与黑名单
- `storage.*`：数据库（sqlite/mysql）、备份与日志保留策略

## 构建

```bash
./gradlew clean build
```

产物：`build/libs/SlimeAE-<version>.jar`（Java 17+，Gradle 9.8 wrapper 自动下载）。

部分扩展依赖（蓬松机器、网络拓展等）以本地 `lib/*.jar` 形式提供，编译期使用，运行时按服务器实际安装的插件接入。

## 下载

[![构建状态](https://builds.guizhanss.com/api/badge/balugaq/SlimeAE/master/latest)](https://builds.guizhanss.com/balugaq/SlimeAE/master)

## 反馈

优先通过 [GitHub Issues](https://github.com/balugaq/SlimeAE/issues) 反馈；没有 GitHub 账号可加作者 QQ。

## 许可

[MIT](LICENSE) © 2024 Ddggdd135

## 感谢

感谢 Zombie_2333, balugaq 帮忙测试并完善插件。

感谢 **myallworld.top** 的服主提供的测试环境。
