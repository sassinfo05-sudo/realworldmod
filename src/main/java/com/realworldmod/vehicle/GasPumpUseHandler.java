package com.realworldmod.vehicle;

import com.realworldmod.economy.BankService;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.Optional;

/**
 * Right-clicking a {@code GAS_PUMP} with an empty hand while riding a
 * {@link CarEntity} refuels it for real money, following the same
 * withdraw-or-refuse pattern {@code medical.PharmacyUseHandler} already
 * established. Unlike a fixed-price purchase, a player who can't afford a
 * full tank gets however much fuel their balance actually covers instead
 * of the whole interaction simply failing — a gas pump selling a partial
 * tank is how this works in reality too.
 */
public final class GasPumpUseHandler {
    private final BankService bankService;

    public GasPumpUseHandler(BankService bankService) {
        this.bankService = bankService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.GAS_PUMP)) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isEmpty()) {
                return ActionResult.PASS;
            }
            if (!(player.getVehicle() instanceof CarEntity car)) {
                player.sendMessage(Text.translatable("message.realworldmod.gas_pump_no_vehicle"), true);
                return ActionResult.FAIL;
            }

            double missingLiters = CarEntity.MAX_FUEL_LITERS - car.vehicleState().fuelLiters();
            if (missingLiters <= 0) {
                player.sendMessage(Text.translatable("message.realworldmod.gas_pump_tank_full"), true);
                return ActionResult.FAIL;
            }

            return refuel(player, car, missingLiters);
        });
    }

    private ActionResult refuel(PlayerEntity player, CarEntity car, double missingLiters) {
        long fullTankCostCents = FuelPricing.costForLiters(missingLiters);
        Optional<Long> fullWithdrawal = bankService.withdraw(player.getUuid(), fullTankCostCents);
        if (fullWithdrawal.isPresent()) {
            car.refuel(missingLiters);
            bankService.remitSalesTax(fullTankCostCents);
            player.sendMessage(Text.translatable("message.realworldmod.gas_pump_refueled",
                    String.format("%.1f", missingLiters), CurrencyFormatter.format(fullTankCostCents)), true);
            return ActionResult.SUCCESS;
        }

        long availableCents = bankService.getBalance(player.getUuid());
        if (availableCents <= 0) {
            player.sendMessage(Text.translatable("message.realworldmod.gas_pump_cannot_afford"), true);
            return ActionResult.FAIL;
        }

        long charged = bankService.withdraw(player.getUuid(), availableCents).orElseThrow();
        double litersAdded = FuelPricing.litersForBudget(charged);
        car.refuel(litersAdded);
        bankService.remitSalesTax(charged);
        player.sendMessage(Text.translatable("message.realworldmod.gas_pump_partial_refuel",
                String.format("%.1f", litersAdded), CurrencyFormatter.format(charged)), true);
        return ActionResult.SUCCESS;
    }
}
