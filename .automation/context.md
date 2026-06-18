# ReadyPlayerFun — Project Context

## What This Is
A server utility mod that pauses time, weather, and seasons when no players are logged in, keeping the server running idle without time progression.

**Note:** Minecraft natively added server pause functionality in 1.21.2. For 1.21.2+ (including 26.1), the mod's value proposition shifted to convenience and history:
- Automatically configures the server pause setting without requiring a manual `server.properties` edit
- Broadcasts a chat message to the first player who joins showing how long the server was paused

The mod was briefly retired at 1.21.2 but restored after user demand — users value both the convenience features and the history of the project.

License: MIT

## Project Structure
Multi-loader: `Common/` + `NeoForge/` (+ `Forge/` on older branches) + `Fabric/`

## Branch Convention
| Branch | Modloaders        |
|--------|-------------------|
| 1.18.2 | Forge + Fabric    |
| 1.20.1 | Forge + Fabric    |
| 1.21.1 | NeoForge + Fabric |
| 26.1   | NeoForge + Fabric |

Maintained: 1.20.1, 1.21.1, 26.1

## Dependencies
- WhiteNoise (jarJar/include)

## Distribution
Side: server-only (clientRequired = false, serverRequired = true)

## Release Process
Follow the standard wendall911 release process in `../docs/minecraft/MINECRAFT_DEVELOPMENT_NOTES.md`.
