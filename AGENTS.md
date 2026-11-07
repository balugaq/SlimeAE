# AGENTS.md — SlimeAE 操作契约

> 本文件是 AI 编程助手与本仓库开发者的**操作契约**。所有改动（代码、构建、文档）均须遵守本文约定；当本文与代码冲突时，以代码为准并向开发者报告差异。

## TL;DR（太长了不看）

1. 本仓库是 **Minecraft Paper + Slimefun4 附属插件**（Java 17 目标 / Gradle Kotlin DSL / Shadow），所有改动必须通过 `./gradlew clean build` 验收后才能提交，禁止提交未编译验证的代码。
2. 玩家可见文案不走统一语言文件（本项目无 lang 目录，文案散落在代码与 GuizhanLib 机制中），日志一律走 `getLogger()`；**禁止** `System.out.println`、**禁止**改动已注册的 Slimefun 物品 ID、**禁止** `git push -f`。
3. 开发分支为 `fix-pinyin-lib` 等功能分支，发布合入 `master`（origin 默认分支）；提交信息遵循 Conventional Commits（`feat:`/`fix:`/`docs:`/`refactor:`/`chore:`）。
4. 样式/视觉类改动由开发者目视确认，AI 只做静态核对（编译 + grep + YAML 解析），不启动浏览器或截图工具。

---

## 1. 项目身份与边界

| 维度 | 事实 |
| --- | --- |
| 定位 | Minecraft Paper 服务器插件，**Slimefun4 附属**：AE 风格的 ME 存储网络（控制器、终端、总线、存储元件、自动合成） |
| 入口类 | `me.ddggdd135.slimeae.SlimeAEPlugin`（`extends JavaPlugin implements SlimefunAddon`） |
| 版本目标 | 编译目标 Java 17（`options.release = 17`）、Paper 1.20.1 API、Slimefun4 2025.1.2、`api-version: 1.16` |
| 仓库策略 | origin = `balugaq/SlimeAE`（维护 fork），upstream = `TimetownDev/SlimeAE`；推送由开发者执行 |
| AI 职责范围 | 编写/修改功能代码、Bug 修复、构建/CI 维护、文档维护 |
| AI 不负责 | 发布版本、推送远端、git 历史改写 |

### 1.1 代码包地图（改代码前先定位）

| 包 | 职责 |
| --- | --- |
| `me.ddggdd135.slimeae`（根） | `SlimeAEPlugin` 主类：启动顺序、集成实例化、任务调度、命令注册 |
| `api.abstracts` | ME 物品抽象基类：`MEObject`、`MEBus`、`MEAdvancedBus`、`MEChainedBus`、`AEMachineBlock`、`Card` |
| `api.autocraft` | 自动合成核心：`AutoCraftingTask`、`CraftType(Registry)`、`IterativeCraftCalculator`、任务持久化序列化 |
| `api.database.v3` | v3 数据库层：`V3DatabaseManager`、SQLite/MySQL 连接管理、WAL/journal、checkpoint、备份、v2→v3 迁移 |
| `api.blockdata` / `api.interfaces` | 方块数据持久化（各总线/接口的 `*Data` + `*DataAdapter`）、能力接口 |
| `core.items` | 注册入口：`SlimeAEItemGroups.onSetup()` → `SlimeAEItems.onSetup()`（约百余个 `SlimefunItemStack`） |
| `core.slimefun` | 物品实现：`MEController`、`MEUnit`、`MEDrive`、`terminals.*`（8 类终端）、`buses.*`（总线族）、`assembler.*`（分子装配室族）、`cards.*`、`tools.*` |
| `core.recipes` | 自定义配方类型：`WorldGeneratingRecipeType`、`BlockDestroyRecipeType` 等 |
| `core.generations` | 世界生成：`OreGenerator` + `SlimefunBlockPopulator` |
| `core.listeners` / `core.commands` | 事件监听（`BlockListener`、`NetworkListener` 等）与 `/ae` 命令族（subcommands 15 个） |
| `integrations` | 12 个其他附属的联动与配方注册（`*Integration` + `*RecipeRegistration`），均带 `isLoaded()` 判空 |
| `tasks` | 定时任务：`NetworkTickerTask`（网络运行）、`DataSavingTask`、`TaskBackupTask` |

## 2. 技术栈与项目结构（事实核对）

