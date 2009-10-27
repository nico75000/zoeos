
import java.io.*;

// name the class and the File after your program no.

public class P10000 {
    // possible exit codes
    public static final int ERC_SUCCESS = 00;
    public static final int ERC_SUCCESS_BIN = 01;
    public static final int ERC_ERROR = 10;
    public static final int ERC_MEMORY = 11;
    public static final int ERC_FILE_IO = 12;
    public static final int ERC_BAD_ARGS = 13;
    public static final int ERC_BAD_INPUT = 14;
    public static final int ERC_EXPIRED = 15;
    public static final int ERC_INTERNAL = 16;

    // possible values for language_id
    public static final int liEnglish = 1;
    public static final int liGerman = 2;
    public static final int liPortuguese = 3;
    public static final int liSpanish = 4;
    public static final int liItalian = 5;
    public static final int liFrench = 6;

    // parsed input values are stored here
    public static String reg_name;
    public static String email;
    public static int language_id;
    static String userkey, cckey;

    // parse a single line of the input File ans store data in class variables
    static final void parse(String line) {
        String key, value;
        int p = line.indexOf('=');

        if (p == 0) return;

        key = line.substring(0, p);
        value = line.substring(p + 1);

        if (key.compareTo("REG_NAME") == 0)
            reg_name = value;
        else if (key.compareTo("EMAIL") == 0)
            email = value;
        else if (key.compareTo("LANGUAGE_ID") == 0)
            language_id = Integer.valueOf(value).intValue();

        // addDesktopElement more lines as you need them
    }

    // a simple example algorithm
    static int CalcKey(String s) {
        int i, key = 0;

        for (i = 0; i < s.length(); i++) {
            key += s.charAt(i);
            key = key << 2;
        }
        return key;
    }

    // implement your key generator here
    static boolean GenerateKey() {
        int key = CalcKey(reg_name);
        cckey = "x" + key;

        if (language_id == liGerman)
            userkey = "Benutzername: " + reg_name + "\r\nSchlüssel: " + cckey;
        else
            userkey = "Username: " + reg_name + "\r\nKey: " + cckey;

        return true;
    }

    // write output File
    static final void WriteFile(String name, String value) {
        try {
            System.out.println("writing: " + name);

            BufferedWriter fout;
            fout = new BufferedWriter(new FileWriter(name));
            fout.write(value);
            fout.flush();

        } catch (IOException e) {
            System.out.println("Could not write File: " + name);
        }
    }

    // load and parse input, call key generator and write output files
    public static final int KeyMain(String args[]) {
        BufferedReader fin;
        String s;

        if (args.length != 3) {
            System.out.println("Illegal number of arguments: " + args.length);
            return ERC_BAD_ARGS;
        }

        // read input
        try {
            fin = new BufferedReader(new FileReader(args[0]));
            while ((s = fin.readLine()) != null) {
                parse(s);
            }
            fin.close();
        } catch (IOException e) {
            System.out.println("Could not open input File: " + args[0]);
            return ERC_FILE_IO;
        }

        if (GenerateKey()) {
            WriteFile(args[1], userkey);
            WriteFile(args[2], cckey);
        }

        return ERC_SUCCESS;
    }

    // call KeyMain method and report exit code
    public static final void main(String args[]) {
        int exitcode;

        System.out.println("Example Key Generator");
        exitcode = KeyMain(args);
        System.out.println("exit code: " + exitcode);
    }
}