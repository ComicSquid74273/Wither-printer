package com.bonggerz.witherprinter;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;

/**
 * Prints the lifetime per-server Wither statistics together with the
 * SHA-256 of the jar that is actually running, so a screenshot of this
 * output can be checked against the digest GitHub publishes for the
 * official release jar.
 */
public final class WitherStatsCommand extends Command {
    public WitherStatsCommand() {
        super("witherstats", "Prints lifetime wither statistics with the running jar SHA-256.", "ws");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            WitherLedger ledger = WitherLedger.get();
            String server = WitherLedger.currentServerKey();

            long withers = ledger.withers(server);
            long skulls = ledger.skulls(server);

            String playerName = mc.player != null ? mc.player.getName().getString() : "unknown";
            String playerUuid = mc.player != null ? mc.player.getStringUUID() : "unknown";

            info("Withers placed: (highlight)%d(default) | Skulls placed: (highlight)%d(default) | Server: (highlight)%s(default) | Player: (highlight)%s(default)", withers, skulls, server, playerName);
            info("Player UUID: (highlight)%s(default)", playerUuid);
            info("Jar SHA-256: (highlight)%s(default)", WitherLedger.jarSha256());

            if (ledger.corrupted()) {
                warning("The stats file was modified externally; lifetime counts are unreliable.");
            }

            return SINGLE_SUCCESS;
        });
    }
}
