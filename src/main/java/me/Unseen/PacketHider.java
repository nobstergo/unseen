package me.Unseen;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PacketHider {
    private final Unseen plugin;
    private final ProtocolManager manager;

    public PacketHider(Unseen plugin) {
        this.plugin = plugin;
        this.manager = ProtocolLibrary.getProtocolManager();
        registerListener();
    }

    private void registerListener() {
        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player receiver = event.getPlayer();

                // OPs see everyone
                if (receiver.isOp()) return;

                PacketContainer packet = event.getPacket();
                List<PlayerInfoData> infoDataList = packet.getPlayerInfoDataLists().read(0);
                if (infoDataList == null) return;

                int visibilityDist = plugin.getConfig().getInt("visibility-distance", 20);

                List<PlayerInfoData> filtered = infoDataList.stream()
                        .filter(info -> {
                            if (info == null || info.getProfile() == null) return false;
                            Player target = plugin.getServer().getPlayer(info.getProfile().getUUID());
                            if (target == null) return false;

                            double dist = receiver.getLocation().distance(target.getLocation());

                            // Always allow OPs if within range
                            if (target.isOp()) return dist <= visibilityDist;

                            // Allow normal players if within range
                            return dist <= visibilityDist;
                        })
                        .collect(Collectors.toList());

                packet.getPlayerInfoDataLists().getValues().set(0, filtered);
            }
        });
    }
}
