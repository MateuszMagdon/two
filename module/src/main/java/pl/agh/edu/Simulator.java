package pl.agh.edu;

import static com.google.common.collect.Iterables.filter;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import pl.agh.edu.model.Bullet;
import pl.agh.edu.model.ChangeRequest;
import pl.agh.edu.model.Game;
import pl.agh.edu.model.Map;
import pl.agh.edu.model.Plane;
import pl.agh.edu.model.PlaneTypes;
import pl.agh.edu.model.Player;
import pl.agh.edu.model.Team;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

/**
 * Created by Michal
 * 2014-12-05.
 */
public class Simulator extends Verticle {
    Logger logger;

    ConcurrentMap<String, Game> shared;
    private Game game;
    private EventBus eb;
    private Boolean continueGame = true;
    long time = System.currentTimeMillis();
    float delta = 0;
    private static final Gson GSON = new Gson();
    private final Random random = new Random();
    private final Map map = Map.getMap();

    public Plane createSimplePlane(int dir) {
        return new Plane(PlaneTypes.STANDARD.getPlaneType(), 600, 300, dir, 5.f, null, 10, false, System.currentTimeMillis(), null);
    }

    public Game createNewGame() {
        return new Game(ImmutableList.<Player>of(), ImmutableList.<Plane>of(), ImmutableList.<Bullet>of());
    }

    public void start() {
        logger = container.logger();

        shared = vertx.sharedData().getMap("shared");
        game = createNewGame();
        shared.put("game", game);

        eb = getVertx().eventBus();
        logger.info("Starting simulator");

        eb.publish("game.start", true);
        getVertx().setPeriodic(5, p -> {
            upadteDelta();

            // update positions
            ImmutableList<Plane> planes = updatePlanePositions(game.getPlanes());

            // detect collisions

            // make changes according to keys
            game = new Game(game.getPlayers(), planes, game.getBullets());
            shared.put("game", game);
            eb.publish("game.updated", true);
            // eb.publish("game.over", true);
            // eb.publish("game.information", "{winner: Player, gameTime: 10000, someting else?}");
        });
        eb.publish("game.start", false);

        eb.registerHandler("client.connected", (Message<JsonObject> message) -> {
            String login = message.body().getString("login");
            String group = message.body().getString("group");

            addPlayer(login, group);
        });

        eb.registerHandler("client.disconnected", (Message<JsonObject> message) -> removePlayer(message.body().getString("login")));

        //handler to receive updates from clients
        eb.registerHandler("two.server", (Message<JsonObject> message) -> {
        		ChangeRequest change = GSON.fromJson(message.body().toString(), ChangeRequest.class);
        		//logger.debug("Received update from player: " + change.getPlayer());   		
        		game = new Game(game.getPlayers(), applyChangeOnPlane(change,game.getPlanes()), game.getBullets());
        });

        //periodic task to broadcast game state every interval
        vertx.setPeriodic(50, timerID -> eb.publish("two.clients", game.toJson()));

        eb.registerHandler("game.players", (Message<String> message) -> {
            message.reply(GSON.toJson(game.getPlayers()));
        });
    }

    
    private ImmutableList<Plane> applyChangeOnPlane(ChangeRequest change,ImmutableList<Plane> planes){
    	ImmutableList.Builder<Plane> planeBuilder = new ImmutableList.Builder<>();
    	for (Plane p : planes){
    		if (p.getPlayer().getNickName().equals(change.getPlayer())){
    			Plane plane = p.handleChangeRequest(change);
    			planeBuilder.add(plane);
    		}
    		else{
    			planeBuilder.add(p);
    		}
    	}
    	return planeBuilder.build();
    }
    
    private void removePlayer(String login) {
        game = new Game(removePlayerFromListByLogin(login), removePlaneFromListByPlayerLogin(login), game.getBullets());
    }

    private ImmutableList<Plane> removePlaneFromListByPlayerLogin(String login) {
        return removeFromImmutableList(game.getPlanes(), (Predicate<Plane>) plane -> plane.getPlayer().getNickName().equals(login));
    }

    private ImmutableList<Player> removePlayerFromListByLogin(String login) {
        return removeFromImmutableList(game.getPlayers(), (Predicate<Player>) player -> player.getNickName().equals(login));
    }

    private void addPlayer(String login, String group) {
        Player player = new Player(login, 0, Team.valueOf(group.toUpperCase()));
        Plane plane = new Plane(PlaneTypes.STANDARD.getPlaneType(), random.nextInt(map.getWidth()), random.nextInt(map.getHeight()), random.nextInt(360), 40.f, player, 100, false, System.currentTimeMillis(), ChangeRequest.Turn.NONE);

        game = new Game(addToImmutableList(game.getPlayers(), player), addToImmutableList(game.getPlanes(), plane), game.getBullets());
    }

    private<E> ImmutableList<E> addToImmutableList(ImmutableList<E> list, E newElement) {
        return ImmutableList.<E>builder().addAll(list).add(newElement).build();
    }

    private<E> ImmutableList<E> removeFromImmutableList(ImmutableList<E> list, E elementToRemove) {
        return ImmutableList.copyOf(filter(list, Predicates.not(Predicates.equalTo(elementToRemove))));
    }

    private<E> ImmutableList<E> removeFromImmutableList(ImmutableList<E> list, Predicate<E> elementToRemove) {
        return ImmutableList.copyOf(filter(list, Predicates.not(elementToRemove)));
    }

    private void upadteDelta() {
        delta = ((float)(System.currentTimeMillis() - time)) / 1000;
        time = System.currentTimeMillis();
    }

    public ImmutableList<Plane> updatePlanePositions(ImmutableList<Plane> planes) {
        ImmutableList.Builder<Plane> planeBuilder = new ImmutableList.Builder<>();
        int correction = -90;
        for (Plane plane : planes) {
            float newX = (float) (plane.getX() + (plane.getSpeed() * Math.cos(Math.toRadians(plane.getDirection()+correction))) * delta);
            float newY = (float) (plane.getY() + (plane.getSpeed() * Math.sin(Math.toRadians(plane.getDirection()+correction))) * delta);

            if(newX > map.getWidth()) newX = 0;
            if(newX < 0) newX = map.getWidth();

            if(newY > map.getHeight()) newY = 0;
            if(newY < 0) newY = map.getHeight();

            logger.trace("Moving plane from " + plane.getX() + "," + plane.getY() + " to " + newX + ", " + newY);
            Plane element = plane.moveTo(newX, newY);

            if(element.getTurn() != ChangeRequest.Turn.NONE) {
                float howMuch = element.getPlaneType().getTurnDigreesPerInterval()*delta;
                if(element.getTurn() == ChangeRequest.Turn.LEFT) {
                    element = element.changeDirection(-howMuch);
                } else {
                    element = element.changeDirection(howMuch);
                }
            }

            planeBuilder.add(element);
        }
        return planeBuilder.build();
    }
}
