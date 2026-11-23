package com.krown.tweaks.utils;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class ChatUtils {
    // Gradient format: <gradient:#RRGGBB:#RRGGBB>text</gradient>
    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
        "<gradient:(#[a-fA-F0-9]{6}):(#[a-fA-F0-9]{6})>(.*?)</gradient>",
        Pattern.CASE_INSENSITIVE
    );

    // RGB Hex formats: &#RRGGBB, <#RRGGBB>, {#RRGGBB}, <color:#RRGGBB>, #RRGGBB (Birdflop formats)
    private static final Pattern HEX_PATTERN = Pattern.compile(
        "(?:&#|#|<#|\\{#|<color:#)([a-fA-F0-9]{6})[>\\}]?"
    );

    private ChatUtils() {
    }

    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // 1. Process Gradients (<gradient:#hex1:#hex2>text</gradient>)
        message = applyGradients(message);

        // 2. Process RGB Hex color codes (&#RRGGBB, <#RRGGBB>, {#RRGGBB}, <color:#RRGGBB>, #RRGGBB)
        message = applyHexColors(message);

        // 3. Process standard Minecraft legacy color codes (&0-9, &a-f, &k-r)
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String toMinecraftHex(String hex6) {
        StringBuilder sb = new StringBuilder("§x");
        for (int i = 0; i < 6; i++) {
            sb.append('§').append(Character.toLowerCase(hex6.charAt(i)));
        }
        return sb.toString();
    }

    private static String applyGradients(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                Color startColor = Color.decode(matcher.group(1));
                Color endColor = Color.decode(matcher.group(2));
                String text = matcher.group(3);

                StringBuilder gradientText = new StringBuilder();
                int length = text.length();
                if (length == 0) {
                    matcher.appendReplacement(sb, "");
                    continue;
                }

                for (int i = 0; i < length; i++) {
                    char ch = text.charAt(i);
                    float ratio = (length == 1) ? 0.0f : (float) i / (float) (length - 1);
                    int r = Math.min(255, Math.max(0, (int) (startColor.getRed() + ratio * (endColor.getRed() - startColor.getRed()))));
                    int g = Math.min(255, Math.max(0, (int) (startColor.getGreen() + ratio * (endColor.getGreen() - startColor.getGreen()))));
                    int b = Math.min(255, Math.max(0, (int) (startColor.getBlue() + ratio * (endColor.getBlue() - startColor.getBlue()))));

                    String hex = String.format("%02x%02x%02x", r, g, b);
                    gradientText.append(toMinecraftHex(hex)).append(ch);
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(gradientText.toString()));
            } catch (Exception e) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String applyHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                String hex = matcher.group(1);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(toMinecraftHex(hex)));
            } catch (Exception e) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String format(String message, Map<String, String> placeholders) {
        if (message == null) {
            return "";
        }
        String result = message;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String val = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace("{" + entry.getKey() + "}", val);
            }
        }
        return color(result);
    }

    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null || message.trim().isEmpty()) {
            return;
        }
        try {
            player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(message)
            );
        } catch (Throwable t) {
            try {
                player.sendActionBar(message);
            } catch (Throwable t2) {
                player.sendMessage(message);
            }
        }
    }
}
