package com.nexvary.emergencymesh.core;

public final class RetryPolicy {
    private final int maxAttempts;
    private final long baseDelayMillis;
    private final long maxDelayMillis;

    public RetryPolicy(int maxAttempts, long baseDelayMillis, long maxDelayMillis) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.baseDelayMillis = Math.max(100L, baseDelayMillis);
        this.maxDelayMillis = Math.max(this.baseDelayMillis, maxDelayMillis);
    }

    public int maxAttempts() { return maxAttempts; }

    public boolean shouldRetry(int attempts, MeshPacket packet, long now) {
        return packet != null && attempts < maxAttempts && packet.canRelay(now);
    }

    public long delayForAttempt(int attempts, MeshPacket.Priority priority) {
        int safe = Math.max(0, Math.min(attempts, 30));
        long multiplier = 1L << Math.min(safe, 10);
        long delay = Math.min(maxDelayMillis, baseDelayMillis * multiplier);
        if (priority == MeshPacket.Priority.EMERGENCY) delay = Math.max(500L, delay / 2L);
        if (priority == MeshPacket.Priority.HIGH) delay = Math.max(750L, (delay * 3L) / 4L);
        return delay;
    }
}
