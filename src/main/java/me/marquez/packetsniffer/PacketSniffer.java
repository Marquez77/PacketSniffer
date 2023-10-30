package me.marquez.packetsniffer;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PacketSniffer extends JavaPlugin implements TabExecutor {

    @Override
    public void onEnable() {
        getCommand("packets").setExecutor(this);
    }

    private final Map<CommandSender, PacketRecorder> commandMap = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length == 0) {
            sender.sendMessage("/packets start");
            sender.sendMessage("/packets stop");
        }else {
            switch(args[0]) {
                case "start" -> {
                    if(commandMap.containsKey(sender)) {
                        sender.sendMessage("Already processing");
                        return true;
                    }
                    PacketRecorder recorder = new PacketRecorder(this);
                    commandMap.put(sender, recorder);
                    ProtocolLibrary.getProtocolManager().addPacketListener(recorder);
                    sender.sendMessage("Start packets sniffing");
                }
                case "stop" -> {
                    if(!commandMap.containsKey(sender)) {
                        sender.sendMessage("Not started sniffing");
                        return true;
                    }
                    PacketRecorder recorder = commandMap.remove(sender);
                    ProtocolLibrary.getProtocolManager().removePacketListener(recorder);
                    recorder.getSendPackets().entrySet().stream().sorted(
                            ((Comparator<Map.Entry<Player, Map<String, Integer>>>) (o1, o2) -> Integer.compare(o1.getValue().values().stream().mapToInt(value -> value).sum(), o2.getValue().values().stream().mapToInt(value -> value).sum())).reversed()
                    ).limit(5).forEach(entry -> {
                        sender.sendMessage("§e[" + entry.getKey().getName() + "] §fTotal Send: §6§l" + entry.getValue().values().stream().mapToInt(value -> value).sum());
                        entry.getValue().entrySet().stream().sorted(((Comparator<? super Map.Entry<String, Integer>>) (o1, o2) -> Integer.compare(o1.getValue(), o2.getValue())).reversed())
                                .limit(5)
                                .forEach(e -> {
                                    sender.sendMessage("§7└ §f" + e.getKey() + ": §6" + e.getValue());
                                });
                    });
                    recorder.getReceivePackets().entrySet().stream().sorted(
                            ((Comparator<Map.Entry<Player, Map<String, Integer>>>) (o1, o2) -> Integer.compare(o1.getValue().values().stream().mapToInt(value -> value).sum(), o2.getValue().values().stream().mapToInt(value -> value).sum())).reversed()
                    ).limit(5).forEach(entry -> {
                        sender.sendMessage("§b[" + entry.getKey().getName() + "] §fTotal Receive: §2§l" + entry.getValue().values().stream().mapToInt(value -> value).sum());
                        entry.getValue().entrySet().stream().sorted(((Comparator<? super Map.Entry<String, Integer>>) (o1, o2) -> Integer.compare(o1.getValue(), o2.getValue())).reversed())
                                .limit(5)
                                .forEach(e -> {
                                    sender.sendMessage("§7└ §f" + e.getKey() + ": §2" + e.getValue());
                                });
                    });
                }
            }
        }

        return true;
    }
}
