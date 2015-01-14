package pl.agh.edu;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;
import pl.agh.edu.model.*;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.find;
import static pl.agh.edu.ImmutableHelper.addToImmutableList;
import static pl.agh.edu.ImmutableHelper.removeFromImmutableList;

/**
 * Created by Michal
 * 2014-12-05.
 */
public class Simulator extends Verticle {
    private static final Gson GSON = new Gson();
    private final Random random = new Random();
    private final Map map = Map.getMap();

    Logger logger;

    ConcurrentMap<String, Game> shared;
    private Game game;
    private EventBus eb;
    long time = System.currentTimeMillis();
    float delta = 0;
    private Long simulationTimer;

    public void start() {
        logger = container.logger();

        shared = vertx.sharedData().getMap("shared");
        game = createNewGame();
        shared.put("game", game);

        eb = getVertx().eventBus();
        logger.info("Starting simulator");
        eb.publish("game.start", true);
        restartGame();
        createLoginLogoutHandlers();

        //handler to receive updates from clients
        eb.registerHandler("two.server", (Message<JsonObject> message) -> {
            ChangeRequest change = GSON.fromJson(message.body().toString(), ChangeRequest.class);
            game = new Game(game.getPlayers(), applyChangeOnPlane(change, game.getPlanes()), game.getBullets(), game.getGameState());
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

    private void restartGame() {
        simulationTimer = createSimulationPeriodic();

        logger.info("Setting game over timer");
        getVertx().setTimer(Game.GAME_TIME, o -> {
            getVertx().cancelTimer(simulationTimer);
            eb.publish("game.over", true);
            logger.info("Game Over");

            game = new Game(game.getPlayers(), game.getPlanes(), game.getBullets(), GameState.END_GAME);

            logger.info("Setting restart timer");
            getVertx().setTimer(Game.BREAK_TIME, input -> {
                ImmutableList.Builder<Plane> planes = new ImmutableList.Builder<>();
                ImmutableList.Builder<Player> players = new ImmutableList.Builder<>();

                game.getPlayers().stream().forEach(player -> {
                    Plane oldPlane = getPlaneForPlayer(player.getNickName());

                    planes.add(new Plane(oldPlane.getPlaneType(),
                            random.nextInt(map.getWidth()),
                            random.nextInt(map.getHeight()),
                            random.nextInt(360),
                            PlaneTypes.STANDARD.getPlaneType().getSpeed(),
                            player,
                            PlaneTypes.STANDARD.getPlaneType().getHealth(),
                            false,
                            System.currentTimeMillis(),
                            ChangeRequest.Turn.NONE));

                    players.add(player.resetPoints());

                });

                game = new Game(players.build(),
                        planes.build(),
                        new ImmutableList.Builder<Bullet>().build(),
                        GameState.RUNNING);

                eb.publish("game.start", true);
                restartGame();

                logger.info("Restarting game");
            });
        });
    }

    private long createSimulationPeriodic() {
        return getVertx().setPeriodic(5, p -> {
            upadteDelta();

            ImmutableList<Plane> planes = updatePlanePositions(game.getPlanes());
            CollisionDetector collisionDetector = new CollisionDetector();
            planes = collisionDetector.collidePlanes(planes, planes);
            planes = collisionDetector.collidePlanes(planes, game.getBullets());

            game = new Game(game.getPlayers(), planes, game.getBullets(), game.getGameState());
            game = handleBulletBehaviour(game);

            shared.put("game", game);
            eb.publish("game.updated", true);
        });
    }

    public Game createNewGame() {
        return new Game(ImmutableList.<Player>of(), ImmutableList.<Plane>of(), ImmutableList.<Bullet>of(), GameState.RUNNING);
    }

    private ImmutableList<Plane> applyChangeOnPlane(ChangeRequest change, ImmutableList<Plane> planes) {
        ImmutableList.Builder<Plane> planeBuilder = new ImmutableList.Builder<>();
        for (Plane p : planes) {
            if (p.getPlayer().getNickName().equals(change.getPlayer())) {
                Plane plane = p.handleChangeRequest(change);
                planeBuilder.add(plane);
            } else {
                planeBuilder.add(p);
            }
        }
        return planeBuilder.build();
    }

    private void removePlayer(String login) {
        game = new Game(removePlayerFromListByLogin(login), removePlaneFromListByPlayerLogin(login), game.getBullets(), game.getGameState());
    }

    private ImmutableList<Plane> removePlaneFromListByPlayerLogin(String login) {
        return removeFromImmutableList(game.getPlanes(), (Predicate<Plane>) plane -> plane.getPlayer().getNickName().equals(login));
    }

    private Plane getPlaneForPlayer(String login) {
        return find(game.getPlanes(), plane -> plane.getPlayer().getNickName().equals(login));
    }

    private ImmutableList<Player> removePlayerFromListByLogin(String login) {
        return removeFromImmutableList(game.getPlayers(), (Predicate<Player>) player -> player.getNickName().equals(login));
    }

    private void addPlayer(String login, String group) {
        Player player = new Player(login, 0, Team.valueOf(group.toUpperCase()));
        Plane plane = new Plane(PlaneTypes.STANDARD.getPlaneType(), random.nextInt(map.getWidth()), random.nextInt(map.getHeight()), random.nextInt(360), PlaneTypes.STANDARD.getPlaneType().getSpeed(), player, PlaneTypes.STANDARD.getPlaneType().getHealth(), false, System.currentTimeMillis(), ChangeRequest.Turn.NONE);

        game = new Game(addToImmutableList(game.getPlayers(), player), addToImmutableList(game.getPlanes(), plane), game.getBullets(), game.getGameState());
    }

    private void upadteDelta() {
        delta = ((float) (System.currentTimeMillis() - time)) / 500;
        time = System.currentTimeMillis();
    }

    public ImmutableList<Plane> updatePlanePositions(ImmutableList<Plane> planes) {
        ImmutableList.Builder<Plane> planeBuilder = new ImmutableList.Builder<>();
        for (Plane plane : planes) {
            Plane element = getNewPosition(plane);

            if (element.getTurn() != ChangeRequest.Turn.NONE) {
                float howMuch = element.getPlaneType().getTurnDigreesPerInterval() * delta;
                if (element.getTurn() == ChangeRequest.Turn.LEFT) {
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
        for (Bullet bullet : game.getBullets()) {
            Bullet element = getNewPosition(bullet);

            if (Math.sqrt(Math.pow(element.getX() - element.getStartPositionX(), 2) + Math.pow(element.getY() - element.getStartPositionY(), 2)) < bullet.getWeapon().getRange()) {
                bulletsBuilder.add(element);
                // send message kaboom or add kaboom to game object?!
                // "Yes Rico, kaboom!"
            }
        }
        return new Game(game.getPlayers(), planes, bulletsBuilder.build(), game.getGameState());
    }

    private ImmutableList.Builder<Plane> handleShootingAndUpdateLastShotTime(Game game, ImmutableList.Builder<Bullet> bulletsBuilder) {
        ImmutableList.Builder<Plane> planesBuilder = ImmutableList.builder();

        // Check who is shooting
        for (Plane plane : game.getPlanes()) {
            if (plane.getFiringEnabled() && plane.getLastFiredAt() < System.currentTimeMillis() - plane.getPlaneType().getWeapon().getMinTimeBetweenShots()) {
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
        float newX = (float) (element.getX() + (element.getSpeed() * Math.cos(Math.toRadians(element.getDirection() + correction))) * delta);
        float newY = (float) (element.getY() + (element.getSpeed() * Math.sin(Math.toRadians(element.getDirection() + correction))) * delta);

        if (newX > map.getWidth()) newX = 0;
        if (newX < 0) newX = map.getWidth();

        if (newY > map.getHeight()) newY = 0;
        if (newY < 0) newY = map.getHeight();

        logger.trace("Moving bullet from " + element.getX() + "," + element.getY() + " to " + newX + ", " + newY);
        return element.moveTo(newX, newY);
    }
}
