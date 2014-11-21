package pl.agh.edu;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import pl.agh.edu.model.Game;

public class StateUpdateListener extends Verticle{

	
	Logger logger;
	EventBus eb;
	Game game;
	
	private synchronized void updateModel(JsonObject update){
		//unpack update
		//do updates on game object
	}
	
	
	private synchronized void broadcastModel(){
		
		//JsonObject gameJSON = game.toJson()
		JsonObject gameJSON = new JsonObject();
		eb.publish("two.clients", gameJSON);
	}
	
	
	
    public void start() {
        logger = container.logger();
        eb = vertx.eventBus();        
        
        //handler to receive updates from clients
        Handler<Message<JsonObject>> updateHandler = new Handler<Message<JsonObject>>() {
        	public void handle(Message<JsonObject> message) {
        		logger.info("Received update from: ");
        		updateModel(message.body());
        		logger.info("Updated model from client: ");
            }
        };
        eb.registerHandler("two.server", updateHandler);
        
        
        //periodic task to broadcast game state every interval
        long timerID = vertx.setPeriodic(1200, new Handler<Long>() {
            public void handle(Long timerID) {
            	broadcastModel();
            }
        });
        logger.info("TimerID is: " + timerID);
        
    }
}
