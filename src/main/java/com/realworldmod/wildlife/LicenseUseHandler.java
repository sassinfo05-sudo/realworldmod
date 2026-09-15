package com.realworldmod.wildlife;

import com.realworldmod.economy.BankService;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/** Right-clicking a {@code LICENSE_OFFICE} with an empty hand buys a hunting license, if affordable and not already held. */
public final class LicenseUseHandler {
    public static final long LICENSE_PRICE_CENTS = 3000;

    private final BankService bankService;
    private final HuntingLicenseService licenseService;

    public LicenseUseHandler(BankService bankService, HuntingLicenseService licenseService) {
        this.bankService = bankService;
        this.licenseService = licenseService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.LICENSE_OFFICE)) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isEmpty()) {
                return ActionResult.PASS;
            }

            if (licenseService.hasLicense(player.getUuid())) {
                player.sendMessage(Text.translatable("message.realworldmod.license_already_held"), true);
                return ActionResult.FAIL;
            }

            if (bankService.withdraw(player.getUuid(), LICENSE_PRICE_CENTS).isEmpty()) {
                player.sendMessage(Text.translatable("message.realworldmod.license_cannot_afford",
                        CurrencyFormatter.format(LICENSE_PRICE_CENTS)), true);
                return ActionResult.FAIL;
            }

            licenseService.grantLicense(player.getUuid());
            player.sendMessage(Text.translatable("message.realworldmod.license_purchased",
                    CurrencyFormatter.format(LICENSE_PRICE_CENTS)), true);
            return ActionResult.SUCCESS;
        });
    }
}
