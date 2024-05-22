package client.commands.models;

import constants.ServerConstants;
import lombok.Data;

@Data
public class CommandResult {
    private String command;
    private ServerConstants.CommandType type;
    private String message;
    private boolean success;
}
