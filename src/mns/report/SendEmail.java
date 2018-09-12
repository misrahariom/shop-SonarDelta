package mns.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmail {

	public void sendEmail(String html, String emailTo, Properties prop) throws javax.mail.internet.AddressException {

		String to = prop.getProperty("emailDefaultTo");
		if (!"".equalsIgnoreCase(emailTo)) {
			to = emailTo;
		}
		String cc = prop.getProperty("emailCC");
		String replyTo = prop.getProperty("emailReplyTo");
		InternetAddress[] iAdressArray = null;
		InternetAddress[] ccArray = null;
		InternetAddress[] rToArray = null;
		
			iAdressArray = InternetAddress.parse(to);
			ccArray = InternetAddress.parse(cc);
			rToArray = InternetAddress.parse(replyTo);
		
		// Sender's email ID needs to be mentioned
		String from = prop.getProperty("emailFrom");

		// Assuming you are sending email from localhost
		String host = prop.getProperty("emailHost");

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			// message.addRecipient(Message.RecipientType.TO, new
			// InternetAddress(to));
			message.setRecipients(Message.RecipientType.TO, iAdressArray);
			message.setRecipients(Message.RecipientType.CC, ccArray);
			message.setReplyTo(rToArray);

			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			dateFormat.format(cal.getTime());

			// Set Subject: header field
			message.setSubject(prop.getProperty("emailSubject") + dateFormat.format(cal.getTime()));

			// Send the actual HTML message, as big as you like
			// message.setContent(html, "text/html" );

			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Now set the actual message
			messageBodyPart.setContent(html, "text/html");

			// Create a multipar message
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			String filename = prop.getProperty("attchmentFilePath");
			
			DataSource source = new FileDataSource(filename);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(prop.getProperty("attchmentFileName"));
			multipart.addBodyPart(messageBodyPart);

			// Send the complete message parts
			message.setContent(multipart);

			// Send message
			Transport.send(message);
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

}
