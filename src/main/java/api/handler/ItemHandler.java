package api.handler;

import api.Response;
import api.model.AddItemRequest;
import client.MapleCharacter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import handling.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;

import java.io.IOException;

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
            var addItemRequest = mapper.readValue(jsonString, AddItemRequest.class);

            MapleCharacter character = null;
            var itemName = MapleItemInformationProvider.getInstance().getName(addItemRequest.getItemId());
            if (itemName == null)
            {
                response.setResponse(String.format("Item %d not found.", addItemRequest.getItemId()));
                response.setCode(404);
                return response;
            }

            for (final var channel: ChannelServer.getAllInstances())
            {
                character = channel.getPlayerStorage().getCharacterById(addItemRequest.getCharacterId());
                if (character != null)
                {
                    MapleInventoryManipulator.addById(character.getClient(), addItemRequest.getItemId(), addItemRequest.getQuantity(), (byte) 0);
                    response.setResponse(String.format("Add %d of %s to %s.", addItemRequest.getQuantity(), MapleItemInformationProvider.getInstance().getName(addItemRequest.getItemId()), character.getName()));
                    response.setCode(200);
                    return response;
                }
            }

            if (character == null)
            {
                response.setResponse("Character not found.");
                response.setCode(404);
                return response;
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
