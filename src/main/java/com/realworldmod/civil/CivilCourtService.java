package com.realworldmod.civil;

import com.realworldmod.economy.BankService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Small-claims civil court (Section 7's "no civil courts" gap) —
 * deliberately distinct from the criminal {@code crime.CrimeService}/
 * {@code crime.TrialService}: no wanted level, no police, no punishment,
 * just one player claiming money from another. Filing a claim against a
 * defendant starts a fixed response window; if the defendant doesn't
 * contest it in time, a real default judgment (the actual legal term for
 * a ruling entered because the defendant failed to respond) transfers the
 * claimed amount automatically. As of slice 64, every resolved case (either
 * way) is archived in memory, closing "no filing history or past-case
 * archive" — the gap every Court Registry slice since 53 has repeated.
 */
public final class CivilCourtService {
    public static final long RESPONSE_WINDOW_TICKS = 20L * 60L;
    public static final long CLAIM_AMOUNT_CENTS = 2_000L;

    public record Case(UUID plaintiffId, UUID defendantId, long amountCents, long deadlineTick) {
    }

    /** A resolved case, kept after it closes. {@code contested} is false for a default judgment. */
    public record ArchivedCase(UUID plaintiffId, UUID defendantId, long amountCents, long resolvedTick, boolean contested) {
    }

    private final BankService bankService;
    private final Map<UUID, Case> casesByDefendant = new HashMap<>();
    private final List<ArchivedCase> history = new ArrayList<>();

    public CivilCourtService(BankService bankService) {
        this.bankService = bankService;
    }

    public boolean hasPendingCase(UUID defendantId) {
        return casesByDefendant.containsKey(defendantId);
    }

    /** The defendant's pending case, if any — see {@code civil.net.CourtRegistryNetworking}. */
    public Optional<Case> getCase(UUID defendantId) {
        return Optional.ofNullable(casesByDefendant.get(defendantId));
    }

    /** Files a claim against {@code defendantId} if they don't already have one pending and aren't the plaintiff themselves. */
    public boolean fileClaim(UUID plaintiffId, UUID defendantId, long currentTick) {
        if (plaintiffId.equals(defendantId) || casesByDefendant.containsKey(defendantId)) {
            return false;
        }
        casesByDefendant.put(defendantId,
                new Case(plaintiffId, defendantId, CLAIM_AMOUNT_CENTS, currentTick + RESPONSE_WINDOW_TICKS));
        return true;
    }

    /** The defendant contests (dismisses) their pending claim; returns whether one existed to dismiss. */
    public boolean contest(UUID defendantId, long currentTick) {
        Case dismissed = casesByDefendant.remove(defendantId);
        if (dismissed == null) {
            return false;
        }
        history.add(new ArchivedCase(dismissed.plaintiffId(), dismissed.defendantId(),
                dismissed.amountCents(), currentTick, true));
        return true;
    }

    /** Call once per server tick: any case past its deadline gets a default judgment (an automatic transfer) and closes. */
    public void tick(long currentTick) {
        casesByDefendant.values().removeIf(civilCase -> {
            if (currentTick < civilCase.deadlineTick()) {
                return false;
            }
            bankService.transfer(civilCase.defendantId(), civilCase.plaintiffId(), civilCase.amountCents());
            history.add(new ArchivedCase(civilCase.plaintiffId(), civilCase.defendantId(),
                    civilCase.amountCents(), currentTick, false));
            return true;
        });
    }

    /** Every resolved case {@code playerId} was a party to (as either plaintiff or defendant), most recently resolved first. */
    public List<ArchivedCase> getHistoryFor(UUID playerId) {
        List<ArchivedCase> result = new ArrayList<>();
        for (ArchivedCase archivedCase : history) {
            if (archivedCase.plaintiffId().equals(playerId) || archivedCase.defendantId().equals(playerId)) {
                result.add(archivedCase);
            }
        }
        result.sort(Comparator.comparingLong(ArchivedCase::resolvedTick).reversed());
        return Collections.unmodifiableList(result);
    }
}
