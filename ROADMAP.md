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

- **Slice 2 — Smartphone + GUI app framework** (Section 3):
  - `ModItems.SMARTPHONE`: a real item, added to its own creative tab
    (`ModItemGroups.ELECTRONICS`).
  - `ModDataComponents.PHONE_BATTERY`: persistent 0-100 battery level stored
    directly on the item stack (Minecraft's data-component API), synced to
    clients over the network.
  - `PhoneBattery` / `TimeOfDayFormatter`: pure, unit-tested logic for
    battery drain/charge curves and rendering the in-game clock.
  - `PhoneUseHandler` (common) decrements battery server-side and blocks use
    when dead; `RealWorldModClient` (client-only, split source set) opens
    the UI.
  - `PhoneLockScreen` → `PhoneHomeScreen` → per-app `Screen`s
    (`SettingsAppScreen`, `MessagesAppScreen` as first two apps), wired
    through `PhoneApp` (common enum) and `ClientPhoneApps` (client-side
    enum → `Screen` mapping) — this is the extension point every later app
    (banking, real estate, BlockTube, dark web) plugs into.

- **Slice 3 — Land claims & anti-griefing** (intro constraint: "modifying
  property requires owning the deed"):
  - `Claim`: an immutable X/Z-rectangle (full height) owned by a player
    UUID, with inclusive-boundary containment.
  - `ClaimRegistry`: pure, unit-tested query logic (`canModify`,
    `findClaimAt`) — unclaimed land stays freely modifiable, only deeded
    plots are protected (see the scope note in `ClaimRegistry`'s Javadoc).
  - `ClaimDatabase`: SQLite persistence, same pattern as `NpcDatabase`,
    loaded into the in-memory registry on server start.
  - `PropertyProtection`: hooks Fabric API's `PlayerBlockBreakEvents.BEFORE`
    to cancel breaking blocks inside a claim the breaking player doesn't
    own, with a feedback message.
  - **Known gap at the time**: block *placement* wasn't gated (still true —
    see item 1 below), and there was no in-game way to actually create a
    claim.

- **Slice 4 — Deed item & in-game claim purchase flow**:
  - `PropertyService`: the single place claims get created, wrapping
    `ClaimRegistry` + `ClaimDatabase` so they can't drift out of sync
    (`RealWorldMod` no longer touches `ClaimDatabase` directly).
  - `ClaimRegistry.overlapsAny(...)`: rectangle-intersection check (not just
    point containment) used before creating a new claim, unit tested
    including the edge-touching case.
  - `ModItems.LAND_DEED`: a real, obtainable item (in the creative tab).
  - `DeedUseHandler`: right-clicking a block with a deed in hand claims a
    33x33 plot centered on it via `UseBlockCallback`, consuming one deed on
    success and refusing (with feedback) if it would overlap existing land.
  - This closes the biggest gap from slice 3: claims can now actually be
    created during play, not just loaded from a pre-seeded database.

- **Slice 5 — Bank accounts & Banking app** (Sections 3 & 6):
  - `BankMath`: pure, unit-tested cents arithmetic (`deposit`, `withdraw`
    returning `Optional` on insufficient funds).
  - `BankDatabase` / `BankService`: same open/cache/persist shape as the
    property system, plus `transfer(from, to, amount)` for
    player<->player/NPC payments.
  - `CurrencyFormatter`: cents -> `"$12.34"` display strings.
  - First real client<->server networked feature: `BankBalanceRequestPayload`
    (C2S) / `BankBalanceResponsePayload` (S2C) using Minecraft's
    `CustomPayload` API, registered through `BankNetworking`.
  - `BankingAppScreen` (third `PhoneApp`): opening it requests the player's
    balance from the server and displays it once the response lands,
    proving out the pattern every later networked app (real estate,
    BlockTube, courts) will reuse.
  - **Known gap at the time**: nothing in-game deposited or spent money yet
    (closed by slice 6, below).

- **Slice 6 — Cash register block & job wage payout**:
  - `JobService`: pure cooldown-gated payout logic (`tryWorkShift`,
    `ticksRemaining`), unit tested against a real `BankService` so the
    money actually moves, not just a mock.
  - `ModBlocks.CASH_REGISTER`: a real, placeable block (registered with a
    matching `BlockItem`, added to the creative tab).
  - `JobUseHandler`: right-clicking a cash register with an empty hand
    (via `UseBlockCallback`, same pattern as `DeedUseHandler`) works a
    shift, depositing a wage through `BankService` — one cooldown per
    player, not per block, for this slice.
  - This closes the slice 5 gap: a balance can now actually go from $0 to
    something during play, and the Banking app reflects it.

- **Slice 6b — Deeds cost money**: `DeedUseHandler` now checks for overlap
  first (a pure, free query), then withdraws `PRICE_CENTS` (5000, i.e.
  $50.00) from the buyer's `BankService` account before creating the
  claim, refusing with a clear message if either check fails. The
  property and economy systems from slices 3-6 are now fully connected.

- **Slice 7 — Block-placement mixin**: the mod's first Mixin. `BlockItemMixin`
  injects into `BlockItem.place(ItemPlacementContext)` at `HEAD`
  (cancellable) and refuses placement inside a claim the placing player
  doesn't own, using the same `ClaimRegistry.canModify` check as breaking.
  Since the mixin runs outside the mod's own constructor-injected object
  graph, `PropertyAccess` is a small static holder set once from
  `RealWorldMod.onInitialize()` so the mixin can reach the live registry.
  This closes the anti-griefing gap left open since slice 3: both breaking
  and placing are now gated.
  - **Known limitation**: the check only runs server-side (the standard
    pattern for this kind of gate), so a client may briefly render the
    block before the server's rejection reaches it — a normal, cosmetic
    rubber-banding effect, not a correctness bug.

## Not started yet (tracked, in priority order)

1. **Vehicle entity + drivetrain physics** (Section 4).
2. **Targeted anatomical damage model** (Section 5).
3. **Police/crime state machine + court flow** (Section 7).
4. **Biome/wildlife AI overhaul** (Section 8).
5. **Utilities (power/water/telecom) + waste** (Section 9).
6. Rendering/PBR overhaul, aviation/ATC, space program — largest, latest.

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
