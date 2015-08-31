package in.foodmash.app;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class Cryptography {

    private static SecretKeySpec secretKey ;
    private static byte[] key ;

    private static void setKey(String myKey) throws UnsupportedEncodingException {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
            secretKey = new SecretKeySpec(key, "AES");
            new String(key,"UTF-8");
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
    }

    public static String encrypt(String strToEncrypt, String key) {
        try {
            setKey(key);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encode(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {  System.out.println("Error while encrypting: "+e.toString()); }
        return null;
    }

    public static String decrypt(String strToDecrypt, String key) {
        try {
            setKey(key);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.decode(strToDecrypt)));
        } catch (Exception e) { System.out.println("Error while decrypting: "+e.toString()); }
        return null;
    }
}