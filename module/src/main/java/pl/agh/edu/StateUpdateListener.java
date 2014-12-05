package pl.agh.edu;

import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import pl.agh.edu.model.ChangeRequest;
import pl.agh.edu.model.Game;

import com.google.gson.Gson;

public class StateUpdateListener extends Verticle{

	
	Logger logger;
	EventBus eb;
	ConcurrentMap<String, Game> map;
	private static final Gson GSON = new Gson();
	
	
    public void start() {
        logger = container.logger();
        eb = vertx.eventBus();        
        map =  vertx.sharedData().getMap("shared");
        
        
        //handler to receive updates from clients
        Handler<Message<JsonObject>> updateHandler = new Handler<Message<JsonObject>>() {
        	public void handle(Message<JsonObject> message) {
        		JsonObject json = message.body();
        		ChangeRequest change = GSON.fromJson(json.toString(), ChangeRequest.class);
        		Simulator.changeRequestQueue.add(change);
        		logger.info("Update from player: " + change.getPlayerId() + "appended.");
            }
        };
        eb.registerHandler("two.server", updateHandler);
        
        
        //periodic task to broadcast game state every interval
        long timerID = vertx.setPeriodic(1200, new Handler<Long>() {
            public void handle(Long timerID) {
        		Game game = map.get("game");
        		eb.publish("two.clients", game.toJson());
            }
        });
        logger.info("TimerID is: " + timerID);
        
    }
}
