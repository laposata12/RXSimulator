/**
 * Created by michaellaposata on 6/5/17.
 */
import io.reactivex.Flowable;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
@SuppressWarnings("serial")
public class Main extends Applet implements ActionListener, MouseMotionListener, MouseListener{
    Canvas c;
    Graphics g;
    Dimension d;
    Image buffer;
    RenderedImage frames;
    Graphics gB;
    Graphics framesGraphics;
    Particle[] collection;
    Flowable<Particle> total;
    Color background;
    Button b;
    Button r;
    Timer timer;
    Graphics testG;
    int roundsPerSecond = 5000;
    int counter =0;
    int framesPerSecond = 5;
    int framesCaptured = 0;
    boolean started = false;
    boolean setup = false;
    boolean act = true;
    int zLimit = 200;
    GifSequenceWriter maker;
    public void init(){
        b = new Button();
        b.addActionListener(this);

        add(b);
        timer = new Timer();
    }
    public boolean draw(Graphics g){
        boolean active = false;

        Sort.sort(0, total.length-1, total);
        for(Particle p: total){

            p.drawParticle(g);
            if(p.onScreen(d.width, d.height, zLimit)){
                active = true;
            }
        }
        return total
                .any(p -> p.onScreen(d.width, d.height, zLimit))
                .blockingGet();
    }
    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void particleSet(){
        //System.out.println("stuff");
        collection = new Particle[2];
        collection[0] = new Particle(roundsPerSecond, "0");
        collection[0].setCharge(-50);
        collection[0].setVelocities(100, -10, -5);
        collection[0].setXYZStart(000, 400, 50);
        collection[1] = new Particle(roundsPerSecond, "1");
        collection[1].setCharge(20);
        collection[1].setVelocities(-100, 10, 5);
        collection[1].setXYZStart(1000, 200, -50);


        //total holds all particles from collection and coil
        total = Flowable.fromArray(collection);
    }
    public void actionPerformed(ActionEvent e) {
        if(!started){
            r = new Button("rerun");
            add(r);
            r.addActionListener(this);
            d = new Dimension(1000, 600);
            this.addMouseListener(this);
            this.setSize(d);
            c = new Canvas();
            add(c);
            c.setLocation(0, 0);
            c.setSize(1000,600);
            background = Color.gray;
            buffer = c.createImage(d.width,d.height);
            System.out.println(buffer.getHeight(null));
            System.out.println(buffer.getWidth(null));
            c.addMouseListener(this);
            started = true;
        }
        else if(!setup && (e.getSource() == b || e.getSource() == r)){
            remove(b);
            g = c.getGraphics();
            gB = buffer.getGraphics();
            if(e.getSource() == r){
                try{
                    getSavedImages();
                }catch(Exception i){

                }
                System.out.println("rerunning");
            }
            else{
                particleSet();
                //System.out.println("collection 0 "+collection[0].getZ()+" "+collection[0].getY()+" "+collection[0].getZ());
                setup = true;
                frames = (RenderedImage) c.createImage(d.width,d.height);
                process(300);
                getSavedImages();
                g.drawImage(buffer, 0, 0, 1000, 600,null);
                //Looper((float)100.0/roundsPerSecond);
            }
        }else{
            try{
                getSavedImages();
                g.drawImage(buffer, 0, 0, 1000, 600,null);
            }catch(Exception i){

            }
            System.out.println("rerunning");
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    public void process(int seconds){
        System.out.println("starting to process, simulating "+seconds+" seconds with "+ roundsPerSecond*seconds+" calculations and "+ seconds*roundsPerSecond/framesPerSecond +" frames");
        ImageOutputStream output = null;
        String fileName ="";
        try {
            fileName = getNewGIFName();
            File g = new File(fileName+".gif");
            System.out.println(fileName);
            output = new FileImageOutputStream(g);
            maker =  new GifSequenceWriter(output, BufferedImage.TYPE_BYTE_BINARY, 1, true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        framesGraphics= ((Image)frames).getGraphics();
        File f = new File("/Users/MichaelLaposata/Documents/workspace/PysicsCFinal/bin/images/frames");
        deleteSavedImages();
        int i;
        for(i = 1;i<roundsPerSecond*seconds && act; i++ ){
            for(int a = 0; a < collection.length; a ++){
                //System.out.println("X in process "+collection[a].getX() +" y "+collection[a].getY()+" z "+ collection[a].getZ());
                //System.out.println("forces");
                for(int b = collection.length-1; b> a; b--){
                    collection[a].chargeAccelerate(collection[b]);
                    collection[a].magneticAccelerate(collection[b]);
                }

                //System.out.println("move");
                collection[a].move();

            }

            //System.out.println(i);
            if(i % (60*roundsPerSecond) == 0){

                System.out.println(i/roundsPerSecond);
            }
            if(i % (roundsPerSecond/framesPerSecond)==0){
                for(int a = 0; a < collection.length; a ++){
                }
                draw(gB);
                f = new File("images/frames"+framesCaptured);

                saveImage(f);
                try {
                    maker.writeToSequence((RenderedImage) frames);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                frames = (RenderedImage) c.createImage(d.width,d.height);

                framesGraphics= ((Image)frames).getGraphics();
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
        System.out.println(i/roundsPerSecond);
        System.out.println("framesCaptured");
        System.out.println("done");
    }
    public void saveImage(File f){
        if(draw(framesGraphics)){
            try {

                ImageIO.write(frames, "png", f);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            framesCaptured++;

        }else{
            act = false;
        }
    }
    public String getNewGIFName(){
        File f = new File("Gif/experimentRound0.gif");
        int counter = 1;
        while(f.exists()){
            f = new File("Gif/experimentRound"+counter+".gif");
            counter ++;
        }
        String temp = f.getPath();
        String name = "";
        for(int i = 0; i < temp.length()-4;i++){
            name += temp.charAt(i);
        }
        return name;
    }
    public void getSavedImages(){
        System.out.println("get saved images");
        boolean working = true;
        int counter  = 0;
        File f;
        Image i;
        while(working){
            f = new File("images/frames"+counter);
            if(counter % 100 == 0){
                System.out.println("saved "+counter);
            }
            try {
                i = ImageIO.read(f);
                g.drawImage(i, 0, 0, null);
            } catch (Exception e) {
                working = false;
                System.out.println("ran out at "+counter);
            }
            counter ++;
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
        int counter  = 0;
        File f;
        while(working){
            f = new File("images/frames"+counter);
            if(counter % 100 == 0){
                System.out.println("deleted "+counter);
            }
            if(f.delete()){
                counter ++;
            }else{
                working = false;
            }

        }
    }
    public void display(Graphics g, ArrayList<Image> p){
        for(Image i:p){
            g.drawImage(i, 0, 0, null);
            try {
                Thread.sleep((roundsPerSecond/1000));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            g.drawImage(buffer, 0, 0, null);
        }
    }
}
