package pl.agh.edu;

import com.google.common.collect.ImmutableList;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;
import pl.agh.edu.model.*;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

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

    public Plane createSimplePlane() {
        return new Plane(PlaneTypes.STANDARD.getPlaneType(), 0, 0, 0, 10.f, null, 10, false, System.currentTimeMillis(), null);
    }

    public Game createNewGame(){
        return new Game(ImmutableList.<Player>of(), ImmutableList.<Plane>of(createSimplePlane()), ImmutableList.<Bullet>of());
    }

    public void start() {
        logger = container.logger();

        map =  vertx.sharedData().getMap("shared");
        game = createNewGame();
        map.put("game", game);

        eb = getVertx().eventBus();
        logger.info("Starting simulator");

        eb.publish("game.start", true);
        Long time = System.currentTimeMillis();
        while(continueGame) {
            float delta = (System.currentTimeMillis() - time)/1000;
            time = System.currentTimeMillis();

            ImmutableList.Builder<Plane> planeBuilder = new ImmutableList.Builder<>();
            // update positions
            for(Plane plane: game.getPlanes()) {
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
        }
        eb.publish("game.start", false);
    }
}
