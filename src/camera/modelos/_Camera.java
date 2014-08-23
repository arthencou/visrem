package camera.modelos;

import java.awt.Image;
import javax.swing.JPanel;

/*
 * Essa classe define um modelo genérico de camera. Obviamente este é um
 * modelo que não funciona se for instaciado de forma direta.
 */
public class _Camera {

    protected static final String NEWLINE = System.getProperty("line.separator");

    // Nome da camera.
    protected String myCamsName;
    // Campos de Login.
    protected String myUser = "";
    protected String myPass = "";
    protected boolean needAuthentication = false;
    protected boolean useDefaultPass;
    // Campos relacionados à janela principal.
    camera.Camera myParentWindow;
    protected int listIndex;
    protected int tabbedPaneIndex = -1;
    protected JPanel cameraFrame;
    
    /*
     * Construtor da camera. Inicializa alguns campos.
     */
    public _Camera(String name, camera.Camera parentWindow,
            Boolean useDefPass)
    {
        myCamsName = name;
        myParentWindow = parentWindow;
        if(useDefaultPass = useDefPass){
            String[] auth = myParentWindow.getDefAuth();
            if(auth != null) setCamsAuth(auth[0], auth[1]);
        }
        else setCamsAuth();
    }

    /*
     * Providencia a definição dos campos de Login da camera.
     */
    public void setCamsAuth(){
        if(useDefaultPass){
            myParentWindow.setDefAuth();
            return;
        }
        String[] auth = myParentWindow.getAuth(myCamsName, myUser, myPass);
        if(auth != null){
            setCamsAuth(auth[0], auth[1]);
        }
    }

    /*
     * Define os campos de Login da camera com base nos parametros.
     */
    public void setCamsAuth(String user, String pass){
        myUser = user;
        myPass = pass;
        needAuthentication = !( myUser.equals("") && myPass.equals("") );
    }

    /*
     * Retorna o valor no campo myUser
     */
    public String getCamsUser(){
        return myUser;
    }

    /*
     * Retorna o valor no campo myPass
     */
    public String getCamsPass(){
        return myPass;
    }
    
    /*
     * Confirma se a camera está usando os dados de Login compartilhados.
     */
    public boolean usesDefaultPassword(){
        return useDefaultPass;
    }

    /*
     * Essa função deve criar o Frame principal da camera e retorna-lo.
     */
    public JPanel createFrame(){
        return null;
    }
    
    /*
     * Essa função deve proceder adequadamente para remover seus Frames
     * da janela principal.
     */
    public void remFrame(){
        cameraFrame.setVisible(false);
    }
    
    /*
     * Define o campo da camera que representa o seu ordinal entre os
     * Frames adicionados na janela principal.
     */
    public void setIndex(int index){
        listIndex = index;
    }

    /*
     * Define o campo da camera que representa a posição de seu Frame
     * ampliado entre as abas da janela principal.
     */
    public void setTabIndex(int index){
        tabbedPaneIndex = index;
    }

    /*
     * Retorna o nome da camera.
     */
    public String getCamsName(){
        return myCamsName;
    }

    /*
     * Essa função deve receber uma imagem para momstra-la no(s) Frame(s)
     * da camera
     */
    public void refreshLabel(Image newImage){
    }

}
