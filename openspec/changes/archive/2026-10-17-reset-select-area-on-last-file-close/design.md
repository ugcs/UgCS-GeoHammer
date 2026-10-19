# Design

## Context

The Select Area tool is the `buttonCutMode` toggle in `TraceCutter` (a map `Layer` and `ToolProducer`). Its pressed
state drives `updateCutMode()`: on creates a `PolygonSelector` fitted to the open files, off clears it; both publish
`WhatChanged(justdraw)`. `MapView` enables the button through `ActivationPolicy.fileSelected()`. When the last file is
closed, that policy disables the button, but nothing releases it, so the button stays pressed and cannot be clicked.
`TraceCutter` resets itself only on `FileOpenedEvent`.

On close, `FileClosedEvent` is published:
- by `Chart.close()` on the FX thread (header close button);
- by `Saver.onCloseAll` once per file, from a background task.

The file is removed from `FileManager` by another listener (`ProfileView.fileClosed`). Spring does not define the order
of listeners for the same event.

## Goals / Non-Goals

**Goals:**
- Release the Select Area tool and clear its crop area when no files are left, whichever close path was used.

**Non-Goals:**
- Changing `ToolNode`/`ActivationPolicy` or `MapView` tool activation.
- Fixing the existing background-thread publishing in Close All.

## Decisions

**Listen to `FileClosedEvent` in `TraceCutter`.** A new `@EventListener` method `onFileClosed` decides whether any other
file is still open and, if none is, releases the button (`setSelected(false)`) and calls `updateCutMode()`, which
clears the polygon and repaints.
- Alternative: listen to `FileSelectedEvent(null)`, which `Model.removeChart` publishes when nothing is left to select.
  Rejected: it couples "no files open" to "no file selected", which are different concepts.
- Alternative: release any disabled `Toggle` in `ToolNode.activate`. Rejected: `setSelected` does not fire `onAction`,
  so the polygon would stay; and it hides a side effect in an enable/disable method.

**Decide on the publishing thread, update the UI on the FX thread.** The listener checks synchronously whether every
file in `model.getFileManager().getFiles()` equals the closed file (true both before and after `ProfileView` removes
it, so listener order does not matter; Close All publishes per file sequentially, so earlier files are already
removed). Only the button and polygon reset run in `Platform.runLater`, since Close All publishes off the FX thread.

**Do nothing when the tool is off.** The FX-thread part checks that the button is selected before resetting; there is
nothing to reset and no repaint is needed otherwise. The check runs on the FX thread because the button state is read
there.

No file data changes: no snapshot, undo, `setUnsaved` or cache invalidation is involved.

## Risks / Trade-offs

- [Close All reads the file list from a background thread while the FX thread may read it too] → The check only reads
  a list that the same background task mutates sequentially; this matches how `ProfileView` already uses it on this
  path.
- [`MapView.activateTools` and the reset both run in `runLater` in unspecified order] → They touch independent state
  (disabled vs selected), so the final state is the same either way.
