package com.bcd.sys.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.function.Consumer;

@Component
public class MailUtil {

    static JavaMailSender mailSender;

    static String from;

    @Autowired
    public void setMailSender(JavaMailSender mailSender, @Value(value = "${spring.mail.username}")String from) {
        MailUtil.mailSender = mailSender;
        MailUtil.from=from;
    }

    public static void sendSimple(Consumer<SimpleMailMessage> consumer){
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        consumer.accept(simpleMailMessage);
        MailUtil.mailSender.send(simpleMailMessage);

    }

    public static void sendMime(Consumer<MimeMessageHelper> consumer){
        try {
            MimeMessage mimeMessage= MailUtil.mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper=new MimeMessageHelper(mimeMessage);
            mimeMessageHelper.setFrom(from);
            consumer.accept(mimeMessageHelper);
            MailUtil.mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
