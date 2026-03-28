package patcher;


import org.apache.kafka.clients.consumer.*;
import java.util.*;
import java.time.Duration;
import java.io.File;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;

public class IssueConsumer {

    static ObjectMapper mapper = new ObjectMapper();
    static File file = new File("issues.json");

    // Save issues to JSON file
    private static void saveToJson(Map<String, Object> issue) throws Exception {
        List<Map<String, Object>> issues = new ArrayList<>();

        if (file.exists()) {
            issues = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
        }

        issues.add(issue);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, issues);
    }

    // Send Email
    private static void sendEmailAlert(String messageText) {

        final String fromEmail = "your_email@gmail.com";
        final String password = "your_app_password"; // NOT your real password (https://myaccount.google.com/apppasswords)
        final String toEmail = "receiver_email@gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(toEmail));
            message.setSubject("Critical Issue Detected");
            message.setText("A critical issue occurred:\n\n" + messageText);

            Transport.send(message);

            System.out.println("Email Sent Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "issue-monitor");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Arrays.asList("issues-log"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                String json = record.value();
                System.out.println("New Issue: " + json);

                try {
                    // Convert JSON string → Map
                    Map<String, Object> issue = mapper.readValue(json, new TypeReference<>() {});
                    saveToJson(issue);

                    // Check severity
                    String severity = issue.get("severity").toString();
                    if (severity.equalsIgnoreCase("Critical")) {
                        sendEmailAlert(json);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}