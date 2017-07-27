/**
 * Created by michaellaposata on 6/5/17.
 */

import io.reactivex.Flowable;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

@SuppressWarnings("serial")
public class Main extends Applet implements ActionListener {
  private Canvas c;
  private Graphics g;
  private Dimension d;
  private Image buffer;
  private RenderedImage frames;
  private Graphics gB;
  private Graphics framesGraphics;
  private Particle[] collection;
  private Flowable<Particle> total;
  private Color background;
  private Button b;
  private Button r;
  private Timer timer;
  private Graphics testG;
  private int roundsPerSecond = 5000;
  private int counter = 0;
  private int framesPerSecond = 5;
  private int framesCaptured = 0;
  private boolean started = false;
  private boolean setup = false;
  private boolean act = true;
  private int zLimit = 200;
  private GifSequenceWriter maker;

  /***
   * Creates the full sized display and creates a button to start the main functionality.
   */
  public void init() {
    b = new Button();
    b.addActionListener(this);
    add(b);
    timer = new Timer();
  }

  /***
   * Draws all particles to a graphics.
   * @param g the graphics on which everything will be drawn.
   * @return the image with all the particles presently active drawn on it
   */
  public boolean draw(Graphics g) {

    boolean active = false;
    total.sorted((p1, p2) -> (int) Math.signum(p1.z - p2.z))
        .blockingSubscribe(p -> p.drawParticle(g));
    return total
        .any(p -> p.onScreen(d.width, d.height, zLimit))
        .blockingGet();
  }

  /***
   * Creates the initial set of particles
   */
  public void particleSet() {
    collection = new Particle[3];
    collection[0] = new Particle(roundsPerSecond, "0");
    collection[0].setCharge(-50);
    collection[0].setVelocities(100, -10, -5);
    collection[0].setXYZStart(000, 400, 50);
    collection[1] = new Particle(roundsPerSecond, "1");
    collection[1].setCharge(20);
    collection[1].setVelocities(-100, 10, 5);
    collection[1].setXYZStart(1000, 200, -50);
    collection[2] = new Particle(roundsPerSecond, "2");
    collection[2].setCharge(10);
    collection[2].setVelocities(0, -100, -500);
    collection[2].setXYZStart(400, 400, 2000);

    //total holds all particles from collection and coil
    total = Flowable.fromArray(collection);
    total.blockingSubscribe(x -> System.out.println(x));
  }

  /**
   * starts the entire program
   */
  public void main() {
    init();
  }

  /***
   * runs when ever the user hits a button
   * @param e the data about which button was pushed
   */
  public void actionPerformed(ActionEvent e) {
    if (!started) {
      initialStart();
    } else if (!setup && (e.getSource() == b || e.getSource() == r)) {
      secondaryActivation(e);
    } else {
      try {
        getSavedImages();
        g.drawImage(buffer, 0, 0, 1000, 600, null);
      } catch (Exception i) {
        i.printStackTrace();
      }
      System.out.println("rerunning");
    }
  }

  public void initialStart() {
    r = new Button("rerun");
    add(r);
    r.addActionListener(this);
    d = new Dimension(1000, 600);
    this.setSize(d);
    c = new Canvas();
    add(c);
    c.setLocation(0, 0);
    c.setSize(1000, 600);
    background = Color.gray;
    buffer = c.createImage(d.width, d.height);
    System.out.println(buffer.getHeight(null));
    System.out.println(buffer.getWidth(null));
    started = true;
  }

  public void secondaryActivation(ActionEvent e) {
    remove(b);
    g = c.getGraphics();
    gB = buffer.getGraphics();
    if (e.getSource() == r) {
      try {
        getSavedImages();
      } catch (Exception i) {

      }
      System.out.println("rerunning");
    } else {
      particleSet();
      //System.out.println("collection 0 "+collection[0].getZ()+" "+collection[0].getY()+" "+collection[0].getZ());
      setup = true;
      frames = (RenderedImage) c.createImage(d.width, d.height);
      process(60);
      getSavedImages();
      g.drawImage(buffer, 0, 0, 1000, 600, null);
      //Looper((float)100.0/roundsPerSecond);
    }
  }

