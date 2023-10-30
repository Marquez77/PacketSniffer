package me.marquez.packetsniffer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketRecorder extends PacketAdapter {

    private final Map<Player, Map<String, Integer>> sendPackets = new ConcurrentHashMap<>();
    private final Map<Player, Map<String, Integer>> receivePackets = new ConcurrentHashMap<>();

    public PacketRecorder(Plugin plugin) {
        super(plugin, PacketType.values());
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        recordPacket(event, receivePackets);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        recordPacket(event, sendPackets);
    }

    private void recordPacket(PacketEvent event, Map<Player, Map<String, Integer>> packets) {
        packets.compute(event.getPlayer(), (player, map) -> {
            if(map == null)
                map = new ConcurrentHashMap<>();
            map.compute(event.getPacketType().name(), (s, i) -> {
                if(i == null)
                    i = 0;
                return i+1;
            });
            return map;
        });
    }

    public Map<Player, Map<String, Integer>> getSendPackets() {
        return sendPackets;
    }

    public Map<Player, Map<String, Integer>> getReceivePackets() {
        return receivePackets;
    }
}
