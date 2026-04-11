package com.github.zamponimarco.itemdrink.command;

import com.github.zamponimarco.cubescocktail.CubesCocktail;
import com.github.zamponimarco.cubescocktail.libs.command.AbstractCommand;
import com.github.zamponimarco.cubescocktail.libs.core.Libs;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupCommand extends AbstractCommand {

    @Override
    protected void execute(String[] arguments, CommandSender sender) {
        File pluginFolder = CubesCocktail.getInstance().getDataFolder();

        File backupFolder = new File(pluginFolder, "backup");
        if (!backupFolder.exists()) {
            backupFolder.mkdir();
        }

        if (backupFile(pluginFolder, backupFolder, "item") &&
                backupFile(pluginFolder, backupFolder, "savedskill") &&
                backupFile(pluginFolder, backupFolder, "savedplaceholder"))
            sender.sendMessage(Libs.getLocale().get("messages.command.backup-success"));
        else
            sender.sendMessage(Libs.getLocale().get("messages.command.backup-failure"));
    }

    private boolean backupFile(File pluginFolder, File backupFolder, String fileName) {
        File file = new File(pluginFolder, fileName + ".yml");

        if (file.exists()) {
            return FileUtil.copy(file, new File(backupFolder, new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss_a").
                    format(new Date()) + "-" + fileName + ".yml"));
        }
        return false;
    }

    @Override
    protected boolean isOnlyPlayer() {
        return false;
    }

    @Override
    protected Permission getPermission() {
        return new Permission("cubescocktail.item.backup");
    }
}
