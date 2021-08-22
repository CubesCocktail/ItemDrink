package com.github.zamponimarco.itemdrink.command.cloud;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.function.AbstractFunction;
import com.github.zamponimarco.cubescocktail.function.Function;
import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.cubescocktail.libs.util.MessageUtils;
import com.github.zamponimarco.cubescocktail.util.CompressUtils;
import com.github.zamponimarco.itemdrink.item.AbstractItem;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

public abstract class AbstractCloudCommand extends AbstractCommand {

    protected void printHelpMessage(CommandSender sender) {
        sender.sendMessage(MessageUtils.color("        &c&lItem&6&lDrink &cCloud Help\n" +
                "&2/cc item cloud help &7Show cloud help message.\n" +
                "&2/cc item cloud explore &7Show public items.\n" +
                "&2/cc item cloud list &7Show personal published items.\n" +
                "&2/cc item cloud publish [item] &7Makes the item public (needs moderator approval to be visible).\n" +
                "&2/cc item cloud remove [item] &7Remove an exported item from the cloud.\n" +
                "&2/cc item cloud export [item] &7Export to cloud the item saved locally.\n" +
                "&2/cc item cloud import [player] [item] &7Import from cloud the selected item from the selected player.\n"));
    }

    protected String getPlayerUUID(String playerName) throws IOException {
        String playerId;
        URL url = new URL("https://api.mojang.com/profiles/minecraft");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.setDoOutput(true);
        String load = "[\"" + playerName + "\"]";
        byte[] out = load.getBytes(StandardCharsets.UTF_8);
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        try (InputStream is = http.getInputStream()) {
            Reader reader = new InputStreamReader(is);
            Gson gson = new GsonBuilder().create();
            final TypeAdapter<JsonObject> jsonObjectTypeAdapter = gson.getAdapter(JsonObject.class);
            JsonReader jsonReader = gson.newJsonReader(reader);
            jsonReader.beginArray();
            final JsonObject incomingJsonObject = jsonObjectTypeAdapter.read(jsonReader);
            playerId = incomingJsonObject.get("id").getAsString();
            jsonReader.endArray();
            jsonReader.close();
        }
        http.disconnect();

        return String.format("%s-%s-%s-%s-%s", playerId.substring(0, 8), playerId.substring(8, 12),
                playerId.substring(12, 16), playerId.substring(16, 20), playerId.substring(20));
    }

    protected JsonObject getItemJson(Player player, AbstractItem item) {
        JsonArray skillsArray = new JsonArray();
        Set<Function> usedSkills = item.getUsedExecutableSkills();
        Set<AbstractFunction> topLevelUsedSkills = Sets.newHashSet();
        usedSkills.forEach(skill -> topLevelUsedSkills.add(CubesCocktail.getInstance().getFunctionManager().
                getTopFunctionByName(skill.getName())));
        topLevelUsedSkills.stream().map(skill -> parseFunction(skill, player.getUniqueId().toString())).
                forEach(skillsArray::add);

        JsonObject obj = new JsonObject();
        obj.addProperty("name", item.getName());
        obj.addProperty("item", new String(Base64.getEncoder().encode(CompressUtils.
                compress(item.toSerializedString().getBytes())), Charset.defaultCharset()));
        obj.addProperty("owner", player.getUniqueId().toString());
        obj.add("skills", skillsArray);
        return obj;
    }

    protected JsonElement parseFunction(AbstractFunction skill, String owner) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", skill.getName());
        obj.addProperty("owner", owner);
        obj.addProperty("skill", new String(Base64.getEncoder().encode(CompressUtils.
                compress(skill.toSerializedString().getBytes())), Charset.defaultCharset()));
        return obj;
    }

}
