# Shipping a 3 GB model through Google Play

The hardest problem in on-device AI isn't inference — it's getting the model file onto the phone. Google Play caps APK/AAB download sizes far below 3 GB, and users won't tolerate a broken first-run. This is the delivery pipeline NovaSaur's production app (DinoSpace) uses.

## Path 1 — Play Asset Delivery (the normal case)

Google Play's **asset packs** deliver large files alongside the app install, but each pack is capped at 1.5 GB. So:

1. **Chunk at build time.** A build script splits `NovaSaur.litertlm` into 1 GB parts (`.part1`…`.partN`). The app's project file conditionally includes each part as a `fast-follow` asset pack — when the parts aren't present (day-to-day dev builds), the lines quietly no-op.
2. **Play delivers the packs** right after install, in the background, on Play's own bandwidth.
3. **Assemble on first run.** The app locates the delivered chunks, validates the set (contiguous part numbers, sane sizes — a same-size final chunk means a pack is still in flight, so wait), then streams them into one model file with progress shown in-app.
4. **Free the duplicate.** Once assembled, the app asks Play to remove the delivered packs so the model isn't stored twice.

## Path 2 — direct download (the fallback)

Sideloads and non-Play installs don't get asset packs. The app falls back to downloading the model directly (e.g. from Hugging Face), engineered so it can't frustrate anyone:

- **Resumable:** HTTP range requests against a `.part` file; killing the app mid-download loses nothing.
- **Retries with backoff** on transient failures, keeping the partial file.
- **Pause/resume/cancel** in the UI; free-space and wifi checks before starting.
- **No permissions needed:** progress lives in the app, not a notification — so no `POST_NOTIFICATIONS`, no foreground service. If the user leaves, the download resumes on return.

## The state machine

```
NotStarted ──► Downloading ──► Completed
    ▲               │
    │           Paused / Failed (partial file kept)
    └───────────────┘  resume
```

Both paths share one state machine and progress surface, so the UI is identical whether the model came from Play or the fallback.

## Design rules worth stealing

- **The app must be 100% usable with no model.** Treat "not downloaded" as a permanent, normal state.
- **Never trust a chunk set blindly** — validate contiguity and sizes before assembling; Play delivers packs asynchronously and out of order.
- **Show progress in-app, not in a notification.** It's one less permission at install review, and users are on that screen anyway.
- **Keep the partial file on failure.** Redownloading gigabytes because of one network blip is how you earn 1-star reviews.
