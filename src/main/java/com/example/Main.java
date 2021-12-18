/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@SpringBootApplication
public class Main {

  @Value("${spring.rabbitmq.queue:emailQueue}")
  private String queue;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private JavaMailSender emailSender;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<String> index() {

    rabbitTemplate.convertAndSend(queue, "AMQP Message OK!!");

    return ResponseEntity.ok("OK Sucesso!!");
  }

  @RequestMapping(method = RequestMethod.GET, value = "/api/email/send", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<String> sendEmail(
          @RequestParam String emailTo,
          @RequestParam String subject,
          @RequestParam String message) {

    final EmailModel emailModel = EmailModel.builder()
            .emailTo(emailTo)
            .subject(subject)
            .message(message)
            .build();

    rabbitTemplate.convertAndSend(queue, emailModel);

    return ResponseEntity.ok("OK Email Sucesso!!");
  }

  @RabbitListener(queues = "${spring.rabbitmq.queue:emailQueue}")
  public void listen(EmailModel emailModel) {

    System.out.println("Email Message send TO : " + emailModel.getEmailTo());

    try {

      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(emailModel.getEmailTo());
      message.setSubject(emailModel.getSubject());
      message.setText(emailModel.getMessage());

      emailSender.send(message);

    } catch (Exception e){
      e.printStackTrace();
      throw e;
    }
  }

  @Bean
  public Queue myQueue() {
    return new Queue(queue, true);
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

}
