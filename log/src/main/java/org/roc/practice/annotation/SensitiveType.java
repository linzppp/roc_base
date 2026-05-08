package org.roc.practice.annotation;

public enum SensitiveType {
    /**
     * 手机号：保留前3位和后4位，中间替换为 ****
     * 示例：138****8888
     */
    PHONE,

    /**
     * 邮箱：保留本地部分首字符和完整域名，中间替换为 ***
     * 示例：a***@example.com
     */
    EMAIL,

    /**
     * 密码/密钥等高敏感字段：固定替换为 ***，不透露原始长度
     */
    PASSWORD
}
