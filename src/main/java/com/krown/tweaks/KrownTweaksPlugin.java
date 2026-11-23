package com.krown.tweaks;

import com.krown.tweaks.commands.KrownTweaksCommand;
import com.krown.tweaks.commands.ShopEditCommand;
import com.krown.tweaks.commands.SpectateCommand;
import com.krown.tweaks.listeners.ShopEditListener;
import com.krown.tweaks.utils.ChatUtils;
import de.marcely.bedwars.api.AddonAPI;
import de.marcely.bedwars.api.BedwarsAddon;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class KrownTweaksPlugin extends JavaPlugin {
    private static KrownTweaksPlugin instance;
    private ShopEditListener shopEditListener;
    private BedwarsAddon bedwarsAddon;
    private File customDataFolder;
    private FileConfiguration customConfig;

    public static KrownTweaksPlugin getInstance() {
        return instance;
    }

    public File getCustomDataFolder() {
        if (this.customDataFolder == null) {
            this.customDataFolder = resolveDataFolder();
        }
        return this.customDataFolder;
    }

    private File resolveDataFolder() {
        try {
            if (this.bedwarsAddon != null) {
                File folder = this.bedwarsAddon.getDataFolder();
                if (folder != null) {
                    return folder;
                }
            }
            if (Bukkit.getPluginManager().isPluginEnabled("MBedwars")) {
                File addonsFolder = AddonAPI.get().getFolder();
                if (addonsFolder != null) {
                    return new File(addonsFolder, "KrownTweaks");
                }
            }
        } catch (Throwable ignored) {
        }

        Plugin mbedwars = Bukkit.getPluginManager().getPlugin("MBedwars");
        File baseFolder = (mbedwars != null) ? mbedwars.getDataFolder() : new File("plugins", "MBedwars");
        File addonsDir = new File(baseFolder, "add-ons");
        if (!addonsDir.exists()) {
            File alt = new File(baseFolder, "addons");
            if (alt.exists()) {
                addonsDir = alt;
            }
        }
        return new File(addonsDir, "KrownTweaks");
    }

    private void setupDataFolder() {
        File folder = this.getCustomDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try {
            Field dataFolderField = JavaPlugin.class.getDeclaredField("dataFolder");
            dataFolderField.setAccessible(true);
            dataFolderField.set(this, folder);

            Field configFileField = JavaPlugin.class.getDeclaredField("configFile");
            configFileField.setAccessible(true);
            configFileField.set(this, new File(folder, "config.yml"));
        } catch (Throwable ignored) {
        }

        File oldConfig = new File(new File("plugins", "KrownTweaks"), "config.yml");
        File newConfig = new File(folder, "config.yml");
        if (oldConfig.exists() && !newConfig.exists()) {
            try {
                Files.copy(oldConfig.toPath(), newConfig.toPath());
                this.getLogger().info("Migrated existing config.yml from plugins/KrownTweaks to " + newConfig.getPath());
            } catch (Exception e) {
                this.getLogger().warning("Failed to migrate config: " + e.getMessage());
            }
        }
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.customConfig == null) {
            this.reloadConfig();
        }
        return this.customConfig;
    }

    @Override
    public void reloadConfig() {
        File configFile = new File(this.getCustomDataFolder(), "config.yml");
        this.customConfig = YamlConfiguration.loadConfiguration(configFile);

        InputStream defConfigStream = this.getResource("config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            this.customConfig.setDefaults(defConfig);
        }
    }

    @Override
    public void saveConfig() {
        if (this.customConfig != null) {
            try {
                this.customConfig.save(new File(this.getCustomDataFolder(), "config.yml"));
            } catch (Exception e) {
                this.getLogger().severe("Could not save config to " + new File(this.getCustomDataFolder(), "config.yml"));
            }
        }
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(this.getCustomDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = this.getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in plugin jar");
        }

        File outFile = new File(this.getCustomDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(this.getCustomDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                Files.copy(in, outFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            this.getLogger().severe("Could not save " + outFile.getName() + " to " + outFile);
        }
    }

    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("MBedwars") == null) {
            this.getLogger().severe("Marcely's Bedwars (MBedwars) was not found! Disabling KrownTweaks...");
            Bukkit.getPluginManager().disablePlugin((Plugin)this);
            return;
        }

        try {
            this.bedwarsAddon = new BedwarsAddon(this) {
                @Override
                public String getName() {
                    return "KrownTweaks";
                }
            };
            this.bedwarsAddon.register();
            this.getLogger().info("Registered KrownTweaks as an MBedwars add-on!");
        } catch (Throwable t) {
            this.getLogger().warning("Could not register BedwarsAddon hook: " + t.getMessage());
        }

        this.setupDataFolder();
        this.saveDefaultConfig();

        this.shopEditListener = new ShopEditListener(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.shopEditListener, (Plugin)this);
        this.registerCommand("shopedit", new ShopEditCommand(this));
        this.registerCommand("spectate", new SpectateCommand(this));
        this.registerCommand("krowntweaks", new KrownTweaksCommand(this));
        this.getLogger().info("KrownTweaks v" + this.getDescription().getVersion() + " (MC 1.21.1) has been enabled!");
    }

    public void onDisable() {
        if (this.bedwarsAddon != null && this.bedwarsAddon.isRegistered()) {
            try {
                this.bedwarsAddon.unregister();
            } catch (Throwable ignored) {
            }
        }
        this.getLogger().info("KrownTweaks has been disabled.");
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand cmd = this.getCommand(name);
        if (cmd != null) {
            if (executor instanceof CommandExecutor) {
                cmd.setExecutor((CommandExecutor)executor);
            }
            if (executor instanceof TabCompleter) {
                cmd.setTabCompleter((TabCompleter)executor);
            }
        } else {
            this.getLogger().warning("Could not register command /" + name + " - missing in plugin.yml!");
        }
    }

    public ShopEditListener getShopEditListener() {
        return this.shopEditListener;
    }

    public String getPrefix() {
        return ChatUtils.color(this.getConfig().getString("prefix", "&8[&6Krown&eTweaks&8]&r "));
    }

    public String getMessage(String key) {
        return this.getMessage(key, null);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String msg = this.getConfig().getString("messages." + key, "");
        if (msg == null || msg.isEmpty()) {
            return "";
        }
        return this.getPrefix() + ChatUtils.format(msg, placeholders);
    }
}
