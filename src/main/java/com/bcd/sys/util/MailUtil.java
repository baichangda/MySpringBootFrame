package com.bcd.sys.util;

import com.bcd.base.exception.BaseRuntimeException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;


/**
 * 如果存在发送附件乱码是因为spring会自动截取过长的附件名称、需要设置如下
 * System.setProperty("mail.mime.splitlongparameters","false");
 */
@Component
public class MailUtil {

    static JavaMailSender mailSender;
    static String from;

    static {
        System.setProperty("mail.mime.splitlongparameters", "false");
    }

    public static void sendSimple(Consumer<SimpleMailMessage> consumer) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        consumer.accept(simpleMailMessage);
        MailUtil.mailSender.send(simpleMailMessage);

    }

    public static void sendMime(Consumer<MimeMessageHelper> consumer) {
        try {
            MimeMessage mimeMessage = MailUtil.mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            mimeMessageHelper.setFrom(from);
            consumer.accept(mimeMessageHelper);
            MailUtil.mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw BaseRuntimeException.get(e);
        }
    }

    @Autowired
    public void setMailSender(JavaMailSender mailSender, @Value(value = "${spring.mail.username}") String from) {
        MailUtil.mailSender = mailSender;
        MailUtil.from = from;
    }
}
