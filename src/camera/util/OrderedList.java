package camera.util;

import java.util.LinkedList;

/*
 * Lista com inserção ordenada de strings.
 */
public class OrderedList extends LinkedList {
    @Override
    public boolean add(Object e){
        String a = ( (String[]) e )[0];
        if( this.size() == 0) return super.add(e);
        for(int i = 0; i < this.size(); i++){
            String s = ( (String[]) this.get(i) )[0];
            if( a.equals(s) )
                return false;
            for(int j = 0;
                j < ((a.length() < s.length()) ? s.length() : a.length());
                j++ )
            {
                int comp = a.compareTo(s);
                if( comp < 0 ){
                    super.add(i, e);
                    return true;
                }
                else if( comp > 0 ) break;
            }
            if( i+1 == this.size() ){
                super.add(i+1, e);
                return true;
            }
        }
        return false;
    }
}
