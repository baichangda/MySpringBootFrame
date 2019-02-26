package com.bcd.sys.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {

    static JavaMailSender mailSender;

    static String from;

    @Autowired
    public void setMailSender(JavaMailSender mailSender, @Value(value = "${spring.mail.username}")String from) {
        MailUtil.mailSender = mailSender;
        MailUtil.from=from;
    }

    public static void send(String to, String subject, String content){
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(content);
        MailUtil.mailSender.send(simpleMailMessage);
    }
}
