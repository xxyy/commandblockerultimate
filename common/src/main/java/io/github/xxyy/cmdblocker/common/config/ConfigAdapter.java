package io.github.xxyy.cmdblocker.common.config;

import java.util.logging.Logger;

/**
 * Represents a configuration adapter (in most cases a YAML file) that is used to fetch CommandBlockerUltimate
 * configuration options and messages.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 16.7.14
 */
public interface ConfigAdapter {

    //Path definitions
    public static final String SHOW_ERROR_MESSAGE_PATH = "show-error-message";
    public static final String TARGET_COMMANDS_PATH = "target-commands";
    public static final String BYPASS_PERMISSION_PATH = "bypass-permission";
    public static final String ERROR_MESSAGE_PATH = "error-message";
    public static final String PREVENT_TAB_PATH = "prevent-tab";
    public static final String TAB_RESTRICTIVE_MODE_PATH = "tab-restrictive-mode";

    /**
     * Tries to initialize this config adapter.
     * If an exception occurs, it is logged to {@code logger} with details on how to get help from this plugin's author.
     * @param logger Logger to print to
     * @return whether the configuration was initialized successfully.
     */
    boolean tryInitialize(Logger logger);

    /**
     * Initializes this config adapter, loading all values into cache.
     * @throws InvalidConfigException if the underlying configuration is invalid for some reason (e.g. syntax error)
     */
    void initialize() throws InvalidConfigException;

    /**
     * @param commandName Command name to check
     * @return whether the given command is blocked.
     */
    boolean isBlocked(String commandName);

    String getBypassPermission();

    boolean isShowErrorMessage();

    String getErrorMessage();

    boolean isPreventTab();

    boolean isTabRestrictiveMode();
}