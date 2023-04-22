package com.github.zamponimarco.itemdrink.command.cloud;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.function.AbstractFunction;
import com.github.zamponimarco.cubescocktail.libs.model.NamedModel;
import com.github.zamponimarco.cubescocktail.libs.util.CompressUtils;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.itemdrink.ItemDrink;
import com.github.zamponimarco.itemdrink.item.AbstractItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Base64;

public class CloudImportCommand extends AbstractCloudCommand {
    @Override
    protected void execute(String[] strings, CommandSender commandSender) {
        Bukkit.getScheduler().runTaskAsynchronously(CubesCocktail.getInstance(), () -> {
            if (strings.length < 3) {
                printHelpMessage(commandSender);
                return;
            }

            String playerName = strings[1];
            String itemName = strings[2];

            URL url;
            try {
                String uuid = getPlayerUUID(playerName);

                url = new URL("http://188.34.166.204:3000/items/" + uuid + "/" + itemName);
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                http.setRequestMethod("GET");
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                http.setDoInput(true);
                http.connect();
                try (InputStream is = http.getInputStream()) {
                    Reader reader = new InputStreamReader(is);
                    Gson gson = new GsonBuilder().create();
                    final TypeAdapter<JsonObject> jsonObjectTypeAdapter = gson.getAdapter(JsonObject.class);
                    JsonReader jsonReader = gson.newJsonReader(reader);
                    final JsonObject incomingJsonObject = jsonObjectTypeAdapter.read(jsonReader);
                    incomingJsonObject.getAsJsonArray("skills").forEach(elm ->
                            CubesCocktail.getInstance().getFunctionManager().addFunction((AbstractFunction) NamedModel.
                                    fromSerializedString(new String(CompressUtils.decompress(Base64.getDecoder().
                                            decode(elm.getAsJsonObject().get("skill").getAsString())), Charset.defaultCharset()))));
                    ItemDrink.getInstance().getItemManager().addItem((AbstractItem) NamedModel.fromSerializedString(
                            new String(CompressUtils.decompress(Base64.getDecoder().decode(incomingJsonObject.
                                    get("item").getAsString())), Charset.defaultCharset())));
                    commandSender.sendMessage(MessageUtils.color("&aThe item has been succesfully imported."));
                    jsonReader.close();
                }
                http.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                commandSender.sendMessage(MessageUtils.color("&cError during item import."));
            }
        });
    }

    @Override
    protected boolean isOnlyPlayer() {
        return false;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.cloud.import");
    }
}
