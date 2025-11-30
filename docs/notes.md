# rules of system

### sqldelight

#### deny operations if there is no changes needed
not using - "IS DISTINCT FROM" - is not supported in android.
using - "name <> :newName" - same behavior, but there is problem when passing NULL to arguments

conclusion:
pass only NOT NULL params

## state()
- avoid every move cursor offset changes, if it doesn't affect UI at all. Store offset in viewmodel as temporary value.
  Run the desktop application: `./gradlew :shared:run`


`./gradlew :jvmRun`
`./gradlew :refreshVersions`
`./gradlew :shared:jsRun`
`./gradlew :wasmJsBrowserDevelopmentRun`
`./gradlew :shared:jvmRunHotAsync`
`./gradlew :shared:hotRunJvm --auto`
`./gradlew :kotlinWasmUpgradeYarnLock`
`./gradlew :jsBrowserDevelopmentRun`
`./gradlew assembleRelease -PcomposeCompilerReports=true --rerun-tasks`
`./gradlew createModuleGraph --rerun-tasks`
