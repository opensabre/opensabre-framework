package io.github.opensabre.boot.sensitive.rule;

import java.util.regex.Pattern;

/**
 * 简单的脱敏策略
 *
 * @author zhoutaoo
 */
@SuppressWarnings("unused")
public enum DefaultSensitiveRule implements SensitiveRule {
    /**
     * 自定义
     */
    CUSTOM("custom", 0, 0),
    /**
     * 账号类脱敏策略
     */
    ACCOUNT_NO("accountNo", 3, 2),
    /**
     * 姓名类脱敏策略
     */
    NAME("name", "([^\\u4e00-\\u9fa5])([\\u4e00-\\u9fa5])([\\u4e00-\\u9fa5]{1,3})([^\\u4e00-\\u9fa5])", 1, 0),
    /**
     * 身份证号类脱敏策略
     */
    ID_CARD("idCard", "(\\d{6})(19|20\\d{9})([Xx])", 6, 2),
    /**
     * 银行卡号
     */
    BANK_CARD("bankCard", "([3-6]\\d{3})(\\d{8,12})(\\d{4})", 4, 3),
    /**
     * 车牌号
     */
    CAR_LICENSE("carLicense", "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|(DF[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})", 3, 4),
    /**
     * 邮箱类脱敏策略
     */
    EMAIL("email", "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}", 2, 7),
    /**
     * 手机号码类脱敏策略
     */
    MOBILE("mobile", "(13[0-9]|14[01456789]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])(\\d{8})", 3, 4),
    /**
     * 中国固定电话
     */
    PHONE("phone", "^(0[1-9]\\d{2})(-)\\d{7,8}$", 3, 4),
    /**
     * 密码
     */
    PASSWORD("password", "((?i)password|pass|passwd|secret|key|credential|token)\\s*(?:((?i)is)|[:：=<>]+)\\s*(\\S+)", 0, 0),
    /**
     * 住址类
     */
    ADDRESS("address", "([\\u4e00-\\u9fa5]{2,5}[省|市|自治区|行政区])([\\u4e00-\\u9fa5]{2,}[市|地区|自治州|行政单位|县])([\\u4e00-\\u9fa5]{2,}[市|区|市辖区|县|旗|镇|岛|海域])([\\u4e00-\\u9fa5]+)", 3, 4);

    private final String category;

    private final Pattern pattern;

    private final int retainPrefixCount;

    private final int retainSuffixCount;

    private char replaceChar = '*';

    DefaultSensitiveRule(String category, String pattern, int retainPrefixCount, int retainSuffixCount, char replaceChar) {
        this.category = category;
        this.pattern = Pattern.compile(pattern);
        this.retainPrefixCount = retainPrefixCount;
        this.retainSuffixCount = retainSuffixCount;
        this.replaceChar = replaceChar;
    }

    DefaultSensitiveRule(String category, String pattern, int retainPrefixCount, int retainSuffixCount) {
        this.category = category;
        this.pattern = Pattern.compile(pattern);
        this.retainPrefixCount = retainPrefixCount;
        this.retainSuffixCount = retainSuffixCount;
    }

    DefaultSensitiveRule(String category, int retainPrefixCount, int retainSuffixCount) {
        this.category = category;
        this.pattern = Pattern.compile("\\*");
        this.retainPrefixCount = retainPrefixCount;
        this.retainSuffixCount = retainSuffixCount;
    }

    DefaultSensitiveRule(String category, String pattern) {
        this.category = category;
        this.pattern = Pattern.compile(pattern);
        this.retainPrefixCount = 0;
        this.retainSuffixCount = 0;
    }

    @Override
    public String category() {
        return this.category;
    }

    @Override
    public Pattern pattern() {
        return this.pattern;
    }

    @Override
    public int retainPrefixCount() {
        return this.retainPrefixCount;
    }

    @Override
    public int retainSuffixCount() {
        return this.retainSuffixCount;
    }

    @Override
    public char replaceChar() {
        return this.replaceChar;
    }
}
