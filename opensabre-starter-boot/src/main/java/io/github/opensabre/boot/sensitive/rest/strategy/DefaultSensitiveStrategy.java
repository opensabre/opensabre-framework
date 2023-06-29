package io.github.opensabre.boot.sensitive.rest.strategy;

import cn.hutool.core.util.DesensitizedUtil;
import io.github.opensabre.boot.sensitive.rest.DesensitizationTypeEnum;
import org.apache.commons.lang3.StringUtils;

public class DefaultSensitiveStrategy implements SensitiveStrategy {

    /**
     * 脱敏处理
     *
     * @param str 原字符
     * @return 脱敏后的字符
     */
    public String desensitizing(DesensitizationTypeEnum type, String str) {
        switch (type) {
            // userId脱敏
            case USER_ID:
                return String.valueOf(DesensitizedUtil.userId());
            // 中文姓名脱敏
            case CHINESE_NAME:
                return DesensitizedUtil.chineseName(String.valueOf(str));
            // 身份证脱敏
            case ID_CARD:
                return DesensitizedUtil.idCardNum(String.valueOf(str), 1, 2);
            // 固定电话脱敏
            case FIXED_PHONE:
                return DesensitizedUtil.fixedPhone(String.valueOf(str));
            // 手机号脱敏
            case MOBILE_PHONE:
                return DesensitizedUtil.mobilePhone(String.valueOf(str));
            // 地址脱敏
            case ADDRESS:
                return DesensitizedUtil.address(String.valueOf(str), 8);
            // 邮箱脱敏
            case EMAIL:
                return DesensitizedUtil.email(String.valueOf(str));
            // 密码脱敏
            case PASSWORD:
                return DesensitizedUtil.password(String.valueOf(str));
            // 中国车牌脱敏
            case CAR_LICENSE:
                return DesensitizedUtil.carLicense(String.valueOf(str));
            // 银行卡脱敏
            case BANK_CARD:
                return DesensitizedUtil.bankCard(String.valueOf(str));
            default:
                return StringUtils.EMPTY;
        }
    }
}