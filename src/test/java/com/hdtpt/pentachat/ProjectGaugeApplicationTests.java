package com.hdtpt.pentachat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = ProjectGaugeApplication.class)
class ProjectGaugeApplicationTests {

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void contextLoads() {
    }

}
