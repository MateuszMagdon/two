package pl.agh.edu;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.google.gson.Gson;

import pl.agh.edu.model.ChangeRequest;
import pl.agh.edu.model.Game;

public class StateUpdateListener extends Verticle{

	
	Logger logger;
	EventBus eb;
	Game game;
	
	private synchronized void updateModel(ChangeRequest update){
		//do updates on game object
	}
	
	
	private synchronized void broadcastModel(JsonObject gameJSON){
		eb.publish("two.clients", gameJSON);
	}
	
    public void start() {
        logger = container.logger();
        eb = vertx.eventBus();        
        
        //handler to receive updates from clients
        Handler<Message<JsonObject>> updateHandler = new Handler<Message<JsonObject>>() {
        	public void handle(Message<JsonObject> message) {
        		JsonObject json = message.body();
        		//TO DO unpack change from json
        		ChangeRequest change = new ChangeRequest(1,1,true);
        		
        		logger.info("Received update from player: " + change.getPlayerId());
        		updateModel(change);
            }
        };
        eb.registerHandler("two.server", updateHandler);
        
        
        //periodic task to broadcast game state every interval
        long timerID = vertx.setPeriodic(1200, new Handler<Long>() {
            public void handle(Long timerID) {
            	broadcastModel(game.toJson());
            }
        });
        logger.info("TimerID is: " + timerID);
        
    }
}
