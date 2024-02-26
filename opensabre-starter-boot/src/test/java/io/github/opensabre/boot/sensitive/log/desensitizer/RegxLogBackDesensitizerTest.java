package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RegxLogBackDesensitizer.class)
@TestPropertySource(properties = {"opensabre.sensitive.log.rules=idCard,mobile,phone,name,email,address,carLicense"})
class RegxLogBackDesensitizerTest {

    @Resource
    private RegxLogBackDesensitizer regxLogBackDesensitizer;

    @Test
    void desensitizingMobile() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "mobile=13612345678");
        assertEquals("mobile=136****5678", desensitizing);
    }

    @Test
    void desensitizingPhone4WithSeparator() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=0571-12345678");
        assertEquals("phone=057********78", desensitizing);
    }

    @Test
    void desensitizingPhone4WithSeparator7() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=0571-1234567");
        assertEquals("phone=057*******67", desensitizing);
    }

    @Test
    void desensitizingPhone4() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=057112345678");
        assertEquals("phone=057*******78", desensitizing);
    }

    @Test
    void desensitizingPhone47() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=05711234567");
        assertEquals("phone=057******67", desensitizing);
    }

    @Test
    void desensitizingPhone3() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=02912345678");
        assertEquals("phone=029******78", desensitizing);
    }

    @Test
    void desensitizingPhone37() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=0291234567");
        assertEquals("phone=029*****67", desensitizing);
    }

    @Test
    void desensitizingPhone3WithSeparator() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=029-12345678");
        assertEquals("phone=029*******78", desensitizing);
    }

    @Test
    void desensitizingPhone3WithSeparator7() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "phone=029-1234567");
        assertEquals("phone=029******67", desensitizing);
    }

    @Test
    void desensitizingIdCardx() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "id=61231019900909101x");
        assertEquals("id=612310**********1x", desensitizing);
    }

    @Test
    void desensitizingIdCardX() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "id=61231019900909101X");
        assertEquals("id=612310**********1X", desensitizing);
    }

    @Test
    void desensitizingIdCardNum() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "id=612310199009091011");
        assertEquals("id=612310**********11", desensitizing);
    }

    @Test
    void desensitizingEmail() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "email:123@123.com");
        assertEquals("email:12**123.com", desensitizing);
    }

    @Test
    void desensitizingEmailShort() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "email:123@qq.cn");
        assertEquals("email:123@qq.cn", desensitizing);
    }

    @Test
    void desensitizingAddressCity() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "上海市浦东新区上丰路1288号");
        assertEquals("上海*********288号", desensitizing);
    }

    @Test
    void desensitizingAddressProvince() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "陕西省汉中市南郑区汉中街道100号");
        assertEquals("陕西***********100号", desensitizing);
    }

    @Test
    void desensitizingAddressHK() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "中国香港特别行政区大埔区普门路638号");
        assertEquals("中国香************638号", desensitizing);
    }

    @Test
    void desensitizingAddressZhou() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "广西壮族自治区南宁市在四大区上江路10号502室");
        assertEquals("广西******************502室", desensitizing);
    }

    @Test
    void desensitizingAddressTW() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "中国台湾省台北市新竹县桃源村5号天下花园1幢103室");
        assertEquals("中国********************103室", desensitizing);
    }

    @Test
    void desensitizingCarLicense() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "粤AA12345");
        assertEquals("粤A****45", desensitizing);
    }

    @Test
    void desensitizingCarLicenseBus() {
        String desensitizing = regxLogBackDesensitizer.desensitizing(new LoggingEvent(), "沪FE0708");
        assertEquals("沪F***08", desensitizing);
    }
}