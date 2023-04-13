package curso.api.rest.service;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class ServiceEnviaEmail {
	
	/*E-mail padrão para envio*/
	private String userName = "fbcvendaspro@gmail.com";
	private String senha = "jgoxlxwytmcuqzid";
	
	/*método para o envio de e-mail*/
	public void enviarEmail (String assunto, String emailDestino, String menssagem) throws Exception {
		
		Properties properties = new Properties();
		
		properties.put("mail.smtp.ssl.trust", "*"); 
		properties.put("mail.smtp.ssl.checkserveridentity", "true"); 
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, senha);
			}
		});
		
		Address[] toUser = InternetAddress.parse(emailDestino);
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(userName)); /*Quem está enviando*/
		message.setRecipients(Message.RecipientType.TO, toUser); /*Para quem vai o e-mail*/
		message.setSubject(assunto); /*Assunto do e-mail*/
		message.setText(menssagem);
		
		Transport.send(message); /*Envia a mensagem*/
	}
}
