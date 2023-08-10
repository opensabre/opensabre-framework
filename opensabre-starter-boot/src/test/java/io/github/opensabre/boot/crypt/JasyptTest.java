package io.github.opensabre.boot.crypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JasyptTest {

    @Test
    public void jasyptTest() {
        //加密工具
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //生成秘钥的公钥
        encryptor.setPassword("Pa55w0rd@opensabre");
        //加密
        String ciphertext = encryptor.decrypt("yLMBDZMhsJziMcceIT1IWw==");
        assertEquals("123456", ciphertext);
    }
}
