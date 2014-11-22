package pl.agh.edu.model;

/**
 * GameObject that represents a bullet shot from a weapon.
 */
public class Bullet extends GameObject {
	/**
	 * The initial X position.
	 */
	private final float startPositionX;

	/**
	 * The initial Y position.
	 */
	private final float startPositionY;

	/**
	 * The waepon that shot this bullet.
	 */
	private final Weapon weapon;

	public Bullet(float x, float y, int direction, float speed,
			float startPositionX, float startPositionY, Weapon weapon) {
		super(x, y, direction, speed);
		this.startPositionX = startPositionX;
		this.startPositionY = startPositionY;
		this.weapon = weapon;
	}

	public float getStartPositionX() {
		return startPositionX;
	}

	public float getStartPositionY() {
		return startPositionY;
	}

	public Weapon getWeapon() {
		return weapon;
	}
}
