package com.nexvary.emergencymesh.core;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public final class MeshPacket {
    public enum Priority { NORMAL, HIGH, EMERGENCY }

    public final String id;
    public final String origin;
    public final String target;
    public final String type;
    public final String body;
    public final long createdAt;
    public final long ttlMillis;
    public final int hop;
    public final int maxHop;
    public final Priority priority;

    public MeshPacket(String id, String origin, String target, String type, String body,
                      long createdAt, long ttlMillis, int hop, int maxHop, Priority priority) {
        this.id = require(id, "id");
        this.origin = require(origin, "origin");
        this.target = require(target, "target");
        this.type = require(type, "type");
        this.body = body == null ? "" : body;
        this.createdAt = createdAt;
        this.ttlMillis = Math.max(1L, ttlMillis);
        this.hop = Math.max(0, hop);
        this.maxHop = Math.max(1, maxHop);
        this.priority = Objects.requireNonNull(priority, "priority");
    }

    public static MeshPacket create(String origin, String target, String type, String body,
                                    long now, Priority priority) {
        int hops = priority == Priority.EMERGENCY ? 7 : 5;
        long ttl = priority == Priority.EMERGENCY ? 2L * 60L * 60L * 1000L : 30L * 60L * 1000L;
        return new MeshPacket(UUID.randomUUID().toString(), origin, target, type, body,
                now, ttl, 0, hops, priority);
    }

    public boolean isExpired(long now) {
        return now < createdAt || now - createdAt > ttlMillis;
    }

    public boolean canRelay(long now) {
        return !isExpired(now) && hop < maxHop;
    }

    public MeshPacket relayed() {
        if (hop >= maxHop) return this;
        return new MeshPacket(id, origin, target, type, body, createdAt, ttlMillis,
                hop + 1, maxHop, priority);
    }

    public String encode() {
        return String.join("|",
                "NEM3",
                b64(id), b64(origin), b64(target), b64(type), b64(body),
                Long.toString(createdAt), Long.toString(ttlMillis),
                Integer.toString(hop), Integer.toString(maxHop), priority.name());
    }

    public static MeshPacket decode(String encoded) {
        String[] p = encoded == null ? new String[0] : encoded.split("\\|", -1);
        if (p.length != 11 || !"NEM3".equals(p[0])) throw new IllegalArgumentException("Invalid NEM3 packet");
        try {
            return new MeshPacket(unb64(p[1]), unb64(p[2]), unb64(p[3]), unb64(p[4]), unb64(p[5]),
                    Long.parseLong(p[6]), Long.parseLong(p[7]), Integer.parseInt(p[8]),
                    Integer.parseInt(p[9]), Priority.valueOf(p[10]));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Corrupt NEM3 packet", ex);
        }
    }

    private static String b64(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String unb64(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static String require(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " required");
        return value;
    }
}
