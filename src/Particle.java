import java.awt.*;

public class Particle {
  public final double ELECTRIC_CONSTANT = 8.99 * Math.pow(10, 9);
  public final double MAGNETIC_CONSTANT = 4 * Math.PI * Math.pow(10, -7);
  protected double x;
  protected double y;
  protected double z;
  protected double xVelocity;
  protected double yVelocity;
  protected double zVelocity;
  protected String tag;
  protected Color base;
  protected double charge;
  private double baseRadius = 10;
  private double radius = 10;
  private double scale = 10;
  protected int roundsPerSecond;
  private double zLimit = 200;
  protected double mass = 10 * Math.pow(10, -14);
  private boolean fixed = false;
  private boolean active = true;

  public Particle(int perSec, int startX, int startY, int startZ, int startCharge, double initialVelocity, double initialTheta, double initalZVelocity, String name) {
    roundsPerSecond = perSec;
    x = startX;
    y = startY;
    z = startZ;
    charge = startCharge * Math.pow(10, -9);
    base = new Color(Color.HSBtoRGB(((float) ((int) (Math.random() * 10000)) / 10000), (float) 1.0, (float) 1.0));
    xVelocity = initialVelocity * Math.cos(initialTheta);
    yVelocity = initialVelocity * Math.sin(initialTheta);
    zVelocity = initalZVelocity;
    tag = name;
  }

  public Particle(int perSec, String name) {
    tag = name;
    roundsPerSecond = perSec;
    base = new Color(Color.HSBtoRGB(((float) ((int) (Math.random() * 1000)) / 1000), (float) 1.0, (float) 1.0));

  }

  public void drawParticle(Graphics g) {

    //System.out.println(z);
    g.setColor(calcColor());
    g.fillOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
    //drawVelocity(g);
    //System.out.println(velocity);

  }

  public Color calcColor() {
    float[] parts = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
    //System.out.println("tag: "+tag);
    //System.out.println("   "+z+ " "+ zLimit);
    double change = z / zLimit * 2;
    if (change > 0) {
      //System.out.println("      "+change+" laRGER ");
      radius = baseRadius * Math.min(1 + change, 3);
    } else {
      //System.out.println("      "+change+" smaller "+tag);
      radius = baseRadius / Math.min(1 + Math.abs(change), 3);
    }
    if (z < 0) { //into the page
      parts[1] += change;
      if (parts[1] < 0) {
        parts[1] = 0;
      }
    } else { // out of the page
      parts[2] -= change;
      if (parts[2] < 0) {
        parts[2] = 0;
      }
    }
    return new Color(Color.HSBtoRGB(parts[0], parts[1], parts[2]));
  }

  public boolean onScreen(int xBound, int yBound, int zBound) {
    if (!active) {
      return false;
    }
    if (x + radius < 0.0 || x - radius > (double) xBound) {
      active = false;
      return false;
    }
    if (y + radius < 0.0 || y - radius > (double) yBound) {
      active = false;
      return false;
    }
    if (z < -zBound * 2 || z > zBound * 2) {
      active = false;
      return false;
    }
    return true;
  }

  public void drawLast(Graphics g) {
    g.setColor(Color.black);
    g.fillOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
    g.setColor(calcColor());
    g.fillOval((int) (x - radius + 2), (int) (y - radius + 2), (int) (radius * 2 - 4), (int) (radius * 2 - 4));
  }

  public void drawVelocity(Graphics g) {
    g.drawLine((int) (x), (int) (y), (int) (x + xVelocity), (int) (y - yVelocity));
  }

  public void accelerate(double acceleration, double alpha, double beta) {
    try {
      double speedAdded = acceleration / roundsPerSecond;
      xVelocity = xVelocity + speedAdded * Math.cos(alpha);
      //System.out.println(velocity + " velocity "+ Math.cos(theta) + " cos theta " + speedAdded +" speed " + Math.cos(alpha)+" cos alpha");
      yVelocity = yVelocity + (speedAdded * Math.sin(alpha));
      zVelocity = zVelocity + (speedAdded * Math.cos(beta));
      //System.out.println(zVelocity+" z "+tag);
      //System.out.println(yVelocity +" y "+tag);
      //System.out.println(velocity + " velocity "+ Math.sin(theta) + " sin theta " + speedAdded +" speed " + Math.sin(alpha)+" sin alpha");
    } catch (Exception e) {
    }
  }

  public void accelerate(double[] acceleration) {
    double[] speedAdded = scaleArray(acceleration, 1 / roundsPerSecond);
    xVelocity = xVelocity + speedAdded[0];
    yVelocity = yVelocity + speedAdded[1];
    zVelocity = zVelocity + speedAdded[2];
  }

