package com.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
public class EmailService {

    @Value("${app.email.from}")
    private String fromEmail;

    private final SesClient sesClient;

    public EmailService() {
        this.sesClient = SesClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public void sendEmail(
            String to,
            String subject,
            String body) {

        SendEmailRequest request =
                SendEmailRequest.builder()
                        .source(fromEmail)
                        .destination(
                                Destination.builder()
                                        .toAddresses(to)
                                        .build())
                        .message(
                                Message.builder()
                                        .subject(Content.builder()
                                                .data(subject)
                                                .build())
                                        .body(
                                                Body.builder()
                                                        .text(
                                                                Content.builder()
                                                                        .data(body)
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();

        sesClient.sendEmail(request);
    }
}