/*
 * Command Blocker Ultimate
 * Copyright (C) 2014-2019 Philipp Nowak / Literallie (l1t.li)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.xxyy.cmdblocker.common.config;

import io.github.xxyy.cmdblocker.common.util.CommandHelper;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the CommandBlockerUltimate configuration file.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 16.7.14
 */
public class CBUConfig extends Config implements ConfigAdapter {

    @Path(ConfigAdapter.TARGET_COMMANDS_PATH)
    @Comments({"Define what commands should be blocked in the following property: (without leading slash)",
            "With Spigot/Bukkit, if you specify a command, its aliases will be blocked also. (Example: 'tell' will also block 'msg', 'bukkit:tell', etc.)",
            "On BungeeCord, only BungeeCord command aliases can be blocked - If you want to block Spigot/Bukkit, you'll have to write all aliases down."})
    private List<String> rawTargetCommands = new ArrayList<>(Arrays.asList("help", "plugins", "version"));
    private transient Set<String> blockedCommands = new HashSet<>(rawTargetCommands); //has aliases

    @Path(ConfigAdapter.BYPASS_PERMISSION_PATH)
    @Comment("Define the permission that a player needs to bypass the protection: (Default: cmdblock.bypass)")
    private String bypassPermission = "cmdblock.bypass";

    @Path(ConfigAdapter.SHOW_ERROR_MESSAGE_PATH)
    @Comment("Should the plugin send an error message if one is not allowed to execute a command? (Default: true)")
    private boolean showErrorMessage = true;

    @Path(ConfigAdapter.SHOW_TAB_ERROR_MESSAGE_PATH)
    @Comments({"@since 1.5.1", "Should the plugin send an error message if one is not allowed to tab-complete a command? (Default: true)"})
    private boolean showTabErrorMessage = false;

    @Path(ConfigAdapter.ERROR_MESSAGE_PATH)
    @Comments({"What should that message be? (Use & for color codes, HTML escape codes accepted)",
            "<command> will be replaced with the blocked command's name",
            "Example: &c&lError message &euro;&auml;&#00A7;"})
    private String errorMessage = "&cYou are not permitted to execute this command.";

    @Path(ConfigAdapter.PREVENT_TAB_PATH)
    @Comments({"@since 1.02",
            "Whether to prevent tab-completion for blocked commands.",
            "Note: Requires ProtocolLib on Spigot!",
            "Default value: true"})
    private boolean preventTab = true;

    @Path(TAB_RESTRICTIVE_MODE_PATH)
    @Comments({"What strategy to use when blocking tab-complete replies from the server.",
            "true: block all completions returning a targeted command (for example, if /p is typed and /pl is blocked, print error message)",
            "false: just remove blocked commands from list (in the above example, other commands starting with p would still be shown without notice)",
            "Default value: false"})
    private boolean tabRestrictiveMode = false;

    @Path(NOTIFY_BYPASS_PATH)
    @Comments({"@since 1.4.0",
            "Whether to display a message when executing an otherwise blocked command to permitted users",
            "Default value: false"})
    private boolean notifyBypass = false;

    @Path(BYPASS_MESSAGE_PATH)
    @Comments({"@since 1.4.0",
            "The message to display when executing an otherwise blocked command (permitted users only, see notify-bypass)",
            "<command> will be replaced with the blocked command's name",
            "Example: '&c/<command> is blocked. Executing anyways since you have permission.'"})
    private String bypassMessage = "&c[CBU] This command is blocked. Executing anyways since you have permission.";

    @Path(TAB_ERROR_MESSAGE_PATH)
    @Comments({"@since 1.4.0",
            "The message to display when somebody tries to tab-complete a command that's blocked.",
            "Example: '&cYou cannot see completions for this.'"})
    private String tabErrorMessage = "&cI am sorry, but I cannot let you do this, Dave.";

    @Path(RESOLVE_ALIASES_PATH)
    @Comments({"@since 1.5.6",
            "Whether to resolve aliases at all.",
            "If disabled, only commands specified explicitly will be blocked.",
            "Default value: true"})
    private boolean resolveAliases = true;

    public CBUConfig(File configFile) {
        CONFIG_HEADER = new String[]{
                "Configuration file for CommandBlockerUltimate. CommandBlockerUltimate is licensed under " +
                        "the GNU GPL v2 license (see the LICENSE file in the plugin jar for details)." +
                        "Find its source at https://github.com/xxyy/commandblockerultimate." +
                        "If you need help configuring, open an issue at GitHub. (link above)"
        };
        CONFIG_FILE = configFile;
    }

    @Override
    public boolean tryInitialize(Logger logger) {
        try {
            this.initialize(); //This does not call #initialize() to avoid another try-catch block
            this.save(); //Save, just in case any new options were added
        } catch (InvalidConfigException | InvalidConfigurationException e) {
            logger.log(Level.WARNING, "Encountered exception!", e);
            logger.warning("Could not load configuration file. Please double-check your YAML syntax with http://yaml-online-parser.appspot.com/.");
            logger.warning("The plugin might (will) not function in the way you want it to (since it doesn't know what you want)");
            logger.warning("If you don't understand this error, try opening an issue at https://github.com/xxyy/commandblockerultimate/issues");
            return false;
        }

        return true;
    }

    @Override
    public void initialize() throws InvalidConfigException {
        try {
            this.init();
            blockedCommands.addAll(rawTargetCommands); //if aliases are not resolved
        } catch (InvalidConfigurationException e) {
            throw new InvalidConfigException(e);
        }
    }

    @Override
    public boolean trySave() {
        try {
            save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void resolveAliases(AliasResolver aliasResolver) {
        if (!resolveAliases) {
            blockedCommands = new HashSet<>(rawTargetCommands);
            return;
        }
        aliasResolver.refreshMap();
        blockedCommands.clear();

        for (String requestedCommand : new ArrayList<>(rawTargetCommands)) {
            blockedCommands.add(requestedCommand);
            blockedCommands.addAll(aliasResolver.resolve(requestedCommand));
        }
    }

    @Override
    public boolean isBlocked(String commandName) {
        return blockedCommands.contains(commandName) ||
                blockedCommands.contains(CommandHelper.removeModPrefix(commandName)); //e.g. minecraft:me
    }

    @Override
    public Collection<String> getBlockedCommands() {
        return blockedCommands;
    }

    @Override
    public void addBlockedCommand(String command) {
        if (rawTargetCommands.contains(command)) {
            return;
        }
        getBlockedCommands().add(command);
        rawTargetCommands.add(command);
    }

    @Override
    public boolean removeBlockedCommand(String command) {
        rawTargetCommands.remove(command);
        return getBlockedCommands().remove(command);
    }

    @Override
    public String getBypassPermission() {
        return bypassPermission;
    }

    @Override
    public boolean isShowErrorMessage() {
        return showErrorMessage;
    }

    @Override
    public boolean isShowTabErrorMessage() {
        return showTabErrorMessage;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean isPreventTab() {
        return preventTab;
    }

    @Override
    public boolean isTabRestrictiveMode() {
        return tabRestrictiveMode;
    }

    @Override
    public boolean isNotifyBypass() {
        return notifyBypass;
    }

    @Override
    public String getBypassMessage() {
        return bypassMessage;
    }

    @Override
    public String getTabErrorMessage() {
        return tabErrorMessage;
    }
}
