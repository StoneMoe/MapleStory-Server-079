package api.handler;

import api.Response;
import api.model.addItemRequest;
import client.MapleCharacter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import handling.channel.ChannelServer;
import lombok.extern.slf4j.Slf4j;
import server.MapleInventoryManipulator;


import java.io.IOException;

@Slf4j
public class ItemHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var method = exchange.getRequestMethod();
        var response = new Response();
        switch (method) {
            case "POST":
                response = addItem(exchange);
                break;
            case "GET":
            default:
                response.setResponse("Not implemented.");
                response.setCode(404);
        }

        var responseBytes = response.getResponse().getBytes();
        var responseStream = exchange.getResponseBody();
        exchange.sendResponseHeaders(response.getCode(), response.getResponse().getBytes().length);
        responseStream.write(responseBytes);
        responseStream.close();
    }

    private Response addItem(HttpExchange exchange) throws IOException {
        var response = new Response();
        try
        {
            var jsonString = new String(exchange.getRequestBody().readAllBytes());
            var mapper = new JsonMapper();
            var addItemRequest = mapper.readValue(jsonString, addItemRequest.class);

            MapleCharacter character = null;
            for (final var channel: ChannelServer.getAllInstances())
            {
                character = channel.getPlayerStorage().getCharacterById(addItemRequest.getCharacterId());
                if (character != null)
                {
                    MapleInventoryManipulator.addById(character.getClient(), addItemRequest.getItemId(), addItemRequest.getQuantity(), (byte) 0);
                    response.setResponse(String.format("Add %d of %d to %s.", addItemRequest.getQuantity(), addItemRequest.getItemId(), character.getName()));
                    response.setCode(200);
                    break;
                }
            }

            if (character == null)
            {
                response.setResponse("Character not found.");
                response.setCode(404);
            }
        }
        catch (Exception e)
        {
            response.setResponse("Unexpected error.");
            response.setCode(400);
        }

        return response;
    }
}
