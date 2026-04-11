package com.github.zamponimarco.itemdrink.command.cloud;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CloudListCommand extends AbstractCloudCommand {
    @Override
    protected void execute(String[] strings, CommandSender commandSender) {
        if (!Bukkit.getServer().getOnlineMode()) {
            commandSender.sendMessage(MessageUtils.color("&c&lAvailable only in online mode."));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(CubesCocktail.getInstance(), () -> {
            Player player = (Player) commandSender;
            String id = player.getUniqueId().toString();

            URL url;
            try {
                url = new URL("http://188.34.166.204:3000/items/" + id);
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                http.setRequestMethod("GET");
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                http.setDoInput(true);
                http.connect();
                try (InputStream is = http.getInputStream()) {
                    Reader reader = new InputStreamReader(is);
                    Gson gson = new GsonBuilder().create();
                    final TypeAdapter<JsonArray> jsonObjectTypeAdapter = gson.getAdapter(JsonArray.class);
                    JsonReader jsonReader = gson.newJsonReader(reader);
                    final JsonArray incomingJsonObject = jsonObjectTypeAdapter.read(jsonReader);
                    player.sendMessage(MessageUtils.color("&6&lList of items in cloud:"));
                    player.sendMessage(MessageUtils.color("&cName &6&l| &cPublic &6&l| &cApproved"));
                    incomingJsonObject.forEach(elm -> {
                        JsonObject obj = elm.getAsJsonObject();
                        player.sendMessage(MessageUtils.color(String.format("&6&l - &c%s",
                                obj.get("name").getAsString() + " &6&l| " + getBooleanString(obj.get("public").
                                        getAsBoolean()) + " &6&l| " + getBooleanString(obj.get("approved").getAsBoolean()))));
                    });
                    jsonReader.close();
                }
                http.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String getBooleanString(boolean bool) {
        return bool ? "&a✓" : "&c✗";
    }

    @Override
    protected boolean isOnlyPlayer() {
        return true;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.cloud.list");
    }
}
