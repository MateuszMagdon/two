package pl.agh.edu.model;

/**
 * Anything that can be placed on the map.
 */
public class GameObject {
	/**
	 * X position in range of (0, Map.X_MAX).
	 */
	private final float x;
	
	/**
	 * X position in range of (0, Map.Y_MAX).
	 */
	private final float y;

	/**
	 * Direction in degrees from north. In right hand side direction.
	 */
	private final int direction;

	/**
	 * The number of distance units traveled per server time interval.
	 */
	private final float speed;

	public GameObject(float x, float y, int direction, float speed) {
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.speed = speed;
	}
	
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getDirection() {
		return direction;
	}

	public float getSpeed() {
		return speed;
	}
}
