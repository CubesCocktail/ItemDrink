package com.github.zamponimarco.itemdrink;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.addon.Addon;
import com.github.zamponimarco.cubescocktail.libs.command.PluginCommandExecutor;
import com.github.zamponimarco.cubescocktail.libs.core.Libs;
import com.github.zamponimarco.cubescocktail.libs.gui.FieldInventoryHolderFactory;
import com.github.zamponimarco.itemdrink.command.*;
import com.github.zamponimarco.itemdrink.command.cloud.*;
import com.github.zamponimarco.itemdrink.gui.ItemCollectionInventoryHolder;
import com.github.zamponimarco.itemdrink.item.AbstractItem;
import com.github.zamponimarco.itemdrink.item.Item;
import com.github.zamponimarco.itemdrink.item.ItemFolder;
import com.github.zamponimarco.itemdrink.listener.TimerListener;
import com.github.zamponimarco.itemdrink.manager.ItemManager;
import com.github.zamponimarco.itemdrink.skill.SkillKey;
import com.github.zamponimarco.itemdrink.skill.TimedSkill;
import com.github.zamponimarco.itemdrink.skill.TriggeredSkill;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;

@Getter
public class ItemDrink extends Addon {

    private static ItemDrink instance;

    static {
        ConfigurationSerialization.registerClass(Item.class);
        ConfigurationSerialization.registerClass(ItemFolder.class);
        ConfigurationSerialization.registerClass(SkillKey.class);
        ConfigurationSerialization.registerClass(TimedSkill.class);
        ConfigurationSerialization.registerClass(TriggeredSkill.class);
    }

    private ItemManager itemManager;

    public static ItemDrink getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        setUpConfig();
        setUpLibrary();
        setUpData();
        setUpCommands();
        CubesCocktail.getInstance().getServer().getPluginManager().registerEvents(new TimerListener(),
                CubesCocktail.getInstance());
    }

    @Override
    public void renameFunction(String oldName, String name) {
        itemManager.getItems().forEach(item -> item.changeSkillName(oldName, name));
    }


    private void setUpConfig() {
    }

    private void setUpLibrary() {
        FieldInventoryHolderFactory.collectionGUIMap.put(AbstractItem.class, ItemCollectionInventoryHolder.class);
        File folder = new File(getDataFolder(), "locale");

        if (!folder.exists()) {
            folder.mkdir();
        }
        saveResource("locale" + File.separatorChar + "en-US.yml");

        File dataFile = new File(folder, "en-US.yml");

        Libs.getLocale().registerLocaleFiles(dataFile);
    }

    private void setUpData() {
        itemManager = new ItemManager(AbstractItem.class, "comp", CubesCocktail.getInstance());
    }

    private void setUpCommands() {
        PluginCommandExecutor cloudEx = new PluginCommandExecutor("help", new CloudHelpCommand());
        cloudEx.registerCommand("explore", new CloudExploreCommand());
        cloudEx.registerCommand("export", new CloudExportCommand());
        cloudEx.registerCommand("import", new CloudImportCommand());
        cloudEx.registerCommand("list", new CloudListCommand());
        cloudEx.registerCommand("publish", new CloudPublishCommand());
        cloudEx.registerCommand("remove", new CloudRemoveCommand());
        PluginCommandExecutor ex = new PluginCommandExecutor("help", new HelpCommand());
        ex.registerCommand("list", new ItemListCommand());
        ex.registerCommand("give", new ItemGiveCommand());
        ex.registerCommand("get", new ItemGetCommand());
        ex.registerCommand("cloud", cloudEx);
        ex.registerCommand("backup", new BackupCommand());
        CubesCocktail.getInstance().getCommandExecutor().registerCommand("item", ex);
    }
}
