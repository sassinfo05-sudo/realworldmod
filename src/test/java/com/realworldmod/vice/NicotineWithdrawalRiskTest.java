package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NicotineWithdrawalRiskTest {
    @Test
    void belowDependencyThresholdIsNotDependent() {
        assertFalse(NicotineWithdrawalRisk.isDependent(NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES - 1));
    }

    @Test
    void atOrAboveDependencyThresholdIsDependent() {
        assertTrue(NicotineWithdrawalRisk.isDependent(NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES));
        assertTrue(NicotineWithdrawalRisk.isDependent(NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES + 1));
    }

    @Test
    void belowWithdrawalTicksDoesNotCauseWithdrawal() {
        assertFalse(NicotineWithdrawalRisk.causesWithdrawal(NicotineWithdrawalRisk.WITHDRAWAL_TICKS - 1));
    }

    @Test
    void atOrAboveWithdrawalTicksCausesWithdrawal() {
        assertTrue(NicotineWithdrawalRisk.causesWithdrawal(NicotineWithdrawalRisk.WITHDRAWAL_TICKS));
        assertTrue(NicotineWithdrawalRisk.causesWithdrawal(NicotineWithdrawalRisk.WITHDRAWAL_TICKS + 1));
    }
}
