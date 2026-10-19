# Tasks

## 1. Launcher option

- [x] 1.1 In `pom.xml`, `macos-build` profile, "Build Native Mac App" jpackage execution, add
  `<argument>--java-options</argument> <argument>-Dapple.awt.application.name=${project.name}</argument>` next to the
  existing `--java-options` arguments; verify the Windows jpackage calls are untouched (`git diff pom.xml` shows a
  single added line in the macOS profile)
- [x] 1.2 Build the macOS app image with `mvn clean install -P macos-build` and verify the generated
  `GeoHammer.app/Contents/app/GeoHammer.cfg` contains `java-options=-Dapple.awt.application.name=GeoHammer`
- [x] 1.3 Launch the built `GeoHammer.app` and verify manually against `specs/macos-integration/spec.md`: the splash
  is shown, and the application menu reads "About GeoHammer", "Hide GeoHammer" and "Quit GeoHammer" with no
  "MainGeoHammer" (needs a person at the screen)

## 2. Verify

- [x] 2.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 2.2 If the `code-review` skill is available, invoke it (not the `code-review:code-review` plugin) on the
  uncommitted changes with this change's proposal and specs as the completeness reference, fix critical and major
  issues, and report the scores and any issues left open