  //these methods scale the movement by a flat factor so more of it fits on the screen
  public double calcXGraphSpeed() {
    return xVelocity / scale;
  }

  public double calcYGraphSpeed() {
    return yVelocity / scale;
  }

  public double calcZGraphSpeed() {
    return zVelocity / scale;
  }

  public void move() {
    if (fixed) {
      xVelocity = 0;
      yVelocity = 0;
      zVelocity = 0;
    }
    if (!fixed) {
      freeMove();
    }
  }

  public void freeMove() {
    x += calcXGraphSpeed() / roundsPerSecond;
    y -= calcYGraphSpeed() / roundsPerSecond;
    z += calcZGraphSpeed() / roundsPerSecond;
  }

  public void forceAccelerate(double force, double alpha, double beta) {
    double a = force / mass;

    accelerate(a, alpha, beta);
  }

  public void forceAccelerate(double[] force) {
    force = scaleArray(force, 1 / mass);
    //System.out.println("force Accelerate");
    accelerate(force);
  }

  public void chargeAccelerate(Particle p) {
    try {
      double force = charge * p.getCharge();
      force /= Math.pow(getDist(p), 2);
      force *= ELECTRIC_CONSTANT;
      double alpha = sideAngle(p);
      double beta = zAngle(p);
      force = Math.abs(force);
      if (Math.signum(p.getCharge()) == Math.signum(charge)) {
        force = -force;
      }
      forceAccelerate(force, alpha, beta);
      p.forceAccelerate(force, alpha + Math.PI, beta + Math.PI);
    } catch (Exception e) {
      System.out.println("no particle");
    }
  }


  public double[] crossProduct(double[] velocity, double[] r) {
    double uvi, uvj, uvk;
    uvi = velocity[1] * r[2] - r[1] * velocity[2];
    uvj = r[0] * velocity[2] - velocity[0] * r[2];
    uvk = velocity[0] * r[1] - r[0] * velocity[1];
    return new double[] {uvi, uvj, uvk};
  }

  public double getCharge() {
    return charge;
  }

  public void setCharge(double c) {
    charge = c * Math.pow(10, -9);
  }

  private double[] scaleArray(double[] arr, double scalar) {
    double[] temp = new double[arr.length];
    for (int i = 0; i < arr.length; i++) {
      temp[i] = arr[i] * scalar;
    }
    return temp;
  }

  private void printArray(double[] d) {
    for (int i = 0; i < d.length; i++) {
      System.out.println(i + ") " + d[i]);
    }
  }

  public double getDist(Particle p) {
    //System.out.println(x+" "+p.getX() +" "+y+" "+ p.getY()+" "+z +" "+ p.getZ());
    double sum = Math.pow((x - p.getX()) * scale, 2) + Math.pow((y - p.getY()) * scale, 2) + Math.pow((z - p.getZ()) * scale, 2);
    return Math.sqrt(sum);
  }

  public double[] getDistComponents(Particle p) {
    double[] results = new double[] {(x - p.getX()) * scale, (y - p.getY()) * scale, (z - p.getZ()) * scale};
    return results;
  }

  public double getVelocity() {
    double sum = Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2) + Math.pow(zVelocity, 2);
    return Math.sqrt(sum);
  }

  public double[] getVelocityComponents() {
    return new double[] {xVelocity, yVelocity, zVelocity};
  }

  public double sideAngle(Particle p) {
    return Math.atan2(y - p.getY(), p.getX() - x);
  }

  public double zAngle(Particle p) {
    return Math.atan2(y - p.getY(), p.getZ() - z);
  }

  public double getX() {
    return x;
  }

  public double getZ() {
    return z;
  }

  public double getY() {
    return y;
  }

  public void makeFixed() {
    fixed = true;
  }

  public void unFix() {
    fixed = false;
  }

  public String getTag() {
    return tag;
  }

  public boolean isActive() {
    return active;
  }

  public void setXYZStart(double xStart, double yStart, double zStart) {
    x = xStart;
    y = yStart;
    z = zStart;
  }

  public void setVelocities(double xVel, double yVel, double zVel) {
    xVelocity = xVel;
    yVelocity = yVel;
    zVelocity = zVel;
    //System.out.println("x "+xVelocity+" "+yVelocity+" "+zVelocity);
  }

  public void setMass(double m) {
    mass = m;
  }

  public double getMass(){return mass;}

  public void setRadius(double r) {
    radius = r;
  }

  public void setColor(Color c) {
    base = c;
  }

  public String toString() {
    return "tag:" + tag + " x:" + x + " y:" + y + " z:" + z + " xVelocity:" + xVelocity + " yVelocity:" + yVelocity + " zVelocity:" + zVelocity + "\n";
  }
}