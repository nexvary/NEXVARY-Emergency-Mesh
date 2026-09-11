package com.nexvary.emergencymesh.core;

import org.junit.Test;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MeshCoreTest {
    @Test public void packetRoundTripArabicAndRelay() {
        long now = 1_800_000_000_000L;
        MeshPacket p = MeshPacket.create("node-a", "rescue", "CHANNEL", "استغاثة عاجلة", now,
                MeshPacket.Priority.EMERGENCY);
        MeshPacket decoded = MeshPacket.decode(p.encode());
        assertEquals("استغاثة عاجلة", decoded.body);
        assertEquals(MeshPacket.Priority.EMERGENCY, decoded.priority);
        assertEquals(7, decoded.maxHop);
        assertTrue(decoded.canRelay(now + 1000));
        assertEquals(1, decoded.relayed().hop);
    }

    @Test public void replayGuardRejectsDuplicateExpiredAndFuture() {
        long now = 1_800_000_000_000L;
        ReplayGuard guard = new ReplayGuard(64, 60_000L);
        MeshPacket good = MeshPacket.create("a", "b", "NODE", "hello", now, MeshPacket.Priority.NORMAL);
        assertTrue(guard.accept(good, now));
        assertFalse(guard.accept(good, now));
        MeshPacket expired = new MeshPacket("expired", "a", "b", "NODE", "x",
                now - 100_000L, 1000L, 0, 5, MeshPacket.Priority.NORMAL);
        assertFalse(guard.accept(expired, now));
        MeshPacket future = new MeshPacket("future", "a", "b", "NODE", "x",
                now + 120_000L, 100_000L, 0, 5, MeshPacket.Priority.NORMAL);
        assertFalse(guard.accept(future, now));
    }

    @Test public void emergencyRetriesSooner() {
        RetryPolicy p = new RetryPolicy(6, 2000L, 60_000L);
        assertTrue(p.delayForAttempt(2, MeshPacket.Priority.EMERGENCY)
                < p.delayForAttempt(2, MeshPacket.Priority.NORMAL));
        assertEquals(6, p.maxAttempts());
    }

    @Test public void aesGcmRoundTripAndWrongPinFails() throws Exception {
        MeshCrypto crypto = new MeshCrypto();
        char[] pin = "839201".toCharArray();
        String encrypted = crypto.encrypt("موقع الإنقاذ 31.2,29.9", pin, "CHANNEL:rescue");
        assertNotEquals("موقع الإنقاذ 31.2,29.9", encrypted);
        assertEquals("موقع الإنقاذ 31.2,29.9", crypto.decrypt(encrypted, pin, "CHANNEL:rescue"));
        try {
            crypto.decrypt(encrypted, "111111".toCharArray(), "CHANNEL:rescue");
            fail("wrong PIN must fail authentication");
        } catch (GeneralSecurityException expected) {
            assertNotNull(expected);
        }
    }

    @Test public void radioChunksReassembleOutOfOrderAndRejectCorruption() {
        String payload = "رسالة طوارئ طويلة ".repeat(90) + "END";
        List<String> chunks = RadioChunker.split("msg-1050", payload, 120);
        assertTrue(chunks.size() > 5);
        List<String> shuffled = new ArrayList<>(chunks);
        Collections.reverse(shuffled);
        RadioChunker.Reassembler r = new RadioChunker.Reassembler(600_000L);
        String result = null;
        long now = 1_800_000_000_000L;
        for (String chunk : shuffled) {
            String maybe = r.offer(chunk, now++);
            if (maybe != null) result = maybe;
        }
        assertEquals(payload, result);
        String first = chunks.get(0);
        String corrupt = first.substring(0, first.length() - 1) + (first.endsWith("A") ? "B" : "A");
        RadioChunker.Reassembler bad = new RadioChunker.Reassembler(600_000L);
        assertNull(bad.offer(corrupt, now));
    }

    @Test public void routerDeliversBroadcastRelaysDirectAndDropsDuplicate() {
        long now = 1_800_000_000_000L;
        MeshRouter router = new MeshRouter("node-b", new ReplayGuard(128, 60_000L));
        MeshPacket broadcast = MeshPacket.create("node-a", "CHANNEL:rescue", "CHANNEL", "help", now, MeshPacket.Priority.HIGH);
        assertEquals(MeshRouter.Action.DELIVER_AND_RELAY, router.inspect(broadcast, now));
        assertEquals(MeshRouter.Action.DROP, router.inspect(broadcast, now));

        MeshRouter directRouter = new MeshRouter("node-b", new ReplayGuard(128, 60_000L));
        MeshPacket direct = MeshPacket.create("node-a", "node-c", "NODE", "direct", now, MeshPacket.Priority.NORMAL);
        assertEquals(MeshRouter.Action.RELAY, directRouter.inspect(direct, now));
        MeshPacket relayed = directRouter.relay(direct, now);
        assertNotNull(relayed);
        assertEquals(1, relayed.hop);

        MeshRouter destination = new MeshRouter("node-c", new ReplayGuard(128, 60_000L));
        assertEquals(MeshRouter.Action.DELIVER, destination.inspect(relayed, now));
    }
}
