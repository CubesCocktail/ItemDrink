package com.github.zamponimarco.itemdrink.command.cloud;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.AbstractItem;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class CloudExportCommand extends AbstractCloudCommand{
    @Override
    protected void execute(String[] arguments, CommandSender sender) {
        if (!Bukkit.getServer().getOnlineMode()) {
            sender.sendMessage(MessageUtils.color("&c&lAvailable only in online mode."));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(CubesCocktail.getInstance(), () -> {
            Player player = (Player) sender;

            if (arguments.length < 2) {
                printHelpMessage(sender);
                return;
            }

            String name = arguments[1];
            AbstractItem item = ItemDrink.getInstance().getItemManager().getAbstractItemByName(name);

            if (item == null) {
                sender.sendMessage(MessageUtils.color("&cThe item &e" + name + " &ccouldn't be found."));
                return;
            }

            JsonObject obj = getItemJson(player, item);

            byte[] out = obj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            URL url;
            try {
                url = new URL("http://188.34.166.204:3000/items");
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                http.setFixedLengthStreamingMode(length);
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                http.setDoOutput(true);
                http.connect();
                try (OutputStream os = http.getOutputStream()) {
                    os.write(out);
                }
                sender.sendMessage(MessageUtils.color("&aItem correctly exported."));
                http.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(MessageUtils.color("&cError during item export."));
            }
        });
    }

    @Override
    protected boolean isOnlyPlayer() {
        return true;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.cloud.export");
    }
}
