package api;
import api.handler.CharacterHandler;
import api.handler.ItemHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class APIServer {
    private HttpServer server;
    private final String listenIP;
    private final Integer listenPort;

    public APIServer(String listenIP, Integer listenPort) {
        this.listenIP = listenIP;
        this.listenPort = listenPort;
    }

    public void start() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(listenIP, listenPort), 0);
        server.createContext("/item", new ItemHandler());
        server.createContext("/character", new CharacterHandler());
        server.start();
        log.info("API Server started at {}:{}", this.listenIP, this.listenPort);
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }
}
