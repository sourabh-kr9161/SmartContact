package com.smart.service;

//import java.net.PasswordAuthentication;
import java.util.Properties;
import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;


@Service
public class EmailService {
     public boolean sendEmail(String subject, String message,String to)
     {
    	 //rest of code..
    	 boolean f=false;
    	 String from="sourabhkr9161@gmail.com";
    	 
    	 //variable for gmail
    	 String host="smtp.gmail.com";
    	 
    	 //get the system properties
    	 Properties properties = System.getProperties();
    	 System.out.println("PROPERTIES" +properties);
    	 
    	 //setting important information to properties object
    	 
    	 //host set
    	 properties.put("mail.smtp.host", host);
    	 properties.put("mail.smtp.port","465");
    	 properties.put("mail.smtp.ssl.enable","true");
    	 properties.put("mail.smtp.auth", "true");
    	 
    	 //step 1: to get the session object...
    	 Session session=Session.getInstance(properties,new Authenticator() {
    		 @Override
    		 protected PasswordAuthentication getPasswordAuthentication() {
    			 return new PasswordAuthentication("sourabhkr9161@gmail.com", "9161699283");
    		 } 
    		 
    	 }); 
    	 session.setDebug(true);
    	 
    	 //step 2: compose the message[text,multi media]
    	 MimeMessage m=new MimeMessage(session);
    	 try {
    		 //from email
    		 m.setFrom(from);
    		 
    		 //adding recipient to message
    		 m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
    		 
    		 //adding subject to message
    		 m.setSubject(subject);
    		 
    		 //adding text to message
    		// m.setText(message);
    		 m.setContent(message,"text/html");    		 
    		 //send
    		 
    		 //step 3: send the message using Transport class
    		 Transport.send(m);
    		 System.out.println("sent success......");
    		 f=true;
    		 
    	 }
    	 catch(Exception e) {
    		 e.printStackTrace();
    	 }
    	 return f;
    	 
    	 
    	 
    	 
    	 
    	 
    	 
    	 
    	 
    	 
     }
}
