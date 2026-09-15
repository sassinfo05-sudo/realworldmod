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

## Done (21 slices so far, 127 unit tests, all passing)

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

- **Slice 18 — Visual assets for every registered block/item**: closes a
  gap that had gone unaddressed (and unmentioned) through 17 slices —
  every item and block added so far (`SMARTPHONE`, `LAND_DEED`,
  `MEDICINE`, `CASH_REGISTER`, `UTILITY_LAMP`, `PHARMACY_COUNTER`,
  `LICENSE_OFFICE`) had no model, blockstate, or texture, meaning they'd
  render as Minecraft's missing-texture purple/black checkerboard in an
  actual game session despite all their interaction logic working
  correctly. Added a distinct 16x16 placeholder texture per item/block,
  `item/generated`-based item models for the plain items, `cube_all`
  block models + blockstates for the plain blocks, and a lit/unlit
  blockstate variant pair for `UTILITY_LAMP` (mirroring vanilla's
  redstone lamp) so it now visibly swaps texture, not just luminance,
  when power is cut. Verified by parsing every new JSON file and opening
  every PNG (not just trusting a clean `gradle build`, since Gradle
  doesn't semantically validate Minecraft resource JSON).
  - **Known gap**: textures are simple placeholder pixel art (flat colors
    + basic shapes), not real art — this closes the "nothing renders"
    gap, not the "AAA visual fidelity" gap from Section 1.

- **Slice 19 — `CarEntity`: `VehiclePhysics` is finally driveable**
  (Section 4), the highest-risk slice of the project so far:
  - `CarEntity extends Entity`: reads the controlling passenger's public
    `forwardSpeed`/`sidewaysSpeed` fields each server tick, feeds
    `forwardSpeed` into `VehiclePhysics.tick` as throttle, turns at a
    fixed rate off `sidewaysSpeed` while moving, and applies the
    resulting velocity via `move(MovementType.SELF, ...)` with a simple
    constant-gravity fall when airborne. Fuel and speed persist through
    `readCustomDataFromNbt`/`writeCustomDataToNbt`.
  - `ModEntities.CAR`: real `EntityType` registration (`SpawnGroup.MISC`,
    1.4x1.0 bounding box).
  - Mounting: `Entity.interact` starts riding on an empty right-click if
    unoccupied; `ModItems.CAR_KEY` + `CarSpawnHandler` spawn one via
    `UseBlockCallback`, same event pattern as every other spawn-a-thing
    handler this session.
  - `CarEntityRenderer` (client): reuses
    `BlockRenderManager.renderBlockAsEntity` — the same mechanism vanilla's
    `FallingBlockEntityRenderer` uses for sand/gravel — scaled into a
    rough car silhouette, instead of writing custom cuboid model geometry.
  - **Why this one is different from every other slice**: every prior
    Minecraft-side integration (items, blocks, mixins, events, data
    components, networking) was verified by inspecting the actual 1.21.1
    mappings and, for the mixin, the generated refmap — real, but
    checkable without a running client. Whether this entity actually
    *feels* driveable (turn rate, acceleration feel, camera behavior),
    whether the placeholder block-as-entity visual is oriented/scaled
    correctly, and whether passenger positioning looks right cannot be
    confirmed the same way — only by actually launching the game. Treat
    it as implemented-but-unflown until someone (or a future session with
    client access) actually gets in and drives it.
  - **Known gaps**: no client-side movement prediction/reconciliation (a
    laggy connection would feel poor — moot in single-player, matters if
    this is ever played with others), no suspension/tire-friction-by-
    surface, no collision damage, no dismount key handling beyond
    whatever Minecraft's default riding controls provide, no fuel gauge
    or speed HUD (fuel/speed aren't networked to the client at all — they
    live only in the server-side entity), no actual car model/texture
    (placeholder vanilla concrete block), and only one vehicle type exists
    against the brief's 300+.

- **Slice 20 — `CitizenEntity`: NPCs become real, visible entities**
  (Section 2), the second entity-based slice, applying slice 19's
  "custom `Entity` + `BlockRenderManager`-as-renderer" pattern to
  something more consequential:
  - `CitizenEntity extends PathAwareEntity`: uses vanilla's off-the-shelf
    `WanderAroundGoal`/`LookAtEntityGoal`/`LookAroundGoal`/`SwimGoal` —
    real pathfinding and wandering behavior with zero custom AI code —
    registered with `FabricDefaultAttributeRegistry` (required for any
    `LivingEntity` subclass, unlike the plain-`Entity` car).
  - The entity's own UUID *is* its `NpcProfile` primary key — no separate
    synced link field needed. `NpcDatabase.findById` (new, replacing an
    O(n) `findAll` + filter scan; both are unit tested) looks up the
    profile on interact.
  - `NpcDialogue`: pure, unit-tested formatting of a one-line greeting
    that varies by the citizen's current `DailyState` — the first time
    the slice-1 daily-schedule data actually surfaces to the player.
  - `ModItems.CITIZEN_SPAWNER` + `CitizenSpawnHandler`: spawns the entity
    *and* creates its matching `NpcProfile` row in one action, so
    `NpcScheduleManager` picks it up on the very next tick.
  - `NpcAccess`: a new static holder, needed because (unlike
    `PropertyService`/`BankService`/`UtilityService`, which are stable
    wrapper objects reused across world reloads) `NpcDatabase` itself is
    replaced wholesale in `RealWorldMod`'s `SERVER_STARTING` handler each
    time a world loads — `CitizenSpawnHandler` registers its event
    listener exactly once at mod init but always reads the *current*
    database through this holder rather than a stale constructor-injected
    reference.
  - Same unverified-without-a-client caveat as slice 19: whether wandering
    actually looks reasonable, whether the placeholder box silhouette
    reads as a person, and whether pathfinding behaves near the mod's own
    blocks (cash registers, lamps, etc.) is unconfirmed.
  - **Known gaps**: see slice 21 below for movement; dialogue is one
    fixed line per state, not a branching tree; no NPC behavioral AI
    (crime, mugging, reacting to the player); NPCs still can't get sick,
    injured, arrested, or paid — every other system this session built
    (medical, crime, economy) only ever touches players.

- **Slice 21 — `CommuteGoal`: the daily schedule actually moves
  `CitizenEntity`** (Section 2), closing slice 20's biggest known gap:
  - `NpcProfile` gained six persisted `int` fields (`homeX/Y/Z`,
    `workplaceX/Y/Z`), cascaded through `NpcDatabase`'s schema, `upsert`,
    and `readRow`.
  - `CommuteTarget`: a pure, unit-tested function mapping each
    `DailyState` to `Destination.HOME`/`WORKPLACE`/`NONE`.
  - `CommuteGoal extends Goal`: a new custom goal (verified against the
    real `Goal`/`Goal.Control` mappings via `javap`, not guessed) that
    reads the citizen's live `NpcProfile` through `NpcAccess` each tick,
    calls `EntityNavigation.startMovingTo` toward the stored home or
    workplace coordinate when `CommuteTarget` calls for one, and yields
    (returns `false` from `canStart`/`shouldContinue`) once arrived or
    during `LEISURE` so it doesn't fight the existing `WanderAroundGoal`
    for navigation control. Registered in `CitizenEntity.initGoals()` at
    priority 1 — below `SwimGoal` (0) so citizens still surface for air,
    above `WanderAroundGoal` (now 2) so a schedule commute always wins
    over free wandering.
  - `CitizenSpawnHandler` now sets a new citizen's home to its spawn
    position and its workplace to a fixed 24-block offset from it, so
    every spawned citizen has somewhere real to commute to immediately.
  - **Known gaps**: home/workplace are still bare coordinates, not actual
    house/workplace structures (a "WORKING" citizen walks to an empty
    point in the world); no arrival behavior beyond stopping (no sitting,
    working animation, or job-task mini-behavior once at the workplace);
    unverified without a running client whether the walk actually looks
    right or whether citizens get stuck on the mod's own blocks en route.

- **Slice 22 — `DeerEntity`: real wildlife AI** (Section 8), replacing the
  "any vanilla passive animal counts as game" stand-in from slice 17:
  - `DeerEntity extends PathAwareEntity`, same architectural shape as
    `CitizenEntity`/`CarEntity`, rendered via `BlockRenderManager` as a
    scaled brown-terracotta box (placeholder, not real art).
  - `WildlifeBehavior`: a pure, unit-tested helper holding the flee-trigger
    and herd-crowding distance thresholds plus a centroid-averaging
    function — the same "pure logic separate from the `Goal`" split
    `CommuteTarget`/`CommuteGoal` used in slice 21.
  - `FleeFromPlayerGoal`: makes a deer run from the nearest player once
    within range, *before* being hit — a genuine behavioral difference
    from every vanilla passive mob, which only ever flees after damage.
  - `HerdWithOthersGoal`: finds nearby `DeerEntity` instances via
    `World.getEntitiesByClass` and drifts toward their average position,
    a real (if simple) take on "prey herding," lower priority than fleeing
    so a scare always breaks up the herd's drift.
  - `PoachingHandler` now checks `instanceof DeerEntity` instead of
    `instanceof AnimalEntity`, fixing the wildlife/livestock distinction
    called out as missing since slice 17 — killing a cow or pig is no
    longer a crime.
  - `DeerSpawnHandler` + `ModItems.DEER_SPAWNER`: item-triggered spawning,
    mirroring `CitizenSpawnHandler`, since the mod still has no
    biome-based natural spawning for anything.
  - Every new Minecraft API surface touched (`World.getClosestPlayer`,
    `World.getEntitiesByClass`, `Box.expand`, `Entity.squaredDistanceTo`)
    was verified via `javap` against the real mappings before use.
  - **Known gaps**: see the updated Section 8 status above — no predator
    AI, no true migration, item-spawned only, no warden-NPC agent;
    unverified without a running client whether flee/herd movement looks
    natural rather than jittery.

- **Slice 23 — `PoliceEntity` and a real court/trial step before arrest**
  (Section 7), closing the "no police NPCs, no trial before sentencing"
  gap open since slice 16's automatic detainment:
  - `PoliceEntity extends PathAwareEntity`, same architectural shape as
    `CitizenEntity`/`DeerEntity`, rendered as a scaled blue-concrete box.
  - `PoliceBehavior`: a pure, unit-tested helper (`shouldChase`,
    `canApprehend`) — the same "pure logic separate from the `Goal`"
    split every custom AI goal in this mod now uses.
  - `ChaseWantedPlayerGoal`: pursues the nearest player once
    `PoliceBehavior.shouldChase` says they're "Wanted" (level 3+), and on
    contact while still at maximum wanted level hands off to a new
    `TrialService` instead of arresting directly.
  - `TrialService` + `TrialVerdict`: a fixed-length (30-second) trial
    starts on apprehension; the verdict is decided against the wanted
    level *at the trial's end* (which keeps decaying the whole time via
    the existing `CrimeService.tick`), not the level at capture — staying
    clean (or lucky) through the whole trial gets you acquitted instead of
    convicted on stale evidence.
  - `ArrestService` was rewired around this: reaching max wanted level no
    longer arrests anyone by itself (`ArrestServiceTest` updated
    accordingly) — only a `GUILTY` verdict from `TrialService.tick` starts
    detention; `NOT_GUILTY` produces a new `ArrestOutcome.ACQUITTED`,
    handled by `ArrestHandler` with its own message.
  - `TrialAccess`/`ArrestAccess`: new static holders (same pattern as
    `CrimeAccess`/`NpcAccess`) so `ChaseWantedPlayerGoal` — instantiated by
    `EntityType.Builder` with no constructor-injection path — can reach
    the live `TrialService`/`ArrestService` to avoid re-apprehending an
    already-detained or already-on-trial player.
  - `PoliceSpawnHandler` + `ModItems.POLICE_SPAWNER`: item-triggered
    spawning, mirroring `CitizenSpawnHandler`/`DeerSpawnHandler`.
  - Every new Minecraft API surface touched (`EntityNavigation.
    startMovingTo(Entity, double)`, `World.getTime()`) was verified via
    `javap` against the real mappings before use.
  - **Known gaps**: see the updated Section 7 status above — no tactical
    AI, no real courtroom/judge simulation behind the verdict, item-spawned
    only, no undercover variant yet; unverified without a running client
    whether the chase actually looks like a chase.

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
- Nothing implemented requires a `/`-command to use — every system is
  reached through normal gameplay (items, blocks, entity interaction,
  the phone UI). This was raised explicitly as a requirement and already
  holds; it stays a hard constraint for everything still to build.

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
  for construction beyond owning a deed don't exist. **Requested and
  tracked, not started**: a working construction mechanic (actually
  building/renovating a structure as its own gameplay loop, distinct from
  just placing individual blocks); a single huge main city plus ten
  smaller cities worldwide, surrounded by real terrain/nature, where
  every single building and house has a defined in-game purpose and is
  fully decorated (today the mod adds a handful of standalone functional
  blocks — cash register, pharmacy counter, license office, lamp — with
  no surrounding structures or city layout at all); random/special events
  occurring over the course of play, beyond the systems already listed.
  (The "no commands needed" requirement raised alongside this already
  holds today — see the cross-cutting notes above.)

