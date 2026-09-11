package com.nexvary.emergencymesh.core;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

public final class RadioChunker {
    public static final String PREFIX = "NXC2";

    public static List<String> split(String messageId, String payload, int maxBodyBytes) {
        if (messageId == null || messageId.isEmpty()) throw new IllegalArgumentException("messageId required");
        if (payload == null) payload = "";
        if (maxBodyBytes < 32) throw new IllegalArgumentException("chunk size too small");
        String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        int slice = Math.max(12, maxBodyBytes - 48 - messageId.length());
        int total = Math.max(1, (b64.length() + slice - 1) / slice);
        List<String> out = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            String part = b64.substring(i * slice, Math.min(b64.length(), (i + 1) * slice));
            long crc = crc(part);
            out.add(PREFIX + ":" + messageId + ":" + (i + 1) + ":" + total + ":" + Long.toHexString(crc) + ":" + part);
        }
        return out;
    }

    public static final class Reassembler {
        private final Map<String, Assembly> assemblies = new HashMap<>();
        private final long ttlMillis;

        public Reassembler(long ttlMillis) { this.ttlMillis = Math.max(10_000L, ttlMillis); }

        public synchronized String offer(String frame, long now) {
            String[] p = frame == null ? new String[0] : frame.split(":", 6);
            if (p.length != 6 || !PREFIX.equals(p[0])) return null;
            int index, total;
            long expected;
            try {
                index = Integer.parseInt(p[2]);
                total = Integer.parseInt(p[3]);
                expected = Long.parseLong(p[4], 16);
            } catch (RuntimeException e) { return null; }
            if (total < 1 || total > 128 || index < 1 || index > total || crc(p[5]) != expected) return null;
            cleanup(now);
            Assembly a = assemblies.computeIfAbsent(p[1], k -> new Assembly(total, now));
            if (a.total != total) { assemblies.remove(p[1]); return null; }
            a.parts[index - 1] = p[5];
            a.lastUpdate = now;
            for (String part : a.parts) if (part == null) return null;
            StringBuilder all = new StringBuilder();
            for (String part : a.parts) all.append(part);
            assemblies.remove(p[1]);
            try {
                return new String(Base64.getUrlDecoder().decode(all.toString()), StandardCharsets.UTF_8);
            } catch (RuntimeException e) { return null; }
        }

        private void cleanup(long now) {
            assemblies.entrySet().removeIf(e -> now - e.getValue().lastUpdate > ttlMillis);
        }

        public synchronized int pendingAssemblies() { return assemblies.size(); }
    }

    private static final class Assembly {
        final int total; final String[] parts; long lastUpdate;
        Assembly(int total, long now) { this.total = total; this.parts = new String[total]; this.lastUpdate = now; }
    }

    private static long crc(String value) {
        CRC32 crc = new CRC32();
        crc.update(value.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }
}
