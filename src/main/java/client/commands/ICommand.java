package client.commands;

import client.MapleClient;
import constants.ServerConstants;

public interface ICommand {
    ServerConstants.CommandType getCommandType();

    String getCommand();

    CommandResult execute(final MapleClient mapleClient, final String[] args);
}
