package com.realworldmod.crime;

import com.realworldmod.economy.BankService;

import java.util.UUID;

/**
 * First real consequence for a high wanted level (Section 7): once a
 * player's level reaches {@link #FINE_THRESHOLD}, each further offense
 * also tries to withdraw a citation from their bank account. No police
 * NPCs, arrests, or court flow yet — a fine (or a failed one, if they
 * can't afford it) is the entire enforcement story for this slice.
 */
public final class LawEnforcementService {
    public static final long FINE_CENTS = 2500;
    public static final int FINE_THRESHOLD = 3;

    private final CrimeService crimeService;
    private final BankService bankService;

    public LawEnforcementService(CrimeService crimeService, BankService bankService) {
        this.crimeService = crimeService;
        this.bankService = bankService;
    }

    public CrimeService crimeService() {
        return crimeService;
    }

    /** Records the offense and, if the resulting wanted level meets the fine threshold, attempts to withdraw a fine. */
    public OffenseOutcome recordOffense(UUID playerId, int severity) {
        int newLevel = crimeService.recordCrime(playerId, severity);
        boolean fined = newLevel >= FINE_THRESHOLD && bankService.withdraw(playerId, FINE_CENTS).isPresent();
        return new OffenseOutcome(newLevel, fined);
    }
}
