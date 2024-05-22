package client.commands;

import client.MapleClient;
import client.commands.models.CommandResult;
import constants.ServerConstants;

@Command(command = "CommandAbstract", commandType = ServerConstants.CommandType.PlayerCommand)
public abstract class CommandAbstract {
    public abstract CommandResult execute(final MapleClient mapleClient, final String[] args);

    public abstract String showHelp();

    public CommandResult createResult() {
        var result = new CommandResult();
        result.setType(getCommandType());
        result.setCommand(getCommand());
        return result;
    }

    public String getCommand() {
        return this.getClass().getAnnotation(Command.class).command();
    }

    public ServerConstants.CommandType getCommandType() {
        return this.getClass().getAnnotation(Command.class).commandType();
    }

    public boolean preCheck(MapleClient mapleClient) {
        var commandType = getCommandType();
        if (commandType == ServerConstants.CommandType.GMCommand) {
            return mapleClient.getPlayer().isGM();
        } else {
            return true;
        }
    }
}
