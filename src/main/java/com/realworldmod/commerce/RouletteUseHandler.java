package com.realworldmod.commerce;

import com.realworldmod.economy.BankService;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.Random;

/**
 * Right-clicking a {@code ROULETTE_TABLE} with an empty hand bets a fixed
 * amount on red; sneaking while right-clicking bets on black instead —
 * the mod's second real casino game (alongside {@link SlotMachine}). A
 * winning color bet pays real 1:1 against {@link Roulette}'s actual
 * 38-pocket wheel; a loss (including landing on green) keeps the bet.
 */
public final class RouletteUseHandler {
    public static final long BET_CENTS = 1000;

    private final BankService bankService;
    private final Random random = new Random();

    public RouletteUseHandler(BankService bankService) {
        this.bankService = bankService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.ROULETTE_TABLE)) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isEmpty()) {
                return ActionResult.PASS;
            }

            if (bankService.withdraw(player.getUuid(), BET_CENTS).isEmpty()) {
                player.sendMessage(Text.translatable("message.realworldmod.roulette_cannot_afford",
                        CurrencyFormatter.format(BET_CENTS)), true);
                return ActionResult.FAIL;
            }

            Roulette.Color betColor = player.isSneaking() ? Roulette.Color.BLACK : Roulette.Color.RED;
            Roulette.Pocket pocket = Roulette.spin(random);

            if (Roulette.colorBetWins(pocket, betColor)) {
                long payout = BET_CENTS * 2;
                bankService.deposit(player.getUuid(), payout);
                player.sendMessage(Text.translatable("message.realworldmod.roulette_win",
                        pocket.label(), CurrencyFormatter.format(payout)), true);
            } else {
                player.sendMessage(Text.translatable("message.realworldmod.roulette_lose", pocket.label()), true);
            }
            return ActionResult.SUCCESS;
        });
    }
}
