package io.github.opensabre.boot.crypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.junit.jupiter.api.Test;

public class JasyptTest {

    @Test
    public void jasyptTest() {
        //加密工具
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        //加密配置
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        //算法类型
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        //生成秘钥的公钥
        config.setPassword("Pa55w0rd@opensabre");
        //应用配置
        encryptor.setConfig(config);
        //加密
        String ciphertext = encryptor.decrypt("3iIE/7wbUTBB0g9WuUHIFbduqiLHafNVnB+Oo+SP6uFGntypXzyZsd1SPlwJchgv");
        System.out.println(ciphertext);
    }
}
