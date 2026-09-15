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
compile, run, and review. This document is the single source of truth for
what's actually implemented versus what the original brief asked for; it's
updated with every slice so neither side ever has to guess.

## Done (18 slices so far, 123 unit tests, all passing)

- **Project scaffold**: Fabric Loom-based Gradle build (Minecraft 1.21.1,
  Yarn `1.21.1+build.3`, Fabric Loader `0.19.5`, Fabric API
  `0.116.17+1.21.1`), `fabric.mod.json`, mod entry point, split
  main/client source sets.

- **Slice 1 — Persistent citizen simulation core** (Section 2):
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

- **Slice 2 — Smartphone + GUI app framework** (Section 3):
  - `ModItems.SMARTPHONE`, `ModDataComponents.PHONE_BATTERY` (persistent
    0-100 battery on the item stack, synced to clients).
  - `PhoneBattery` / `TimeOfDayFormatter`: pure battery drain/charge curves
    and in-game clock formatting.
  - `PhoneUseHandler` (common, drains battery) + `RealWorldModClient`
    (client-only, opens the UI).
  - `PhoneLockScreen` → `PhoneHomeScreen` → per-app `Screen`s, wired
    through `PhoneApp` (common enum) and `ClientPhoneApps` (client-side
    enum → `Screen` mapping) — the extension point every later app plugs
    into. Five apps exist today: Settings, Messages, Banking, Criminal
    Record, Utilities.

