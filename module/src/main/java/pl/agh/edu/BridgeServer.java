package pl.agh.edu;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

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

        HttpServer server = vertx.createHttpServer();

        // Also serve the static resources. In real life this would probably be done by a CDN
        server.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                if (req.path().equals("/")) req.response().sendFile("communication_test.html"); // Serve the index.html
                if (req.path().endsWith("vertxbus.js")) req.response().sendFile("vertxbus-2.1.js"); // Serve the js
                if (req.path().endsWith("js/login.js")) req.response().sendFile("js/login.js");
                if (req.path().endsWith("js/lib/angular-vertxbus.min.js")) req.response().sendFile("js/lib/angular-vertxbus.min.js");
                if (req.path().endsWith("style.css")) req.response().sendFile("static/css/styles.css"); // Serve the js
                if (req.path().endsWith("sock.js")) req.response().sendFile("static/js/sock.js"); // Serve the js
                if (req.path().endsWith("angular.js")) req.response().sendFile("static/js/angular.js"); // Serve the js
                if (req.path().endsWith("jquery.js")) req.response().sendFile("static/js/jquery-2.1.1.min.js"); // Serve the js
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
        
        
        container.deployVerticle("pl.agh.edu.StateUpdateListener", new AsyncResultHandler<String>() {
            public void handle(AsyncResult<String> deployResult) {
              if (deployResult.succeeded()) {
            	  logger.info("Successfully deployed StateUpdateListener");
              } else {
            	  logger.error("Deploying StateUpdateListener failed.");
              }
            }
          });
        
        
    }
}