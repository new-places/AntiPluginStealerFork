package dev.imptovskii.aps;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;
import dev.imptovskii.aps.commands.ReloadCommand;
import dev.imptovskii.aps.listeners.CommandBlocker;
import dev.imptovskii.aps.listeners.CustomVersion;
import dev.imptovskii.aps.listeners.SyntaxBlocker;
import dev.imptovskii.aps.packetadapters.AntiPluginStealer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Создание конфигурации если не существует
        if (!new File(getDataFolder(), "config.yml").exists()) {
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }
        
        // Регистрация команд
        getCommand("apsreload").setExecutor(new ReloadCommand(this));
        
        // Регистрация слушателей событий
        Bukkit.getPluginManager().registerEvents(new SyntaxBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(new CommandBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(new CustomVersion(this), this);
        
        Bukkit.getConsoleSender().sendMessage("AntiPluginStealer by JoseMarcellio, Fork by /new places/");
        
        // Проверка наличия ProtocolLib
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().log(Level.WARNING, "Required ProtocolLib 5.0.0 or higher!");
            return;
        }
        
        // Регистрация packet listener для поддерживаемых версий
        registerPacketListenerForSupportedVersions();
    }
    
    private void registerPacketListenerForSupportedVersions() {
        try {
            String mcVersion = getMinecraftVersion();
            List<String> supportedVersions = Arrays.asList("1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20", "1.21");
            
            if (supportedVersions.stream().anyMatch(mcVersion::startsWith)) {
                ProtocolLibrary.getProtocolManager().addPacketListener(
                    new AntiPluginStealer(this, PacketType.Play.Client.TAB_COMPLETE)
                );
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Could not register packet listener: ", e);
        }
    }
    
    /**
     * Получает версию Minecraft, совместимую со старыми версиями Bukkit API
     */
    private String getMinecraftVersion() {
        // Современный способ (для новых версий Paper)
        try {
            java.lang.reflect.Method method = Bukkit.class.getMethod("getMinecraftVersion");
            return (String) method.invoke(null);
        } catch (Exception ignored) {
            // Если метод не найден, используем старый способ
        }
        
        // Старый способ для всех версий Bukkit/Spigot/Paper
        String version = Bukkit.getServer().getClass().getPackage().getName();
        version = version.substring(version.lastIndexOf('.') + 1);
        
        // Приводим к формату "1.XX"
        if (version.startsWith("v1_")) {
            version = version.replace("v1_", "1.");
            version = version.replace("_", ".");
        }
        
        // Обрезаем дополнительную информацию
        if (version.contains("_")) {
            version = version.substring(0, version.indexOf('_'));
        }
        
        return version;
    }
    
    @Override
    public void onDisable() {
        // Удаление packet listeners при выключении
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        }
    }
}