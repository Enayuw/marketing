package com.br.marketing.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * 发送邮件工具类
 *
 * @author tp
 *
 */
@Slf4j
public class SendMailClint {

    /**
     * 发送带附件的邮件
     *
     * @param receive  收件人
     * @param subject  邮件主题
     * @param msg      邮件内容
     * @param filename 附件地址
     * @return
     * @throws GeneralSecurityException
     */
    public static boolean sendMail(String receive, String subject, String msg, String path,String filename)
            throws GeneralSecurityException {
        if (StringUtils.isEmpty(receive)) {
            return false;
        }
        // 发件人电子邮箱
        final String from = "xiaoxin.pang@brgroup.com";
        // 发件人电子邮箱密码
        final String pass = "afwafeQFE231%";
        // 指定发送邮件的主机为 smtp.qq.com邮件服务器
        String host = "smtp.brgroup.com";
        // 获取系统属性
        Properties props = System.getProperties();
        // 邮件发送协议
        props.setProperty("mail.transport.protocol", "smtp");
        //SMTP邮件服务器
        props.put("mail.smtp.host", host);
        // 需要验证用户名密码
        props.put("mail.smtp.auth", "true");
        //SMTP邮件服务器默认端口
        props.put("mail.smtp.port", "465");
        //是否启用调试模式（启用调试模式可打印客户端与服务器交互过程时一问一答的响应消息）
        props.setProperty("mail.debug","true");
        // 获取默认session对象
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            /**
             * 邮箱服务器账户、第三方登录授权码
             *  发件人邮件用户名、密码
             * @return
             */
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);
            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));
            InternetAddress[] internetAddressTo = new InternetAddress().parse(receive);
            // Set To: 头部头字段
            message.addRecipients(Message.RecipientType.TO, internetAddressTo);
            // Set Subject: 主题文字
            message.setSubject(subject);
            // 创建消息部分
            BodyPart messageBodyPart = new MimeBodyPart();
            // 消息
            messageBodyPart.setDataHandler(new DataHandler(msg,"text/html;charset=UTF-8"));
            //messageBodyPart.setText(msg);
            // 创建多重消息
            Multipart multipart = new MimeMultipart();
            // 设置文本消息部分
            multipart.addBodyPart(messageBodyPart);
            // 附件部分
            messageBodyPart = new MimeBodyPart();
            // 设置要发送附件的文件路径
            DataSource source = new FileDataSource(path+filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            // 处理附件名称中文（附带文件路径）乱码问题
            messageBodyPart.setFileName(MimeUtility.encodeText(filename));
            multipart.addBodyPart(messageBodyPart);
            // 发送完整消息
            message.setContent(multipart);
            // 发送消息
            Transport.send(message);
            return true;
        } catch (Exception e) {
            log.error("发送邮件出错",e);
        }
        return false;
    }

}