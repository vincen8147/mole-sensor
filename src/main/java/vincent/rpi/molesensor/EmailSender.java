package vincent.rpi.molesensor;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

public class EmailSender implements EventHandler {

    private String to;
    private String from;
    private String smtpServer;
    private String smtpPort;
    private String subject;
    private String body;
    private String username;
    private String password;
    private long lastSendTime;

    @Override
    public void configure(Map<String, String> configuration) {
        to = configuration.get("to");
        from = configuration.get("from");
        username = configuration.get("username");
        password = configuration.get("password");
        smtpServer = configuration.get("smtpServer");
        smtpPort = configuration.get("smtpPort");
        subject = configuration.get("subject");
        body = configuration.get("body");
    }

    @Override
    public void handleEvent() {
        long now = System.currentTimeMillis();
        if (now - lastSendTime < 60000L) {
            // Don't send too many emails.
            System.out.println("Too soon since last email.");
            return;
        }
        lastSendTime = now;

        // Setup mail server
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(body);

            // Send message
            Transport.send(message);
            System.out.println("Email sent.");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}

