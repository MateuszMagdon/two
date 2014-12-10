package pl.agh.edu;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import pl.agh.edu.model.Plane;
import pl.agh.edu.model.Player;
import pl.agh.edu.model.Team;

import static org.junit.Assert.*;

public class CollisionDetectorTest {

    private CollisionDetector collisionDetector;

    private Plane first;

    private Plane second;

    private Plane third;

    private Plane fourth;

    private ImmutableList.Builder<Plane> planes;

    @Before
    public void setUp(){
        collisionDetector = new CollisionDetector();
        first = new Plane(null, 100, 100,0,0,new Player("first", 0, Team.RED),100, true, 0, null);
        second = new Plane(null, 120, 100,0,0,new Player("second", 0, Team.BLUE),100, true, 0, null);
        third = new Plane(null, 220, 100,0,0,new Player("third", 0, Team.RED),100, true, 0, null);
        fourth = new Plane(null, 220, 100,0,0,new Player("fourth", 0, Team.RED),100, true, 0, null);


        planes = new ImmutableList.Builder<>();
        planes.add(first);
        planes.add(second);
        planes.add(third);
        planes.add(fourth);
    }

    @Test
    public void testCollision() throws Exception {
        assertTrue(collisionDetector.collision(first, second));
        assertFalse(collisionDetector.collision(third,second));
    }

    @Test
    public void testCalculateMiddle(){
        CollisionDetector.Middle actualResult = collisionDetector.new Middle(20, 20);
        CollisionDetector.Middle expectedResult = collisionDetector.calculateMiddle(0, 0, 40, 40);
        assertTrue(expectedResult.getX() == actualResult.getX());
        assertTrue(expectedResult.getY() == actualResult.getY());
    }

    @Test
    public void testCollidePlanes(){
        ImmutableList.Builder<Plane> expectedResult = new ImmutableList.Builder<>();
        expectedResult.add(third);
        expectedResult.add(fourth);
        assertEquals(expectedResult.build(), collisionDetector.collidePlanes(planes.build()));
    }

    @Test
    public void testMergePlaneLists(){
        ImmutableList.Builder<Plane> expectedResult = new ImmutableList.Builder<>();
        expectedResult.add(third);
        expectedResult.add(fourth);

        ImmutableSet.Builder<Plane> test = new ImmutableSet.Builder<>();
        test.add(first);
        test.add(second);

        assertEquals(expectedResult.build(), collisionDetector.mergeLists(planes.build(), test.build()));
    }
}