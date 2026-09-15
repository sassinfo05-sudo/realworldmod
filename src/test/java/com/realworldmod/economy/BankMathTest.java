package com.realworldmod.economy;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankMathTest {
    @Test
    void depositIncreasesBalance() {
        assertEquals(150, BankMath.deposit(100, 50));
    }

    @Test
    void depositRejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> BankMath.deposit(100, -1));
    }

    @Test
    void withdrawSucceedsWhenFundsAreSufficient() {
        Optional<Long> result = BankMath.withdraw(100, 40);
        assertTrue(result.isPresent());
        assertEquals(60, result.get());
    }

    @Test
    void withdrawFailsWhenFundsAreInsufficient() {
        assertFalse(BankMath.withdraw(100, 101).isPresent());
    }

    @Test
    void withdrawExactBalanceLeavesZero() {
        assertEquals(0, BankMath.withdraw(100, 100).orElseThrow());
    }

    @Test
    void withdrawRejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> BankMath.withdraw(100, -1));
    }
}
