package io.github.opensabre.boot.metadata;

/**
 * 获取opensabre cloud相关信息，用于支持云原生多活等
 */
public class OpensabreCloud {
    /**
     * Opensabre云部署AZ环境变量Key
     */
    public static final String OPENSABRE_CLOUD_AZ = "opensabre.cloud.az";
    /**
     * Opensabre云部署REGION环境变量Key
     */
    public static final String OPENSABRE_CLOUD_REGION = "opensabre.cloud.region";
    /**
     * 云部署时环境变量，代表可用区
     */
    public static final String ENV_CLOUD_AZ = "CLOUD_AZ";
    /**
     * 云部署时环境变量，代表地区
     */
    public static final String ENV_CLOUD_REGION = "CLOUD_REGION";

    /**
     * 私有构造方法，不允许初使化实例
     */
    private OpensabreCloud() {
    }

    /**
     * 从环境变量中获取Opensabre部署AZ信息
     *
     * @return 云环境AZ可用区代号
     */
    public static String getCloudAz() {
        return System.getenv(ENV_CLOUD_AZ);
    }

    /**
     * 从环境变量中获取Opensabre部署REGION信息
     *
     * @return 云环境REGION地域
     */
    public static String getCloudRegion() {
        return System.getenv(ENV_CLOUD_REGION);
    }
}
