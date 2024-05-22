package client.commands;

import constants.ServerConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String command();

    ServerConstants.CommandType commandType();
}
