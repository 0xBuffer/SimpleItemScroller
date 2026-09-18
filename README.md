# Simple Item Scroller

A lightweight client-side Fabric mod for Minecraft 1.21.11 that makes moving many item stacks faster.

Hold **Shift + Left Mouse Button** and drag across inventory slots. Every item stack touched by the cursor is quick-moved as if it had been shift-clicked.

## Features

- Fast item transfer across multiple slots
- Works with survival and creative inventories
- Tracks the full cursor path, including quick movements
- Supports repeated passes over the same slots without releasing the mouse button
- Configurable transfer delay, including `0 ms` for maximum speed
- Optional Mod Menu configuration screen
- Client-side only

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.5 or newer
- Java 21 or newer
- Mod Menu is optional

## Installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Download `SIS-1.21.11.jar` from the latest GitHub release.
3. Place the JAR in the Minecraft profile's `mods` folder.
4. Start the game and hold **Shift + Left Mouse Button** while moving across item slots.

## Configuration

With Mod Menu installed, open the Simple Item Scroller configuration screen. The mod can also be configured in:

```text
config/simple-item-scroller.json
```

Set `delayMs` to `0` for the highest transfer speed.

## Building

```bash
./gradlew build
```

The release JAR is generated at `build/libs/SIS-1.21.11.jar`.

## License

MIT

Writed by gpt 5.6 sol with deep(seek) v4 pro
