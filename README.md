
## PoC Build process visualization plugin for IntelliJ Platform IDEs

## TODO - interactive gui

## SAMPLES
#### Build without any changes in code:
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                      INCREMENTAL BUILD REPORT                       ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃  Total time    : 16.3s
┃  Total tasks   : 337
┃  Total modules : 18
┃
┃  🔨 Executed    : 0 tasks in 0 modules
┃  ✅ Up-to-date  : 288
┃  📦 From cache  : 0
┃  ⏭  Skipped     : 49
┃  ❌ Failed      : 0
┃  💡 Reuse rate  : 100%
┃
┃  ⚪ UNCHANGED MODULES (18):
┃    ✅ :analytics (25 tasks)
┃    ✅ :app (48 tasks)
┃    ✅ :build-logic:convention (7 tasks)
┃    ✅ :data:api:contract (6 tasks)
┃    ✅ :data:api:implementation (25 tasks)
┃    ✅ :data:api:model (6 tasks)
┃    ✅ :data:repository:contract (6 tasks)
┃    ✅ :data:repository:implementation (25 tasks)
┃    ✅ :di (25 tasks)
┃    ✅ :domain:contract (6 tasks)
┃    ✅ :domain:implementation (25 tasks)
┃    ✅ :domain:model (6 tasks)
┃    ✅ :navigation (6 tasks)
┃    ✅ :navigation-graph (23 tasks)
┃    ✅ :resources (23 tasks)
┃    ✅ :ui:common (25 tasks)
┃    ✅ :ui:details (25 tasks)
┃    ✅ :ui:home (25 tasks)
┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