- **构建**：Gradle Kotlin DSL + `com.gradleup.shadow`（9.6.1）；`defaultTasks("clean", "build")`；产物 `build/libs/SlimeAE-<version>.jar`；relocation：`org.bstats`→`me.ddggdd135.slimeae.libs.bstats`、`com.zaxxer.hikari`→`...libs.hikari`、`net.byteflux.libby`→`...libs.libby`。
- **依赖版本**集中在 `gradle/libs.versions.toml`，`build.gradle.kts` 只用 `libs.xxx` 引用；改动依赖先改 catalog。
- **本地依赖**：`lib/*.jar`（蓬松机器、网络拓展、末地科技、星系、黑曜石科技、异域花园、逻辑工艺、乱序技艺）走 `compileOnly(fileTree("lib"))`；`.gitignore` 有 `!lib/*.jar` 白名单，**不要删**。
- **plugin.yml**：`version: '${version}'` 占位符由 `processResources.expand` 注入；`depend: [Slimefun, CMILib, GuguSlimefunLib]`；`softdepend` 列 12 个扩展。**勿动 depend 列表与物品 ID。**
- **数据库**：默认 SQLite（WAL），可切 MySQL；`storage.*` 配置块控制连接池/journal/checkpoint/备份；启动时会自动迁移或要求手动 `/ae migrate`（见 `MigrationV2ToV3`）。
- **启动顺序**（`SlimeAEPlugin.onEnable()`，新增初始化须插对应阶段）：`loadLibraries()`（libby 运行时拉 sqlite-jdbc 等）→ 检查 GuizhanLibPlugin（缺失即 disable）→ `reloadConfig0()` → `V3DatabaseManager` → `tryUpdate()`（auto-update）→ `SlimeAEItemGroups/SlimeAEItems.onSetup()` → `RecipeUtils.init()` → 注册监听器 → 集成 `isLoaded()` 日志 → `craftTaskPersistence` → 命令注册（15 个子命令）→ 定时任务调度。
- **CI**：`build.yml`（JDK 21、`./gradlew clean build`、master push/PR 触发）、`release.yml`（版本号取自 `gradle.properties` 的 `version=`，tag `v<version>` 自动 Release）、`test-compatibility.yml`（Paper 1.20.1 / 26.2 矩阵，装 Slimefun4+GuizhanLib+CMILib+GuguSlimefunLib 后真机加载）。

## 3. 决策树

- **新增物品/机器**：在 `core.slimefun` 对应子包实现（继承 `MEObject`/`AEMachineBlock` 系抽象），物品定义进 `SlimeAEItems`，分组挂 `SlimeAEItemGroups` 的 tier；配方走 `SlimefunAERecipeTypes` 或标准 RecipeType。
- **新增自动合成设备类型**：实现 `IMECraftDevice`/`IMERealCraftDevice`，在 `CraftTypeRegistry` 注册 `CraftType`；跨附属设备的合成注册放 `integrations.<Addon>RecipeRegistration`。
- **改动存储元件数据结构**：必须同步 `MEStorageCellStorageData`、其 `*DataController`（v3 数据库层）与 `MEAdvancedBusDataAdapter` 系序列化；涉及 schema 变化要写 `MigrationV2ToV3` 式迁移或升级 v3 DDL，禁止裸改序列化格式。
- **新增子命令**：实现 `api.SubCommand`，在 `SlimeAEPlugin.onEnable()` 命令注册段 `addSubCommand`；权限加进 `plugin.yml` 的 `permissions`。
- **新增配置项**：加进 `src/main/resources/config.yml`（带中文注释），读取处集中且带默认值。
- **新增联动**：仿照 `integrations` 现有模式——`XxxIntegration`（`isLoaded()` 探测）+ `XxxRecipeRegistration`，pom/catalog 加 `compileOnly` 依赖（本地 jar 则放 `lib/`），`plugin.yml` `softdepend` 补条目，`config.yml` 加 `enable_Xxx` 开关，主类 `onEnable()` 实例化。
- **异常处理**：后台任务（`NetworkTimeConsumingTask`、数据库线程）里捕获的异常要记日志，禁止吞掉。

## 4. CI 验收（提交前自查）

1. `./gradlew clean build` 全绿（含 `test`）。
2. `git status --short` 无意外文件；`build/`、`.gradle/` 已被忽略，`gradle/wrapper/gradle-wrapper.jar` 未被忽略（`git check-ignore` 为否）。
3. `git ls-files -s gradlew` 为 `100755`。
4. 改 workflow 时：分支写 `master`；调用 `./gradlew` 前有 `chmod +x gradlew`；不出现裸 `gradle` 调用。
5. `plugin.yml` 版本占位符保持 `${version}`；禁止改回 `${project.version}`。

## 5. 红线

1. 不改动既有 Slimefun 物品 ID 与 `plugin.yml` 的 `name`/`main`。
2. 不引入新的私有 Maven 仓库；依赖一律走 catalog 记录的公开源。
3. 不删除或绕过 GuizhanLibPlugin 启动检查。
4. 不在主线程做数据库 IO（走 `api.database.v3` 的连接管理与后台策略）。
5. 不提交 `lib/` 之外的二进制；`!lib/*.jar` 白名单之外的 jar 一律忽略。
6. 不改写 git 历史；推送由开发者本人执行。

## 6. 已知债务

- `CMILib` 依赖计划移除（README 原文注明），新代码尽量避免直接引用。
- 部分 system 作用域依赖以 `lib/*.jar` 供货，版本锁死，升级需手动换 jar。
- 语言文案未抽离，散落代码中；国际化为待办方向。
