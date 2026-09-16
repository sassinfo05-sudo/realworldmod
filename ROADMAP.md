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

- **Slice 24 — real humanoid entity models, replacing block-placeholder
  rendering** (Section 1), in response to a direct request to stop
  everything looking like scaled vanilla blocks:
  - `HumanoidEntityModel<T>`: a real, hand-built `SinglePartEntityModel`
    with a proper head/body/right-arm/left-arm/right-leg/left-leg
    `ModelPart` hierarchy (the same cuboid-hierarchy technique vanilla
    uses for its own mobs, verified cuboid-by-cuboid against the real
    `ModelPartBuilder`/`ModelData`/`TexturedModelData` API via `javap`),
    plus a real walk-cycle (leg/arm swing driven by `limbAngle`/
    `limbDistance`) and head-yaw/pitch tracking in `setAngles` — the
    mod's first entity animation of any kind.
  - `CitizenEntityRenderer` and `PoliceEntityRenderer` were rewritten from
    scratch to extend `LivingEntityRenderer` and use this shared model
    (each with its own texture) instead of `BlockRenderManager.
    renderBlockAsEntity` on a scaled vanilla block — the placeholder
    technique slices 19-23 all used.
  - Two hand-painted, correctly-UV-mapped 64x64 skin-style textures
    (`citizen.png`, `police.png`) replacing solid-color vanilla block
    textures: distinct skin tone, clothing colors, and a simple face for
    each, plus a cap and badge for `police.png`.
  - **Honesty check on the "10/10, nothing looks like Minecraft" ask this
    closes part of**: this is a genuine, shaped body with real animation
    now — not a box — but it's still pixel-art-resolution texture work
    generated by script, not hand-crafted high-fidelity art, and it's
    still built from Minecraft's own cuboid-model system rather than an
    external rendering pipeline, so "nothing looks like Minecraft" is not
    accurate yet. `DeerEntity` and `CarEntity` were *not* touched this
    slice and still use the block-placeholder technique (a quadruped
    model and a vehicle model are each their own follow-up); no block or
    item textures changed either (still slice 18's placeholder pixel
    art); nothing here is verified visually without a running game
    client. See the updated Section 1 status below for the honest
    running tally of what "10/10 assets, nothing looks like Minecraft"
    would actually still require.

- **Slice 25 — LabPBR normal/specular maps for every existing texture**
  (Section 1), in response to a direct request to "add shaders and PBR":
  - What this is *not*: a custom shader/rendering pipeline written into
    the mod. That would mean raw GLSL hooked into Minecraft's renderer
    via Mixin, with zero way to compile-test or visually verify it
    without a GPU-attached running client — a broken shader mixin doesn't
    just render wrong, it can take down the entire render thread. That
    risk wasn't taken.
  - What this actually is, and how "PBR" genuinely exists in the
    Minecraft ecosystem: a `_n.png` (normal) and `_s.png` (specular) map
    next to every one of the mod's existing textures, in the
    [LabPBR](https://shaderlabs.org/wiki/LabPBR_Material_Standard) format
    that Iris and every modern shader pack (Complementary, BSL, etc.)
    already read by filename convention — no Java code, no model JSON
    changes, nothing to register. The actual PBR lighting math is done by
    whatever shader pack the *player* runs; this just supplies the
    material data for it to consume.
  - Normal maps are derived per-texture via a Sobel filter over each
    base texture's own luminance (a standard "fake bump from a diffuse
    image" technique, since there's no real height/3D data to sample —
    documented in the generation script itself). Specular maps encode
    smoothness/F0/porosity/emissive per texture, with the lit
    `utility_lamp_on` texture the only one actually marked emissive
    (the previously "just visibly changes state" lamp texture would now
    genuinely glow under a PBR shader pack).
  - **Known gaps**: normal maps are inferred from 2D texture detail, not
    real geometry, so the bump is subtle and approximate, not sculpted;
    every specular value is a reasonable guess, not measured/authored per
    material; nothing here has been visually confirmed — that requires a
    running client with Iris and a PBR shader pack installed, neither of
    which this session has; this covers every *existing* texture but adds
    no new ones, so it doesn't move the "10/10, hundreds of assets"
    backlog at all — only the "PBR" half of the last two requests.

- **Slice 26 — real quadruped model for `DeerEntity`** (Section 1),
  continuing straight on from slice 24's humanoid model into the other
  entity slice 24 explicitly deferred:
  - `DeerEntityModel<T>`: a body/head/two-antler/four-leg `ModelPart`
    hierarchy (verified via the same `javap`-checked
    `ModelPartBuilder`/`ModelData`/`TexturedModelData` API as slice 24),
    with a diagonal-trot walk cycle — front-right paired with back-left,
    front-left paired with back-right, the standard approximation every
    vanilla quadruped mob model uses — replacing slice 22's scaled
    brown-terracotta block.
  - `DeerEntityRenderer` rewritten to extend `LivingEntityRenderer` with
    this model, same pattern as `CitizenEntityRenderer`/
    `PoliceEntityRenderer`.
  - A hand-painted, correctly-UV-mapped `deer.png` (reddish-brown fur,
    a lighter underside patch, dark hooves, antlers) plus its LabPBR
    `_n`/`_s` maps generated the same way as slice 25's.
  - **Known gaps**: still Minecraft's own blocky cuboid style, not
    sculpted 3D art; `CarEntity` is the one entity left on the
    block-placeholder technique (a wheeled vehicle body is a distinct
    shape from a biped/quadruped and needs its own model, not a reuse
    of either); nothing here is verified visually without a running
    client, including whether the trot cycle actually reads as a deer
    walking rather than four legs swinging independently.

- **Slice 27 — real vehicle model for `CarEntity`, closing out the
  block-placeholder backlog across every entity** (Section 1):
  - `CarEntity` gained its first synced state ever: a `TrackedData<Float>`
    wheel-rotation angle, accumulated server-side each tick from the
    vehicle's actual speed (`radians = speedBlocksPerTick / wheelRadius`,
    wrapped mod `MathHelper.TAU`) and automatically synced to the client
    via `DataTracker` — closing the "no synced fields yet" gap the
    class's own Javadoc called out since slice 19, and giving the wheels
    something real to spin from instead of a client-side guess.
  - `CarEntityModel`: a chassis + raised cabin + four independently
    rotating wheels, the same verified `ModelPart` technique as slices
    24/26. `CarEntity` is a plain `Entity`, not a `LivingEntity`, so this
    model is driven by a hand-written `CarEntityRenderer.render()`
    override (yaw rotation, `model.setAngles`, `model.render`) rather
    than `LivingEntityRenderer` — the two humanoid/quadruped renderers
    get that machinery for free, this one doesn't.
  - A hand-painted, correctly-UV-mapped `car.png` (red paint, tinted
    glass cabin, headlights/taillights, dark tires with a rim highlight)
    plus its LabPBR `_n`/`_s` maps.
  - **Every entity in the mod now has a real shaped model with real
    animation** — `CitizenEntity`, `PoliceEntity`, `DeerEntity`, and
    `CarEntity` all replaced the "scaled vanilla block" placeholder that
    every entity used through slice 23. See Section 1 below for what
    "10/10, nothing looks like Minecraft" would still require beyond
    this.
  - **Known gaps**: still Minecraft's own blocky cuboid style, not
    sculpted 3D art; the model's proportions (chassis width, wheel
    placement) are reasoned from the entity's registered hitbox
    dimensions, not measured against a real reference, since there's no
    way to render and eyeball it; nothing here is verified visually
    without a running client, including whether the ground-contact math
    (wheel-bottom at the entity's actual world position, everything else
    negative-Y/"up" from there) is actually right for a renderer that —
    unlike the other three — gets no automatic positioning help from
    `LivingEntityRenderer`.

- **Slice 28 — `GameWardenEntity`: a real agent for poaching enforcement**
  (Section 8), closing the item at the top of the "Priority order"
  section below:
  - `GameWardenService`: a fixed 45-second poaching alert window per
    player (`flagPoacher`/`isFlagged`/`apprehend`), deliberately separate
    from `CrimeService`'s single overall wanted-level number, which has
    no per-offense-type breakdown and so can't represent "wanted
    specifically for poaching" — unit tested against a real `BankService`
    the same way `LawEnforcementServiceTest` is.
  - `PoachingHandler` now flags the poacher in `GameWardenService` in
    addition to its existing crime-pipeline fine, so an unlicensed kill
    has two independent consequences: the immediate fine, and a real
    entity that comes looking for you.
  - `GameWardenEntity` + `ChasePoacherGoal`: the same
    `PathAwareEntity`/custom-`Goal` chase-and-apprehend shape
    `PoliceEntity`/`ChaseWantedPlayerGoal` used in slice 23, reused for a
    different trigger condition — evading a warden for the full alert
    window means no further consequence; getting caught means a real fine
    on contact (`GameWardenService.APPREHENSION_FINE_CENTS`).
  - Reuses slice 24's `HumanoidEntityModel` with its own hand-painted
    forest-green ranger-uniform texture (`game_warden.png`, wide-brim hat,
    badge) plus LabPBR maps, rather than building a fourth body shape from
    scratch — the model system built for Section 1 already generalizes.
  - **Known gaps**: see the updated Section 8 status above — item-spawned
    only, no radio/backup/vehicle patrol, no investigation step; unverified
    without a running client whether the chase-and-catch actually reads
    right in play.

- **Slice 29 — municipal sales tax on existing purchases** (Section 7):
  - `economy.SalesTax`: a pure, unit-tested flat 8% rate calculator —
    deliberately *not* the "city-specific" rate system the ROADMAP gap
    describes, since no city boundaries exist anywhere in the world yet
    for a rate to vary by.
  - `BankService.TREASURY_ACCOUNT_ID` + `remitSalesTax(long)`: a reserved
    account and a one-line helper so a purchase handler can remit its tax
    cut in a single call; unit tested the same way every other
    `BankService` behavior is.
  - `DeedUseHandler`, `PharmacyUseHandler`, and `LicenseUseHandler` — the
    mod's three existing paid purchases — now all remit their tax cut on
    a successful sale. Previously the *entire* price of every purchase in
    the mod simply vanished on withdrawal with no corresponding deposit
    anywhere; this doesn't fix that for the remainder (there's still no
    business-ownership model for who'd receive it), but the tax portion
    specifically is now real, tracked money sitting in an account rather
    than disappearing.
  - **Known gaps**: one flat rate everywhere, not per-city; only covers
    sales-style purchases, not property or income tax; the treasury
    balance isn't exposed anywhere yet (no phone app, no admin/government
    UI) — it accumulates but nothing reads it back; the remaining
    non-tax portion of every purchase still vanishes into nothing rather
    than reaching a real payee.

- **Slice 30 — real home/workplace structures for `CitizenEntity`**
  (Section 2), closing the gap called out since slice 21:
  - `StructureBuilder`: places a small rectangular room (walls, a
    doorway gap, a flat roof) block-by-block at a given coordinate —
    `buildHouse` for a citizen's home, `buildWorkplace` for a visually
    distinct version with a `ModBlocks.CASH_REGISTER` fixture placed
    inside it.
  - `CitizenSpawnHandler` now calls both when spawning a citizen, so
    `CommuteGoal` walks them into an actual building at each end of their
    commute instead of a bare point in the world.
  - **Known gaps**: every citizen's building is identical (one fixed
    shape/size, no interior detail beyond the workplace's single fixture);
    no terrain/water/overlap checking before placing blocks, so a
    structure can spawn floating, submerged, or cutting through existing
    claims or another citizen's building; not unit tested (like every
    other world-block-placement handler in the mod, this needs a real
    `ServerWorld` to run against, not something a plain JUnit test can
    exercise); unverified without a running client whether the doorway
    gap actually pathfinds correctly for `CommuteGoal`.

- **Slice 31 — property and income tax alongside sales tax** (Section 7),
  finishing the three-tax-type set slice 29 started:
  - `economy.IncomeTax`: a pure, unit-tested flat 5% rate calculator,
    withheld from every `JobService.tryWorkShift` payout before it
    reaches the player and remitted to the treasury —
    `JobService.NET_WAGE_CENTS` is what a player actually takes home now,
    and `JobServiceTest`/`JobUseHandler`'s wage message were both updated
    to reflect it instead of the pre-tax gross.
  - `property.PropertyTaxService`: a periodic (once-per-in-game-day) tax
    on every land claim, proportional to its area
    (`RATE_CENTS_PER_BLOCK`), following the same tick-bucket pattern
    `CrimeService`/`NpcScheduleManager` already use; an owner who can't
    afford it is skipped that cycle rather than the withdrawal failing
    loudly.
  - `BankService.depositToTreasury(long)`: a small refactor extracting
    the "deposit an already-computed tax amount" step `remitSalesTax` did
    inline, so `PropertyTaxService`/`JobService` (which compute their own
    tax amounts rather than a percentage-of-price like sales tax) can
    reuse it directly.
  - **Known gaps**: see the updated Section 7 status above — still one
    flat rate everywhere for all three tax types, no forfeiture/seizure
    for unpaid property tax; the treasury balance itself wasn't exposed
    anywhere a player could see it until slice 32.

