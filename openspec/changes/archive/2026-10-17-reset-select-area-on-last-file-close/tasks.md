# Tasks

## 1. Release Select Area on last file close

- [x] 1.1 Add a private `@EventListener` `onFileClosed(FileClosedEvent)` to `TraceCutter`: return early if the event
  file is null; if every file in `model.getFileManager().getFiles()` equals the closed file, in `Platform.runLater`
  call `buttonCutMode.setSelected(false)` and `updateCutMode()` when the button is selected. Verify by reading the code
  against design.md (decision taken on the publishing thread, UI reset on the FX thread)
- [x] 1.2 Run the app (`mvn javafx:run`), open one file, turn Select Area on, close the file with the header close
  button; verify the button is released and disabled and the crop polygon is gone
- [x] 1.3 Open two files, turn Select Area on, close one; verify the button stays pressed and the polygon stays. Then
  use Close All; verify the button is released and disabled and no polygon is drawn. Open a file again; verify the
  button is enabled and turning it on draws a new polygon

## 2. Verify

- [x] 2.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings in changed code
- [x] 2.2 Invoke the `code-review` skill on the uncommitted changes with this change's proposal and specs as the
  completeness reference; fix critical and major issues and report the scores and any issues left open
