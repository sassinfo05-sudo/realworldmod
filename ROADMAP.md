# RealWorld Total Conversion — status & roadmap

This mod targets Fabric on Minecraft 1.21.1 (Java 21). It is being built
incrementally, one working "slice" at a time — each slice adds real,
compiling, tested code rather than placeholder stubs.

## Why incrementally

The full brief (custom PBR renderer, GOAP city-scale NPC simulation,
in-game internet/OS, aviation + ATC, medical/anatomy system, government and
courts, ecology and wildlife AI, utilities and space program, etc.) is the
scope of a multi-year AAA game studio project, not something any single
session — human or AI — can deliver as finished, working code all at once.
Building it slice by slice means every commit is something you can actually
compile, run, and review.

## Done

- **Project scaffold**: Fabric Loom-based Gradle build (Minecraft 1.21.1,
  Yarn `1.21.1+build.3`, Fabric Loader `0.19.5`, Fabric API
  `0.116.17+1.21.1`), `fabric.mod.json`, mod entry point.
- **Slice 1 — Persistent citizen simulation core** (Section 2 of the brief):
  - `NpcProfile`: per-citizen record (name, home/workplace addresses,
    income, shift hours, current daily-routine state).
  - `NpcDatabase`: SQLite-backed persistence (`org.xerial:sqlite-jdbc`,
    embedded in the jar), one row per citizen, stored under the world save
    folder (`<world>/realworldmod/citizens.sqlite`).
  - `DailyScheduleFSM`: deterministic rule-tree state machine driving each
    NPC through `SLEEPING → WAKING → COMMUTING_TO_WORK → WORKING →
    COMMUTING_HOME → LEISURE → SLEEPING`, parameterized per-citizen by shift
    hours (including overnight shifts that wrap past midnight).
  - `NpcScheduleManager`: hooks the server tick loop, advances every
    citizen's FSM once per in-game hour, persists only the transitions that
    actually change state.
  - Unit tests for the FSM transition table and the SQLite CRUD layer.

## Not started yet (tracked, in priority order)

1. **In-game smartphone / GUI framework** (Section 3) — an `Item` +
   `Screen` for a lock screen and home screen grid; the foundation every
   later "app" (banking, real estate, BlockTube, dark web) will render into.
2. **World gen & anti-griefing** (intro constraints) — city/suburb/highway
   structure templates, deed/permit-gated block breaking.
3. **Vehicle entity + drivetrain physics** (Section 4).
4. **Targeted anatomical damage model** (Section 5).
5. **Economy ledger + banking app** (Sections 3 & 6).
6. **Police/crime state machine + court flow** (Section 7).
7. **Biome/wildlife AI overhaul** (Section 8).
8. **Utilities (power/water/telecom) + waste** (Section 9).
9. Rendering/PBR overhaul, aviation/ATC, space program — largest, latest.

Each future slice will follow the same pattern: a self-contained Java
package, unit tests where the logic doesn't require a running game client,
and a `gradle build` check before it's committed.

## Building

```
gradle build   # compiles + runs unit tests, produces build/libs/realworldmod-<version>.jar
gradle test    # unit tests only
```

No external services, API keys, or network access are required at
**runtime** — everything above (Section 2 in particular) runs entirely
against the local SQLite file. Network access is only used at *build time*
to fetch Minecraft/Fabric/Maven artifacts, same as any other Fabric mod.