- **Slice 32 — a Government phone app exposing the treasury balance**
  (Section 3/7), closing the gap slices 29 and 31 both called out:
  - `PhoneApp.GOVERNMENT` + `GovernmentAppScreen`: requests and displays
    `BankService.TREASURY_ACCOUNT_ID`'s balance — the exact same
    request/render shape `BankingAppScreen` already used, just pointed at
    the government account instead of the player's own.
  - `TreasuryBalanceRequestPayload`/`ResponsePayload` +
    `TreasuryNetworking`: mirror `BankNetworking`'s payload/handler
    shape exactly, registered alongside it in `RealWorldMod.onInitialize`.
  - `ClientTreasuryState`: the client-side cache, same shape as
    `ClientBankState`.
  - **Known gaps**: read-only — a player can see the treasury total but
    there's still no admin/government UI to spend it on anything, no
    breakdown by tax type (sales vs. income vs. property), and no way for
    a non-government player to interact with it at all; unverified
    without a running client whether the screen actually renders
    correctly (same caveat as every other phone app in the mod).

- **Slice 33 — a real slot-machine casino game** (Section 6), the first
  actual game behind the "casino" category:
  - `SlotMachine`: pure, exhaustively unit-tested game logic — three
    reels drawn from a 4-symbol set, and a real payout table (three
    sevens 10x, bars 5x, bells 3x, cherries 2x; any two matching is a
    push; no match loses the bet), not a placeholder RNG with fake
    numbers.
  - `SlotMachineUseHandler` + `ModBlocks.SLOT_MACHINE`: the same
    withdraw-then-resolve `BankService` pattern every other paid
    interaction in the mod uses — a fixed bet is withdrawn up front, then
    a win pays out a real multiple of it, a push returns it, and a loss
    keeps it withdrawn.
  - **Known gaps**: one game, one fixed bet size, no roulette/blackjack/
    poker (the rest of "a casino" from the brief); a losing bet simply
    vanishes rather than reaching a tracked "house"/casino-owner account
    the way slice 29's sales tax reaches the treasury; no slot-machine
    visual/animation beyond a static block texture; unverified without a
    running client.

- **Slice 34 — a real roulette game, the casino's second table**
  (Section 6):
  - `Roulette`: pure, unit-tested logic for a real 38-pocket American
    wheel — `0` and `00` as green, `1`-`36` colored by the actual standard
    red/black assignment (not an arbitrary split), and a `colorBetWins`
    rule where green always loses a color bet — the house's genuine edge
    on this bet type, not a fudged probability.
  - `RouletteUseHandler` + `ModBlocks.ROULETTE_TABLE`: right-click bets
    red, sneak-right-click bets black, same withdraw-then-resolve
    `BankService` pattern as `SlotMachineUseHandler` — a win pays real
    1:1, a loss (including green) keeps the bet.
  - **Known gaps**: color bets only — no number, split, street, or other
    real roulette bet types; same fixed-bet-size and vanishing-loss gaps
    as slice 33's slot machine; still no blackjack/poker/craps; unverified
    without a running client.

- **Slice 35 — real interactive blackjack, the casino's third table**
  (Section 6), the mod's first multi-step casino game rather than a
  single right-click:
  - `BlackjackGame`: a pure engine with real rules, not simplified ones —
    Ace-aware hand scoring (counts as 11 unless that would bust, then
    demotes to 1, checked one Ace at a time so a hand with multiple Aces
    demotes only as many as needed), a dealer that hits on any total
    below 17 and stands otherwise, a natural-blackjack check that
    resolves the round on the deal itself when the player draws a
    two-card 21, and a real outcome table (`PLAYER_BLACKJACK` at 3:2,
    `PLAYER_WIN` at even money, `PUSH` returns the bet, `DEALER_WIN`
    forfeits it) — exhaustively unit-tested including a deterministic
    "rigged `Random`" test harness so dealer-hits-until-17 and the ace
    soft/hard logic could be asserted exactly rather than only
    statistically.
  - `BlackjackService`: per-player session tracking (one round at a time,
    a resolved round stays visible until a new one starts) over the same
    withdraw-then-payout `BankService` pattern every other paid
    interaction in the mod uses.
  - A real multi-message networking protocol — `BlackjackStatePayload`
    (state on screen open, so closing and reopening mid-round doesn't
    lose track of it), `BlackjackStartPayload`/`HitPayload`/`StandPayload`
    (C2S actions), and `BlackjackStateResponsePayload` (S2C, concealing
    the dealer's hole card until the round resolves — the real "hidden
    card" rule, not just omitted for no reason) — a step up from every
    earlier phone-app payload pair, since this is the mod's first
    genuinely multi-step, multi-message game rather than one request and
    one response.
  - `BlackjackScreen`: the mod's first interactive, stateful game UI
    (Deal/Hit/Stand `ButtonWidget`s that enable/disable based on whether
    a round is in progress) rather than a read-only display or a single
    action button.
  - **Known gaps**: no double-down, split pairs, insurance, or surrender;
    draws with replacement from an infinite shoe rather than a finite
    deck (defensible for a game with no card-counting mechanic, but not
    how a real physical shoe works); one fixed bet size; a losing bet
    vanishes rather than reaching a tracked house account; the screen's
    button enable/disable logic and hand rendering are unverified without
    a running client, the same caveat as every other UI in the mod.

- **Slice 36 — a real civil small-claims court** (Section 7), the next
  item on the priority list and deliberately separate from the criminal
  system slice 23 built:
  - `CivilCourtService`: `fileClaim`/`contest`/`tick`, following the same
    tick-bucket-deadline shape `TrialService` uses, but with none of that
    system's machinery — no wanted level, no police, no punishment, just
    one player claiming money from another. A defendant who doesn't
    contest within the response window gets a real default judgment
    (the actual legal term for a ruling entered because the defendant
    failed to respond in time) — `BankService.transfer` moves the money
    automatically, or simply fails if the defendant can't afford it (a
    judgment can be uncollectible in reality too), either way closing
    the case.
  - `CivilCourtHandler` + `ModBlocks.COURTHOUSE`: right-click files a
    claim against the nearest other player; if the interacting player
    is themselves a pending defendant, that check runs first and
    contesting takes priority over filing a new claim — no text-entry UI
    needed, reusing the same nearest-player targeting
    `ChaseWantedPlayerGoal`/`ChasePoacherGoal` already established for
    aim-based interaction.
  - Exhaustively unit tested: filing succeeds/fails correctly (duplicate
    defendant, self-claim), contesting dismisses and reopens eligibility,
    ticking before/after the deadline, and the judgment-closes-the-case-
    regardless-of-affordability behavior.
  - **Known gaps**: see the updated Section 7 status above — no named
    defendant (nearest-player only), one fixed claim amount, no real
    adjudication (contesting just dismisses, it isn't a defense that gets
    weighed against anything), no lease/partnership contracts, no judge
    NPC or courtroom; unverified without a running client.

- **Slice 37 — underworld narcotics dealing tied into the crime system**
  (Section 7), the other half of the priority list's slice-36 choice and
  the first real content in the previously entirely-unbuilt underworld
  gap:
  - `NarcoticsService`: `tryCook`/`tryDeal`, following the same
    cooldown-gated-action shape `JobService.tryWorkShift` already
    established, but building a per-player stash count instead of paying
    out immediately — cooking takes 30 seconds per unit, dealing sells
    one stashed unit for a flat payout on a separate 10-second cooldown.
  - `NarcoticsHandler` + `ModBlocks.NARCOTICS_LAB`: right-click (empty
    hand) cooks, sneak-right-click deals; a successful deal has a real
    30% chance of calling the *existing*
    `LawEnforcementService.recordOffense` — the same method every other
    crime in the mod already goes through — rather than inventing a
    parallel narcotics-specific consequence system. That means dealing
    raises wanted level exactly like any other offense, and the
    already-built `PoliceEntity` chase/trial/arrest pipeline from slices
    23/16 applies to a caught dealer with no additional code: an
    illegal, faster, higher-paying alternative to a legal job with
    genuine risk attached, not an isolated minigame.
  - Exhaustively unit tested: first cook/deal always succeeds, repeat
    attempts within cooldown are refused, cooldown expiry allows another
    attempt, dealing with an empty stash fails, payout amounts and stash
    counts update correctly, and different players have fully independent
    stashes/cooldowns.
  - **Known gaps**: see the updated Section 7 status above — one drug
    type at one block, no smuggling/laundering, no rival dealers, catch
    chance is a flat constant with no scaling by wanted level or law
    enforcement presence; unverified without a running client.

- **Slice 38 — alcohol and cigarettes as real consumable vice items**
  (Sections 5 and 6), closing the "cigarettes and alcohol as sellable
  retail items with real effects" gap tracked in both sections:
  - `IntoxicationCalculator`/`IntoxicationService`: drinking `ALCOHOL`
    raises a per-player intoxication level (capped at 3), the same
    pure-calculator-plus-service split `medical.FallInjuryCalculator`/
    `IllnessService` already established. Unlike the earlier medical
    effects, intoxication genuinely sobers back up on its own — one level
    clears every 60 seconds since the last drink, computed lazily from
    elapsed ticks rather than a periodic tick call — and the Slowness
    (plus Nausea once "drunk" or worse) applied scales with the current
    level instead of one flat effect for any amount had.
  - `NicotineRisk`/`NicotineService`: smoking a `CIGARETTE` always gives a
    brief Speed buzz, but every fifth one in a row (the same
    count-until-threshold shape `medical.IllnessRisk` uses for rain
    exposure) triggers the exact Nausea+Weakness illness
    `WeatherIllnessEffect` already applies — reusing an existing
    consequence instead of inventing a parallel one, so smoking has a
    real, escalating health cost rather than being purely cosmetic.
  - `LiquorStoreUseHandler` + `ModBlocks.LIQUOR_STORE`: the same
    withdraw-or-refuse retail pattern `PharmacyUseHandler` established —
    right-click buys `ALCOHOL`, sneak-right-click buys `CIGARETTE`, both
    remitting the standard sales-tax cut.
  - Exhaustively unit tested: intoxication level increments and caps
    correctly, decays over elapsed ticks and never below zero, drinking
    again before fully sober stacks on the remaining level; the nicotine
    counter doesn't trigger before the threshold, triggers exactly at it,
    resets afterward, and tracks players independently.
  - **Known gaps**: one drink type and one cigarette type, no
    hangover/withdrawal effects, no interaction between the two systems
    or with the existing medical illness/injury systems beyond sharing
    the same status effects, and NPCs never drink or smoke — see the
    updated Section 5 status above; unverified without a running client.

- **Slice 39 — gas station refueling and a fuel gauge readout for
  `CarEntity`** (Section 4): `VehiclePhysics` has simulated fuel
  consumption since slice 19, but nothing let a player see or refill it —
  once empty, a car was permanently stranded.
  - `FuelPricing`: a pure liters-to-cost calculator, the same
    calculator-plus-handler split every paid interaction in the mod
    already uses.
  - `CarEntity.refuel`: adds fuel capped at the tank's capacity
    (`MAX_FUEL_LITERS`, now public so `GasPumpUseHandler` can reference
    it); sneak-right-clicking your own car now reports its exact fuel
    level as a chat message instead of it being invisible.
  - `GasPumpUseHandler` + `ModBlocks.GAS_PUMP`: right-click while riding a
    car refuels it, following the same withdraw-or-refuse pattern
    `PharmacyUseHandler` established — except a player who can't afford a
    full tank gets however much fuel their balance actually covers
    instead of the interaction just failing, the same way a real gas
    pump sells a partial tank.
  - Unit tested: `FuelPricing`'s cost/budget conversions in both
    directions, rounding, and the zero-liters edge case. `CarEntity`
    itself remains untested like the rest of the class, per its own
    documented caveat — it needs a running Minecraft entity/world to
    exercise.
  - **Known gaps**: the fuel gauge is a request-response chat message,
    not a persistent HUD bar; one fixed fuel price with no per-station
    variation; no "out of gas, stranded on the highway" narrative beyond
    the vehicle simply refusing to accelerate; see the updated Section 4
    status above for the rest of what a real automotive system needs.

- **Slice 40 — assault as a distinct tracked crime type, plus a real
  knife weapon** (Section 7), closing a "requested and tracked, not
  started" gap: until now, hurting another player registered nothing at
  all in `CrimeService`.
  - `AssaultHandler`: hooks `ServerLivingEntityEvents.AFTER_DAMAGE` — the
    same Fabric event `medical.LegInjuryEffect` already uses for fall
    damage — and, whenever the damage is `DamageTypes.PLAYER_ATTACK` and
    both the victim and the attacker are players, records the offense
    through the *existing* `LawEnforcementService.recordOffense`
    pipeline at a higher severity (3) than poaching (1) or dealing
    narcotics (2), rather than inventing a parallel assault-specific
    consequence system.
  - `ModItems.KNIFE`: a real `SwordItem` on iron-tier stats — the melee
    weapon item category the same gap called for — though the crime
    itself is recorded for any player-on-player hit, armed or not, since
    a fistfight is assault too.
  - Not separately unit tested: `LawEnforcementServiceTest` and
    `CrimeServiceTest` already exhaustively cover `recordOffense` for any
    severity value, the same mechanism poaching and narcotics dealing
    already reused without adding their own duplicate tests — only the
    event-hook wiring is new, and like every other Fabric event handler
    in the mod, it needs a running Minecraft entity/world to exercise.
  - **Known gaps**: see the updated Section 7 status above — no separate,
    harsher severity for actually killing a player versus merely hitting
    them, no consequence for attacking an NPC (NPCs have no health/death
    of their own), and no weapon-specific detection.

