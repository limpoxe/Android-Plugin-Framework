package no.agens.depth.lib.headers;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;
import no.agens.depth.lib.MathHelper;

public class ParticleSystem extends Renderable {
  public static final int DURATION = 10000;
  Paint particlePaint = new Paint();
  long lastEmit;
  int startColor;
  int endColor;
  int particleSize = 20;
  float randomMovementX = 10;
  List<Particle> paricles = new ArrayList<>();
  ValueAnimator color;
  private int emitInterWall;
  private float gravityY;
  private float randomXPlacement;
  private float minYCoord;
  private float randomMovementY;
  private int randomMovementChangeInterval = 2000;

  public ParticleSystem(float x, float y, int emitInterWall, float gravityY,
      float randomXPlacement) {
    super(null, x, y);
    lastEmit = System.currentTimeMillis();
    particlePaint.setColor(Color.RED);
    this.emitInterWall = emitInterWall;
    this.gravityY = gravityY;
    this.randomXPlacement = randomXPlacement;
  }

  public void setParticleSize(int particleSize) {
    this.particleSize = particleSize;
  }

  public void setRandomMovementX(float randomMovement) {
    this.randomMovementX = randomMovement;
  }

  public void setColors(int startColor, int endColor) {
    this.startColor = startColor;
    this.endColor = endColor;
    color = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor, Color.TRANSPARENT)
        .setDuration(DURATION);
  }

  @Override public void draw(Canvas canvas) {

    for (int i = 0; i < paricles.size(); i++) {
      Particle p = paricles.get(i);
      setParticlePaintColor(p);
      canvas.drawRect(p.x, p.y, p.x + particleSize, p.y + particleSize, particlePaint);
    }
  }

  @Override public void update(float deltaTime, float wind) {
    long currentTimeMillis = System.currentTimeMillis();
    if (lastEmit + emitInterWall < currentTimeMillis) {
      addParticle();
    }
    for (int i = 0; i < paricles.size(); i++) {
      Particle particle = paricles.get(i);
      particle.y += gravityY * deltaTime;
      particle.y += particle.randomSpeedY * deltaTime;
      particle.x += particle.randomSpeedX * deltaTime;
      particle.x += wind * deltaTime;

      if (particle.lastRandomizeChange + randomMovementChangeInterval < currentTimeMillis) {
        particle.lastRandomizeChange = System.currentTimeMillis();
        particle.setRandomSpeed(MathHelper.randomRange(-randomMovementX, randomMovementX),
            MathHelper.randomRange(-randomMovementY, randomMovementY));
      }
      if (particle.y < minYCoord) {
        paricles.remove(i);
        i--;
      }
    }
  }

  private void addParticle() {
    paricles.add(new Particle(x + MathHelper.randomRange(-randomXPlacement, randomXPlacement), y,
        MathHelper.randomRange(-randomMovementX, randomMovementX),
        MathHelper.randomRange(-randomMovementY, randomMovementY)));
    lastEmit = System.currentTimeMillis();
  }

  public void setMinYCoord(float minYCoord) {
    this.minYCoord = minYCoord;
  }

  public void setRandomMovementY(float randomMovementY) {
    this.randomMovementY = randomMovementY;
  }

  public void setParticlePaintColor(Particle particle) {
    float currentY = y - particle.y;
    float maxMoveDistance = y - minYCoord;
    float travelDistanceInPercentOfMax = currentY / maxMoveDistance;
    color.setCurrentPlayTime((long) (DURATION * travelDistanceInPercentOfMax));
    particlePaint.setColor((Integer) color.getAnimatedValue());
  }

  public void setRandomMovementChangeInterval(int randomMovementChangeInterval) {
    this.randomMovementChangeInterval = randomMovementChangeInterval;
  }
}
