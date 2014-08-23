package camera.modelos;

import IPCam.Camera.*;
import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class IP_Cam extends Applet implements Runnable{

    private Thread myThread;
    private String myCamsName;
    private Image myCurrentImage;
    private String myCodeBase;
    private String myLocation;
    private String myAuthorization;
    private StreamController myStreamController;
    private boolean finalizarLoop;
    public _Camera myCam;
    
    public IP_Cam( String nome, String Base, String Location,
            String Authorization, _Camera cam )
    {
        myCamsName = nome;
        myCodeBase = Base;
        myLocation = Location;
        myAuthorization = Authorization;
        try {
            String path = System.getProperty("user.dir");
            myCurrentImage =
                    ImageIO.read(new File(path +"//images//blank.jpg"));
        } catch (IOException ex) {
            Logger.getLogger(IP_Cam.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        myCam = cam;
        myCam.refreshLabel(myCurrentImage);
        finalizarLoop = false;
        myStreamController = null;
    }

    public String getCamName(){
        return this.myCamsName;
    }

    @Override
    public void start(){
        super.start();
        System.out.println( "Thread started IP_Cam: " +myCamsName );
        if( myThread == null ){
            myThread = new Thread(this);
            myThread.setPriority(1);
            myThread.start();
        }
    }

    @Override
    public synchronized void stop(){
        System.out.println("Applet stopped");
        finalizarLoop = true;
        if(myStreamController != null) myStreamController.finalizarLoop = true;
        if (myCurrentImage != null) {
            myCurrentImage.flush();
            myCurrentImage = null;
        }
        if(myStreamController != null) myStreamController.closeConnection();
        if(myThread != null) myThread.interrupt();
        myThread = null;
        System.gc();
    }

    public void run(){
        try {
            myStreamController = new StreamController(
                    ReturnURL( myCodeBase ), myLocation, this );
            myStreamController.connectToStream();
            myStreamController.processStreamHead();
            myStreamController.initParser();
            do{
                Image image;
                do{
                    image = null;
                    try{
                        image = myStreamController.getImage();
                    }catch( Exception exception ){
                        System.out.println("Read image error");
                    }
                }while( image == null && (finalizarLoop == false) );
                myCurrentImage = image;
                myCam.refreshLabel(myCurrentImage);
            }while( finalizarLoop == false );
            finalizarLoop = false;
            if(myThread != null){
                myThread.interrupt();
                myThread = null;
            }
        System.gc();
        } catch (MalformedURLException ex) {
            Logger.getLogger(IP_Cam.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IP_Cam.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public URL ReturnURL( String caminho ) throws MalformedURLException, IOException{
        URL url = new URL( caminho );
        return url;
    }

    @Override
    public String getParameter(String name){
        return myAuthorization;
    }
}
