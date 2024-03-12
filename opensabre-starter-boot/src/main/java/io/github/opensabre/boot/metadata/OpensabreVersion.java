package io.github.opensabre.boot.metadata;

/**
 * 获取opensabre版本信息
 */
public class OpensabreVersion {
    /**
     * Opensabre版本号环境变量Key
     */
    public static final String OPENSABRE_VERSION = "opensabre.version";
    /**
     * Opensabre完整版本号环境变量Key
     */
    public static final String OPENSABRE_FORMATTED_VERSION = "opensabre.formatted-version";
    /**
     * 私有构造方法，不允许初使化实例
     */
    private OpensabreVersion() {
    }

    /**
     * 获取Opensabre版本号
     *
     * @return 版本号 如：1.x
     */
    public static String getVersion() {
        return OpensabreVersion.class.getPackage().getImplementationVersion();
    }

    /**
     * 获取Opensabre完整版本号
     *
     * @return 版本号，如：(v1.x.x)
     */
    public static String getVersionString() {
        return " (v" + getVersion() + ")";
    }
}
