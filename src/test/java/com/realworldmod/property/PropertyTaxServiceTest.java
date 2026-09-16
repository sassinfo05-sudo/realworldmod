package com.realworldmod.property;

import com.realworldmod.economy.BankService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyTaxServiceTest {
    private BankService bankService;
    private ClaimRegistry claimRegistry;
    private PropertyTaxService propertyTaxService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        claimRegistry = new ClaimRegistry();
        propertyTaxService = new PropertyTaxService(claimRegistry, bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void areaOfAClaimIsWidthTimesDepthInclusive() {
        Claim claim = new Claim("c1", UUID.randomUUID(), -2, -2, 2, 2);
        assertEquals(25, PropertyTaxService.areaOf(claim));
    }

    @Test
    void taxIsAreaTimesTheRate() {
        Claim claim = new Claim("c1", UUID.randomUUID(), -2, -2, 2, 2);
        assertEquals(25 * PropertyTaxService.RATE_CENTS_PER_BLOCK, PropertyTaxService.taxFor(claim));
    }

    @Test
    void firstTickChargesEveryOwnerImmediately() {
        UUID owner = UUID.randomUUID();
        Claim claim = new Claim("c1", owner, -2, -2, 2, 2);
        claimRegistry.add(claim);
        bankService.deposit(owner, 100_000);

        propertyTaxService.tick(0);

        long tax = PropertyTaxService.taxFor(claim);
        assertEquals(100_000 - tax, bankService.getBalance(owner));
        assertEquals(tax, bankService.getBalance(BankService.TREASURY_ACCOUNT_ID));
    }

    @Test
    void doesNotChargeAgainWithinTheSameInterval() {
        UUID owner = UUID.randomUUID();
        Claim claim = new Claim("c1", owner, -2, -2, 2, 2);
        claimRegistry.add(claim);
        bankService.deposit(owner, 100_000);

        propertyTaxService.tick(0);
        propertyTaxService.tick(PropertyTaxService.TAX_INTERVAL_TICKS - 1);

        long tax = PropertyTaxService.taxFor(claim);
        assertEquals(100_000 - tax, bankService.getBalance(owner));
    }

    @Test
    void chargesAgainOnceTheNextIntervalArrives() {
        UUID owner = UUID.randomUUID();
        Claim claim = new Claim("c1", owner, -2, -2, 2, 2);
        claimRegistry.add(claim);
        bankService.deposit(owner, 100_000);

        propertyTaxService.tick(0);
        propertyTaxService.tick(PropertyTaxService.TAX_INTERVAL_TICKS);

        long tax = PropertyTaxService.taxFor(claim);
        assertEquals(100_000 - (2 * tax), bankService.getBalance(owner));
    }

    @Test
    void ownerWithoutFundsIsSkippedRatherThanFailing() {
        UUID owner = UUID.randomUUID();
        Claim claim = new Claim("c1", owner, -2, -2, 2, 2);
        claimRegistry.add(claim);

        propertyTaxService.tick(0);

        assertEquals(0, bankService.getBalance(owner));
        assertEquals(0, bankService.getBalance(BankService.TREASURY_ACCOUNT_ID));
    }
}
