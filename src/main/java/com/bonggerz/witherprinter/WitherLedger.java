package com.bonggerz.witherprinter;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Lifetime per-server Wither/Skull statistics with tamper-evident storage.
 *
 * The save file ends with a SHA-256 digest over its own contents and every
 * increment folds into a per-server chain hash, so a hand-edited file can no
 * longer produce a self-consistent ledger; such a file is reported as
 * corrupted instead of being trusted.
 *
 * Code authenticity is not proven with a secret. The stats command prints the
 * SHA-256 of the jar that is actually running, which can be compared against
 * the digest GitHub publishes for the official release jar. A rebuilt jar
 * with a modified counter necessarily has a different digest.
 */
public final class WitherLedger {
    private static final int MAGIC = 0x57505354; // "WPST"
    private static final int FORMAT_VERSION = 1;
    private static final WitherLedger INSTANCE = new WitherLedger();

    private static final class Entry {
        long skulls;
        long withers;
        final byte[] chain = new byte[32];
    }

    private final Map<String, Entry> entries = new HashMap<>();
    private boolean loaded;
    private boolean corrupted;

    private WitherLedger() {}

    public static WitherLedger get() {
        return INSTANCE;
    }

    public synchronized boolean corrupted() {
        ensureLoaded();
        return corrupted;
    }

    public synchronized long skulls(String server) {
        ensureLoaded();
        Entry entry = entries.get(server);
        return entry == null ? 0 : entry.skulls;
    }

    public synchronized long withers(String server) {
        ensureLoaded();
        Entry entry = entries.get(server);
        return entry == null ? 0 : entry.withers;
    }

    public synchronized void recordSkulls(String server, int count) {
        if (count <= 0) return;
        ensureLoaded();
        if (corrupted) return;

        Entry entry = entries.computeIfAbsent(server, s -> new Entry());
        entry.skulls += count;
        foldChain(entry, count, 0);
        save();
    }

    public synchronized void recordWither(String server) {
        ensureLoaded();
        if (corrupted) return;

        Entry entry = entries.computeIfAbsent(server, s -> new Entry());
        entry.withers += 1;
        foldChain(entry, 0, 1);
        save();
    }

    private void foldChain(Entry entry, long skullDelta, long witherDelta) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(48);
            DataOutputStream out = new DataOutputStream(buffer);
            out.write(entry.chain);
            out.writeLong(skullDelta);
            out.writeLong(witherDelta);
            byte[] next = sha256().digest(buffer.toByteArray());
            System.arraycopy(next, 0, entry.chain, 0, 32);
        } catch (IOException ignored) {
            // ByteArrayOutputStream never throws on write; unreachable.
        }
    }

    private synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        Path path = statsPath();
        if (!Files.isRegularFile(path)) return;

        try {
            byte[] all = Files.readAllBytes(path);
            if (all.length < 32) throw new IOException("file too short");

            byte[] storedDigest = new byte[32];
            System.arraycopy(all, all.length - 32, storedDigest, 0, 32);
            byte[] actualDigest = sha256().digest(java.util.Arrays.copyOf(all, all.length - 32));
            if (!MessageDigest.isEqual(storedDigest, actualDigest)) {
                throw new IOException("integrity digest mismatch");
            }

            try (DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(all, 0, all.length - 32))) {
                if (in.readInt() != MAGIC) throw new IOException("bad magic");
                if (in.readInt() != FORMAT_VERSION) throw new IOException("unsupported version");

                int count = in.readInt();
                if (count < 0 || count > 1024) throw new IOException("unreasonable entry count");

                for (int i = 0; i < count; i++) {
                    String server = in.readUTF();
                    if (server.isEmpty() || server.length() > 256) throw new IOException("bad server key");
                    Entry entry = new Entry();
                    entry.skulls = in.readLong();
                    entry.withers = in.readLong();
                    if (entry.skulls < 0 || entry.withers < 0 || entry.withers * 3 < entry.skulls) {
                        throw new IOException("unreasonable counters");
                    }
                    in.readFully(entry.chain);
                    entries.put(server, entry);
                }
            }
        } catch (IOException | RuntimeException e) {
            entries.clear();
            corrupted = true;
        }
    }

    private synchronized void save() {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);
            DataOutputStream out = new DataOutputStream(buffer);
            out.writeInt(MAGIC);
            out.writeInt(FORMAT_VERSION);
            out.writeInt(entries.size());
            for (Map.Entry<String, Entry> e : entries.entrySet()) {
                out.writeUTF(e.getKey());
                out.writeLong(e.getValue().skulls);
                out.writeLong(e.getValue().withers);
                out.write(e.getValue().chain);
            }
            out.write(sha256().digest(buffer.toByteArray()));

            Path path = statsPath();
            Files.createDirectories(path.getParent());
            Files.write(path, buffer.toByteArray());
        } catch (IOException ignored) {
            // Losing a save must never interrupt printing; counts continue in memory.
        }
    }

    private static Path statsPath() {
        return FabricLoader.getInstance().getGameDir().resolve("wither-printer-stats.bin");
    }

    /**
     * @return the SHA-256 of the jar file this addon is actually running
     * from, or a short marker when it cannot be determined (dev environment).
     */
    public static String jarSha256() {
        try {
            var container = FabricLoader.getInstance().getModContainer("bonggerz-wither-printer");
            if (container.isEmpty()) return "unknown";

            Path root = container.get().getRootPaths().get(0);
            if (!Files.isRegularFile(root)) return "dev-environment";

            MessageDigest digest = sha256();
            try (var in = Files.newInputStream(root)) {
                byte[] buf = new byte[8192];
                int read;
                while ((read = in.read(buf)) > 0) digest.update(buf, 0, read);
            }
            return hex(digest.digest());
        } catch (Throwable t) {
            return "unknown";
        }
    }

    public static String currentServerKey() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip;
            if (mc.hasSingleplayerServer()) return "singleplayer";
        } catch (Throwable ignored) {
            // Fall through to the neutral key.
        }
        return "unknown";
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        return sb.toString();
    }
}