#### Changed ViewModel :ui:dashboard, build with changes:
```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                      INCREMENTAL BUILD REPORT                       ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃  Total time    : 32.2s
┃  Total tasks   : 337
┃  Total modules : 18
┃
┃  🔨 Executed    : 10 tasks in 2 modules
┃  ✅ Up-to-date  : 278
┃  📦 From cache  : 0
┃  ⏭  Skipped     : 49
┃  ❌ Failed      : 0
┃  💡 Reuse rate  : 97%
┃
┃  🔨 REBUILT MODULES (code changed):
┃
┃    🟢 :ui:details  [17.8s]
┃       🔨 kspDebugKotlin [8.9s]
┃       🔨 compileDebugKotlin [8.1s]
┃       🔨 transformDebugClassesWithAsm [347ms]
┃       🔨 bundleLibCompileToJarDebug [150ms]
┃       🔨 bundleLibRuntimeToJarDebug [93ms]
┃       🔨 bundleLibRuntimeToDirDebug [18ms]
┃       ⚪ ... 19 tasks unchanged
┃
┃    🟢 :app  [2.5s]
┃       🔨 kspDebugKotlin [917ms]
┃       🔨 packageDebug [588ms]
┃       🔨 mergeLibDexDebug [113ms]
┃       🔨 assembleDebug [0ms]
┃       ⚪ ... 44 tasks unchanged
┃
┃  ⚪ UNCHANGED MODULES (16):
┃    ✅ :analytics (25 tasks)
┃    ✅ :build-logic:convention (7 tasks)
┃    ✅ :data:api:contract (6 tasks)
┃    ✅ :data:api:implementation (25 tasks)
┃    ✅ :data:api:model (6 tasks)
┃    ✅ :data:repository:contract (6 tasks)
┃    ✅ :data:repository:implementation (25 tasks)
┃    ✅ :di (25 tasks)
┃    ✅ :domain:contract (6 tasks)
┃    ✅ :domain:implementation (25 tasks)
┃    ✅ :domain:model (6 tasks)
┃    ✅ :navigation (6 tasks)
┃    ✅ :navigation-graph (23 tasks)
┃    ✅ :resources (23 tasks)
┃    ✅ :ui:common (25 tasks)
┃    ✅ :ui:home (25 tasks)
┃
┃  ⏱  SLOWEST REBUILT TASKS:
┃    8.9s     :ui:details:kspDebugKotlin
┃    8.1s     :ui:details:compileDebugKotlin
┃    917ms    :app:kspDebugKotlin
┃    588ms    :app:packageDebug
┃    347ms    :ui:details:transformDebugClassesWithAsm
┃    150ms    :ui:details:bundleLibCompileToJarDebug
┃    113ms    :app:mergeLibDexDebug
┃    93ms     :ui:details:bundleLibRuntimeToJarDebug
┃    18ms     :ui:details:bundleLibRuntimeToDirDebug
┃    0ms      :app:assembleDebug
┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

#### Changed domain model:

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                      INCREMENTAL BUILD REPORT                       ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃  Total time    : 28.5s
┃  Total tasks   : 337
┃  Total modules : 18
┃
┃  🔨 Executed    : 13 tasks in 8 modules
┃  ✅ Up-to-date  : 275
┃  📦 From cache  : 0
┃  ⏭  Skipped     : 49
┃  ❌ Failed      : 0
┃  💡 Reuse rate  : 96%
┃
┃  🔨 REBUILT MODULES (code changed):
┃
┃    🟢 :domain:model  [3.6s]
┃       🔨 compileKotlin [3.6s]
┃       🔨 jar [52ms]
┃       ⚪ ... 4 tasks unchanged
┃
┃    🟢 :app  [3.6s]
┃       🔨 kspDebugKotlin [643ms]
┃       🔨 mergeDebugJavaResource [513ms]
┃       🔨 packageDebug [352ms]
┃       🔨 mergeLibDexDebug [281ms]
┃       🔨 assembleDebug [1ms]
┃       ⚪ ... 43 tasks unchanged
┃
┃    🟢 :domain:implementation  [1.8s]
┃       🔨 kspDebugKotlin [1.6s]
┃       ⚪ ... 24 tasks unchanged
┃
┃    🟢 :analytics  [1.8s]
┃       🔨 kspDebugKotlin [1.6s]
┃       ⚪ ... 24 tasks unchanged
┃
┃    🟢 :ui:common  [1.7s]
┃       🔨 kspDebugKotlin [1.5s]
┃       ⚪ ... 24 tasks unchanged
┃
┃    🟢 :data:repository:implementation  [1.5s]
┃       🔨 kspDebugKotlin [1.4s]
┃       ⚪ ... 24 tasks unchanged
┃
┃    🟢 :ui:details  [630ms]
┃       🔨 kspDebugKotlin [396ms]
┃       ⚪ ... 24 tasks unchanged
┃
┃    🟢 :ui:home  [585ms]
┃       🔨 kspDebugKotlin [456ms]
┃       ⚪ ... 24 tasks unchanged
┃
┃  ⚪ UNCHANGED MODULES (10):
┃    ✅ :build-logic:convention (7 tasks)
┃    ✅ :data:api:contract (6 tasks)
┃    ✅ :data:api:implementation (25 tasks)
┃    ✅ :data:api:model (6 tasks)
┃    ✅ :data:repository:contract (6 tasks)
┃    ✅ :di (25 tasks)
┃    ✅ :domain:contract (6 tasks)
┃    ✅ :navigation (6 tasks)
┃    ✅ :navigation-graph (23 tasks)
┃    ✅ :resources (23 tasks)
┃
┃  ⏱  SLOWEST REBUILT TASKS:
┃    3.6s     :domain:model:compileKotlin
┃    1.6s     :domain:implementation:kspDebugKotlin
┃    1.6s     :analytics:kspDebugKotlin
┃    1.5s     :ui:common:kspDebugKotlin
┃    1.4s     :data:repository:implementation:kspDebugKotlin
┃    643ms    :app:kspDebugKotlin
┃    513ms    :app:mergeDebugJavaResource
┃    456ms    :ui:home:kspDebugKotlin
┃    396ms    :ui:details:kspDebugKotlin
┃    352ms    :app:packageDebug
┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```


![Build](https://github.com/K-Terelak/build-process-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.
- [ ] Configure the [CODECOV_TOKEN](https://docs.codecov.com/docs/quick-start) secret for automated test coverage reports on PRs

<!-- Plugin description -->
This Fancy IntelliJ Platform Plugin is going to be your implementation of the brilliant ideas that you have.

This specific section is a source for the [plugin.xml](/src/main/resources/META-INF/plugin.xml) file which will be extracted by the [Gradle](/build.gradle.kts) during the build process.

To keep everything working, do not remove `<!-- ... -->` sections. 
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "build-process-plugin"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/K-Terelak/build-process-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
