package com.peoples.api.service;

import com.peoples.api.domain.EmailAuthToken;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.EmailAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;

@RequiredArgsConstructor
@Service
public class EmailService {

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private String port;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${spring.mail.password}")
    private String sender_pw;

    private final EmailAuthTokenRepository emailAuthTokenRepository;
    private final SpringTemplateEngine templateEngine;

    // 이메일 인증 토큰 생성
    @Transactional
    public boolean createEmailAuthToken(String userId, String nickname){
        EmailAuthToken emailAuthToken = EmailAuthToken.createEmailAuthToken(userId);
        emailAuthTokenRepository.save(emailAuthToken);

        JavaMailSenderImpl mailSender = getJavaMailSender();

        Context context = new Context();
        context.setVariable("user", nickname);
        context.setVariable("auth",emailAuthToken.getId());
        context.setVariable("authURL", ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam("token", emailAuthToken.getId()).toUriString());
        String html = templateEngine.process("/emailAuthForm", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setSubject("PEOPLES 회원가입 이메일 인증");
            messageHelper.setFrom(sender);
            messageHelper.setTo(userId);
            messageHelper.setText(html,true);

            mailSender.send(message);

            return true;
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    // 유효한 토큰 가져오기
    public Optional<EmailAuthToken> getToken(String emailAuthTokenId){
        Optional<EmailAuthToken> emailAuthToken = emailAuthTokenRepository.findByIdAndExpirationDateAfterAndExpired(emailAuthTokenId, LocalDateTime.now(),false);
        return emailAuthToken;
    }

    // 임시 비밀번호 전송
    public void sendTempPw(String userId, String temp_pw) {
        JavaMailSenderImpl mailSender = getJavaMailSender();

        Context context = new Context();
        context.setVariable("tempPW", temp_pw);
        String html = templateEngine.process("/tempPWForm", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setSubject("PEOPLES 임시 비밀번호");
            messageHelper.setFrom(sender);
            messageHelper.setTo(userId);
            messageHelper.setText(html,true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private JavaMailSenderImpl getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(Integer.parseInt(port));
        mailSender.setUsername(sender);
        mailSender.setPassword(sender_pw);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return mailSender;
    }
}
