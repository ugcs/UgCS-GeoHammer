# Design

## Context

See proposal.md (Why) for the symptom. How the packaged app starts on macOS:

```
GeoHammer launcher (jpackage, GeoHammer.cfg: -splash:$APPDIR/splash_screen.jpg)
  |
  +-- libjli: apple.awt.application.name not set --> sets it to "MainGeoHammer"
  +-- splash  --> creates NSApplicationAWT (first NSApp in the process)
  |               app menu items named from: APP_NAME_<pid> (-Xdock:name)
  |                                          -> apple.awt.application.name
  |                                          -> CFBundleName (never reached)
  +-- main() --> JavaFX (Glass) reuses the existing NSApp and leaves its menu alone
```

The only thing that affects the name is what the JVM receives on its command line before the splash. No Java code,
thread or event is involved: the menu is built natively before `MainGeoHammer.main()` runs.

## Goals / Non-Goals

**Goals:**
- Name the AWT-built app menu after the product, from the same `${project.name}` the bundle already uses.

**Non-Goals:**
- Changing which toolkit owns the app menu.

## Decisions

**Pass `-Dapple.awt.application.name=${project.name}` as a jpackage `--java-options` argument in the `macos-build`
profile.** The launcher sets this property only when it is not already set, so an explicit value wins. It ends up in
`GeoHammer.cfg` next to the existing `-Xmx` and `-splash:` options and stays on macOS only.

Alternatives considered:
- `-Xdock:name=GeoHammer`: also works (checked first, through the `APP_NAME_<pid>` environment variable), but it is an
  older, less self-explanatory flag. The system property names exactly what it controls.
- `System.setProperty(...)` in `MainGeoHammer.main()`: too late. The splash has already created the AWT application
  and its menu before `main()` runs.
- Dropping the macOS splash: fixes the name (JavaFX would own the menu) but removes a feature.
- Hardcoding `GeoHammer` instead of `${project.name}`: duplicates the name that `--name` and `--mac-package-name`
  already take from the POM.

## Risks / Trade-offs

- [The property name is AWT-internal behavior that a future JDK could change] → It is documented macOS AWT
  configuration and has been stable across JDK releases; the manual menu check in tasks.md catches a regression on a JDK
  upgrade.
- [Dev runs from the IDE still show a different menu name] → Out of scope (see proposal.md, Non-goals); they have no
  splash and are never shipped.
