package pl.agh.edu;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

import java.nio.file.Paths;

/**
 * Created by Michal
 * 2014-11-08.
 */
public class BridgeServer extends Verticle {
    private static final long AUTH_TIMEOUT = 5 * 60 * 1000;
    Logger logger;

    public void start() {
        logger = container.logger();
        container.deployVerticle("pl.agh.edu.LoginVerticle");
        container.deployWorkerVerticle("pl.agh.edu.Simulator");

        HttpServer server = vertx.createHttpServer();

        // Also serve the static resources. In real life this would probably be done by a CDN
        server.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                if (req.path().equals("/")) req.response().sendFile(local("resources/index.html")); // Serve the index.html
                else req.response().sendFile(local(req.path()));
            }
        });

        JsonArray inboundPermitted = new JsonArray();
        inboundPermitted.add(new JsonObject().putString("address", "connect"))
                .add(new JsonObject().putString("address", "disconnect").putBoolean("requires_auth", true))
                .add(new JsonObject().putString("address_re", "chat.message.*").putBoolean("requires_auth", true));

        JsonArray outboundPermitted = new JsonArray();
        outboundPermitted.add(new JsonObject()); // Let everything through

        ServerHook hook = new ServerHook(logger);

        SockJSServer sockJSServer = vertx.createSockJSServer(server);
        sockJSServer.setHook(hook);
        sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"), inboundPermitted, outboundPermitted, AUTH_TIMEOUT, "authorise");

        server.listen(8888);
    }

    private String local(String file) {
        return Paths.get("src/main", file).toAbsolutePath().toString();
    }
}