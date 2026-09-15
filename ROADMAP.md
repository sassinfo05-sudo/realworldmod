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

- **Slice 8 — Leg injuries from fall damage** (first piece of Section 5's
  localized anatomical damage system):
  - `LegInjury` (NONE/BRUISED/FRACTURED) and `FallInjuryCalculator`: pure,
    unit-tested thresholds mapping fall damage to injury severity, and
    injury severity to a Slowness effect amplifier/duration.
  - `LegInjuryEffect`: hooks Fabric API's
    `ServerLivingEntityEvents.AFTER_DAMAGE`, and on fall damage above the
    bruise threshold applies Slowness (standing in for a real limp) plus a
    feedback message.
  - **Known gaps, deliberately scoped out of this slice**: players only
    (NPCs don't get hurt yet), no persistence of the injury across a
    relog, and no hospital/cast/treatment to heal it early — the effect
    just expires on its own. Head/arm/torso zones and their own effects
    (aim sway, reduced carry weight, etc.) aren't started.

- **Slice 9 — Vehicle drivetrain physics** (first piece of Section 4):
  - `VehicleState` (speed, fuel) and `VehiclePhysics.tick(state, throttle)`:
    a pure, deliberately Minecraft-independent per-tick simulation —
    acceleration, braking (faster than coasting), coast-to-stop with no
    overshoot past zero, reverse, top-speed/reverse-speed clamping,
    throttle-input clamping, and fuel consumption that stops mattering
    once the tank is empty (throttle is then ignored, not just weakened).
  - Deliberately does **not** attempt the entity/rendering wrapper (a
    custom `Entity` subclass, `EntityType` registration, input capture,
    and a client-side model/renderer) in this slice — that is a
    substantially larger, harder-to-verify piece of work than everything
    shipped so far (it can't be checked the way items/blocks/mixins were,
    by inspecting bytecode/refmaps; it needs an actual running game
    client), so it's left as its own follow-up rather than shipped
    half-working.

- **Slice 10 — Wanted-level crime tracking** (first piece of Section 7):
  - `WantedLevelMath`: pure clamp arithmetic (0-5), and
    `WantedLevelDescriptions`: pure level -> translation-key mapping, both
    unit tested.
  - `CrimeService`: in-memory per-player wanted level, with a global
    tick-bucket decay (same pattern as `NpcScheduleManager`) that fades
    everyone's level by 1 roughly once a minute, removing the entry
    entirely once it reaches zero.
  - Trespassing is now a crime: both `PropertyProtection` (break) and
    `BlockItemMixin` (place) call `CrimeService.recordCrime` when they
    refuse a player, via the same `CrimeAccess` static-holder pattern
    `PropertyAccess` established for mixins.
  - A fourth `PhoneApp`, Criminal Record, reuses the exact
    request/response `CustomPayload` pattern from the Banking app
    (`WantedLevelRequestPayload`/`WantedLevelResponsePayload`) to show the
    player's live wanted level and a plain-language description.
  - **Known gaps**: no actual police NPCs or consequences yet (no arrest,
    no chase, no fines) — a high wanted level is currently purely
    informational. Not persisted across a relog. Only trespassing counts
    as a crime so far; no other Section 7 offenses are wired up.

- **Slice 11 — Fines: crime finally has an economic consequence**:
  - `LawEnforcementService` wraps `CrimeService` + `BankService`:
    `recordOffense` records the crime as before, and once the resulting
    wanted level reaches `FINE_THRESHOLD` (3), attempts to withdraw
    `FINE_CENTS` ($25) — a best-effort citation, not an error, if the
    player can't afford it (unit tested for both outcomes, plus the
    below-threshold no-op case).
  - `PropertyProtection` and `BlockItemMixin` both switched from calling
    `CrimeService` directly to going through `LawEnforcementService`, so
    trespassing (break or place) now both raises the record and, once
    flagged, drains the wallet.
  - **Known gaps**: still no police NPCs, arrests, or court flow — a fine
    is the entire consequence. Fines only trigger from trespassing, since
    that's still the only tracked offense.

- **Slice 12 — Utility billing** (first piece of Section 9: "Unpaid
  utility bills result in lights shutting off"):
  - `UtilityState` (connected, unpaid cents) and `UtilityBillingMath`: pure
    transitions for an automatic billing attempt and a manual payoff, unit
    tested.
  - `UtilityDatabase`/`UtilityService`: the same open/cache/persist shape
    as the property/bank/economy services, billing through `BankService`
    so a household's power is only as reliable as its owner's balance.
    Failed payments disconnect and *accumulate* debt (a full-amount
    all-or-nothing withdrawal each cycle, consistent with how
    `BankService.withdraw` already works elsewhere) rather than partially
    paying it down.
  - A fifth `PhoneApp`, Utilities, shows connection status and unpaid
    balance and adds a "Pay Now" button — the mod's first phone app with
    a C2S action payload (`PayUtilityBillPayload`) rather than a
    read-only display.
  - Wired into the server tick over all currently online players; anyone
    newly disconnected gets an action-bar message.
  - **Known gaps**: billing only runs for online players (an offline
    player's meter doesn't run while they're away — arguably realistic,
    but not a deliberate design choice, just what the online-player tick
    naturally gives you); no actual visible in-world consequence yet
    (no lights, no blocks that turn off) — "power" is currently a purely
    account-level flag exposed only through the phone.

- **Slice 13 — Utility Lamp: power outages become visible**:
  - `UtilityLampBlock`: a real light-emitting block (`Properties.LIT`,
    `luminance` tied to that property, same pattern as vanilla's redstone
    lamp) that self-schedules a check every 2 seconds
    (`world.scheduleBlockTick`), looks up which claim it sits in via the
    already-existing `ClaimRegistry.findClaimAt`, and reflects that
    claim owner's `UtilityService` connection status — going dark within
    seconds of their power being cut, no new per-block storage needed.
  - `UtilityAccess`: the same static-holder pattern as `PropertyAccess`/
    `CrimeAccess`, since the block's instance is created at
    class-registration time, not through the mod's constructor-injected
    services.
  - No new pure logic in this slice (it's Minecraft-glue reusing
    `ClaimRegistry` and `UtilityService` as-is), so no new unit tests —
    same category as `PropertyProtection`/`JobUseHandler` before it.
  - **Known gap**: lamps on unclaimed land are always lit (matching the
    property system's existing "unclaimed = unrestricted" convention) —
    only lamps inside an owned claim are actually billable.

## Not started yet (tracked, in priority order)

1. **Police NPCs / arrest / court flow** for a wanted level that's
   maxed out, beyond just fines (Section 7).
2. **Wire `VehiclePhysics` into an actual rideable entity**: custom
   `Entity`/`EntityType`, input capture, and a client-side model/renderer
   — the part of Section 4 that needs a running game client to verify.
3. **Expand the medical system**: other body zones, illness/hospital
   treatment, NPC injuries, persistence (Section 5).
4. **Biome/wildlife AI overhaul** (Section 8).
5. Rendering/PBR overhaul, aviation/ATC, space program — largest, latest.

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
