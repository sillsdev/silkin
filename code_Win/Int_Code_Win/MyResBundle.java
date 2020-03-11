
import java.util.*;
import java.io.*;

/**This is my own implementation of the essential features of a text-oriented 
 * Resource Bundle. It is needed because the standard Java ResourceBundle class
 * does not read French special characters when encoded in UTF-8, and it does not
 * seem to read regular English keys when encoded in UTF-16.
 * <p>
 * MyResBundle reads a *.properties file (UTF-8) with normal file readers and
 * stores the lookup table in a TreeMap. Its getString() method returns the 
 * value for any key, or an error message if the key is not found. 
 *</p>
 * getBundle gives error messages in English because we can't load the lookUpTable
 * for any other language.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class MyResBundle implements Serializable {
    TreeMap<String, String> lookUpTable = new TreeMap<String, String>();

    static MyResBundle getBundle(String baseName, Locale locale, String resDir) 
            throws KSInternalErrorException {
        // Error check the args, then read the resource file and load TMap
        if (baseName == null || locale == null) {
            throw new KSInternalErrorException("Missing argument in 'getBundle' call.");
        }
        if (baseName.isEmpty() || locale.getLanguage().isEmpty()) {
            throw new KSInternalErrorException("Missing argument in 'getBundle' call.");
        }
        MyResBundle bundle = new MyResBundle();
        String resFile = Library.resourceDirectory + baseName + "_"
                + locale.getLanguage() + ".properties";
        BufferedReader file = null;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(resFile), "UTF8"));
            String key, value, line = file.readLine();
            int start, end;
            while (line != null) {
                if (!line.startsWith("#")) {
                    end = line.indexOf(" = ");
                    start = end + 3;
                    if (end == -1){  //  test for blank lines
                        line = file.readLine();
                        continue;
                    }
                    key = line.substring(0, end);
                    value = line.substring(start);
                    bundle.lookUpTable.put(key, value);
                }
                line = file.readLine();
            }
        } catch (Exception e) {
            String oops = "MyResBundle Error:\n" + e;
            throw new KSInternalErrorException(oops);
        }
        return bundle;
    }
    
    String getString(String key) {
        String value = "Key '" + key + "' not found";
        if (lookUpTable.containsKey(key)) {
            value = lookUpTable.get(key);
        }
        return restoreNewLines(value);
    }
    
    String restoreNewLines(String input) {
        String output = "";
        int start = 0, size = input.length(), stop;
        stop = input.indexOf("\\n", start);
        while (stop > -1 && start < size) {
            output += input.substring(start, stop);
            output += "\n";
            start = stop + 2;
            stop = input.indexOf("\\n", Math.min(size -1, start));
        }
        if (start < size) {
            output += input.substring(start, size);
        }
        return output;
    }
}
