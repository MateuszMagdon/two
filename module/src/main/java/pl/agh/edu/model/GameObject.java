package pl.agh.edu.model;

/**
 * Anything that can be placed on the map.
 */
public abstract class GameObject<T> {
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
	private final float direction;

	/**
	 * The number of distance units traveled per server time interval.
	 */
	private final float speed;

	public GameObject(float x, float y, float direction, float speed) {
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

	public float getDirection() {
		return direction;
	}

	public float getSpeed() {
		return speed;
	}

	/**
	 * Returns new object instance moved to the given position.
	 */
	public abstract T moveTo(float x, float y);

	/**
	 * Returns new plane instance with direction degrees added.
	 */
	public abstract T changeDirection(float degreesToAdd);
}