- **Slice 41 — murder as a separate, harsher crime tier than assault**
  (Section 7), the direct follow-up slice 40's own known gaps called out:
  - `MurderHandler`: hooks `ServerLivingEntityEvents.AFTER_DEATH` — fired
    once, at the exact moment a victim dies, rather than on every hit
    like `AssaultHandler`'s `AFTER_DAMAGE` — and when the fatal blow was
    `DamageTypes.PLAYER_ATTACK` from another player, records the offense
    through the same `LawEnforcementService.recordOffense` pipeline at
    `WantedLevelMath.MAX` severity: a killing jumps a player straight to
    "Most Wanted" in one offense, rather than climbing gradually the way
    a lesser repeated crime does.
  - Deliberately layered on top of slice 40 rather than replacing it: a
    killing blow still fires `AssaultHandler` too (it's still a hit), so
    a murder now records as both an assault and a murder in sequence —
    two offenses that genuinely both happened, not a bug to suppress; the
    wanted level clamps at its maximum either way.
  - Not separately unit tested, for the same reason slice 40 wasn't:
    `LawEnforcementServiceTest`/`CrimeServiceTest` already cover
    `recordOffense` at any severity, including the maximum; only the new
    event hook is untested, needing a running Minecraft entity/world like
    every other Fabric event handler in the mod.
  - **Known gaps**: see the updated Section 7 status above — no
    consequence for killing an NPC, no attempted-murder distinction from
    a survived assault, and no investigation/detective mechanic — a
    murder is recorded the instant it happens with no possibility of
    getting away with it unseen.

- **Slice 42 — predator AI: `CoyoteEntity` hunts `DeerEntity`** (Section
  8), closing "no predator AI at all — only prey flee/herd behavior
  exists, nothing stalks or hunts anything":
  - `WildlifeBehavior` gains `shouldHunt`/`canAttack`, the same
    pure-distance-threshold shape `shouldFlee`/`isCloseEnoughToHerd`
    already use, so the new AI's trigger ranges are unit tested without a
    running world just like the existing ones.
  - `HuntDeerGoal`: finds the nearest live `DeerEntity` within range via
    `World.getEntitiesByClass`, chases it, and deals real damage on
    contact (on its own attack cooldown, not once per tick) — a hunt that
    can actually kill the deer, the same shape `ChasePoacherGoal` uses for
    chase-then-act, but against another mob instead of a player.
  - `FleeFromPredatorGoal`: the other half of a real predator/prey
    relationship — `DeerEntity` now flees a nearby `CoyoteEntity` too, at
    a higher goal priority than fleeing a player, reusing the same
    `shouldFlee` threshold.
  - `CoyoteEntityRenderer` reuses the existing `DeerEntityModel` quadruped
    rig (a similarly-shaped four-legged animal) with its own texture,
    rather than hand-building a second near-identical `ModelPart`
    hierarchy — the same model-reuse pattern `GameWardenEntityRenderer`
    already established for `HumanoidEntityModel`. The antler-cuboid UV
    region is painted fully transparent in the coyote's texture so the
    borrowed geometry doesn't visibly give a coyote antlers.
  - Unit tested: 5 new `WildlifeBehaviorTest` cases for `shouldHunt`/
    `canAttack` at, above, and below their thresholds. `HuntDeerGoal`/
    `FleeFromPredatorGoal` themselves are untested like every other
    `Goal` in this package, needing a running Minecraft world.
  - **Known gaps**: see the updated Section 8 status above — one
    predator/prey pair, no pack coordination, no drops or population
    consequence from a kill, item-spawned only.

- **Slice 43 — water as a second, fully independent billed utility**
  (Section 9), closing "no water-tower hookups or consequence for
  missing one (no sinks that dry up)":
  - `WaterState`/`WaterBillingMath`/`WaterService`/`WaterDatabase`: a
    deliberate, near-identical mirror of `UtilityState`/
    `UtilityBillingMath`/`UtilityService`/`UtilityDatabase` — same
    billing-cycle-tick-bucket shape, same withdraw-or-disconnect logic,
    same SQLite open/cache/persist pattern — but a wholly separate class
    hierarchy and database table rather than a shared "utility type"
    abstraction, matching the codebase's established convention of
    separate concrete classes for structurally-similar concerns (see
    `SalesTax`/`IncomeTax`/`PropertyTaxService`, or `ArrestService`/
    `TrialService`/`CivilCourtService`). This means water bills, connects,
    and disconnects entirely independently of power — a household can
    lose one without the other, the real-world case a single shared
    "connected" flag can't represent.
  - `WaterOutletBlock` + `ModBlocks.WATER_OUTLET`: a sink whose `FLOWING`
    state reflects its claim owner's water connection, the same
    self-scheduling `ClaimRegistry`-lookup pattern `UtilityLampBlock`
    already established for the power lamp — two textures (a full basin
    with water, a dry rusty one) swapped via blockstate rather than a
    single texture with a tint.
  - Deliberately scoped down from power's own current state: no phone-app
    visibility or manual pay button yet, only the automatic billing cycle
    and the sink's visible state — mirroring power's own history, where
    slice 12 shipped billing alone and slice 32 added the phone app
    twenty slices later.
  - Exhaustively unit tested: `WaterBillingMathTest` (5 cases) and
    `WaterServiceTest` (8 cases, including SQLite persistence-and-reload)
    mirror `UtilityBillingMathTest`/`UtilityServiceTest` case-for-case.
  - **Known gaps**: see the updated Section 9 status above — no phone
    app, no water-tower structure or reservoir capacity, flat-rate
    billing only; unverified without a running client for the block's
    visible states.

- **Slice 44 — water status and Pay Now visibility in the Utilities
  phone app** (Section 9), the direct follow-up slice 43's own known
  gaps called out:
  - `WaterStatusRequestPayload`/`WaterStatusResponsePayload`/
    `PayWaterBillPayload`/`WaterNetworking`: a mirror of the existing
    power networking triple, registered and handled the same way, and
    `ClientWaterState` mirrors `ClientUtilityState` as the client-side
    cache.
  - `UtilitiesAppScreen` now renders both utilities independently: power's
    existing connected/disconnected line and balance, plus a second water
    line and balance, each with its own Pay Now button — because slice 43
    made the two bill and disconnect completely separately, showing only
    one would leave the other invisible again.
  - No new unit tests: this slice is UI/networking wiring around
    `WaterService`, which slice 43 already tested exhaustively — the same
    "no new tests" call the original Utilities/Government phone app UI
    slices made, since there's no new pure logic to test, only glue
    between an already-tested service and a screen that needs a running
    client to verify (265 tests total, unchanged, all still passing).
  - **Known gaps**: still no real water-tower structure, reservoir
    capacity, or usage-based billing (see Section 9 above); the screen's
    layout and button behavior are unverified without a running client,
    the same caveat as every other UI in the mod.

- **Slice 45 — Three Card Poker, the casino's fourth real game**
  (Section 6), the next item on the priority list after blackjack:
  - `Card`/`Card.Rank`/`Card.Suit`: a fresh, self-contained model rather
    than reusing `BlackjackGame.Rank` — poker hand evaluation needs suits
    (for flushes), which blackjack's rank-only representation never
    tracked.
  - `ThreeCardPokerHandRank`/`ThreeCardPokerHand`/`ThreeCardPokerHandEvaluator`:
    real hand evaluation recognizing every category (high card, pair,
    flush, straight, three of a kind, straight flush) including the
    Ace-2-3 "wheel" as the lowest straight, and the genuine, real Three
    Card Poker quirk that a straight outranks a flush — the *opposite* of
    five-card poker, because a straight is the statistically rarer hand
    once a hand is only three cards.
  - `ThreeCardPokerGame`: real Ante/Play structure — deal, then the
    player folds (forfeiting the ante, dealer hand never even needs to be
    compared) or plays (matching the ante with an equal play bet,
    revealing the dealer's hand). The dealer must qualify with Queen-high
    or better; if it doesn't, the ante still pays even money and the play
    bet simply pushes — a real rule, not a simplification, and why
    `anteMultiplier`/`playMultiplier` are two separate functions rather
    than one shared payout table like blackjack's.
  - `ThreeCardPokerService` + full networking (state/deal/fold/play/
    response) + `ThreeCardPokerScreen`: the same withdraw-then-settle
    session shape `BlackjackService`/`BlackjackNetworking`/
    `BlackjackScreen` established, adapted for the two-stage ante-then-
    play bet instead of a single stake.
  - Exhaustively unit tested: `CardTest` (ordinal round-trip), 12
    `ThreeCardPokerHandEvaluatorTest` cases (every hand category, the
    wheel, the straight-beats-flush rule, tiebreak comparisons), 9
    `ThreeCardPokerGameTest` cases (dealer qualification both ways, win/
    lose/push/fold, resolution guards), and 10 `ThreeCardPokerServiceTest`
    cases (ante withdrawal, fold forfeiture, play-bet withdrawal and
    payout, insufficient-funds handling) — 34 new tests, 299 total, all
    passing.
  - **Known gaps**: no Pair Plus side bet or 6-card bonus (the real
    game's optional extra wagers), single-player against the dealer only
    (no multiplayer poker room), draws with replacement from an infinite
    shoe like every other card game in the mod, one fixed bet size, and a
    player who can't afford the play bet is simply stuck (the UI has no
    dedicated message for it, just an unchanged screen) rather than being
    auto-folded; unverified without a running client.

- **Slice 46 — NPCs are no longer exempt from the illness system**
  (Sections 2 and 5), the first crack in a gap repeated in this file
  since slice 22: "NPCs are entirely exempt from every other system this
  session built."
  - `WeatherIllnessEffect.checkEntity`: the existing player-only `check`
    method is split into a shared `applyIfSick` helper plus two thin
    wrappers — `check` (players, sends a chat message) and `checkEntity`
    (any `LivingEntity`, no message since NPCs have no chat to read) —
    rather than duplicating the rain-exposure-and-consequence logic for
    NPCs in a parallel class.
  - `RealWorldMod`'s server tick loop now walks every entity in the
    overworld via `ServerWorld.iterateEntities()`, and for each
    `CitizenEntity` found, runs `checkEntity` against the *same*
    `IllnessService` instance players already use — one shared illness
    system with two kinds of victims, not a second NPC-specific one.
  - No new unit tests: `IllnessServiceTest`/`IllnessRiskTest` already
    exhaustively cover the `IllnessService.tick` logic this reuses
    unchanged; only the entity-iteration wiring is new, and like every
    other tick-loop integration in the mod it needs a running Minecraft
    world to exercise (299 tests total, unchanged, all still passing).
  - **Known gaps**: see the updated Section 2/5 status above — NPCs can
    now get sick but still can't earn, get hurt from a fall, get assaulted
    as a distinct offense, or be arrested; a sick citizen has no way to
    recover early since it can't buy medicine, so its illness only ever
    resolves by the effect's duration expiring.

- **Slice 47 — NPCs earn real wages through the same income pipeline as
  players** (Section 2), the direct follow-up to slice 46's own "still
  can't earn" gap:
  - `NpcProfile.incomeCentsPerPayPeriod` has existed since slice 1,
    persisted to SQLite on every upsert — and was never once read by any
    other code in the mod until this slice. `NpcScheduleManager` now pays
    it the instant a citizen's own `DailyScheduleFSM` transitions it into
    `WORKING`, the same real state-transition event the schedule manager
    already detects to persist the state change and log it.
  - The wage is withheld through the *existing* `IncomeTax` — the same
    5% every player's `JobService` wage already pays — and the net amount
    deposited into a real `BankService` account keyed by the citizen's
    own UUID, with the withheld tax landing in the same treasury account
    every other tax in the mod feeds. NPCs earning through the identical
    pipeline players use, not a parallel NPC-specific ledger.
  - `NpcScheduleManager` gained a `BankService` constructor dependency
    (previously just `NpcDatabase`) to make this possible.
  - Exhaustively unit tested: `NpcScheduleManagerTest` verifies the
    transition-triggers-payment behavior, correct tax withholding into
    the treasury, no double-payment while remaining in `WORKING`, no
    payment on transitioning to any other state, and independent crediting
    across multiple citizens — 5 new tests, 304 total, all passing. Unlike
    most of this mod's NPC/entity code, this was fully testable without a
    running Minecraft world, since `NpcScheduleManager` only ever touches
    `NpcDatabase` and `BankService`, both already SQLite-file-backed and
    already tested that way.
  - **Known gaps**: see the updated Section 2 status above — a citizen
    still can't spend its own income on anything (no NPC purchases of any
    kind), still can't get hurt or arrested, and is paid a flat amount
    once per workday rather than per hour worked or scaled by any kind of
    job performance.

- **Slice 48 — fall injuries apply to any living entity, not just
  players** (Sections 2 and 5), the smallest possible continuation of the
  NPC-inclusion theme slices 46/47 started:
  - `LegInjuryEffect` hooks `ServerLivingEntityEvents.AFTER_DAMAGE`, whose
    `entity` parameter was *already* typed as `LivingEntity` — the
    `instanceof PlayerEntity` check restricting the leg-injury Slowness
    effect to players was never load-bearing, just an explicit early
    return nobody had removed yet. Deleting it means any living entity
    that takes fall damage above the bruise/fracture thresholds — a
    `CitizenEntity`, a `DeerEntity`, a `CoyoteEntity`, even an unrelated
    vanilla cow — gets the exact same `FallInjuryCalculator`-driven
    Slowness effect a player does. Only the chat message announcing the
    injury stays player-only, since nothing else in the mod has chat to
    read it.
  - No new unit tests: `FallInjuryCalculatorTest` already exhaustively
    covers the threshold/severity logic this reuses completely unchanged;
    only the guard clause removal is new, and `LegInjuryEffect` itself was
    never unit tested to begin with (it needs a running Minecraft
    entity/world), so this doesn't add a new untested surface, it widens
    an existing one (304 tests total, unchanged, all still passing).
  - **Known gaps**: see the updated Section 2/5 status above — NPCs still
    can't get assaulted as a distinct offense or arrested, and a citizen's
    leg injury has no visible limping animation, only the Slowness
    effect's speed reduction, the same caveat every status-effect-driven
    injury in the mod shares.

- **Slice 49 — a sick or injured citizen buys medicine and self-treats**
  (Sections 2 and 5), closing "a sick citizen has no way to recover early
  since it never spends its own income on medicine" — the very gap slices
  46-48 kept naming without closing:
  - `CitizenSelfMedicationHandler`: runs alongside the illness check in
    the same per-tick entity loop, and for any `CitizenEntity` carrying
    Nausea, Weakness, or Slowness, checks whether it can afford
    `PharmacyUseHandler.MEDICINE_PRICE_CENTS` out of the real
    `BankService` balance slice 47 started paying into. If it can, the
    citizen pays for it — remitting the exact same municipal sales-tax
    cut `PharmacyUseHandler` already withholds for a player's purchase —
    and the effects clear immediately, the same cure `MedicineUseHandler`
    gives a player. The first NPC purchase of any kind in the mod.
  - Deliberately simplified relative to a player's own trip to the
    pharmacy: a citizen doesn't need to path to a physical
    `PHARMACY_COUNTER` first, since the mod has no NPC
    pathfinding-to-a-shop behavior yet — this models a house call rather
    than a shopping trip, an honest simplification rather than a silent
    one.
  - No new unit tests, for the same reason `MedicineUseHandler` itself
    was never unit tested: it needs a running Minecraft entity to check
    status effects against, and the `BankService`/`PharmacyUseHandler`
    pieces it reuses are already exhaustively tested elsewhere (304 tests
    total, unchanged, all still passing).
  - **Known gaps**: see the updated Section 2/5 status above — medicine
    is still the only thing a citizen can spend money on, no NPC
    pathfinding to a real pharmacy, and a citizen still can't be
    assaulted as a distinct offense or get arrested.

- **Slice 50 — Craps, the casino's fifth real game** (Section 6), rounding
  out the priority list's "fifth casino game" item:
  - `CrapsGame`: the real Pass Line bet structure — a come-out roll of 7
    or 11 wins immediately ("a natural"), 2, 3, or 12 loses immediately
    ("craps"), and any other total establishes "the point," after which
    the shooter keeps rolling until the point repeats (a win) or a 7
    shows first ("seven out," a loss). Deliberately scoped to the Pass
    Line alone — no Come/Don't Pass/Don't Come, odds, or proposition bets
    (Field, Hardways, Any Craps) — but the Pass Line is the game's own
    core structure, not a corner cut from it.
  - `CrapsService` + full networking (state/start/roll/response) +
    `CrapsScreen`: the same withdraw-then-settle session shape
    `BlackjackService`/`ThreeCardPokerService` use, adapted for a bet
    that can take any number of rolls to resolve rather than a fixed
    two- or three-step flow — the client screen just keeps a Roll button
    live until the round resolves, with a Bet button to start the next
    one.
  - Exhaustively unit tested: 13 `CrapsGameTest` cases (every come-out
    outcome, point establishment, point-repeat win, seven-out loss,
    non-resolving rolls continuing the round, post-resolution guard, and
    payout multipliers) and 7 `CrapsServiceTest` cases (bet withdrawal,
    insufficient-funds handling, in-progress-round guard, win/loss payout
    verified against the real `Random`-driven outcome rather than a
    rigged one, and restarting after resolution) — 20 new tests, 324
    total, all passing.
  - **Known gaps**: no Come/Don't Pass/Don't Come/odds/proposition bets
    (the rest of a real craps table), one fixed bet size, draws with
    replacement rather than modeling physical dice; unverified without a
    running client.

- **Slice 51 — car ownership and auto theft as a tracked crime**
  (Section 4), closing part of "no mechanic for stealing cars or car keys
  from an NPC or parked vehicle":
  - `CarEntity` gains a real, NBT-persisted owner (`ownerId`), set by
    `CarSpawnHandler` to the player who spawns it via `CAR_KEY`. Riding
    someone else's car is no longer free: `CarEntity.interact` now
    records real auto theft through the *existing*
    `LawEnforcementService.recordOffense` pipeline (via the already-wired
    `CrimeAccess` static holder) at a severity between poaching and
    assault, rather than a parallel vehicle-specific consequence system —
    the theft still succeeds mechanically (the thief can still drive
    away), it's just genuinely illegal now, with real wanted-level and
    fine consequences.
  - An unowned car (any car spawned before this slice, since the field
    defaults to `null`) stays drivable by anyone with no consequence,
    matching `ClaimRegistry`'s own "unclaimed is unrestricted" convention
    rather than retroactively criminalizing existing saves.
  - No new unit tests, for the same reason slices 40/41's crime-hook
    additions had none: `LawEnforcementServiceTest`/`CrimeServiceTest`
    already exhaustively cover `recordOffense` for any severity value;
    only the ownership check and event wiring inside `CarEntity` are new,
    and `CarEntity` itself has never been unit tested — it needs a
    running Minecraft entity/world, the same documented caveat since
    slice 19 (324 tests total, unchanged, all still passing).
  - **Known gaps**: see the updated Section 4 status above — no NPC-owned
    vehicles yet (citizens don't own cars at all), and theft is only
    detected at the moment of riding, not of taking a spare `CAR_KEY`
    item.

- **Slice 52 — a persistent speed HUD while riding a car** (Section 4),
  closing the other named half of "no speed HUD... the fuel gauge is a
  chat message on request rather than a persistent HUD element":
  - `VehicleSpeedDisplay`: a pure conversion from `VehicleState`'s
    internal blocks-per-tick speed to a real mph reading — Minecraft's
    20-tick second and one-block-per-meter convention makes this an
    honest physical unit conversion, not an invented number, and reverse
    travel displays as a positive speed the same way a real speedometer
    would.
  - `CarSpeedHud`: registers a `HudRenderCallback` (this mod's first use
    of that Fabric API) that draws the reading in the corner of the
    screen whenever `MinecraftClient.player.getVehicle()` is a
    `CarEntity`, and draws nothing otherwise — a real persistent HUD
    element, not a one-off message the player has to request.
  - Exhaustively unit tested: `VehicleSpeedDisplayTest` covers zero
    speed, a known blocks-per-tick-to-mph conversion, `VehiclePhysics`'s
    own real `MAX_SPEED` converting to a realistic highway speed, reverse
    travel displaying as positive, and linear scaling — 5 new tests, 329
    total, all passing. `CarSpeedHud` itself is untested like every other
    client-only rendering class in the mod, needing a running game
    client to verify.
  - **Known gaps**: the fuel gauge still hasn't joined the HUD as a
    persistent element — it remains the sneak-right-click chat message
    from slice 39; no other vehicle stat (RPM, gear, odometer) is shown;
    unverified without a running client for the actual on-screen
    rendering.

- **Slice 53 — a Court Registry phone app** (Section 3), closing part of
  "civil/criminal court registry as a web UI... no court registry/
  filings" for the civil side:
  - `CivilCourtService` gains a read-only `getCase(UUID): Optional<Case>`
    query alongside the existing `hasPendingCase`/`fileClaim`/`contest`,
    so a defendant's pending case (plaintiff, amount, deadline tick) can
    be read without mutating anything.
  - A seventh `PhoneApp`, Court Registry, follows the exact same
    request/cache/render shape as Criminal Record and Government: a
    `CourtRegistryStatusRequestPayload` (C2S, sent on app open) and
    `CourtRegistryStatusResponsePayload` (S2C, carrying `hasPendingCase`,
    `amountCents`, and `ticksRemaining` computed server-side from the
    case's `deadlineTick` minus the current world tick) registered
    through a new `CourtRegistryNetworking`, cached client-side in
    `ClientCourtRegistryState`, and rendered by `CourtRegistryAppScreen` —
    "No pending case" in green when clear, or the claimed amount and
    seconds left to contest in red/white otherwise.
  - No new unit tests for the networking/screen classes, matching every
    prior phone-app slice (32, 44): they need a running client/server
    pair to exercise, while the query method they depend on is covered
    directly. `CivilCourtServiceTest` gains two new cases for `getCase`
    (returns the pending case's fields; empty with no case) — 331 total,
    all passing.
  - **Known gaps**: read-only — no in-app contest button, so a defendant
    still has to use the in-world court flow to dismiss a claim before
    the deadline; no plaintiff name shown (`CivilCourtService.Case` only
    stores a UUID, and nothing in the mod yet resolves an offline UUID to
    a display name); no filing history or archive of past, already-closed
    cases; no equivalent registry app yet for the criminal side beyond
    the existing Criminal Record wanted-level display.

- **Slice 54 — a real, player-chosen wager on three casino tables**
  (Section 6), closing the "each is a single fixed bet size with no way
  to wager more or less" gap the priority list called out once all five
  tables existed:
  - `BetSizing`: the shared bound every wager gets clamped against
    (`MIN_BET_CENTS`/`MAX_BET_CENTS`/`STEP_CENTS`/`DEFAULT_BET_CENTS`),
    so a client can never send a bogus wager that withdraws more than the
    maximum or less than the minimum — `clamp` is the one place that
    guarantee lives, exercised directly by `BetSizingTest`.
  - `BlackjackService.startGame`, `CrapsService.startGame`, and
    `ThreeCardPokerService.deal` all now take a requested bet/ante amount
    instead of withdrawing the same hardcoded constant every round; each
    clamps it through `BetSizing.clamp` before withdrawing, and stores
    the *actual* amount withdrawn (not a shared constant) so settlement
    always pays out against what that specific round actually bet.
    Three Card Poker's real rule that the play bet always matches the
    ante (unchanged from slice 45) now means the *chosen* ante, tracked
    per player between the deal and the play/fold decision.
  - `BlackjackStartPayload`, `CrapsStartPayload`, and
    `ThreeCardPokerDealPayload` all gained a `long betCents`/`anteCents`
    field carrying the client's chosen wager, and each table's screen
    (`BlackjackScreen`, `CrapsScreen`, `ThreeCardPokerScreen`) gained a
    +/- button pair that adjusts a selected-bet field in
    `BetSizing.STEP_CENTS` increments (disabled while a round is in
    progress, matching how the existing action buttons already toggle)
    and a "Bet: $X.XX" label showing the current selection before the
    player deals.
  - Slots (`SlotMachineUseHandler`) and Roulette (`RouletteUseHandler`)
    deliberately weren't touched: both are a single block right-click
    with no persistent screen to put a wager selector on, so they stay
    fixed-bet — an honestly narrower slice than "all five games," called
    out explicitly in Section 6's status below.
  - Exhaustively unit tested at the service layer: each of
    `BlackjackServiceTest`/`CrapsServiceTest`/`ThreeCardPokerServiceTest`
    gained a test asserting a custom in-range bet is exactly what gets
    withdrawn and a test asserting an out-of-range bet clamps to
    `BetSizing.MAX_BET_CENTS`, on top of `BetSizingTest`'s four direct
    clamp cases — 338 total, all passing. The three screens' new +/-
    buttons are untested like every other client-only rendering class in
    the mod, needing a running game client to verify.
  - **Known gaps**: no way to type an exact amount, only step through
    `BetSizing`'s fixed increments; Slots and Roulette still don't have a
    chooseable wager (see above); no per-table minimum/maximum
    (high-roller vs. low-stakes tables) — the same `BetSizing` range
    applies everywhere; a disconnect mid-round still leaves the round's
    actual withdrawn amount, not a shared constant, which is the correct
    behavior but was never explicitly tested for a mid-session
    disconnect since the mod has no session-loss simulation at all.

- **Slice 55 — assault and murder now protect NPCs too** (Section 7),
  closing "no consequence at all for killing an NPC" now that citizens
  have real health to lose:
  - `AssaultHandler` and `MurderHandler` both gain an `isProtectedVictim`
    check widened from "the victim is a `PlayerEntity`" to "the victim is
    a `PlayerEntity` or a `CitizenEntity`" — the exact same NPC-inclusion
    move slices 46/48 already made for illness and fall injury, applied
    to the crime system this time. `DeerEntity`, `CoyoteEntity`,
    `PoliceEntity`, and `GameWardenEntity` are deliberately left out:
    each already has its own distinct consequence (poaching, predation,
    arrest) and folding them into ordinary assault/murder would double
    up rather than fill a real gap.
  - Both handlers still only fire for a `PLAYER_ATTACK` from another
    `PlayerEntity` — a citizen killed by a coyote, fall damage, or
    another citizen still isn't a crime, matching how `MurderHandler`
    already ignored non-player-attack deaths for player victims.
  - Not separately unit tested, for the same reason slices 40/41/46/48
    weren't: `LawEnforcementServiceTest`/`CrimeServiceTest` already cover
    `recordOffense` at any severity; only the widened event-hook guard is
    new, and like every other Fabric event handler in the mod it needs a
    running Minecraft entity/world to exercise (338 tests total,
    unchanged, all still passing).
  - **Known gaps**: no distinct severity for an NPC victim versus a
    player victim (both record at the same `ASSAULT_SEVERITY`/
    `MURDER_SEVERITY`); no reaction from nearby citizens or police
    witnessing the attack; still no attempted-murder distinction from a
    survived assault, and no investigation/detective mechanic, for
    either victim type.

- **Slice 56 — a Contest button in the Court Registry app** (Section 3),
  closing slice 53's own "read-only — no in-app contest button" known gap:
  - A new `CourtRegistryContestPayload` (C2S, unit payload) lets
    `CourtRegistryAppScreen` dismiss the player's pending case without
    leaving the phone; `CourtRegistryNetworking` handles it by calling
    the already-existing `CivilCourtService.contest(UUID)` (unchanged
    since slice 36) and replying with the same status response the
    status-request handler builds, refactored into one shared
    `buildResponse` helper both receivers now call.
  - The screen's new Contest button is only enabled while
    `ClientCourtRegistryState.State.hasPendingCase()` is true, the same
    active-flag-toggle pattern every other multi-button screen in the
    mod (Blackjack, Craps, Three Card Poker) already uses.
  - No new unit tests: `CivilCourtServiceTest` already exhaustively
    covers `contest`'s behavior (dismisses a pending case, returns false
    with none, allows a fresh claim afterward); only the payload
    plumbing and button wiring are new, needing a running client/server
    pair like every other phone-app slice (338 tests total, unchanged,
    all still passing).
  - **Known gaps**: still no plaintiff name shown, no filing history or
    past-case archive, and contesting still just dismisses the claim
    outright with no counter-argument or real adjudication — same
    limitations `CivilCourtService.contest` always had, just reachable
    from one more place now.

- **Slice 57 — plaintiff name resolution in the Court Registry app**
  (Section 3), closing slice 53's "no plaintiff name shown
  (`CivilCourtService.Case` only stores a UUID...)" known gap, still open
  as of slice 56:
  - `CourtRegistryStatusResponsePayload` gains a `plaintiffName` field
    (`PacketCodecs.STRING`, defaulting to `""` when there's no pending
    case). `CourtRegistryNetworking.buildResponse` resolves the case's
    `plaintiffId` server-side via
    `player.getServer().getUserCache().getByUuid(id).map(GameProfile::getName)`,
    falling back to the raw UUID string when the cache has no record for
    that player (offline, or a player who has never joined this server)
    — the same graceful-degradation shape the mod already uses whenever
    it resolves a UUID it didn't mint itself.
  - `ClientCourtRegistryState.State` and `CourtRegistryAppScreen` both
    carry/render the new field: the pending-case message now reads
    "Claim by (plaintiff name): (amount)" instead of just the amount,
    via an updated `gui.realworldmod.phone.court_registry.pending` lang
    entry taking two format arguments instead of one.
  - No new unit tests, matching every other phone-app slice (32, 44, 53,
    56): `CivilCourtService`'s own stored data didn't change, and the new
    resolution logic depends on a live `MinecraftServer`'s `UserCache`,
    which needs a running server to exercise — 338 tests total,
    unchanged, all still passing.
  - **Known gaps**: still no filing history or past-case archive; a
    plaintiff who has never joined this server (no `UserCache` entry at
    all) still shows as a raw UUID string rather than a name; contesting
    still just dismisses the claim outright with no counter-argument or
    real adjudication — the same limitations slice 56 already noted,
    just with the plaintiff-visibility gap itself now closed.

- **Slice 58 — narcotics catch chance scales with wanted level**
  (Section 7), starting on slice 37's "expanding the underworld system"
  priority item by replacing its flat, identical-for-everyone 30% catch
  chance:
  - `NarcoticsCatchChance`, a new pure-arithmetic class matching the
    shape of `WantedLevelMath`/`BetSizing`, maps a wanted level (0-5,
    `WantedLevelMath`'s own range) to a catch probability: a 15% base
    chance plus 15 percentage points per wanted level, capped at 90% so
    even the most wanted dealer never faces a guaranteed catch.
  - `NarcoticsHandler.handleDeal` reads the dealer's current wanted level
    from `lawEnforcementService.crimeService().getWantedLevel(...)`
    *before* rolling against `NarcoticsCatchChance.forWantedLevel(...)`,
    replacing the old hardcoded `CATCH_CHANCE` constant — a repeat
    offender now runs a real, escalating risk every time they deal,
    rather than the same 30% roll a first-time dealer gets.
  - Exhaustively unit tested: `NarcoticsCatchChanceTest` covers the base
    chance at wanted level 0, chance scaling at levels 1-2, clamping at
    the maximum for `WantedLevelMath.MAX` (5), and clamping even for a
    level far outside the real 0-5 range — 342 total, all passing.
    `NarcoticsHandler`'s own wiring is untested like every other Fabric
    event handler in the mod, needing a running server to exercise.
  - **Known gaps**: dealing is still recorded at the same fixed
    `DEALING_SEVERITY` regardless of wanted level or amount dealt — only
    the *catch* odds scale, not the offense's severity; still a single
    cook/deal loop at one block type, with no second drug/lab type, no
    rival dealer NPCs or turf, no dark web purchases, and no drug
    smuggling or money-laundering mechanic — the rest of slice 37's
    "expanding the underworld system" priority item, unstarted.

- **Slice 59 — pack-hunting coordination between coyotes** (Section 8),
  closing slice 42's own "no pack-hunting coordination between multiple
  coyotes" known gap:
  - `WildlifeBehavior` gains `isPackMate` (true for another live coyote
    within a new `PACK_RADIUS_SQUARED`, matching the existing
    proximity-threshold pattern `shouldHunt`/`canAttack` already use) and
    `packAttackMultiplier`, a pure function turning a nearby-ally count
    into a damage multiplier — 1.0 for a lone coyote, +0.5 per pack mate,
    capped at `MAX_PACK_ATTACK_MULTIPLIER` (2.5) so an arbitrarily large
    pack still can't one-shot a deer.
  - `HuntDeerGoal.tryAttack` now counts other live coyotes within pack
    range of the attacker via `World.getEntitiesByClass` (the same query
    shape `findNearestDeer` already uses) and scales `ATTACK_DAMAGE` by
    `WildlifeBehavior.packAttackMultiplier` before applying it — several
    coyotes hunting the same deer together now actually hit harder as a
    pack, instead of each independently landing the same fixed 3 damage.
  - Exhaustively unit tested at the pure-logic layer: `WildlifeBehaviorTest`
    gains cases for a pack mate within/beyond `PACK_RADIUS_SQUARED`, the
    base (lone-coyote) multiplier, the per-ally bonus at 1 and 2 allies,
    and clamping at the maximum for a large pack — 347 total, all
    passing. `HuntDeerGoal`'s own world query is untested like every
    other `Goal` in the package, needing a running world with multiple
    live coyotes to exercise.
  - **Known gaps**: still a single coyote-vs-deer predator/prey
    relationship — no second predator/prey pair; pack mates don't
    actually coordinate their approach or surround the target, they just
    each independently run `HuntDeerGoal` and happen to get a damage
    bonus when close together; no pack-formation/leadership concept, and
    a killed deer still simply dies with no meat/hide drop or
    population-count consequence.

- **Slice 60 — a real hangover effect for heavy drinking** (Section 5),
  starting on slice 38's "alcohol/cigarettes need more variety" priority
  item:
  - `IntoxicationService` gains a `peakedAtMaxLevel` flag per player, set
    whenever `drink` raises them to `IntoxicationCalculator.MAX_LEVEL`,
    and a new `checkHangover(playerId, currentTick)` that returns true
    exactly once — on the tick a player who peaked finishes sobering back
    up to level 0 — the same "cross a threshold, trigger once, then
    reset" shape `medical.IllnessService.tick` already established for
    rain-exposure illness.
  - A new `HangoverEffect.check`, called from the same per-online-player
    loop in the server tick handler that already runs
    `WeatherIllnessEffect.check`, applies real Nausea and Mining Fatigue
    (a genuine "can't mine effectively, feel awful" hangover, not just a
    repeat of the existing drunk Slowness) plus a chat message once
    `checkHangover` triggers.
  - Exhaustively unit tested at the service layer: `IntoxicationServiceTest`
    gains cases for no hangover without ever peaking, no hangover while
    still sobering up, the hangover triggering exactly once at the exact
    tick a peaked player reaches level 0, and a second bender being able
    to trigger a second hangover after the first one fires — 351 total,
    all passing. `HangoverEffect` itself is untested like every other
    status-effect-applying class in the mod (`WeatherIllnessEffect`
    included), needing a running server/player to exercise.
  - **Known gaps**: only one hangover severity regardless of how long a
    player stayed at max level or how many times they re-drank while
    hungover; still only one drink type and one cigarette type (no second
    tier of either); no separate nicotine-withdrawal effect, and drinking
    and smoking still don't interact with each other at all — the rest of
    slice 38's "more variety" priority item, unstarted.

- **Slice 61 — NPC pathfinding to a real pharmacy** (Section 2), closing
  "no NPC pathfinding-to-a-shop behavior yet," the gap slice 49's own
  javadoc called out from the moment it was written:
  - `PharmacyLocator`, a new bounded block-search utility, scans a box
    around a given position for the nearest real `PHARMACY_COUNTER` —
    a wide box (`WALK_SEARCH_RADIUS`/`WALK_SEARCH_VERTICAL_RADIUS`) for
    finding somewhere to walk towards, and a tight one
    (`ARRIVAL_RADIUS`/`ARRIVAL_VERTICAL_RADIUS`) for confirming a citizen
    has actually arrived — a brute-force scan rather than a spatial
    index, the honest limitation a bounded search always has.
  - `WalkToPharmacyGoal`, a new `Goal` registered above `CommuteGoal` in
    `CitizenEntity.initGoals`, makes a sick or injured citizen walk to
    that nearest pharmacy the moment it's found — pre-empting its normal
    commute, the same "urgent goal outranks the routine one" priority
    ordering `FleeFromPredatorGoal` already established over herding.
  - `CitizenSelfMedicationHandler.tryTreat` now requires
    `PharmacyLocator.findNearest` (at the tight arrival radius) to
    actually find a counter before withdrawing payment and clearing the
    effects — a citizen is no longer cured wherever it happens to be
    standing, closing the gap the class's own javadoc has documented
    since slice 49.
  - No new unit tests, matching every other Minecraft-world-coupled class
    in the `npc` package (`CommuteGoal`, `StructureBuilder`,
    `CitizenSelfMedicationHandler` itself): `PharmacyLocator`'s block
    scan and `WalkToPharmacyGoal`'s navigation both need a real
    `ServerWorld` to exercise — 351 total, unchanged, all still passing.
  - **Known gaps**: the search is a brute-force scan with no fallback, so
    a citizen with no pharmacy within `WALK_SEARCH_RADIUS` simply never
    gets treated rather than eventually recovering on its own; no other
    NPC destination uses this same pathfinding pattern yet (no
    pathfinding to a job site's actual task, or to any shop besides a
    pharmacy); a citizen still doesn't queue or wait its turn if another
    citizen is already at the same counter.

- **Slice 62 — a real nicotine-withdrawal effect** (Section 5), finishing
  slice 38/60's "alcohol/cigarettes need more variety" priority item:
  - `NicotineService` now tracks a cumulative, never-resetting smoke
    count (separate from the illness streak that already resets after
    triggering) and the tick of a player's last cigarette. A new
    `checkWithdrawal(playerId, currentTick)` returns true exactly once —
    once a player who's smoked at least
    `NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES` cigarettes
    goes `NicotineWithdrawalRisk.WITHDRAWAL_TICKS` without another — the
    same "cross a threshold, trigger once, reset on the next relevant
    action" shape `vice.IntoxicationService.checkHangover` already
    established for alcohol.
  - A new `NicotineWithdrawalEffect.check`, called from the same
    per-online-player tick loop as `HangoverEffect.check`, applies
    Nausea and Slowness plus a chat message — a deliberately different
    symptom pair from the alcohol hangover's Nausea+Mining Fatigue, so
    quitting cigarettes doesn't feel like a reskin of sobering up.
    `CigaretteUseHandler.smoke` now also passes the current world tick
    so `NicotineService` can time the last cigarette.
  - Exhaustively unit tested: `NicotineWithdrawalRiskTest` covers the
    dependency threshold and withdrawal-timing thresholds directly, and
    `NicotineServiceTest` gains cases for no withdrawal without ever
    becoming dependent, no withdrawal before enough time passes,
    withdrawal triggering exactly once at the exact tick a dependent
    player goes overdue, and a second craving being able to trigger a
    second withdrawal after the first fires — 359 total, all passing.
    `NicotineWithdrawalEffect` itself is untested like every other
    status-effect-applying class in the mod, needing a running
    server/player to exercise.
  - **Known gaps**: only one withdrawal severity regardless of how
    dependent a player has become or how many times they've relapsed; no
    nicotine patch/gum item to ease withdrawal short of smoking again;
    drinking and smoking still don't interact with each other at all —
    slice 38/60's "more variety" priority item is now honestly closed at
    "one drink type, one cigarette type, one hangover, one withdrawal,"
    not the fuller variety (a second tier of either, or cross-system
    interaction) still missing above.

- **Slice 63 — a meat/hide drop and a real deer population count**
  (Section 8), closing the rest of slice 42's "a killed deer simply dies
  with no meat/hide drop or population-count consequence" gap:
  - Two new items, `DEER_MEAT` (a genuine `FoodComponent`-backed food —
    nutrition 3, saturation 0.3 — not just a decorative drop) and
    `DEER_HIDE` (a plain crafting-material byproduct, not consumed by
    anything yet), each with a real 16x16 placeholder texture plus
    LabPBR normal/specular maps, matching every other item in the mod.
  - A new `DeerDropHandler` hooks `ServerLivingEntityEvents.AFTER_DEATH`
    for any `DeerEntity` — poacher, licensed hunter, or coyote kill alike,
    deliberately broader than `PoachingHandler`'s player-only,
    license-gated scope — and drops both items via `ItemScatterer.spawn`
    at the death location.
  - A new `WildlifePopulationService` tracks a real, queryable deer count:
    `DeerSpawnHandler.register` now takes it and calls `recordDeerSpawn`
    on every spawn, and `DeerDropHandler` calls `recordDeerDeath` on
    every death (floored at zero) — an honest running total, not a full
    ecosystem simulation.
  - Exhaustively unit tested at the service layer: `WildlifePopulationServiceTest`
    covers starting at zero, incrementing on spawn, decrementing on
    death, and flooring at zero — 363 total, all passing. `DeerDropHandler`
    itself is untested like every other Fabric event handler in the mod,
    needing a running world to exercise.
  - **Known gaps**: the population count isn't surfaced anywhere yet (no
    phone app, no command, no in-world display) and nothing in the mod
    reacts to it — a population of zero doesn't stop poaching, change
    coyote behavior, or trigger any consequence; drop quantities are
    fixed (always 2 meat, 1 hide) regardless of cause of death or any
    other factor; the meat and hide themselves don't do anything beyond
    existing as items — meat is edible but not used in any recipe, and
    hide isn't craftable into anything.

- **Slice 64 — a filing-history archive for the Court Registry** (Section 3),
  closing "no filing history or past-case archive," the gap every Court
  Registry slice since 53 repeated:
  - `CivilCourtService` gains an `ArchivedCase` record and an in-memory
    `history` list: `contest` now archives the dismissed case (now taking
    a `currentTick` parameter to record exactly when), and `tick`'s
    default-judgment path archives it too, each tagged `contested` true
    or false accordingly. A new `getHistoryFor(playerId)` returns every
    archived case that player was a party to (as plaintiff *or*
    defendant), most-recently-resolved first.
  - Two new payloads, `CourtRegistryHistoryRequestPayload` (C2S, sent on
    app open alongside the existing status request) and
    `CourtRegistryHistoryResponsePayload` (S2C, carrying a past-case
    count plus the most recent case's resolved opponent name, amount,
    and contested flag), handled by a new branch in
    `CourtRegistryNetworking` that resolves the *opponent's* name the
    same `UserCache` way slice 57 resolves a plaintiff's.
  - `ClientCourtRegistryHistoryState` caches the response, and
    `CourtRegistryAppScreen` renders "Past cases: N" plus, when there's a
    most-recent case, "Most recent: contested/default judgment vs
    (name) ((amount))" below the existing pending-case display.
  - Exhaustively unit tested at the service layer: `CivilCourtServiceTest`
    gains cases for an empty history before any case resolves, a
    contested case archiving as contested, a default judgment archiving
    as not contested, history being visible to both plaintiff and
    defendant, most-recently-resolved-first ordering across two cases,
    and a player not party to any case seeing an empty history — 369
    total, all passing. The networking/screen classes are untested,
    matching every other phone-app slice (32, 44, 53, 56, 57).
  - **Known gaps**: the history is a count-plus-most-recent summary, not
    a full scrollable list of every past case — a player with many past
    cases can't page through them or see anything but the latest one;
    like slice 57's plaintiff-name resolution, an opponent who's never
    joined this server still shows as a raw UUID string; contesting
    still just dismisses the claim outright with no counter-argument or
    real adjudication, the same limitation slices 56/57 already noted.

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
- Done: as of slice 27, all four of the mod's entities —
  `CitizenEntity`, `PoliceEntity`, `DeerEntity`, `CarEntity` — render
  with a real hand-built `ModelPart` hierarchy (proper body/limb/wheel
  geometry via Minecraft's own model system) and genuine animation (walk
  cycles, or independently-spinning wheels driven by a real synced speed
  value for the car), with distinct hand-painted textures — replacing the
  "scaled vanilla block with no animation" placeholder every entity used
  through slice 23. As of slice 25, every existing texture (all four
  entities plus every block/item) also ships a LabPBR normal + specular
  map, so a player running Iris with a PBR-aware shader pack gets real
  bump/specular/emissive shading instead of flat lighting.
- Missing — and this is the honest running tally against "10/10 assets,
  nothing looks like Minecraft anymore": every block/item texture is
  still slice 18's simple 16x16 placeholder pixel art, not high-fidelity
  art; the four entity textures that exist are script-painted pixel art
  at Minecraft-skin resolution, not the "hundreds of hand-crafted,
  high-quality" assets requested — no amount of code can substitute for
  actual artist-made assets or a licensed asset pack, and this mod has
  neither; the model geometry itself is still Minecraft's own blocky
  cuboid style (no smooth/organic shapes); the LabPBR maps exist but
  nothing renders them without the *player* separately installing Iris
  and a shader pack — the mod ships no rendering pipeline of its own, no
  PBR/ray-traced lighting, volumetric fog, water refraction, or
  displacement mapping happens by default; no 1/16th sub-grid interior
  decoration system (arbitrary-angle furniture placement); no
  background-thread macro-economics/weather/commute simulation for
  unrendered regions (today's simulation runs only for online
  players/loaded chunks via the normal server tick, not a separate async
  layer); nothing in this section has been visually confirmed in a
  running client — every model/renderer in the mod is still unverified
  against how it actually looks. **Requested and tracked, not started**:
  working window curtains, street lights, and other small
  world-detail props that toggle/animate on their own; emotes as their own
  player-triggered animation/expression system, distinct from an NPC's own
  idle/walk animations above.

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
  navigation cleanly. As of slice 30, `StructureBuilder` places a real
  small building (walls, a doorway, a flat roof) at both the home and
  workplace coordinate when a citizen spawns — the workplace's building
  gets a `CASH_REGISTER` fixture inside it — so `CommuteGoal` actually
  walks the citizen into a structure rather than to a bare point. As of
  slice 46, NPCs are no longer categorically exempt from every other
  system: `WeatherIllnessEffect.checkEntity` runs the same rain-exposure
  check and Nausea/Weakness consequence against every live `CitizenEntity`
  each tick, through the exact same `IllnessService` instance players
  already share — a citizen caught out in the rain gets sick exactly like
  a player does, the first crack in "only players can earn, get sick, get
  hurt, or get arrested." As of slice 47, that same citizen also earns:
  `NpcProfile.incomeCentsPerPayPeriod` — a field that has existed since
  slice 1 but was never once read — now actually gets paid the moment
  `NpcScheduleManager` transitions a citizen into `WORKING`, withheld
  through the same `IncomeTax` every player wage already goes through and
  deposited into a real `BankService` account keyed by the citizen's own
  UUID, the tax landing in the same treasury account players' wages feed.
  As of slice 48, a citizen can also get hurt: `LegInjuryEffect`'s
  restriction to `PlayerEntity` was an artificial one (the underlying
  Fabric event already hands back any `LivingEntity`), so removing it
  means a citizen that takes a bad fall limps with the same Slowness
  effect a player would get. As of slice 49, a citizen can finally spend
  that income too: `CitizenSelfMedicationHandler` checks every tick
  whether a sick or injured citizen can afford
  `PharmacyUseHandler.MEDICINE_PRICE_CENTS` out of its own `BankService`
  balance and, if so, pays for it (remitting the same sales tax a real
  pharmacy purchase would) and clears the effects immediately — the
  first NPC purchase of any kind in the mod. As of slice 61, that
  purchase is no longer a "house call": `WalkToPharmacyGoal` pre-empts a
  sick citizen's normal commute and walks it to the nearest real
  `PHARMACY_COUNTER` found by a new `PharmacyLocator` block search, and
  `CitizenSelfMedicationHandler` only treats a citizen once it's actually
  standing at one — the first real NPC pathfinding-to-a-shop behavior in
  the mod, closing a gap slice 49's own javadoc had called out since it
  was written.
- Missing: every citizen's building is identical (one fixed 5x5 room
  shape, walls-and-roof only, no interior furniture/rooms/windows), placed
  block-by-block with no check for terrain, water, or overlap with an
  existing claim/structure/another citizen's building first; no
  fridge/breakfast/commute-by-vehicle animation, no job-task
  mini-behaviors (cashiering, patrols, factory work), no evening leisure
  destinations, no branching dialogue tree (one fixed line per state), no
  NPC behavioral AI (mugging, reacting to red-light running, independent
  crime, police chases); NPCs can now get sick, earn a wage, get hurt from
  a fall, and buy medicine to treat themselves, but medicine is still the
  only thing a citizen can spend money on — no other NPC purchase exists
  (a meal, a hunting license, land) — and a citizen still can't be
  assaulted as a distinct offense or get arrested; `PharmacyLocator`'s
  block search (closed as of slice 61) is a bounded brute-force scan, not
  a spatial index or a stored per-citizen "usual pharmacy," so a citizen
  with no counter within `PharmacyLocator.WALK_SEARCH_RADIUS` simply never
  gets treated — the same real limitation a player wandering blind would
  face, but with no fallback for a world that just doesn't have a nearby
  pharmacy yet; no other NPC destination uses this pathfinding pattern
  (no pathfinding to a job site's actual task, a shop for a purchase
  besides medicine, or anywhere else) — and — like
  every other status effect the mod applies — the leg injury has no
  visible limping *animation*, only the Slowness effect's speed
  reduction. True GOAP (goal-oriented
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
  player's actions across systems, not just combat); conversation
  awareness — an NPC remembering what a player said or did in an earlier
  interaction and referencing it, rather than every `interactMob` call
  being stateless (today's one-line greeting isn't even two-way dialogue,
  let alone dialogue with memory).

**Section 3 — Consumer Electronics, Computers & In-Game Internet**
- Done: one smartphone item with battery, a 7-app OS shell (Settings,
  Messages, Banking, Criminal Record, Utilities, Government, and — as of
  slice 53 — Court Registry, showing whether the player has a pending
  civil case against them, the claimed amount, and the seconds left to
  contest it, plus — as of slice 56 — a Contest button that dismisses
  the case right from the app, plus — as of slice 57 — the plaintiff's
  resolved display name alongside the claim, plus — as of slice 64 — a
  filing-history summary: how many resolved cases the player has been a
  party to, and the outcome/opponent/amount of the most recent one) over
  a real client↔server networking pattern.
- Missing: PearOS vs. OpenDroid distinction (rooting, sideloading,
  terminal access), cracked screens/repair shops, charging cables as a
  physical item, PC building (motherboard/CPU/GPU/RAM/PSU parts, physical
  assembly), any resource-intensive task tied to PC specs (crypto mining,
  video rendering, hacking), an actual in-game *browser* rendering
  web-page-like content (today's apps are native screens, not pages), real
  estate portal, credit score dashboard, stock/forex exchange, a criminal
  court registry as a web UI (the Criminal Record app is that start; the
  Court Registry app from slices 53/56/57/64 covers the civil side,
  including an in-app Contest button as of slice 56, the plaintiff's
  resolved name as of slice 57, and a filing-history summary as of
  slice 64 — the history is a count-plus-most-recent summary, not a full
  scrollable list of every past case), tax audit portal,
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
  spawnable, rideable `CarEntity`, since slice 27 with a real hand-built
  model (`CarEntityModel`) and synced wheel rotation rather than a
  placeholder box. One vehicle type exists; its feel/visuals are
  unverified without a running client (see slice 19's own caveat above).
  As of slice 39, its fuel is no longer invisible or a dead end once
  empty: sneak-right-clicking your car reads out its exact fuel level,
  and a `GAS_PUMP` retail block refuels it for real money — a partial
  refill, not a failed interaction, if the player can't afford a full
  tank. As of slice 51, a car has a real owner too: `CarSpawnHandler` now
  assigns the spawning player as owner, persisted through relog via NBT,
  and anyone else who starts riding it commits real, tracked auto theft
  through the *existing* `LawEnforcementService.recordOffense` pipeline —
  the same reused-not-parallel pattern every crime-adjacent system in the
  mod follows — closing part of "no mechanic for stealing cars." An
  unowned car (spawned before this slice) is still drivable by anyone
  with no consequence, matching the property system's own "unclaimed is
  unrestricted" convention. As of slice 52, the car finally has a real
  speed HUD too: `VehicleSpeedDisplay` converts the vehicle's internal
  blocks-per-tick speed to a real mph reading (Minecraft's 20-tick
  second and one-block-per-meter convention gives an honest, physically
  real conversion, not a made-up number), and `CarSpeedHud` renders it
  persistently in the corner of the screen via `HudRenderCallback`
  whenever the player is riding a `CarEntity` — no more needing to
  sneak-right-click for a one-off chat message the way the fuel gauge
  still works.
- Missing: the other 299+ vehicle types (including, specifically
  requested: motorcycles, bicycles, e-bikes, skateboards, rollerblades,
  boats and kayaks, cargo ships, cargo/military planes, military ships,
  tanks, helicopters, public buses), mechanic shops/tuning/paint/
  body-damage repair, garage capacity limits, any aviation at all
  (airports, ticketing, TSA, boarding, airliners), ATC job/radar
  minigame, subways/bullet trains/transit cards/timetables. The one
  vehicle that exists still has no suspension/tire-friction modeling or
  collision damage, and the fuel gauge is still a chat message on request
  rather than joining the new speed HUD as a persistent element; car
  theft only exists for
  a player-owned car — an NPC-owned vehicle isn't possible yet since
  citizens don't own cars at all, and there's still no way to steal a
  spare `CAR_KEY` item itself (theft is only detected at the moment of
  riding, not of key possession). **Requested and
  tracked, not started**: any AI-controlled traffic at all — no other
  cars/planes/helicopters share the roads or sky with the player; no
  speed-check/radar-gun mechanic, no bumper/collision-damage system
  between vehicles, no hidden/undercover police vehicles, no in-vehicle
  radio.

**Section 5 — Biology, Medical, Fitness & Lineage**
- Done: two damage/exposure sources (fall damage, rain exposure) each
  driving a status-effect proxy (Slowness; Nausea+Weakness), plus a
  Medicine item that cures those specific effects, sold at a Pharmacy
  Counter for real money. As of slice 38, alcohol and cigarettes are real
  consumable items too: drinking `ALCOHOL` raises a per-player
  intoxication level (`vice.IntoxicationService`) that sobers back up on
  its own over time, with Slowness (and, once drunk enough, Nausea)
  scaling to the current level rather than one flat effect; smoking a
  `CIGARETTE` gives a brief Speed buzz every time, but every fifth one in
  a row (`vice.NicotineService`) triggers the same lasting Nausea+
  Weakness illness the rain-exposure system already applies — a real,
  escalating health cost for repeated use rather than a purely cosmetic
  item. As of slice 46, the rain-exposure illness is no longer
  player-exclusive: `WeatherIllnessEffect.checkEntity` applies the exact
  same check and consequence to every `CitizenEntity` too, sharing the
  same `IllnessService` state players use — not a parallel NPC-specific
  system. As of slice 48, fall-damage leg injuries followed the same
  path: `LegInjuryEffect`'s player-only restriction was never load-bearing
  (the Fabric event it hooks already hands back any `LivingEntity`), so
  removing it means any living entity in the mod — a citizen, a deer, a
  vanilla cow — limps from a bad fall exactly like a player does. As of
  slice 49, a sick or injured citizen no longer just waits it out either:
  `CitizenSelfMedicationHandler` pays for and applies the same Medicine
  cure a player buys at a Pharmacy Counter, out of the citizen's own
  wages from slice 47. As of slice 60, heavy drinking has a real
  next-morning cost: `IntoxicationService.checkHangover` tracks whether a
  player peaked at `IntoxicationCalculator.MAX_LEVEL` and, once they've
  fully sobered back up, `HangoverEffect` applies a real Nausea+Mining
  Fatigue hangover — sobering up from a bender is no longer completely
  consequence-free. As of slice 62, quitting cigarettes has a real cost
  too: once `NicotineService` sees a player smoke
  `NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES` cigarettes
  (cumulative, tracked separately from the streak that triggers illness),
  `checkWithdrawal` fires once they go `WITHDRAWAL_TICKS` without
  another, and `NicotineWithdrawalEffect` applies Nausea+Slowness — a
  distinct symptom set from the alcohol hangover, so the two vice
  systems don't feel identical.
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
  as a distinct mechanic from the general gym/fitness gap above;
  relationships that can actually break up or involve cheating, once the
  marriage/dating-affinity system above exists to break up in the first
  place. (Alcohol/cigarettes themselves are no longer on this list — see
  slice 38 above — but variety is: one drink type and one cigarette type;
  a real hangover effect exists as of slice 60 and nicotine withdrawal as
  of slice 62, but each fires at one fixed threshold with one fixed
  severity — no escalation for repeat benders/quit-attempts, no way to
  ease withdrawal short of smoking again (there's no nicotine patch/gum
  item), and the two vice systems still don't interact with each other
  at all (getting drunk while withdrawing from nicotine, for instance, is
  just two independent effect sets); NPCs can now catch a cold in the
  rain as of slice 46, but still never drink, smoke, or get injured — the
  vice systems and fall injury remain player-only, only the
  weather-illness system is shared.)

**Section 6 — Commercial Enterprises, Retail & Nightlife**
- Done: five "shop" blocks with a withdraw-or-refuse purchase pattern
  (Cash Register pays a wage rather than sells anything, Pharmacy Counter
  sells Medicine, License Office sells a hunting permit, and — as of
  slice 38 — a Liquor Store sells Alcohol and Cigarettes) — a narrow slice
  of "retail," not general commerce — plus five real casino games. As of
  slice 33, `SlotMachine`: three reels over a fixed symbol set and an
  actual payout table (three sevens pays 10x the bet, bars 5x, bells 3x,
  cherries 2x, any two matching is a push, no match loses the bet). As of
  slice 34, `Roulette`: a real 38-pocket American wheel (0, 00, and 1-36
  with the standard red/black assignment) — right-click bets red,
  sneak-right-click bets black, a win pays real 1:1, landing on green
  always loses a color bet. As of slice 35, `BlackjackGame`: the mod's
  first real interactive multi-step casino game (a proper Deal/Hit/Stand
  screen, not a single right-click) — real hand scoring (Aces count 11
  unless that would bust, then drop to 1), a dealer that hits below 17
  and stands otherwise, a hidden hole card until the round resolves, and
  a genuine 3:2 natural-blackjack payout. As of slice 45, `ThreeCardPokerGame`:
  a fourth real interactive game (a Deal/Fold/Play screen) with a real
  Ante/Play betting structure, actual hand evaluation via `Card`/
  `ThreeCardPokerHandEvaluator` (including the genuine, counterintuitive
  Three Card Poker rule that a straight outranks a flush — the opposite
  of five-card poker, since a straight is the rarer hand with only three
  cards), and a dealer that must qualify with Queen-high or better before
  the hand comparison even happens. As of slice 50, `CrapsGame` rounds out
  a fifth: the real Pass Line bet, not a simplified stand-in — a come-out
  roll of 7 or 11 wins immediately ("a natural"), 2/3/12 loses immediately
  ("craps"), and any other total establishes "the point," after which the
  shooter keeps rolling until the point repeats (a win) or a 7 shows first
  ("seven out," a loss). All five are unit-tested exhaustively against
  every outcome, not just the category existing with no game underneath.
  As of slice 54, the table screens for Blackjack, Three Card Poker, and
  Craps let the player pick their own wager (in `BetSizing.STEP_CENTS`
  increments between `MIN_BET_CENTS` and `MAX_BET_CENTS`) instead of
  every round always costing the same fixed amount.
- Missing: grocery stores/shopping carts, furniture stores, clothing
  boutiques with a layered fashion/customization engine, bakeries, gun/
  ammo shops, phone/PC retail beyond the two items that exist, player-run
  businesses (buying commercial plots, setting prices on a POS UI, hiring
  NPC cashiers, automatic Friday payroll), the rest of a real casino floor
  (five games exist now, not the whole floor; roulette itself is
  color-betting only, no number/split/street bets; blackjack has no
  double-down/split-pairs/insurance, no multi-deck penetration tracking;
  poker has no Pair Plus side bet, no 6-card bonus, and only Ante/Play,
  not a full poker room with other players; craps has only the Pass Line
  — no Come/Don't Pass/Don't Come, no odds bets, no proposition bets like
  Field or Hardways; all five games draw with replacement from an
  infinite shoe/dice-pair rather than modeling physical wear or bias),
  strip clubs/VIP lounges/nightclubs/DJ booths with proximity audio; all
  five games' losing bets simply vanish rather than reaching a tracked
  "house" account; as of slice 54, Blackjack/Three Card Poker/Craps each
  let the player choose a wager between `BetSizing.MIN_BET_CENTS` and
  `MAX_BET_CENTS` via +/- buttons on their table screen, but Slots and
  Roulette are still a single fixed bet size — they're plain
  block-right-click interactions with no persistent screen to put a
  wager selector on. **Requested and tracked,
  not started**: real wealth-tier
  recognition (nothing currently distinguishes or reacts to
  a player being a "millionaire" or "billionaire" — `BankService` just
  stores an unbounded `long`); public parks as a distinct, purposeful
  location type; a working kitchen — hireable NPC chefs, real cooking
  with its own animation (not an instant craft), and hundreds of distinct
  food items/recipes rather than reusing vanilla food, plus kitchen
  utensils (knives, pots, pans, etc.) as the item category tied to that
  cooking loop.

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
  through the existing phone app and `ArrestHandler` messages. As of
  slice 29, every existing paid purchase (land deeds, medicine, hunting
  licenses) also remits a real, flat 8% municipal sales-tax cut
  (`economy.SalesTax`) into a reserved government treasury account
  (`BankService.TREASURY_ACCOUNT_ID`) instead of the entire price simply
  vanishing — a first, deliberately narrow step into "municipal tax," not
  the full per-city system described below. As of slice 31, that treasury
  also collects `IncomeTax` (a flat 5% withheld from every `JobService`
  wage before it reaches the player) and `PropertyTaxService` (a periodic,
  once-per-in-game-day charge on every land claim proportional to its
  area) — all three tax types now feed the same account, and as of
  slice 32 a Government phone app shows the treasury's live balance. As
  of slice 36, a real (if minimal) civil court exists too, deliberately
  separate from the criminal system above: `CivilCourtService` lets one
  player file a small-claims case against another via a `COURTHOUSE`
  block, and if the defendant doesn't contest it within a fixed response
  window, a genuine default judgment (the real legal term for exactly
  this situation) automatically transfers the claimed amount. As of
  slice 37, a first real underworld system exists too: a `NARCOTICS_LAB`
  block that a player can cook a stash unit from (`NarcoticsService`,
  cooldown-gated like `JobService`'s wage shifts) and then deal for real
  money — a genuinely higher-paying, faster-cycling alternative to a
  legal `CASH_REGISTER` job, but with a real chance per deal of being
  caught and recorded through the *existing* `LawEnforcementService`
  pipeline, the same one every other crime in the mod uses, so a repeat
  dealer's wanted level climbs and the already-built `PoliceEntity` will
  eventually come looking for them exactly as it would for any other
  offense — the underworld system was deliberately wired into the crime
  system that already exists rather than given its own isolated
  wanted/consequence mechanic. As of slice 58, that catch chance is no
  longer a flat 30% for every dealer: `NarcoticsCatchChance` scales it
  from a 15% base up to a 90% cap in proportion to the dealer's own
  wanted level at the moment they deal, read from the same
  `CrimeService` every other offense already shares — a clean-record
  first-timer and a five-star repeat offender no longer face identical
  odds. As of slice 40, hurting another player is
  finally a tracked crime too: `AssaultHandler` hooks the same
  player-on-player damage event `medical.LegInjuryEffect` already uses
  for fall damage and, on any hit one player lands on another, records an
  assault through the same `LawEnforcementService.recordOffense`
  pipeline — at a higher severity than poaching or dealing, since a real
  fistfight is worse than a stolen deer — alongside a real `KNIFE` melee
  weapon (a `SwordItem` on iron-tier stats) as the item category the gap
  also called for. As of slice 41, killing another player is a separate,
  harsher offense than merely hitting them: `MurderHandler` hooks
  `ServerLivingEntityEvents.AFTER_DEATH` (fired once, at the moment of
  death, rather than on every hit like `AssaultHandler`) and records the
  same kind of offense through the same `recordOffense` pipeline, but at
  the maximum severity — a killing jumps a player straight to "Most
  Wanted" in one offense rather than escalating gradually. As of slice
  55, both `AssaultHandler` and `MurderHandler` protect `CitizenEntity`
  the same way they protect a player: hitting or killing a citizen NPC
  now records the same assault/murder offense a player victim would,
  since citizens have real health and can actually die — the "NPCs have
  no health/death of their own to lose" excuse from slices 40/41 no
  longer applies now that slices 46-49 gave NPCs a real place in every
  other system. Deer, coyotes, police, and game wardens stay excluded,
  since they already have their own distinct consequence (poaching,
  predation, arrest).
- Missing: `PoliceEntity` only patrols/chases — no tactical cover, spike
  strips, pit maneuvers, backup calls, or squad coordination; deer/police
  spawn only via items (`POLICE_SPAWNER`), not real police-station
  structures or patrol routes; the trial's verdict logic is a single
  wanted-level check, not an actual evidence/witness/judge simulation
  (there is no judge NPC, no courtroom, no defense); no *per-city*
  tax-rate system — sales/income/property tax are all one flat rate
  charged everywhere, since there are no city boundaries anywhere in the
  world yet for a rate to vary by; an owner who can't afford property tax
  is simply skipped that cycle, with no forfeiture/seizure/lien
  consequence; civil court has no evidence/lease/partnership contracts,
  no judge NPC or courtroom, no way to name a specific defendant (the
  claim always targets the nearest other player), one fixed claim amount,
  and contesting just dismisses the case with no counter-argument or
  actual adjudication — "contest" currently just means "show up in time,"
  not a real defense; no real prison — no cell block, no yard, no
  prison jobs, no faction/contraband/breakout mechanics; the underworld/
  narcotics system built in slice 37 is a single cook/deal loop at one
  block type — no dark web purchases, no variety of drugs/effects, no
  drug smuggling routes, no money laundering through front businesses,
  no rival dealer NPCs or turf, and no distinct "narcotics" crime
  severity tier (dealing is recorded at a fixed severity through the
  same generic offense pipeline as trespassing or poaching) — the "dark
  web" referenced in Section 3 is still entirely unbuilt. **Requested and
  tracked, not started**: running for and holding government office (up to
  leading the whole in-game country); terrorism attacks that occur
  dynamically as the game progresses and get repaired afterward (also
  needs the "construction actually works" gap below); undercover police
  NPCs indistinguishable from civilians until they act (now buildable as
  a `PoliceEntity` variant that doesn't render as one); kidnapping (of the
  player or of a random citizen); a defined path for the player to
  "become a criminal" as a real career/reputation track, not just an
  accumulating wanted level; illuminati-style secret societies and cults
  as a distinct faction type from ordinary criminal organizations; no
  separate charge for attempted murder versus a
  survived assault, and no homicide investigation/detective mechanic —
  a murder is recorded the instant it happens, with no possibility of
  getting away with it if no one saw; other melee weapons beyond the one
  `KNIFE` item, and no weapon-specific detection (an assault is recorded
  for any player-on-player hit, bare-handed or armed, rather than only
  when a real weapon connects); a
  full-scale
  military branch — enlistment, ranks, deployable operations — distinct
  from the individual military vehicles (tanks, military ships/planes)
  already tracked in Section 4.

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
  reuses the crime pipeline. As of slice 28, a real `GameWardenEntity`
  closes the "warden NPCs are simulated only as an automatic fine, not an
  agent" gap: `PoachingHandler` now flags a poacher in a new
  `GameWardenService` (a fixed 45-second alert window, deliberately
  separate from `CrimeService`'s single overall wanted level, which has
  no way to represent "wanted specifically for poaching"), and
  `ChasePoacherGoal` makes any nearby warden pursue and apprehend a
  flagged player — a real fine on contact, or, if evaded for the whole
  window, no consequence beyond the original fine at the kill itself. As
  of slice 42, wildlife finally has a real predator too: `CoyoteEntity`'s
  `HuntDeerGoal` finds the nearest live `DeerEntity` within range, chases
  it, and deals real damage on contact that can actually kill it — the
  first thing in the mod that stalks or hunts anything, rather than only
  ever fleeing or herding. `DeerEntity` reacts in kind: `FleeFromPredatorGoal`
  makes it flee a nearby coyote the same way it already flees a nearby
  player, at a higher goal priority since a predator is more urgent than
  a person. `CoyoteEntity` reuses the existing `DeerEntityModel` quadruped
  rig for rendering (with its own texture, and the antler cuboids painted
  fully transparent so a coyote doesn't visibly have antlers) rather than
  hand-building a second near-identical model — the same model-reuse
  `GameWardenEntityRenderer` already established. As of slice 59, a hunt
  is no longer several coyotes independently chasing the same deer at the
  same base damage: `HuntDeerGoal` counts other live coyotes within
  `WildlifeBehavior.PACK_RADIUS_SQUARED` of the attacker and scales the
  hit by `WildlifeBehavior.packAttackMultiplier` — a lone coyote still
  deals the base 3 damage, but two or more hunting together deal
  noticeably more, real pack-hunting coordination rather than a cosmetic
  crowd. As of slice 63, a killed deer no longer just disappears: a new
  `DeerDropHandler` drops real `DEER_MEAT` (edible, a genuine food item
  via `FoodComponent`) and `DEER_HIDE` on any `DeerEntity` death —
  poacher, licensed hunter, or coyote kill alike — and records the death
  in a new `WildlifePopulationService`, which `DeerSpawnHandler` also
  feeds on every spawn, so the mod tracks a real, queryable deer
  population instead of nothing at all.
- Missing: no biome-specific mechanics at all (no multi-layer canopy/leaf
  decay/wildfires in forests, no machete-gated jungle thickets/equipment
  rust/malaria, no desert sand-dune physics/heatstroke/mirage/flash
  floods); the predator AI slice 42 added is a single coyote-vs-deer
  relationship — no other predator/prey pairs (pack-hunting coordination
  between multiple coyotes closed as of slice 59, and a meat/hide drop
  plus population tracking as of slice 63, though the population count
  isn't shown anywhere yet and nothing in the mod reacts to it — no
  extinction consequence, no effect on spawn rate); no migration
  (herding is proximity-only, not a seasonal or territorial routine);
  deer/wardens/coyotes only spawn via items (`DEER_SPAWNER`/
  `GAME_WARDEN_SPAWNER`/`COYOTE_SPAWNER`), not natural biome-based
  spawning or real ranger-station structures; `GameWardenEntity` only
  patrols/chases — no radio calls for backup, no vehicle patrols, no
  poaching investigation beyond the instant the kill happens; no
  zoo/safari system (no enclosures, HVAC, vet care, breeding, monorails,
  ticketing, gift shops). **Requested and tracked, not started**:
  abandoned towns as a distinct, generated location type.

**Section 9 — Utilities, Space & Industrial Supply Chains**
- Done: the most complete slice-for-slice implementation of any single
  section — real power billing that can disconnect an account, and a lamp
  block that visibly goes dark within seconds of nonpayment, exposed
  through a phone app with a manual pay option. As of slice 43, water is
  a second, fully independent utility: `WaterService`/`WaterDatabase`
  mirror the power stack's exact billing shape (`WaterBillingMath` is the
  same afterBillingAttempt/afterManualPayment transitions as
  `UtilityBillingMath`) but with their own SQLite table and billing
  cycle, so a household can lose water without losing power or vice
  versa — the real-world case a single shared "connected" flag couldn't
  represent. `WaterOutletBlock` (a sink) visibly goes dry the same way
  `UtilityLampBlock` visibly goes dark, closing "no sinks that dry up".
  As of slice 44, the Utilities phone app shows both independently: its
  own water connection line and unpaid balance alongside power's, with a
  separate Pay Water Bill button next to Pay Power Bill — mirroring the
  request/response/pay-now networking triple (`WaterStatusRequestPayload`/
  `WaterStatusResponsePayload`/`PayWaterBillPayload`) the power app
  already used.
- Missing: no power *generation* (no plants of any kind — nuclear/solar/
  fossil — and no city-wide grid-stability simulation, "power" is purely
  an account flag, not a simulated grid); no real water-tower
  *structure*, reservoir capacity, or usage-based (rather than flat-rate)
  billing for either utility; no cell-tower/phone-
  signal consequence for unpaid bills (the phone's battery/lock system
  from slice 2 is entirely separate from the utility system), no waste
  management (no trash generation, no garbage trucks, no landfills/
  recycling, no land-value or toxicity effects), no orbital/space content
  at all (no rockets, no GPS satellites, no orbital dimension, no Moon/
  Mars, no low-gravity mechanics) — every sentence of Section 9's back
  half is entirely unbuilt.

## Priority order for what's next

1. Everything in the "missing" lists above — expanding the underworld
   system slices 37/58 started (a second drug/lab type, rival dealer
   NPCs, or a distinct narcotics crime-severity tier), giving
   alcohol/cigarettes from slices 38/60/62 more variety (a second
   drink/cigarette tier, escalating hangover/withdrawal severity, a
   nicotine patch/gum item, or letting the two vice systems interact), a
   second vehicle type now that slices 39/51/52 rounded out the first
   car's fuel/ownership/speed, expanding the predator
   system slices 42/59/63 started (a second predator/prey pair, showing
   the new deer population count somewhere, or giving meat/hide an
   actual use), extending the NPC-inclusion slices 46-49/55/61 started to
   another system (an NPC-specific arrest/detainment flow now that
   citizens can be assault/murder victims, or reusing slice 61's
   pathfinding pattern for a job site's actual task or a non-medicine
   shop trip), turning slice 64's Court Registry history summary into a
   real scrollable list of every past case, or bringing Slots/Roulette
   up to the other three tables' slice-54
   wager-selection bar (they'd need a real screen first, since both are
   still a single block right-click) are all reasonable next picks.
   Aviation/ATC and the space program stay deliberately last, as the
   largest and least incrementally verifiable pieces.

(Slice 21 closed out daily-schedule-driven `CitizenEntity` movement — see
Section 2 above. Slice 22 closed out real wildlife AI — see Section 8
above. Slice 23 closed out police NPCs and a real court/trial step — see
Section 7 above. Slices 24/26/27 closed out the block-placeholder
rendering backlog across every entity, and slice 25 added LabPBR maps for
every texture — see Section 1 above. Slice 28 closed out the game-warden
NPC — see Section 8 above. Slices 29/31 closed out sales, income, and
property tax — see Section 7 above. Slice 30 closed out real
home/workplace structures for `CitizenEntity` — see Section 2 above.
Slice 32 closed out treasury visibility with a Government phone app —
see Sections 3/7 above. Slices 33/34/35 got the casino its first three
real tables (slots, roulette, blackjack) — see Section 6 above. Slice 36
closed out a real civil small-claims court — see Section 7 above. Slice
37 started the previously entirely-unbuilt underworld/narcotics system —
see Section 7 above. Slice 38 closed out alcohol/cigarettes as real
consumable items — see Sections 5/6 above. Slice 39 closed out fuel
visibility and refueling for `CarEntity` — see Section 4 above. Slice 40
closed out assault as a tracked crime type and added a real knife
weapon — see Section 7 above. Slice 41 closed out a separate, harsher
murder crime tier — see Section 7 above. Slice 42 closed out predator AI
with `CoyoteEntity` hunting `DeerEntity` — see Section 8 above. Slice 43
closed out water as a second, independent billed utility — see Section 9
above. Slice 44 closed out water's phone-app visibility — see Section 9
above. Slice 45 gave the casino its fourth real table, Three Card Poker —
see Section 6 above. Slice 46 closed the first crack in "NPCs are exempt
from every other system," sharing the illness system with players — see
Sections 2/5 above. Slice 47 gave NPCs real income through the same
wage-and-tax pipeline players use — see Section 2 above. Slice 48
extended fall injuries to every living entity, not just players — see
Sections 2/5 above. Slice 49 closed the loop by having a sick or injured
citizen actually spend that income on medicine — see Sections 2/5
above. Slice 50 gave the casino its fifth real table, Craps — see
Section 6 above. Slice 51 closed part of "no mechanic for stealing
cars" with real car ownership and tracked auto theft — see Section 4
above. Slice 52 gave `CarEntity` a real persistent speed HUD — see
Section 4 above. Slice 53 gave the civil court a Court Registry phone
app — see Section 3 above. Slice 54 gave three of the five casino
tables a real player-chosen wager — see Section 6 above. Slice 55
extended assault and murder to protect `CitizenEntity` victims too —
see Section 7 above. Slice 56 added a Contest button to the Court
Registry app — see Section 3 above. Slice 57 closed out plaintiff name
resolution in the Court Registry app — see Section 3 above. Slice 58
scaled narcotics catch chance by the dealer's wanted level — see
Section 7 above. Slice 59 closed out pack-hunting coordination between
coyotes — see Section 8 above. Slice 60 gave heavy drinking a real
hangover effect — see Section 5 above. Slice 61 closed out NPC
pathfinding to a real pharmacy — see Section 2 above. Slice 62 gave
quitting cigarettes a real withdrawal effect — see Section 5 above.
Slice 63 gave a killed deer a real meat/hide drop and population-count
consequence — see Section 8 above. Slice 64 gave the Court Registry a
real filing-history archive — see Section 3 above.)

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
