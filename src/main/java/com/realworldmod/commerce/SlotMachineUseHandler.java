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
 * Right-clicking a {@code SLOT_MACHINE} with an empty hand places a fixed
 * bet, if affordable, and resolves it against {@link SlotMachine}'s real
 * payout table — a losing spin keeps the withdrawn bet (no separate
 * "house" account exists to remit it to yet, see ROADMAP.md), a push
 * returns it, and a win pays out a real multiple of it.
 */
public final class SlotMachineUseHandler {
    public static final long BET_CENTS = 1000;

    private final BankService bankService;
    private final Random random = new Random();

    public SlotMachineUseHandler(BankService bankService) {
        this.bankService = bankService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.SLOT_MACHINE)) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isEmpty()) {
                return ActionResult.PASS;
            }

            if (bankService.withdraw(player.getUuid(), BET_CENTS).isEmpty()) {
                player.sendMessage(Text.translatable("message.realworldmod.slot_cannot_afford",
                        CurrencyFormatter.format(BET_CENTS)), true);
                return ActionResult.FAIL;
            }

            SlotMachine.Spin spin = SlotMachine.spin(random);
            int multiplier = SlotMachine.payoutMultiplier(spin);
            long payout = BET_CENTS * multiplier;
            if (payout > 0) {
                bankService.deposit(player.getUuid(), payout);
            }

            Text message = switch (multiplier) {
                case 0 -> Text.translatable("message.realworldmod.slot_lose", describe(spin));
                case 1 -> Text.translatable("message.realworldmod.slot_push", describe(spin));
                default -> Text.translatable("message.realworldmod.slot_win", describe(spin), CurrencyFormatter.format(payout));
            };
            player.sendMessage(message, true);
            return ActionResult.SUCCESS;
        });
    }

    private static String describe(SlotMachine.Spin spin) {
        return spin.first() + " " + spin.second() + " " + spin.third();
    }
}