**Section 1 — Rendering, Physics & Graphics Engine**
- Done: nothing. This is explicitly the "largest, latest" bucket.
- Missing: PBR/ray-traced lighting, volumetric fog, water refraction,
  displacement mapping — all of it (needs a custom shader/rendering
  pipeline, unverifiable without a running game client); the 1/16th
  sub-grid interior decoration system (arbitrary-angle furniture
  placement) — not started; background-thread macro-economics/weather/
  commute simulation for unrendered regions — not started (today's
  simulation runs only for online players/loaded chunks via the normal
  server tick, not a separate async layer). **Requested and tracked, not
  started**: full, high-quality realistic animations and textures for
  every item/block/entity (today's textures are simple 16x16 placeholder
  pixel art from slice 18, and both entities use a scaled vanilla block
  as a body with no animation at all — no walk cycle, no idle animation,
  nothing); working window curtains, street lights, and other small
  world-detail props that toggle/animate on their own.

**Section 2 — Autonomous Citizen & NPC Engine (GOAP)**
- Done: persistent SQLite citizen records, a deterministic (not GOAP)
  daily-schedule FSM cycling through 6 states, a real, spawnable
  `CitizenEntity` in the world whose UUID links back to that database row
  with a one-line greeting that reflects its current schedule state, and —
  as of slice 21 — a `CommuteGoal` that actually reads the live
  `DailyState` and walks the entity to a stored home or workplace
  coordinate (`CommuteTarget` maps SLEEPING/WAKING/COMMUTING_HOME→home,
  COMMUTING_TO_WORK/WORKING→workplace, LEISURE→no destination, falling
  back to the existing `WanderAroundGoal`), registered above wander but
  below swim so Minecraft's own goal-control arbitration hands off
  navigation cleanly.
