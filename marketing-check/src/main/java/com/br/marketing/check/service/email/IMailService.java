package com.br.marketing.check.service.email;

/**
 * 发送邮件接口
 */
public interface IMailService {
    /**
     * 发送文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendHtmlMail(String to, String subject, String content);

    /**
     * 发送HTML邮件通过群发
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendGroupHtmlMail(String to, String subject, String content);

    /**
     * 发送带附件的邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param filePath 附件
     */
    void sendAttachmentsMail(String to, String subject, String content, String filePath);

    /**
     * 发送带附件的邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param filePath 附件
     */
    void sendAttachmentsMail(String to, String subject, String content, String filePath,String attachmentFilename);

    /**
     * 发送带附件的邮件
     * @param to 收件人
     * @param cc 抄送人
     * @param subject 主题
     * @param content 内容
     * @param filePath 附件
     */
    void sendAttachmentsMail2(String to,String cc, String subject, String content, String filePath,String attachmentFilename);
}

