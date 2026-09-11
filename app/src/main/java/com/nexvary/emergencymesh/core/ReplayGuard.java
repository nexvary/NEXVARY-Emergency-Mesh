package com.nexvary.emergencymesh.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ReplayGuard {
    private final int capacity;
    private final long futureSkewMillis;
    private final LinkedHashMap<String, Long> seen;

    public ReplayGuard(int capacity, long futureSkewMillis) {
        this.capacity = Math.max(64, capacity);
        this.futureSkewMillis = Math.max(0L, futureSkewMillis);
        this.seen = new LinkedHashMap<String, Long>(this.capacity + 1, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return size() > ReplayGuard.this.capacity;
            }
        };
    }

    public synchronized boolean accept(MeshPacket packet, long now) {
        if (packet == null) return false;
        if (packet.createdAt > now + futureSkewMillis) return false;
        if (packet.isExpired(now)) return false;
        if (packet.hop < 0 || packet.hop > packet.maxHop || packet.maxHop > 32) return false;
        if (seen.containsKey(packet.id)) return false;
        seen.put(packet.id, now);
        return true;
    }

    public synchronized int size() { return seen.size(); }
    public synchronized void clear() { seen.clear(); }
}
