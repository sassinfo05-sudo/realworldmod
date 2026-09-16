package com.realworldmod.civil;

import com.realworldmod.economy.BankService;

import java.util.HashMap;
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
 * claimed amount automatically.
 */
public final class CivilCourtService {
    public static final long RESPONSE_WINDOW_TICKS = 20L * 60L;
    public static final long CLAIM_AMOUNT_CENTS = 2_000L;

    public record Case(UUID plaintiffId, UUID defendantId, long amountCents, long deadlineTick) {
    }

    private final BankService bankService;
    private final Map<UUID, Case> casesByDefendant = new HashMap<>();

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
    public boolean contest(UUID defendantId) {
        return casesByDefendant.remove(defendantId) != null;
    }

    /** Call once per server tick: any case past its deadline gets a default judgment (an automatic transfer) and closes. */
    public void tick(long currentTick) {
        casesByDefendant.values().removeIf(civilCase -> {
            if (currentTick < civilCase.deadlineTick()) {
                return false;
            }
            bankService.transfer(civilCase.defendantId(), civilCase.plaintiffId(), civilCase.amountCents());
            return true;
        });
    }
}
