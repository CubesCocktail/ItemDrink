package com.github.zamponimarco.itemdrink.command.cloud;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CloudRemoveCommand extends AbstractCloudCommand{
    @Override
    protected void execute(String[] arguments, CommandSender sender) {
        if (!Bukkit.getServer().getOnlineMode()) {
            sender.sendMessage(MessageUtils.color("&c&lAvailable only in online mode."));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(CubesCocktail.getInstance(), () -> {
            Player player = (Player) sender;
            String id = player.getUniqueId().toString();

            if (arguments.length < 2) {
                return;
            }

            String itemName = arguments[1];

            URL url;
            try {
                url = new URL("http://188.34.166.204:3000/items/" + id + "/" + itemName);
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                http.setRequestMethod("DELETE");
                http.setDoOutput(true);
                http.connect();
                http.getResponseCode();
                http.disconnect();
                player.sendMessage(MessageUtils.color("&aItem correctly eliminated from cloud"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected boolean isOnlyPlayer() {
        return true;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.cloud.remove");
    }
}