- Missing: home/workplace coordinates are still just the spawn point and a
  fixed 24-block offset (there are no real home/workplace *structures* to
  path to yet, so a "WORKING" citizen walks to an empty point in the
  world, not into an actual building); no fridge/breakfast/commute-by-vehicle
  animation, no job-task
  mini-behaviors (cashiering, patrols, factory work), no evening leisure
  destinations, no branching dialogue tree (one fixed line per state), no
  NPC behavioral AI (mugging, reacting to red-light running, independent
  crime, police chases) — and NPCs are entirely exempt from every other
  system this session built (economy, medical, crime): only players can
  earn, get sick, get hurt, or get arrested. True GOAP (goal-oriented
  action planning, i.e. dynamic plan search over actions) was never
  implemented — the FSM is a simpler deterministic rule tree, called out
  as such in the code's own Javadoc from slice 1 onward. **Requested and
  tracked, not started**: individual NPC personalities/dispositions
  (some criminal, some predatory/"perverts", some kind, some rude, some
  short-tempered, etc. — every `CitizenEntity` today is behaviorally
  identical); children as a distinct NPC category with their own social
  dynamics (bullies, a bullied child's parents getting involved); NPCs
  that can be kidnapped (and a player-facing kidnapping mechanic, of
  either the player or a random citizen); criminal organizations with
  NPC membership and player-vs-organization rivalry; "opponents" in the
  general sense (competing NPCs/businesses/rivals reacting to the
  player's actions across systems, not just combat).

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
  **Requested and tracked, not started**: every phone/PC app being a
  genuinely custom-drawn UI rather than Minecraft's vanilla `Screen`/
  `ButtonWidget` GUI toolkit (today's five apps *are* real client↔server
  screens, but they're built from vanilla widgets, not a distinct in-game
  "OS" look) — and this same "not vanilla Minecraft widgets" bar applies
  to every other UI in the mod (banking, courts, casino, business
  management, etc.), not just the phone; in-game "AI" chatbot-style
  websites/apps; the ability for a player to build/code their own
  website or business complete with simulated traffic, marketing, and
  hireable NPC workers; becoming a multi-platform content creator (the
  brief's BlockTube, above, generalized to several distinct platforms).

**Section 4 — Automotive, Aviation & Global Transit**
- Done: `VehiclePhysics` (pure drivetrain simulation) wired into a real,
  spawnable, rideable `CarEntity` with a placeholder visual — see slice
  19. One vehicle type exists; its feel/visuals are unverified without a
  running client (see slice 19's own caveat above).
- Missing: the other 299+ vehicle types (including, specifically
  requested: motorcycles, bicycles, e-bikes, skateboards, rollerblades,
  boats and kayaks, cargo ships, cargo/military planes, military ships,
  tanks, helicopters, public buses), mechanic shops/tuning/paint/
  body-damage repair, garage capacity limits, any aviation at all
  (airports, ticketing, TSA, boarding, airliners), ATC job/radar
  minigame, subways/bullet trains/transit cards/timetables. The one
  vehicle that exists also has no fuel gauge/speed HUD, no suspension/
  tire-friction modeling, and no collision damage. **Requested and
  tracked, not started**: any AI-controlled traffic at all — no other
  cars/planes/helicopters share the roads or sky with the player; no
  speed-check/radar-gun mechanic, no bumper/collision-damage system
  between vehicles, no hidden/undercover police vehicles, no in-vehicle
  radio, and no mechanic for stealing cars or car keys from an NPC or
  parked vehicle.

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
  system implemented only affects players. **Requested and tracked, not
  started**: hunger/thirst as an actual survival requirement (the mod
  doesn't touch vanilla hunger at all right now); steroids/muscle-building
  as a distinct mechanic from the general gym/fitness gap above; alcohol
  and cigarettes as consumable items with real effects.

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
  proximity audio. **Requested and tracked, not started**: a casino that
  actually functions end-to-end (games with real rules and real payouts
  against `BankService`, not just the category existing); real
  wealth-tier recognition (nothing currently distinguishes or reacts to
  a player being a "millionaire" or "billionaire" — `BankService` just
  stores an unbounded `long`); public parks as a distinct, purposeful
  location type; cigarettes and alcohol as sellable retail items (see
  also Section 5).

**Section 7 — Government, Law Enforcement, Courts & Underworld**
- Done: per-player wanted level with decay, fines at a threshold, and — as
  of slice 23 — a real `PoliceEntity` (`PathAwareEntity`) that patrols and
  actively chases a player once `PoliceBehavior.shouldChase` says they're
  "Wanted" or worse, apprehending them on contact if they're still at
  maximum wanted level (`PoliceBehavior.canApprehend`). Apprehension no
  longer detains anyone directly: it starts a `TrialService` trial (a
  genuine, if minimal, "court/trial step before sentencing" — the
  ROADMAP-tracked gap this closes), and only a `GUILTY` verdict at the
  trial's end — checked against whatever the wanted level has decayed to
  by then, not the level at capture — actually detains the player
  (teleport + Blindness) for a timed sentence; `NOT_GUILTY` acquits them
  outright. All of it, plus the trial/verdict flow, is exposed live
  through the existing phone app and `ArrestHandler` messages.
- Missing: `PoliceEntity` only patrols/chases — no tactical cover, spike
  strips, pit maneuvers, backup calls, or squad coordination; deer/police
  spawn only via items (`POLICE_SPAWNER`), not real police-station
  structures or patrol routes; the trial's verdict logic is a single
  wanted-level check, not an actual evidence/witness/judge simulation
  (there is no judge NPC, no courtroom, no defense); no municipal
  border/tax-rate system (`BankService` has no concept of city-specific
  sales/property/income tax); no real prison — no cell block, no yard, no
  prison jobs, no faction/contraband/breakout mechanics; no civil courts
  (no lease/partnership contracts, no suing NPCs, no judge UI, no search
  warrants); no underworld/narcotics system (no dark web purchases, no
  chemical labs, no drug smuggling, no money laundering through front
  businesses) — the "dark web" referenced in Section 3 and the
  "underworld" here are both entirely unbuilt. **Requested and tracked,
  not started**: running for and holding government office (up to
  leading the whole in-game country); terrorism attacks that occur
  dynamically as the game progresses and get repaired afterward (also
  needs the "construction actually works" gap below); undercover police
  NPCs indistinguishable from civilians until they act (now buildable as
  a `PoliceEntity` variant that doesn't render as one); kidnapping (of the
  player or of a random citizen); a defined path for the player to
  "become a criminal" as a real career/reputation track, not just an
  accumulating wanted level; illuminati-style secret societies and cults
  as a distinct faction type from ordinary criminal organizations.

**Section 8 — Biomes, Ecology, Wildlife & Zoos**
- Done: as of slice 22, a real `DeerEntity` (`PathAwareEntity`) is the
  mod's first genuine wildlife AI — `FleeFromPlayerGoal` makes it run from
  an approaching player on proximity alone (unlike vanilla passive mobs,
  which only flee once actually hit), and `HerdWithOthersGoal` drifts it
  toward the average position of nearby deer (a real, if simple, take on
  "prey herding"), both backed by a pure unit-tested `WildlifeBehavior`
  helper. `PoachingHandler` now only fires on killing a `DeerEntity`,
  fixing the old "any vanilla animal counts as game" stand-in — vanilla
  livestock (cows, pigs, chickens, sheep) is no longer poachable. Plus the
  pre-existing hunting-license permit system and poaching penalty that
  reuses the crime pipeline.
- Missing: no biome-specific mechanics at all (no multi-layer canopy/leaf
  decay/wildfires in forests, no machete-gated jungle thickets/equipment
  rust/malaria, no desert sand-dune physics/heatstroke/mirage/flash
  floods); no predator AI (only prey flee/herd behavior exists, nothing
  stalks or hunts anything); no migration (herding is proximity-only, not
  a seasonal or territorial routine); deer only spawn via a
  `DEER_SPAWNER` item, not natural biome-based spawning; still no warden
  NPCs as agents (poaching remains an automatic fine, not a chasing
  game-warden entity — closely related to item 2 in "Priority order"
  below); no zoo/safari system (no enclosures, HVAC, vet care, breeding,
  monorails, ticketing, gift shops). **Requested and tracked, not
  started**: abandoned towns as a distinct, generated location type.

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

1. **Game-warden NPC for poaching** (Section 8) — `PoliceEntity`/
   `ChaseWantedPlayerGoal` from slice 23 is directly reusable for this: a
   warden entity that chases a player caught poaching is the same shape
   as a police entity chasing a wanted one, closing the "warden NPCs are
   simulated only as an automatic fine, not an agent" gap explicitly
   called out in Section 8 twice now.
2. Everything else in the "missing" lists above — diversify out of the
   `PathAwareEntity`/custom-`Goal` NPC pattern (slices 20-23 all used it)
   into a different section next, then finally rendering/PBR, aviation/
   ATC, and the space program — deliberately last, as the largest and
   least incrementally verifiable pieces.

(Slice 21 closed out daily-schedule-driven `CitizenEntity` movement —
see Section 2 above. Slice 22 closed out real wildlife AI — see Section 8
above. Slice 23 closed out police NPCs and a real court/trial step — see
Section 7 above.)

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
