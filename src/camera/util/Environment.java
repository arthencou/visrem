/*
 * Código obtido de http://quirkygba.blogspot.com/2009/11/setting-environment-variables-in-java.html
 * acessível em 21/10/2011.
 * Permite ajustar variáveis ambiente fora do contexto Java.
 */

package camera.util;

import com.sun.jna.Library;
import com.sun.jna.Native;

/*
 * A classe Environment deveria possuir métodos capazes de ajustar
 * variaveis ambiente tanto no ambiente Windows quanto no ambiente
 * Linux.
 * No entanto, em algumas instalações Linux, as bibliotecas chamadas
 * não são capazes de acessar os caminhos definidos pelo código.
 * Esse problema foi contornado definindo-se manualmente as variáveis
 * ambiente através do shell.
 */
public class Environment {
    public interface WinLibC extends Library {
        public int _putenv(String name);
    }
    /*public interface LinuxLibC extends Library {
        public int setenv(String name, String value, int overwrite);
        public int unsetenv(String name);
    }*/
    static public class POSIX {
        static Object libc;
        static {
            if (!System.getProperty("os.name").equals("Linux")) {
                libc = Native.loadLibrary("msvcrt", WinLibC.class);
            }/* else {
                libc = Native.loadLibrary("c", LinuxLibC.class);
            }*/
        }
        
        public int setenv(String name, String value, int overwrite) {
            if (libc instanceof WinLibC) {
                return ((WinLibC)libc)._putenv(name + "=" + value);
            }
            return 0;
            /*else {
                return ((LinuxLibC)libc).setenv(name, value, overwrite);
            }*/
        }
        
        /*public int unsetenv(String name) {
            if (libc instanceof LinuxLibC) {
                return ((LinuxLibC)libc).unsetenv(name);
            }
            else {
                return ((WinLibC)libc)._putenv(name + "=");
            }
        }*/
    }
    static public POSIX libc = new POSIX();
}