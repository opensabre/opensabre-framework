package io.github.opensabre.boot.version;

/**
 * 获取opensabre version
 */
public class OpensabreVersion {

    private OpensabreVersion() {
    }

    public static String getVersion() {
        return OpensabreVersion.class.getPackage().getImplementationVersion();
    }

    public static String getVersionString() {
        return " (v" + getVersion() + ")";
    }
}
