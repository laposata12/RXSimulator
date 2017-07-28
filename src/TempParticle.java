import java.awt.*;

/**
 * Created by michaellaposata on 7/18/17.
 */
public class TempParticle extends Particle {
  private int particleCount = 0;
  /***
   *Creates an empty tempParticle. Tag is set to "temp" and a random color is assigned.
   *  All other values are set to zero.
   */
  public TempParticle(){
    super(0, "Temp");

    x = 0;
    y = 0;
    z = 0;
    charge = 0;
    mass = 0;
    base = new Color(Color.HSBtoRGB(((float) ((int) (Math.random() * 10000)) / 10000), (float) 1.0, (float) 1.0));
    tag = "temp";

  }

  /***
   * used to create a new tempParticle with one particle averaged out
   * @param avgX average X coordinate
   * @param avgY average Y coordinate
   * @param avgZ average Z coordinate
   * @param avgCharge average charge
   * @param name name of particle being removed
   * @param count number of particles within the average
   */
  private TempParticle(double avgX, double avgY, double avgZ, double avgCharge, String name, int count){
    super(0, "Temp without "+ name);
    x = avgX;
    y = avgY;
    z = avgZ;
    charge = avgCharge;
    particleCount = count;
  }

  /***
   * averages in a new particle
   * @param p the particle being averaged in
   */
  public void addParticle(Particle p){

    particleCount++;
    double baseWeight = (particleCount - 1.0)/particleCount;
    double newWeight = 1.0/particleCount;
    x = x * baseWeight * charge + p.getX() * newWeight * p.getCharge();
    y = y * baseWeight * charge + p.getY() * newWeight * p.getCharge();
    z = z * baseWeight * charge + p.getZ() * newWeight * p.getCharge();
    charge *= baseWeight;
    charge += newWeight * p.getCharge();
    x /= charge;
    y /= charge;
    z /= charge;

  }

  /**
   * removes one particle from the average
   * @param p the particle being removed from the average
   */
  public void removeParticle(Particle p){

    x = (x * particleCount * charge - p.getX() * p.getCharge())/ (particleCount - 1.0);
    y = (y * particleCount * charge - p.getY() * p.getCharge())/ (particleCount - 1.0);
    z = (z * particleCount * charge - p.getZ() * p.getCharge())/ (particleCount - 1.0);
    charge = (charge*particleCount - p.getCharge())/ (particleCount - 1.0);
    x /= charge;
    y /= charge;
    z /= charge;
    particleCount --;

  }

  /**
   * creates a particle with one particle averaged out without changing the original tempParticle
   * @param p the particle to be averaged out
   * @return a new particle with one particle averaged out
   */
  public TempParticle getWithout(Particle p){
    TempParticle temp = new TempParticle(x, y, z, charge, p.getTag(), particleCount);
    temp.removeParticle(p);
    return temp;
  }

  /**
   * creates a string containing the particles tag, x, y, z, charge, and mass values
   * @return a string summarizing the TempParticles data
   */
  public String toString(){
    return "tag:" + tag + " x:" + x + " y:" + y + " z:" + z + " charge:" + charge + "\n";

  }


}
