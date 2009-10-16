
import java.text.ParseException;
import java.text.SimpleDateFormat;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nazo
 */
public class test {
    /**
     * 
     * @param a
     */
    static public  void main(String[] a) {
        SimpleDateFormat s = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.UK);
        Date d=null;
        try {
            d = s.parse(a[0]);
        } catch(ParseException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("orig:" + a[0]);
        System.out.println("a:" + d.getTime());
    }
}
