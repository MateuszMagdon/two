package pl.agh.edu;

import static com.google.common.collect.Iterables.filter;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import pl.agh.edu.model.*;

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

    public void start() {
        logger = container.logger();

        shared = vertx.sharedData().getMap("shared");
        game = createNewGame();
        shared.put("game", game);

        eb = getVertx().eventBus();
        logger.info("Starting simulator");
        eb.publish("game.start", true);
        createSimulationTimer();
        createLoginLogoutHandlers();

        //handler to receive updates from clients
        eb.registerHandler("two.server", (Message<JsonObject> message) -> {
        		ChangeRequest change = GSON.fromJson(message.body().toString(), ChangeRequest.class);
        		//logger.debug("Received update from player: " + change.getPlayer());   		
        		game = new Game(game.getPlayers(), applyChangeOnPlane(change,game.getPlanes()), game.getBullets());
        });

        //periodic task to broadcast game state every interval
        vertx.setPeriodic(50, timerID -> eb.publish("two.clients", game.toJson()));

        eb.registerHandler("game.players", (Message<String> message) -> message.reply(GSON.toJson(game.getPlayers())));
    }

    private void createLoginLogoutHandlers() {
        eb.registerHandler("client.connected", (Message<JsonObject> message) -> {
            String login = message.body().getString("login");
            String group = message.body().getString("group");

            addPlayer(login, group);
        });

        eb.registerHandler("client.disconnected", (Message<JsonObject> message) -> removePlayer(message.body().getString("login")));
    }

    private void createSimulationTimer() {
        getVertx().setPeriodic(5, p -> {
            upadteDelta();

            ImmutableList<Plane> planes = updatePlanePositions(game.getPlanes());
            CollisionDetector collisionDetector = new CollisionDetector();
            planes = collisionDetector.collidePlanes(planes, planes);
            planes = collisionDetector.collidePlanes(planes, game.getBullets());
            
            game = new Game(game.getPlayers(), planes, game.getBullets());
            game = handleBulletBehaviour(game);

            // detect collisions
            shared.put("game", game);
            eb.publish("game.updated", true);
            // eb.publish("game.over", true);
            // eb.publish("game.information", "{winner: Player, gameTime: 10000, someting else?}");
        });
    }

    public Game createNewGame() {
        return new Game(ImmutableList.<Player>of(), ImmutableList.<Plane>of(), ImmutableList.<Bullet>of());
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
        Plane plane = new Plane(PlaneTypes.STANDARD.getPlaneType(), random.nextInt(map.getWidth()), random.nextInt(map.getHeight()), random.nextInt(360), PlaneTypes.STANDARD.getPlaneType().getSpeed(), player, PlaneTypes.STANDARD.getPlaneType().getHealth(), false, System.currentTimeMillis(), ChangeRequest.Turn.NONE);

        game = new Game(addToImmutableList(game.getPlayers(), player), addToImmutableList(game.getPlanes(), plane), game.getBullets());
    }

    private<E> ImmutableList<E> addToImmutableList(ImmutableList<E> list, E newElement) {
        return ImmutableList.<E>builder().addAll(list).add(newElement).build();
    }

    private <E> ImmutableList<E> removeFromImmutableList(ImmutableList<E> list, E elementToRemove) {
        return ImmutableList.copyOf(filter(list, Predicates.not(Predicates.equalTo(elementToRemove))));
    }

    private<E> ImmutableList<E> removeFromImmutableList(ImmutableList<E> list, Predicate<E> elementToRemove) {
        return ImmutableList.copyOf(filter(list, Predicates.not(elementToRemove)));
    }

    private void upadteDelta() {
        delta = ((float)(System.currentTimeMillis() - time)) / 500;
        time = System.currentTimeMillis();
    }

    public ImmutableList<Plane> updatePlanePositions(ImmutableList<Plane> planes) {
        ImmutableList.Builder<Plane> planeBuilder = new ImmutableList.Builder<>();
        for (Plane plane : planes) {
            Plane element = getNewPosition(plane);

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

    public Game handleBulletBehaviour(Game game) {
        ImmutableList.Builder<Bullet> bulletsBuilder = ImmutableList.builder();
        ImmutableList<Plane> planes = handleShootingAndUpdateLastShotTime(game, bulletsBuilder).build();

        // Move bullets and explode expired :)
        for (Bullet bullet: game.getBullets()) {
            Bullet element = getNewPosition(bullet);

            if(Math.sqrt(Math.pow(element.getX() - element.getStartPositionX(), 2) + Math.pow(element.getY() - element.getStartPositionY(), 2)) < bullet.getWeapon().getRange()) {
                bulletsBuilder.add(element);
                // send message kaboom or add kaboom to game object?!
                // "Yes Rico, kaboom!"
            }
        }
        return new Game(game.getPlayers(), planes, bulletsBuilder.build());
    }

    private ImmutableList.Builder<Plane> handleShootingAndUpdateLastShotTime(Game game, ImmutableList.Builder<Bullet> bulletsBuilder) {
        ImmutableList.Builder<Plane> planesBuilder = ImmutableList.builder();

        // Check who is shooting
        for(Plane plane: game.getPlanes()) {
            if(plane.getFiringEnabled() && plane.getLastFiredAt() < System.currentTimeMillis()-plane.getPlaneType().getWeapon().getMinTimeBetweenShots()) {
                bulletsBuilder.add(new Bullet(plane.getX(), plane.getY(), plane.getDirection(), plane.getX(), plane.getY(), plane.getPlaneType().getWeapon(), plane.getPlayer()));
                planesBuilder.add(plane.shotFired(System.currentTimeMillis()));
            } else {
                planesBuilder.add(plane);
            }
        }
        return planesBuilder;
    }

    private <T extends GameObject<T>> T getNewPosition(T element) {
        int correction = -90;
        float newX = (float) (element.getX() + (element.getSpeed() * Math.cos(Math.toRadians(element.getDirection()+correction))) * delta);
        float newY = (float) (element.getY() + (element.getSpeed() * Math.sin(Math.toRadians(element.getDirection()+correction))) * delta);
        if(Plane.class.isAssignableFrom(element.getClass())) {
            Plane plane = Plane.class.cast(element);

            if(newX > map.getWidth()) {
                newX = map.getWidth();
                plane = plane.changeDirection(-2 * plane.getDirection());
            }

            if(newX < 0) {
                newX = 0;
                plane = plane.changeDirection(2 * (180 - plane.getDirection()));
            }

            if(newY > map.getHeight()) {
                newY = map.getHeight();
                plane = plane.changeDirection(180 - 2 * plane.getDirection());

            }

            if(newY < 0) {
                newY = 0;
                plane = plane.changeDirection(180 - 2 * plane.getDirection());
            }
            
            return (T) GameObject.class.cast(plane.moveTo(newX, newY));
        } else {

            if(newX > map.getWidth()) {
                newX = 0;
            }

            if(newX < 0) {
                newX = map.getWidth();
            }

            if(newY > map.getHeight()) {
                newY = 0;
            }

            if(newY < 0) {
                newY = map.getHeight();
            }

            logger.trace("Moving bullet from " + element.getX() + "," + element.getY() + " to " + newX + ", " + newY);
            return element.moveTo(newX, newY);
        }

    }
}
