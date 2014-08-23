package camera;

import camera.util.Environment;

public class Main {

    public static void main(String[] args) throws Exception {

        /*
         * Para usar a biblioteca xuggle-xugger, responsável pela gravação
         * em vídeo, é necessário definir algumas variáveis ambiente.
         * Isso é feito a seguir, antes de dar-se início ao programa principal.
         */
        String path = System.getProperty("user.dir");
        
        Environment env = new Environment();

        System.out.println( "Setting XUGGLE_HOME: "
                +env.libc.setenv("XUGGLE_HOME", path+"/xuggle/", 0) );
        System.out.println( "Setting PATH: "
                +env.libc.setenv("PATH", path+"/xuggle/bin", 0) );
        

        /*
         * Programa principal:
         */
        new Camera();
    }
}