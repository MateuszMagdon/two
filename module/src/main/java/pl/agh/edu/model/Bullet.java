package pl.agh.edu.model;

/**
 * GameObject that represents a bullet shot from a weapon.
 */
public final class Bullet extends GameObject<Bullet> {
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

	public Bullet(float x, float y, float direction, float startPositionX,
			float startPositionY, Weapon weapon) {
		super(x, y, direction, weapon.getBulletSpeed());
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

	@Override
	public Bullet moveTo(float x, float y) {
		return new Bullet(x, y, getDirection(), getStartPositionX(),
				getStartPositionY(), getWeapon());
	}

	@Override
	public Bullet changeDirection(float degreesToAdd) {
		return new Bullet(getX(), getY(), getDirection() + degreesToAdd,
				getStartPositionX(), getStartPositionY(), getWeapon());
	}
}
