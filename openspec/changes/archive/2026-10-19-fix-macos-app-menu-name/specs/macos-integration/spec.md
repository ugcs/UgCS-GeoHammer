# Spec Delta

## Purpose

Defines how the packaged GeoHammer app presents itself to macOS, so that system UI such as the application menu shows
the product name rather than internal identifiers.

## ADDED Requirements

### Requirement: Application menu name

In the packaged macOS app, the application menu title and every menu item that includes the application name SHALL use
the product name `GeoHammer`. Internal identifiers, such as the main class name, SHALL NOT appear in the application
menu.

#### Scenario: Menu items use the product name
- **WHEN** the user launches the installed GeoHammer app on macOS and opens the application menu
- **THEN** the menu title is "GeoHammer" and its items read "About GeoHammer", "Hide GeoHammer" and "Quit GeoHammer"

#### Scenario: No main class name in the menu
- **WHEN** the user opens the application menu of the installed app
- **THEN** no item contains "MainGeoHammer"

#### Scenario: Splash screen still shown
- **WHEN** the user launches the installed GeoHammer app on macOS
- **THEN** the splash screen is shown during startup and closes once the main window appears
