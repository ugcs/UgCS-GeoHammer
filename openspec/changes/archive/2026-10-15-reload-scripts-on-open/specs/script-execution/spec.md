# Spec Delta

## ADDED Requirements

### Requirement: Script list includes scripts added while running

When the user opens the script dropdown in the script panel, the dropdown SHALL list the scripts present in the
`scripts` folder at that moment, filtered by the selected file's template and the current operating system as before.
No restart and no change of the selected file SHALL be needed to see a newly added script.

#### Scenario: Script copied into the folder
- **WHEN** the user copies `my_filter.py` and `my_filter.json` into the `scripts` folder while GeoHammer runs and then
  opens the script dropdown
- **THEN** the dropdown lists `my_filter`

#### Scenario: Script created by an agent
- **WHEN** an agent creates a script through MCP `create_script` for the template of the selected file and the user
  then opens the script dropdown
- **THEN** the dropdown lists the new script

#### Scenario: Script removed from the folder
- **WHEN** a script's files are deleted from the `scripts` folder and the user opens the script dropdown
- **THEN** the dropdown no longer lists that script

### Requirement: Reloading the script list keeps the current selection

Reloading the script list SHALL keep the selected script selected while it is still present in the `scripts` folder,
and SHALL keep the values entered in its parameter inputs. When the selected script is no longer present, the
selection SHALL be cleared.

#### Scenario: Entered values survive opening the dropdown
- **WHEN** the user selects a script, changes one of its parameter values and opens the script dropdown again without
  choosing another script
- **THEN** the same script stays selected and the changed value is still shown

#### Scenario: Selected script deleted
- **WHEN** the selected script's files are deleted from the `scripts` folder and the user opens the script dropdown
- **THEN** no script is selected and no parameter inputs are shown

### Requirement: Unreadable script description does not hide other scripts

A script whose JSON description cannot be read SHALL be left out of the scripts offered in the script panel and by
the MCP script tools, and SHALL be logged as a warning naming the file. The other scripts SHALL stay available, and
no error dialog SHALL be shown for it.

#### Scenario: Malformed description in the folder
- **WHEN** `broken.json` in the `scripts` folder is not valid JSON and the user opens the script dropdown
- **THEN** the dropdown lists every other script for the selected file and no error dialog is shown

#### Scenario: Agent lists scripts
- **WHEN** an agent calls MCP `list_scripts` while `broken.json` is not valid JSON
- **THEN** the call succeeds and returns every other script
