package IPCam.Camera;

import IPCam.Connect.TCPConnection;
import java.applet.Applet;
import java.awt.Image;
import java.net.URL;

public class StreamController{

    private Applet myParent;
    private StreamParser myParser;
    public TCPConnection myConnection;
    private StreamHeadProc myStreamProc;
    private boolean connected;
    private boolean connectedBefore;
    final int NODEF = 0;
    final int NCL = 1;
    final int CL = 2;
    private int streamType;
    public boolean protect;

    public boolean finalizarLoop;

    public synchronized void stop(){
        myParent.stop();
    }

    public synchronized void processStreamHead(){
        myStreamProc = new StreamHeadProc(myConnection.myDataInputStream);
        streamType = myStreamProc.findStreamType();
    }

    public StreamController(URL url, String s, Applet applet){
        connected = false;
        connectedBefore = false;
        streamType = 0;
        protect = false;
        myConnection = new TCPConnection(url, s);
        myParent = applet;

        finalizarLoop = false;
    }

    public void closeConnection(){
        myConnection.closeConnection();
    }

    public synchronized void initParser(){
        switch(streamType){
        case 2: // '\002'
            System.out.println("Stream with Content-Length header");
            myParser = new CLParser(this, myStreamProc);
            break;

        case 1: // '\001'
            System.out.println("Stream without Content-Length header");
            myParser = null;
            myParser = new NCLParser(this, myStreamProc);
            break;

        case 0: // '\0'
            System.out.println("Could not find the type of the stream");
            closeConnection();
            stop();
            break;
        }
    }

    void restartConnection(){
        if(finalizarLoop != true){
            connected = false;
            myConnection.restartConnection();
            System.gc();
            connectToStream();
            processStreamHead();
            initParser();
        }
    }

    public Image getImage() throws Exception{
        if(myParser != null){
            return myParser.parseImage();
        }
        else{
            return null;
        }
    }

    public synchronized void connectToStream(){
        /*if(!connectedBefore){
            //if(myConnection.openConnection()){
            boolean con = false;
            while( !con && !finalizarLoop ){
                if(con = myConnection.openConnection()){
                    if(myConnection.myDataOutputStream != null)
                        myConnection.sendGETRequest();
                    if(myConnection.myDataInputStream != null)
                        connected = myConnection.processResponse();
                    if(myConnection.state == 3){
                        stop();
                        return;
                    }
                }
            }
        }
        else
        if(myConnection.openConnection()){
            myConnection.sendProtectedGETRequest();
            connected = myConnection.processResponse();
            if(myConnection.state == 3){
                stop();
                return;
            }
            myConnection.input = false;
            connectedBefore = true;
        }
        else{
            stop();
        }*/
        while(!connected && !finalizarLoop){
            myConnection.closeConnection();
            myConnection.myBasicAuthStr = myParent.getParameter("Authorization");
            if(myConnection.myBasicAuthStr != null){
                myConnection.useBasicAuthStr = true;
            } else{
                myConnection.myUserName =
                        ((camera.modelos.IP_Cam) myParent).myCam.getCamsUser();
                myConnection.myPassword =
                        ((camera.modelos.IP_Cam) myParent).myCam.getCamsPass();
            }
            if(myConnection.openConnection()){
                myConnection.sendProtectedGETRequest();
                connected = myConnection.processResponse();
                if(myConnection.state == 3){
                    stop();
                    return;
                }
                myConnection.input = false;
                connectedBefore = true;
                protect = true;
            }
            else{
                stop();
            }
        }
    }
}
