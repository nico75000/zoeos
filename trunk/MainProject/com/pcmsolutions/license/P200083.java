package com.pcmsolutions.license;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

public class P200083 {
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
    static String userKey, ccKey;

    private static String FIELD_SEPERATOR = "-";
    private final static String zuonicsPassword = "6H7D CD92 KL92 HAPH 10GN XKG9 57D5 KDPW 5S58 8GJE SP2G KS58 GKSI RJFU W2KG GH67";

    private static String keyProduct = "ZoeOS";
    private static String keyType = "Full";
    private static int keyQuantity = 256;
    private static int lowMajorVersion = 1;
    private static int lowMinorVersion = 0;
    private static int highMajorVersion = 1;
    private static int highMinorVersion = 999;

    static boolean generateKey() throws NoSuchAlgorithmException {
        java.util.Random rand = new Random(reg_name.hashCode() + System.currentTimeMillis());
        DecimalFormat vf = new DecimalFormat("000");
        ccKey = keyProduct + FIELD_SEPERATOR
                + keyType + FIELD_SEPERATOR
                + keyQuantity + FIELD_SEPERATOR
                + vf.format(lowMajorVersion) + FIELD_SEPERATOR
                + vf.format(lowMinorVersion) + FIELD_SEPERATOR
                + vf.format(highMajorVersion) + FIELD_SEPERATOR
                + vf.format(highMinorVersion) + FIELD_SEPERATOR
                + reg_name + FIELD_SEPERATOR
                + Integer.toHexString(rand.nextInt()).toUpperCase()
                ;

        byte[] md5;
        MessageDigest md = null;
        md = MessageDigest.getInstance("MD5");
        md.update(ccKey.getBytes());
        md.update(FIELD_SEPERATOR.getBytes());
        md.update(zuonicsPassword.getBytes());
        md5 = md.digest();
        userKey = ccKey + FIELD_SEPERATOR;
        for (int i = 0; i < md5.length; i++)
            userKey += Integer.toHexString(md5[i]).toUpperCase();
        return true;
    }

    private static String removeDelimiters(String str) {
        StringTokenizer t;
        String outStr = "";
        t = new StringTokenizer(str);
        while (t.hasMoreTokens())
            outStr += t.nextToken();
        return outStr;
    }

    // write output File
    static final void writeFile(String name, String value) throws IOException {
        BufferedWriter fout;
        fout = new BufferedWriter(new FileWriter(name));
        fout.write(value);
        fout.flush();
    }

    // load and parse input, call key generator and write output files
    public static final int KeyMain(String args[]) {
        try {
            if (args.length != 3) {
                return ERC_BAD_ARGS;
            }

            Properties p = new Properties();
            p.load(new FileInputStream(new File(args[0])));

            reg_name = removeDelimiters(p.getProperty("REG_NAME"));
            email = p.getProperty("EMAIL");
            language_id = Integer.valueOf(p.getProperty("LANGUAGE_ID")).intValue();

            try {
                if (generateKey()) {
                    writeFile(args[1], userKey);
                    writeFile(args[2], ccKey);
                }
            } catch (IOException e) {
                return ERC_FILE_IO;
            } catch (NoSuchAlgorithmException e) {
                return ERC_INTERNAL;
            }

            return ERC_SUCCESS;
        } catch (Exception e) {
            return ERC_INTERNAL;
        }
    }

    // call KeyMain method and report exit code
    public static final void main(String args[]) {
        int exitcode;
        System.out.println("ZoeOS Key Generator");
        exitcode = KeyMain(args);
        System.out.println("exit code: " + exitcode);
    }
}
