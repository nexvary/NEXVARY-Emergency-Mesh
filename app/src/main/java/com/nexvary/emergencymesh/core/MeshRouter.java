package com.nexvary.emergencymesh.core;

public final class MeshRouter {
    public enum Action { DELIVER, DELIVER_AND_RELAY, RELAY, DROP }

    private final String localNodeId;
    private final ReplayGuard replayGuard;

    public MeshRouter(String localNodeId, ReplayGuard replayGuard) {
        if (localNodeId == null || localNodeId.isBlank()) throw new IllegalArgumentException("localNodeId required");
        this.localNodeId = localNodeId;
        this.replayGuard = replayGuard;
    }

    public Action inspect(MeshPacket packet, long now) {
        if (packet == null || replayGuard == null || !replayGuard.accept(packet, now)) return Action.DROP;
        boolean broadcast = "ALL".equals(packet.target) || packet.target.startsWith("CHANNEL:");
        boolean local = localNodeId.equals(packet.target);
        boolean canRelay = packet.canRelay(now);
        if (local) return Action.DELIVER;
        if (broadcast) return canRelay ? Action.DELIVER_AND_RELAY : Action.DELIVER;
        return canRelay ? Action.RELAY : Action.DROP;
    }

    public MeshPacket relay(MeshPacket packet, long now) {
        if (packet == null || !packet.canRelay(now)) return null;
        return packet.relayed();
    }
}
