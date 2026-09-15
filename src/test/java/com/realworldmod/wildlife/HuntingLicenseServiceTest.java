package com.realworldmod.wildlife;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntingLicenseServiceTest {
    @Test
    void newPlayerHasNoLicense() {
        HuntingLicenseService service = new HuntingLicenseService();
        assertFalse(service.hasLicense(UUID.randomUUID()));
    }

    @Test
    void grantingLicenseMakesItActive() {
        HuntingLicenseService service = new HuntingLicenseService();
        UUID player = UUID.randomUUID();
        service.grantLicense(player);
        assertTrue(service.hasLicense(player));
    }

    @Test
    void licensesAreTrackedPerPlayer() {
        HuntingLicenseService service = new HuntingLicenseService();
        UUID licensed = UUID.randomUUID();
        UUID unlicensed = UUID.randomUUID();
        service.grantLicense(licensed);

        assertTrue(service.hasLicense(licensed));
        assertFalse(service.hasLicense(unlicensed));
    }
}
