package com.br.marketing.check.service.Impl.email;

import com.br.marketing.check.service.email.IMailService;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

/**
 *
 */
@Slf4j
@Service
public class MailServiceImpl implements IMailService {
    /**
     * Spring Boot 提供了一个发送邮件的简单抽象，使用的是下面这个接口，这里直接注入即可使用
     */
    @Autowired
    private JavaMailSender mailSender;
    /**
     * 配置文件中我的qq邮箱
     */
    @Value("${spring.mail.username}")
    private String from;

    static {
        System.setProperty("mail.mime.splitlongparameters","false");
    }

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        //创建SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件发送人
        message.setFrom(from);
        //邮件接收人
        message.setTo(to);
        //邮件主题
        message.setSubject(subject);
        //邮件内容
        message.setText(content);
        //发送邮件
        mailSender.send(message);
    }

    @Override
    public void sendHtmlMail(String toStr, String subject, String content) {
        //获取MimeMessage对象
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        if (StringUtils.isEmpty(toStr)) {
            return;
        }
        String[] toArray = toStr.split(",");
        for (String to : toArray) {
            try {
                messageHelper = new MimeMessageHelper(message, true);
                //邮件发送人
                messageHelper.setFrom(from);
                //邮件接收人
                messageHelper.setTo(to);
                //邮件主题
                message.setSubject(subject);
                //邮件内容，html格式
                messageHelper.setText(content, true);
                //发送
                mailSender.send(message);
                //日志信息
                log.info("发送邮件给：{}。", to);
            } catch (Exception e) {
                log.error("发送邮件给：{} 时发生异常！", to, e);
            }
        }
    }

    @Override
    public void sendGroupHtmlMail(String toStr, String subject, String content) {
        //获取MimeMessage对象
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        if (StringUtils.isEmpty(toStr)) {
            return;
        }
        String[] toArray = toStr.split(",");
        try {
            messageHelper = new MimeMessageHelper(message, true);
            //邮件发送人
            messageHelper.setFrom(from);
            //邮件接收人
            messageHelper.setTo(toArray);
            //邮件主题
            message.setSubject(subject);
            //邮件内容，html格式
            messageHelper.setText(content, true);
            //发送
            mailSender.send(message);
            //日志信息
            log.info("发送邮件给：{}。", toStr);
        } catch (Exception e) {
            log.error("发送邮件给：{} 时发生异常！", toStr, e);
        }
    }

    @Override
    public void sendAttachmentsMail(String toStr, String subject, String content, String filePath) {
        if (StringUtils.isEmpty(toStr)) {
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        String[] toArray = toStr.split(",");
        for (String to : toArray) {
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom(from);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, true);

                FileSystemResource file = new FileSystemResource(new File(filePath));
                String fileName = filePath.substring(filePath.lastIndexOf(File.separator)+1);
                if(log.isInfoEnabled()){
                    log.info("attachmentFilename:[{}]", fileName);
                }
                helper.addAttachment(fileName, file);
                mailSender.send(message);
                if(log.isInfoEnabled()){
                    //日志信息
                    log.info("邮件已经发送。");
                }
            } catch (MessagingException e) {
                log.error("subject[{}]filePath[{}]to[{}]发送邮件时发生异常！", subject, filePath, to, e);
            }
        }
    }

    @Override
    public void sendAttachmentsMail(String toStr, String subject, String content, String filePath, String attachmentFilename) {
        if (StringUtils.isEmpty(toStr)) {
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        String[] toArray = toStr.split(",");
        for (String to : toArray) {
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");
                helper.setFrom(from);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, true);

                FileSystemResource file = new FileSystemResource(new File(filePath));
                helper.addAttachment(attachmentFilename, file);
                mailSender.send(message);

            } catch (MessagingException e) {
                log.error("发送邮件时发生异常！", e);
            }
        }
        //日志信息
        log.warn("邮件已经发送。toArray:{}",toStr);
    }

    @Override
    public void sendAttachmentsMail2(String to, String cc, String subject, String content, String filePath, String attachmentFilename) {
        if (StringUtils.isEmpty(to)) {
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");
            helper.setFrom(from);
            helper.setTo(to.split(","));
            if (StringUtils.isNotEmpty(cc)){
                helper.setCc(cc.split(","));
            }
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment(attachmentFilename, file);
            mailSender.send(message);
            //日志信息
            log.info("邮件已经发送。to:{},cc:{}",to,cc);
        } catch (MessagingException e) {
            log.error("发送邮件时发生异常！", e);
        }
    }
}