- **Slice 3 — Land claims & anti-griefing** (intro constraint):
  - `Claim` (immutable X/Z rectangle, full height, owned by a player UUID),
    `ClaimRegistry` (pure `canModify`/`findClaimAt`/`overlapsAny` query
    logic — unclaimed land stays freely modifiable), `ClaimDatabase`
    (SQLite persistence), `PropertyProtection` (cancels breaking blocks in
    someone else's claim via `PlayerBlockBreakEvents.BEFORE`).

- **Slice 4 — Deed item & claim purchase flow**:
  - `PropertyService` (single place claims get created/persisted),
    `ModItems.LAND_DEED`, `DeedUseHandler` (claims a 33x33 plot on
    right-click, refuses on overlap).

- **Slice 5 — Bank accounts & Banking app** (Sections 3 & 6):
  - `BankMath` (pure cents arithmetic), `BankDatabase`/`BankService`
    (open/cache/persist + `transfer`), `CurrencyFormatter`.
  - First client↔server networked feature: `BankBalanceRequestPayload`/
    `BankBalanceResponsePayload` (Minecraft `CustomPayload` API) via
    `BankNetworking`; `BankingAppScreen` (3rd `PhoneApp`).

- **Slice 6 — Cash register block & job wage payout**:
  - `JobService` (cooldown-gated payout), `ModBlocks.CASH_REGISTER`,
    `JobUseHandler` (works a shift for a wage on interact).

- **Slice 6b — Deeds cost money**: `DeedUseHandler` withdraws
  `PRICE_CENTS` ($50) via `BankService` before creating a claim.

- **Slice 7 — Block-placement anti-griefing mixin**:
  - `BlockItemMixin` (the mod's first Mixin) injects into
    `BlockItem.place(...)` to refuse placement in someone else's claim,
    mirroring the break-side check; `PropertyAccess` static holder lets
    the mixin reach the live `ClaimRegistry`.

- **Slice 8 — Leg injuries from fall damage** (Section 5 MVP):
  - `LegInjury`/`FallInjuryCalculator` (pure damage→severity→Slowness
    mapping), `LegInjuryEffect` (hooks
    `ServerLivingEntityEvents.AFTER_DAMAGE` on `DamageTypes.FALL`).

- **Slice 9 — Vehicle drivetrain physics** (Section 4, physics only):
  - `VehicleState`/`VehiclePhysics.tick(state, throttle)`: pure,
    Minecraft-independent acceleration/braking/coast/reverse/fuel
    simulation. No entity/rendering wrapper yet (see remaining work).

- **Slice 10 — Wanted-level crime tracking** (Section 7 MVP):
  - `WantedLevelMath`/`WantedLevelDescriptions` (pure), `CrimeService`
    (in-memory per-player level with tick-bucket decay). Trespassing
    (break or place) now raises it. 4th `PhoneApp`: Criminal Record.

- **Slice 11 — Fines**: `LawEnforcementService` wraps `CrimeService` +
  `BankService`; crossing `FINE_THRESHOLD` (3) withdraws `FINE_CENTS`
  ($25), best-effort. `PropertyProtection`/`BlockItemMixin` route through
  it instead of `CrimeService` directly.

- **Slice 12 — Utility billing** (Section 9 MVP):
  - `UtilityState`/`UtilityBillingMath` (pure), `UtilityDatabase`/
    `UtilityService` (bills through `BankService`, disconnects and
    accrues debt on failure). 5th `PhoneApp`: Utilities, with a "Pay Now"
    C2S action.

- **Slice 13 — Utility Lamp**: `UtilityLampBlock` self-schedules a check
  every 2s, looks up its claim via `ClaimRegistry`, reflects that owner's
  `UtilityService` status — actually goes dark on nonpayment.

- **Slice 14 — Illness from rain exposure** (Section 5):
  - `IllnessRisk`/`IllnessService` (pure exposure-tick counter, edge
    trigger), `WeatherIllnessEffect` (uses `World.hasRain`, applies
    Nausea + Weakness).

- **Slice 15 — Pharmacy & medicine**: `ModItems.MEDICINE` +
  `MedicineUseHandler` (cures Nausea/Weakness/Slowness),
  `ModBlocks.PHARMACY_COUNTER` + `PharmacyUseHandler` (sells it via
  `BankService`).

- **Slice 16 — Arrest at max wanted level** (Section 7):
  - `ArrestOutcome`/`ArrestService` (pure tick logic: detain at
    `WantedLevelMath.MAX`, release after `SENTENCE_TICKS`, clears the
    record via the new `CrimeService.clear`), `ArrestHandler` (teleport to
    a holding area + Blindness, then back to spawn on release).

- **Slice 17 — Hunting licenses & poaching** (Section 8 MVP):
  - `HuntingLicenseService` (pure permit flag), `ModBlocks.LICENSE_OFFICE`
    + `LicenseUseHandler`, `PoachingHandler` (killing an `AnimalEntity`
    without a license routes through the existing
    `LawEnforcementService.recordOffense`).

### Cross-cutting things already true of the whole codebase

- Every Minecraft-side API used (items, blocks, events, mixins, data
  components, networking payloads, status effects, scheduled ticks) was
  verified against the real 1.21.1 mappings via `javap` on the actual
  remapped jars before being written — not guessed from memory.
- Every service with real state (`BankService`, `PropertyService`,
  `UtilityService`, `NpcDatabase`, `ClaimDatabase`) follows the same
  open/cache/persist-to-SQLite shape, opened in `SERVER_STARTING` and
  closed in `SERVER_STOPPING`.
- Every paid interaction (deeds, medicine, hunting licenses) uses the same
  withdraw-or-refuse pattern against `BankService`.
- `gradle build` (compile + all unit tests) is run and passes before every
  commit; nothing is committed that doesn't build.

## Everything else — full section-by-section status against the original brief

This maps every section of the original master prompt to what exists
today and exactly what's still missing, so scope is never implicit.

**Intro constraints (single-player/offline, simulated internet, vanilla
eradication, real-world worldgen, anti-griefing, structural physics)**
- Done: offline/local-only (no network calls at runtime, confirmed —
  SQLite only), anti-griefing for break + place against owned claims.
- Missing: no simulated-internet browser beyond the phone app shell (no
  real estate portal, stock exchange, court registry, tax portal,
  BlockTube, dark web market as *web pages* — today "apps" are native
  Screens, not an in-game browser rendering HTML-like pages); zero vanilla
  assets have been replaced (no custom textures/models/sounds/HUD — the
  mod adds new items/blocks on top of vanilla, it doesn't strip anything);
  no real-world-scale worldgen (no city grids, highways, airports,
  biome-accurate scale); no structural load-physics (no collapse
  simulation for removed load-bearing blocks); permits/municipal approval
  for construction beyond owning a deed don't exist.

**Section 1 — Rendering, Physics & Graphics Engine**
- Done: nothing. This is explicitly the "largest, latest" bucket.
- Missing: PBR/ray-traced lighting, volumetric fog, water refraction,
  displacement mapping — all of it (needs a custom shader/rendering
  pipeline, unverifiable without a running game client); the 1/16th
  sub-grid interior decoration system (arbitrary-angle furniture
  placement) — not started; background-thread macro-economics/weather/
  commute simulation for unrendered regions — not started (today's
  simulation runs only for online players/loaded chunks via the normal
  server tick, not a separate async layer).

**Section 2 — Autonomous Citizen & NPC Engine (GOAP)**
- Done: persistent SQLite citizen records, a deterministic (not GOAP)
  daily-schedule FSM cycling through 6 states.
- Missing: NPCs are data rows, not entities — there is no `Entity`/mob
  actually walking around, no pathfinding, no home/workplace structures
  to walk between, no fridge/breakfast/commute-by-vehicle animation, no
  job-task mini-behaviors (cashiering, patrols, factory work), no evening
  leisure destinations, no dialogue system/dialogue trees, no NPC
  behavioral AI (mugging, reacting to red-light running, independent
  crime, police chases). True GOAP (goal-oriented action planning, i.e.
  dynamic plan search over actions) was never implemented — the FSM is a
  simpler deterministic rule tree, called out as such in the code's own
  Javadoc from slice 1 onward.

**Section 3 — Consumer Electronics, Computers & In-Game Internet**
- Done: one smartphone item with battery, a 5-app OS shell (Settings,
  Messages, Banking, Criminal Record, Utilities) over a real client↔server
  networking pattern.
- Missing: PearOS vs. OpenDroid distinction (rooting, sideloading,
  terminal access), cracked screens/repair shops, charging cables as a
  physical item, PC building (motherboard/CPU/GPU/RAM/PSU parts, physical
  assembly), any resource-intensive task tied to PC specs (crypto mining,
  video rendering, hacking), an actual in-game *browser* rendering
  web-page-like content (today's apps are native screens, not pages), real
  estate portal, credit score dashboard, stock/forex exchange, civil/
  criminal court registry as a web UI (a start exists as the native
  Criminal Record app, but no court registry/filings), tax audit portal,
  BlockTube (record/edit/upload video, subscribers, ad revenue), dark web
  marketplace, game consoles/discs/arcades/claw machines/racing sims.

**Section 4 — Automotive, Aviation & Global Transit**
- Done: `VehiclePhysics` — a pure, tested drivetrain simulation (throttle
  → speed, braking, coasting, reverse, fuel consumption).
- Missing: the physics isn't attached to anything — there is no vehicle
  `Entity`, no `EntityType` registration, no player input capture, no
  client-side model/renderer, so nothing is actually driveable in game
  yet. Beyond that: the other 299+ vehicle types, mechanic
  shops/tuning/paint/body damage, garage capacity limits, any aviation at
  all (airports, ticketing, TSA, boarding, airliners), ATC job/radar
  minigame, subways/bullet trains/transit cards/timetables.

**Section 5 — Biology, Medical, Fitness & Lineage**
- Done: two damage/exposure sources (fall damage, rain exposure) each
  driving a status-effect proxy (Slowness; Nausea+Weakness), plus a
  Medicine item that cures those specific effects, sold at a Pharmacy
  Counter for real money.
- Missing: this is a *health-bar replacement disguised as a status
  effect*, not a real localized zone model — there's no per-body-part
  (head/torso/arms/legs) data structure, no bone-fracture-requiring-cast
  mechanic (medicine instantly cures, there's no cast/splint item or
  duration-based healing curve), no reduced tool-swing-speed/carry-weight/
  aim-stability from arm injuries, no diagnostic X-ray/surgery minigame,
  no real hospital *location* (a Pharmacy Counter block sells medicine,
  there's no hospital structure or admission flow), no macronutrient/
  calorie tracking, no weight-gain/stamina/sprint-speed consequences of
  diet, no gym/workout mechanics or body-mesh progression, no dating apps/
  bars/coffee-shop affinity system, no marriage/joint-bank-account
  mechanic (bank accounts are per-player only), no children/parenting/
  aging/inheritance system. NPCs never get sick or injured — every medical
  system implemented only affects players.

**Section 6 — Commercial Enterprises, Retail & Nightlife**
- Done: three "shop" blocks with a withdraw-or-refuse purchase pattern
  (Cash Register pays a wage rather than sells anything, Pharmacy Counter
  sells Medicine, License Office sells a hunting permit) — a narrow slice
  of "retail," not general commerce.
- Missing: grocery stores/shopping carts, furniture stores, clothing
  boutiques with a layered fashion/customization engine, bakeries, gun/
  ammo shops, phone/PC retail beyond the two items that exist, player-run
  businesses (buying commercial plots, setting prices on a POS UI, hiring
  NPC cashiers, automatic Friday payroll), casinos (roulette/blackjack/
  slots/poker), strip clubs/VIP lounges/nightclubs/DJ booths with
  proximity audio.

**Section 7 — Government, Law Enforcement, Courts & Underworld**
- Done: the most fleshed-out non-economy system — per-player wanted level
  with decay, fines at a threshold, automatic detainment (teleport +
  Blindness) at max level with a timed release that clears the record, all
  exposed live through a phone app.
- Missing: no actual police *NPCs* (no patrol AI, no chase, no tactical
  cover/spike strips/pit maneuvers — "arrest" is an instant, automatic
  teleport with no NPC agent involved), no municipal border/tax-rate
  system (`BankService` has no concept of city-specific sales/property/
  income tax), no real prison — no cell block, no yard, no prison jobs, no
  faction/contraband/breakout mechanics, no trial/court step before
  sentencing (arrest is immediate and automatic, not adjudicated), no
  civil courts (no lease/partnership contracts, no suing NPCs, no judge
  UI, no search warrants), no underworld/narcotics system (no dark web
  purchases, no chemical labs, no drug smuggling, no money laundering
  through front businesses) — the "dark web" referenced in Section 3 and
  the "underworld" here are both entirely unbuilt.

**Section 8 — Biomes, Ecology, Wildlife & Zoos**
- Done: a hunting-license permit system and a poaching penalty that
  reuses the existing crime pipeline — a law-enforcement-side stand-in,
  not an ecology system.
- Missing: no biome-specific mechanics at all (no multi-layer canopy/leaf
  decay/wildfires in forests, no machete-gated jungle thickets/equipment
  rust/malaria, no desert sand-dune physics/heatstroke/mirage/flash
  floods), no wildlife AI whatsoever (no predator scent-trail stalking, no
  prey herding/migration, "poaching" currently applies to *any* vanilla
  passive animal with no wildlife/livestock distinction and no warden NPCs
  — game wardens are simulated only as an automatic fine, not an agent),
  no zoo/safari system (no enclosures, HVAC, vet care, breeding, monorails,
  ticketing, gift shops).

**Section 9 — Utilities, Space & Industrial Supply Chains**
- Done: the most complete slice-for-slice implementation of any single
  section — real billing that can disconnect an account, and a lamp block
  that visibly goes dark within seconds of nonpayment, both exposed
  through a phone app with a manual pay option.
- Missing: no power *generation* (no plants of any kind — nuclear/solar/
  fossil — and no city-wide grid-stability simulation, "power" is purely
  an account flag, not a simulated grid), no water-tower hookups or
  consequence for missing one (no sinks that dry up), no cell-tower/phone-
  signal consequence for unpaid bills (the phone's battery/lock system
  from slice 2 is entirely separate from the utility system), no waste
  management (no trash generation, no garbage trucks, no landfills/
  recycling, no land-value or toxicity effects), no orbital/space content
  at all (no rockets, no GPS satellites, no orbital dimension, no Moon/
  Mars, no low-gravity mechanics) — every sentence of Section 9's back
  half is entirely unbuilt.

## Priority order for what's next

1. **Wire `VehiclePhysics` into an actual rideable entity** (Section 4) —
   custom `Entity`/`EntityType`, input capture, client-side model/
   renderer. The biggest "can't verify without a running client" risk of
   anything queued.
2. **NPCs as real entities**, not just database rows (Section 2) — the
   single highest-leverage gap, since almost every other section
   (commerce, dialogue, police chases, wildlife wardens) assumes NPCs
   exist as agents in the world.
3. **Real wildlife AI** to replace the crime-system stand-in (Section 8).
4. **Police NPCs / court/trial step** before an automatic arrest becomes
   an adjudicated one (Section 7).
5. Everything else in the "missing" lists above, then finally
   rendering/PBR, aviation/ATC, and the space program — deliberately
   last, as the largest and least incrementally verifiable pieces.

Each future slice follows the same pattern: a self-contained Java
package, unit tests where the logic doesn't require a running game
client, a `gradle build` check, and an update to this file before it's
committed.

## Building

```
gradle build   # compiles + runs unit tests, produces build/libs/realworldmod-<version>.jar
gradle test    # unit tests only
```

No external services, API keys, or network access are required at
**runtime** — everything above runs entirely against local SQLite files.
Network access is only used at *build time* to fetch Minecraft/Fabric/
Maven artifacts, same as any other Fabric mod.
