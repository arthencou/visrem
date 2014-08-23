package camera.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/*
 * Classe que representa os dados de Login no formato adequado para
 * uso com a camera.
 */
public class HTTPAuthenticator extends Authenticator{
    String username, password;
    public HTTPAuthenticator(String user, String pass){
        username = user;
        password = pass;
    }
    @Override
    protected PasswordAuthentication getPasswordAuthentication(){
        System.out.println( "Requesting Host  : "
                + getRequestingHost() );
        System.out.println( "Requesting Port  : "
                + getRequestingPort() );
        System.out.println( "Requesting Prompt : "
                + getRequestingPrompt() );
        System.out.println( "Requesting Protocol: "
                + getRequestingProtocol() );
        System.out.println( "Requesting Scheme : "
                + getRequestingScheme() );
        System.out.println( "Requesting Site  : "
                + getRequestingSite() );
        return new PasswordAuthentication(username,
                password.toCharArray() );
    }
}