  public void process(int seconds) {
    System.out.println("starting to process, simulating " + seconds + " seconds with " + roundsPerSecond * seconds + " calculations and " + seconds *  framesPerSecond + " frames");
    ImageOutputStream output = null;
    String fileName = "";
    try {
      //fileName = getNewGIFName();
      File g = new File("/Users/michaellaposata/Documents/workspace/RXSimulator/Gif/experimentRound0.gif");
      System.out.println(fileName);
      output = new FileImageOutputStream(g);
      maker = new GifSequenceWriter(output, BufferedImage.TYPE_BYTE_BINARY, 1, true);
    } catch (Exception e) {

      e.printStackTrace();
    }

    framesGraphics = ((Image) frames).getGraphics();
    File f;
    deleteSavedImages();
    int i;
    for (i = 1; i < roundsPerSecond * seconds && act; i++) {
      TempParticle avg = new TempParticle();
      total.blockingSubscribe(x -> avg.addParticle(x));

      total.blockingSubscribe((x) -> x.chargeAccelerate((Particle)(avg.getWithout(x))));

      total.blockingSubscribe(Particle::move);

      if (i % (60 * roundsPerSecond) == 0) {
        total.blockingSubscribe(x -> System.out.println(x));
        System.out.println(i / roundsPerSecond);
      }
      if (i % (roundsPerSecond / framesPerSecond) == 0) {

        draw(gB);
        f = new File("/Users/michaellaposata/Documents/workspace/RXSimulator/images/frames" + framesCaptured);

        saveImage(f);
        try {
          maker.writeToSequence((RenderedImage) frames);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        frames = (RenderedImage) c.createImage(d.width, d.height);

        framesGraphics = ((Image) frames).getGraphics();
      }
    }
    try {
      maker.close();
      output.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("seconds");
    System.out.println(i / roundsPerSecond);
    System.out.println("framesCaptured");
    System.out.println("done");
  }

  public void saveImage(File f) {
    if (draw(framesGraphics)) {
      try {

        ImageIO.write(frames, "png", f);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      framesCaptured++;

    } else {
      act = false;
    }
  }

  public String getNewGIFName() {
    File f = new File("/Users/michaellaposata/Documents/workspace/RXSimulator/Gif/experimentRound0.gif");
    int counter = 1;
    while (f.exists()) {
      f = new File("/Users/michaellaposata/Documents/workspace/RXSimulator/Gif/experimentRound" + counter + ".gif");
      counter++;
    }
    String temp = f.getPath();
    String name = "";
    for (int i = 0; i < temp.length() - 4; i++) {
      name += temp.charAt(i);
    }
    return name;
  }

  public void getSavedImages() {
    System.out.println("get saved images");
    boolean working = true;
    int counter = 0;
    File f;
    Image i;
    while (working) {
      f = new File("/Users/michaellaposata/Documents/workspace/RXSimulator/images/frames" + counter);
      if (counter % 100 == 0) {
        System.out.println("saved " + counter);
      }
      try {
        i = ImageIO.read(f);
        g.drawImage(i, 0, 0, null);
      } catch (Exception e) {
        working = false;
        System.out.println("ran out at " + counter);
      }
      counter++;
//			try {
//
//				//Thread.sleep(500/framesPerSecond);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    }
  }

  public void deleteSavedImages() {
    boolean working = true;
    int counter = 0;
    File f;
    while (working) {
      f = new File("/Users/michaellaposata/Documents/workspace/RXSimulator/images/frames" + counter);
      if (counter % 100 == 0) {
        System.out.println("deleted " + counter);
      }
      if (f.delete()) {
        counter++;
      } else {
        working = false;
      }

    }
  }

  public void display(Graphics g, ArrayList<Image> p) {
    for (Image i : p) {
      g.drawImage(i, 0, 0, null);
      try {
        Thread.sleep((roundsPerSecond / 1000));
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      g.drawImage(buffer, 0, 0, null);
    }
  }
}
