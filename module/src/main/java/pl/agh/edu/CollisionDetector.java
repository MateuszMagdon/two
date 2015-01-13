package pl.agh.edu;

import pl.agh.edu.model.Bullet;
import pl.agh.edu.model.GameObject;
import pl.agh.edu.model.Plane;
import pl.agh.edu.model.Team;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Created by lpalonek on 10/12/14.
 */
public class CollisionDetector {

    private static final float PLANE_WIDTH = 53.25f;
    private static final float PLANE_HEIGHT = 74f;

    private static final float BULLET_WIDTH = 30f;
    private static final float BULLET_HEIGHT = 1f;

    public <T extends GameObject,E extends GameObject> boolean collision(T first, E second, boolean same){
        float firstX = first.getX();
        float firstY = first.getY();

        float secondX = second.getX();
        float secondY = second.getY();

        Middle firstMiddle = calculateMiddle(firstX, firstY, PLANE_WIDTH, PLANE_HEIGHT);
        Middle secondMiddle = calculateMiddle(secondX, secondY, same
                ? PLANE_WIDTH : BULLET_WIDTH,
                same ? PLANE_HEIGHT : BULLET_HEIGHT);

        final float radiusSum = (PLANE_WIDTH + (same ? PLANE_WIDTH : BULLET_WIDTH))/2;
        final float dx = firstMiddle.x - secondMiddle.x;
        final float dy = firstMiddle.y - secondMiddle.y;
        return (radiusSum * radiusSum) > (dx * dx + dy * dy);
    }

    public Middle calculateMiddle(float x, float y, float width, float height){
        return new Middle(x + width / 2, y + height / 2);
    }
    
    public <T extends GameObject,E extends GameObject> ImmutableList<T> collidePlanes(ImmutableList<T> planes, ImmutableList<E> secondList){
        ImmutableSet.Builder<T> deadPlanes = new ImmutableSet.Builder<>();
        for(T plane: planes){
            for(E opponent: secondList){
                if(opponent instanceof Plane) {
                    if (planeCollisionConditions((Plane)plane, (Plane)opponent)) {
                        deadPlanes.add((T)opponent);
                        deadPlanes.add(plane);
                    }
                }else{
                    if(bulletCollisionConditions((Plane)plane, (Bullet)opponent)){
                        deadPlanes.add(plane);
                    }
                }
            }
        }
        return mergeLists(planes, deadPlanes.build());
    }
    
    
    public ImmutableList<Plane> getDeadPlanes(ImmutableList<Plane> planes, ImmutableList<Bullet> bullets){
        ImmutableSet.Builder<Plane> deadPlanes = new ImmutableSet.Builder<>();
        
        for (Plane plane: planes){
        	for (Plane opponent : planes){
        		if (planeCollisionConditions(plane, opponent)) {
                    deadPlanes.add(opponent);
                    deadPlanes.add(plane);
                }
        	}
        }
        
        for (Plane plane: planes){
        	for (Bullet b : bullets){
	        	if(bulletCollisionConditions(plane, b)){
	                deadPlanes.add(plane);
	            }
        	}
        }
        ImmutableList.Builder<Plane> planeList = new ImmutableList.Builder<>();
        planeList.addAll(deadPlanes.build());
        return planeList.build();
    }
    
    public ImmutableList<Bullet> getSuccessBullets(ImmutableList<Plane> planes, ImmutableList<Bullet> bullets){
        ImmutableList.Builder<Bullet> newbullets = new ImmutableList.Builder<>();
        
        for (Plane plane: planes){
        	for (Bullet b : bullets){
	        	if(bulletCollisionConditions(plane, b)){
	        		newbullets.add(b);
	            }
        	}
        }
        return newbullets.build();
    }
    

    public boolean planeCollisionConditions(Plane plane, Plane opponent){

        Team planeTeam = plane.getPlayer().getTeam();
        Team oponentTeam = opponent.getPlayer().getTeam();
        if(plane.equals(opponent) || planeTeam.equals(oponentTeam)
                || plane.getHealth() == 0 || opponent.getHealth() == 0){
            return false;
        }
        if(collision(plane,opponent, true)) {
            return true;
        }
        return false;
    }

    public boolean bulletCollisionConditions(Plane plane, Bullet bullet){
        Team planeTeam = plane.getPlayer().getTeam();
        Team bulletTeam = bullet.getPlayer().getTeam();
        if( planeTeam.equals(bulletTeam)){
            return false;
        }
        if(collision(plane, bullet, false)){
            return true;
        }
        return false;
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
