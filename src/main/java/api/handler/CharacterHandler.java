package api.handler;

import api.Response;
import api.model.CharacterResponse;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import handling.channel.ChannelServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CharacterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var method = exchange.getRequestMethod();
        var response = new Response();
        switch (method) {
            case "GET":
                response = getCharacter(exchange);
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

    private Response getCharacter(HttpExchange exchange) throws IOException {
        var response = new Response();
        var channelCharactersMap = new HashMap<Integer, ArrayList<CharacterResponse>>();
        try
        {
            for (final var channel: ChannelServer.getAllInstances())
            {
                var characters = channel.getPlayerStorage().getAllCharacters();
                if (characters.size() > 0)
                {
                    var result = new ArrayList<CharacterResponse>();
                    for (final var character: characters)
                    {
                        var item = new CharacterResponse();
                        item.setId(character.getId());
                        item.setName(character.getName());
                        result.add(item);
                    }

                    channelCharactersMap.put(channel.getChannel(), result);
                }
            }

            var mapper = new JsonMapper();
            response.setResponse(mapper.writeValueAsString(channelCharactersMap));
        }
        catch (Exception e)
        {
            response.setResponse("Unexpected error.");
            response.setCode(400);
        }

        return response;
    }
}
