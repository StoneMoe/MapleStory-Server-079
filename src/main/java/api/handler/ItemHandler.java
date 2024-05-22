package api.handler;

import api.Response;
import api.Tools;
import api.model.AddItemRequest;
import client.MapleCharacter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import handling.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;

import java.io.IOException;
import java.util.ArrayList;

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
                response = getItem(exchange);
                break;
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

    private static Response addItem(HttpExchange exchange) throws IOException {
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

    private static Response getItem(HttpExchange exchange) throws IOException {
        var response = new Response();
        switch (exchange.getRequestURI().getPath())
        {
            case "/item/checkInvalid":
                response = checkInvalidItem(exchange);
                break;
            default:
                response.setResponse("Not implemented.");
                response.setCode(404);
        }

        return response;
    }

    private static Response checkInvalidItem(HttpExchange exchange) throws IOException {
        var response = new Response();
        var params = Tools.queryToMap(exchange.getRequestURI().getQuery());
        Integer characterId = null;
        try
        {
            characterId = Integer.parseInt(params.get("characterId"));
        }
        catch (Exception ex)
        {
            response.setResponse("Invalid characterId.");
            response.setCode(500);
            return response;
        }

        var character = Tools.getCharacter(characterId);
        if (character == null)
        {
            response.setResponse(String.format("Character %d not found.", characterId));
            response.setCode(404);
            return response;
        }

        var itemMap = Tools.getItemMap();
        if (itemMap == null)
        {
            response.setResponse("Failed to load item map.");
            response.setCode(500);
            return response;
        }

        var invalidItemId = new ArrayList<Integer>();
        for (final var inventory: character.getInventorys())
        {
            for (final var item: inventory.list())
            {
                if (!itemMap.containsKey(item.getItemId()))
                {
                    invalidItemId.add(item.getItemId());
                }
            }
        }

        var mapper = new JsonMapper();
        var jsonString = mapper.writeValueAsString(invalidItemId);
        response.setResponse(jsonString);
        response.setCode(200);
        return response;
    }
}
