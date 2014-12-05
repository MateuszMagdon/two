package pl.agh.edu;

import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import pl.agh.edu.model.Bullet;
import pl.agh.edu.model.ChangeRequest;
import pl.agh.edu.model.Game;
import pl.agh.edu.model.Plane;
import pl.agh.edu.model.PlaneTypes;
import pl.agh.edu.model.Player;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

/**
 * Created by Michal
 * 2014-12-05.
 */
public class Simulator extends Verticle {
    Logger logger;

    ConcurrentMap<String, Game> map;
    private Game game;
    private EventBus eb;
    private Boolean continueGame = true;
    Long time = System.currentTimeMillis();
    private static final Gson GSON = new Gson();

    public Plane createSimplePlane() {
        return new Plane(PlaneTypes.STANDARD.getPlaneType(), 0, 0, 45, 10.f, null, 10, false, System.currentTimeMillis(), null);
    }

    public Game createNewGame() {
        return new Game(ImmutableList.<Player>of(), ImmutableList.<Plane>of(createSimplePlane()), ImmutableList.<Bullet>of());
    }

    public void start() {
        logger = container.logger();

        map = vertx.sharedData().getMap("shared");
        game = createNewGame();
        map.put("game", game);

        eb = getVertx().eventBus();
        logger.info("Starting simulator");

        eb.publish("game.start", true);
        getVertx().setTimer(5, p -> {
            float delta = (System.currentTimeMillis() - time) / 1000;
            time = System.currentTimeMillis();

            ImmutableList.Builder<Plane> planeBuilder = new ImmutableList.Builder<>();
            // update positions
            for (Plane plane : game.getPlanes()) {
                Plane element = plane.moveTo((float) (plane.getX() + (plane.getSpeed() * Math.cos(plane.getDirection())) * delta), (float) (plane.getY() + (plane.getSpeed() * Math.sin(plane.getDirection())) * delta));
                planeBuilder.add(element);
                logger.info("Moving plane to " + element.getX() + " " + element.getY());
            }

            // detect collisions

            // make changes according to keys
            game = new Game(game.getPlayers(), planeBuilder.build(), game.getBullets());
            map.put("game", game);
            eb.publish("game.updated", true);
            // eb.publish("game.over", true);
            // eb.publish("game.information", "{winner: Player, gameTime: 10000, someting else?}");
        });
        eb.publish("game.start", false);

        eb.registerHandler("user.connect", message -> {

        });

        eb.registerHandler("user.disconnect", message -> {

        });
        
        
        
        //handler to receive updates from clients
        eb.registerHandler("two.server", new Handler<Message<JsonObject>>() {
        	public void handle(Message<JsonObject> message) {
        		JsonObject json = message.body();
        		ChangeRequest change = GSON.fromJson(json.toString(), ChangeRequest.class);
        		logger.debug("Update from player: " + change.getPlayerId() + "appended.");
        		
        		//TO DO aply changeRequest to createNewGame    		
            }
        });
        
        
        //periodic task to broadcast game state every interval
        vertx.setPeriodic(1200, new Handler<Long>() {
            public void handle(Long timerID) {
        		eb.publish("two.clients", game.toJson());
            }
        });
        
        
    }
}
