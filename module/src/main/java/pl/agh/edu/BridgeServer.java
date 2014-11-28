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
    Logger logger;

    public void start() {
        logger = container.logger();

        HttpServer server = vertx.createHttpServer();

        // Also serve the static resources. In real life this would probably be done by a  CDN
        server.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                if (req.path().equals("/")) req.response().sendFile("communication_test.html"); // Serve the index.html
                if (req.path().endsWith("vertxbus.js")) req.response().sendFile("vertxbus-2.1.js"); // Serve the js
                if (req.path().endsWith("style.css")) req.response().sendFile("static/css/styles.css"); // Serve the js
                if (req.path().endsWith("sock.js")) req.response().sendFile("static/js/sock.js"); // Serve the js
                if (req.path().endsWith("angular.js")) req.response().sendFile("static/js/angular.js"); // Serve the js
                if (req.path().endsWith("jquery.js")) req.response().sendFile("static/js/jquery-2.1.1.min.js"); // Serve the js
            }
        });

        JsonArray permitted = new JsonArray();
        permitted.add(new JsonObject()); // Let everything through

        ServerHook hook = new ServerHook(logger);

        SockJSServer sockJSServer = vertx.createSockJSServer(server);
        sockJSServer.setHook(hook);
        sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);

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