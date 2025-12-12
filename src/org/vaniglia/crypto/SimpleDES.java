/**
 * Project Vaniglia
 * User: Michele Aiello
 *
 * Copyright (C) 2003/2007  Michele Aiello
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.vaniglia.crypto;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

import javax.crypto.spec.DESKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;

public class SimpleDES {

    private String password;

    public SimpleDES(String password) {
        this.password = password;
    }

    public String encrypt(String input) {

        String encrypted = null;

        try {
            DESKeySpec pbeKeySpec;
            SecretKeyFactory keyFac;

            byte[] pass = password.getBytes();

            pbeKeySpec = new DESKeySpec(pass);
            keyFac = SecretKeyFactory.getInstance("DES");
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher desCipher;

            desCipher = Cipher.getInstance("DES");

            desCipher.init(Cipher.ENCRYPT_MODE, pbeKey);

            byte[] msgbytes = input.getBytes("UTF8");
            byte[] enc = desCipher.doFinal(msgbytes);

            encrypted = new BASE64Encoder().encode(enc);
        } catch (Exception e) {
        }

        return encrypted;
    }

    public String decrypt(String input) {

        String decrypted = null;

        try {
            DESKeySpec pbeKeySpec;
            SecretKeyFactory keyFac;

            byte[] pass = password.getBytes();
            pbeKeySpec = new DESKeySpec(pass);
            keyFac = SecretKeyFactory.getInstance("DES");
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher desCipher;

            desCipher = Cipher.getInstance("DES");
            byte[] dec = new BASE64Decoder().decodeBuffer(input);

            desCipher.init(Cipher.DECRYPT_MODE, pbeKey);

            byte[] cleartext = desCipher.doFinal(dec);

            decrypted = new String(cleartext);
        } catch (Exception e) {
        }

        return decrypted;
    }

}
