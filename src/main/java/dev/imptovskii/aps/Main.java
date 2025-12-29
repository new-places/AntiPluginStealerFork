package dev.imptovskii.aps;
 
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;
import dev.imptovskii.aps.commands.ReloadCommand;
import dev.imptovskii.aps.listeners.CommandBlocker;
import dev.imptovskii.aps.listeners.CustomVersion;
import dev.imptovskii.aps.listeners.SyntaxBlocker;
import dev.imptovskii.aps.packetadapters.AntiPluginStealer;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
 
public void onEnable() {
    if (!(new File(getDataFolder(), "config.yml")).exists()) {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }
    
    getCommand("apsreload").setExecutor(new ReloadCommand(this));
    getServer().getPluginManager().registerEvents(new SyntaxBlocker(this), this);
    getServer().getPluginManager().registerEvents(new CommandBlocker(this), this);
    getServer().getPluginManager().registerEvents(new CustomVersion(this), this);
    
    Bukkit.getConsoleSender().sendMessage("AntiPluginStealer by JoseMarcellio, Fork by /new places/");
    
    if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
        registerPacketListenerIfSupported();
    } else {
        getLogger().log(Level.WARNING, "Required ProtocolLib 5.0.0 or higher!");
    }
}

private void registerPacketListenerIfSupported() {
    try {
        // Получаем чистую версию Minecraft
        String mcVersion = Bukkit.getMinecraftVersion();
        String[] versionParts = mcVersion.split("\\.");
        
        if (versionParts.length >= 2) {
            int minorVersion = Integer.parseInt(versionParts[1]);
            
            // Проверяем диапазон поддерживаемых версий (1.13 - 1.21)
            boolean isSupportedVersion = minorVersion >= 13 && minorVersion <= 21;
            
            if (isSupportedVersion) {
                ProtocolLibrary.getProtocolManager().addPacketListener(
                    new AntiPluginStealer(this, PacketType.Play.Client.TAB_COMPLETE)
                );
            }
        }
    } catch (Exception e) {
        getLogger().log(Level.WARNING, "Could not determine Minecraft version", e);
    }
}
