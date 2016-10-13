package no.agens.depth.lib.headers;

public class Particle extends Renderable {

  float randomSpeedX;
  float randomSpeedY;
  long lastRandomizeChange;

  public Particle(float x, float y, float randomSpeedX, float randomSpeedY) {
    super(null, x, y);
    this.randomSpeedX = randomSpeedX;
    this.randomSpeedY = randomSpeedY;
    lastRandomizeChange = System.currentTimeMillis();
  }

  public void setRandomSpeed(float randomSpeedX, float randomSpeedY) {
    this.randomSpeedX = randomSpeedX;
    this.randomSpeedY = randomSpeedY;
  }
}
