package com.supercom.puretrack.util.encryption;

import android.os.Build;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {

    public static byte[] NewDecrypt(byte[] AESKey, byte[] encrypted) throws Exception {
        byte[] decrypted;
        SecretKeySpec skeySpec = new SecretKeySpec(AESKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        } else {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivParam = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParam);
        }
        decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

}
