# Proposal

## Why

On macOS the application menu items read "About MainGeoHammer", "Hide MainGeoHammer" and "Quit MainGeoHammer" instead
of "GeoHammer" (#996). The `-splash:` option makes AWT create the Cocoa application before JavaFX starts, so AWT builds
the app menu. AWT names it from the `apple.awt.application.name` system property, which the Java launcher fills with
the main class simple name when it is not set explicitly. The bundle name (`CFBundleName = GeoHammer`) is never
consulted.

## What Changes

- The macOS app image passes `-Dapple.awt.application.name=${project.name}` to the JVM through the jpackage launcher
  options, so the app menu items use the product name.

## Non-goals

- Dev runs without the bundle (`mvn javafx:run`, IDE): they have no splash, and JavaFX builds the menu itself.
- Removing or reworking the splash screen.
- Windows and Linux packaging: they have no global application menu. The name users see there comes from other
  sources that are already correct: the window title and the `--name` given to jpackage on Windows, and the
  `.desktop` entry (`Name=GeoHammer`, matched by `StartupWMClass`) on Linux.
- Adding custom About or Preferences handlers to the app menu.

## Capabilities

### New Capabilities

- `macos-integration`: how the packaged app presents itself to macOS (application menu naming).

### Modified Capabilities

None.

## Impact

- `pom.xml`: the jpackage call in the `macos-build` profile gets one more `--java-options` argument. The generated
  `GeoHammer.app/Contents/app/GeoHammer.cfg` gains the matching `java-options` line.
- No Java code changes; Windows and Linux builds are unaffected.
