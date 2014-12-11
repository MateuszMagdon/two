package pl.agh.edu;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import pl.agh.edu.model.Plane;
import pl.agh.edu.model.Team;

/**
 * Created by lpalonek on 10/12/14.
 */
public class CollisionDetector {

    private static final float PLANE_WIDTH = 53.25f;
    private static final float PLANE_HEIGHT = 74f;

    public boolean collision(Plane first, Plane second){
        float firstX = first.getX();
        float firstY = first.getY();

        float secondX = second.getX();
        float secondY = second.getY();

        Middle firstMiddle = calculateMiddle(firstX, firstY, PLANE_WIDTH, PLANE_HEIGHT);
        Middle secondMiddle = calculateMiddle(secondX, secondY, PLANE_WIDTH, PLANE_HEIGHT);

        final float radius = PLANE_WIDTH/4;

        final float radiusSum = 2 * radius;
        final float dx = Math.abs(firstMiddle.x - secondMiddle.x);
        final float dy = Math.abs(secondMiddle.y - secondMiddle.y);

        return radiusSum * radiusSum > (dx * dx + dy * dy);
    }

    public Middle calculateMiddle(float x, float y, float width, float height){
        return new Middle(x + width / 2, y + height / 2);
    }

    public ImmutableList<Plane> collidePlanes(ImmutableList<Plane> planes){
        ImmutableSet.Builder<Plane> deadPlanes = new ImmutableSet.Builder<>();
        for(Plane plane: planes){
            Team planeTeam = plane.getPlayer().getTeam();
            for(Plane opponent: planes){
                Team oponentTeam = opponent.getPlayer().getTeam();
                if(plane.equals(opponent) || planeTeam.equals(oponentTeam)
                        || plane.getHealth() == 0 || opponent.getHealth() == 0){
                    continue;
                }
                if(collision(plane,opponent)){
                    deadPlanes.add(opponent);
                    deadPlanes.add(plane);
                }
            }
        }
        return mergeLists(planes, deadPlanes.build());
    }

    public <T> ImmutableList<T> mergeLists(ImmutableList<T> all, ImmutableSet<T> dead){
        ImmutableList.Builder<T> planeList = new ImmutableList.Builder<>();
        for(T t : all){
            if( !dead.contains(t)){
                planeList.add(t);
            }
        }
        return planeList.build();
    }

    public class Middle{
        private float x;
        private float y;

        public Middle(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
}
