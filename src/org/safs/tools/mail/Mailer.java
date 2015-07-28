/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.safs.IndependantLog;

/**
 * Used to send email.
 * 
 * Examples:<br>
 * <ol>
 * <LI>java org.safs.tools.logs.Mailer -h<br>
 * to get help information of this program
 * <li>java org.safs.tools.logs.Mailer -to user@sas.com -sub "Don not  reply this email!" -msg "Helooooo mail coming" -type text/plain<br>
 * send to user a plain text "Helooooo mail coming" with subject "Don not  reply this email!"
 * <li>java org.safs.tools.logs.Mailer -to user@sas.com -sub "Don not  reply this email!" -msg "&lt;h1>Helooooo mail coming&lt;/h1>" -type text/html<br>
 * send to user an html text "&lt;h1>Helooooo mail coming&lt;/h1>" with subject "Don not  reply this email!"
 * <li>java org.safs.tools.logs.Mailer -to user1@sas.com;user2@sas.com -sub "Don not  reply this email!" -msg "Helooooo mail coming" -type text/plain<br>
 * send to user1 and user2 a plain text "Helooooo mail coming" with subject "Don not  reply this email!"
 * <li>java org.safs.tools.logs.Mailer -to user1@sas.com -cc user2@sas.com -sub "Don not  reply this email!" -msg "Helooooo mail coming" -type text/plain<br>
 * send to user1 a plain text "Helooooo mail coming" with subject "Don not  reply this email!", at the same time this email will be cc to user2
 * <li>java org.safs.tools.logs.Mailer -to user@sas.com -sub "Don not  reply this email!" -msg "Helooooo mail coming" -type text/plain -attach "C:\yourpath\afile.txt"<br>
 * send to user a plain text "Helooooo mail coming" with subject "Don not  reply this email!" with attached file as "afile.txt"
 * <li>java org.safs.tools.logs.Mailer -to  user@sas.com -sub "Don not  reply this email!" -msg "Helooooo mail coming" -type text/plain -attach "C:\yourpath\afile.txt=alias.txt"<br>
 * send to user a plain text "Helooooo mail coming" with subject "Don not  reply this email!" with attached file as "alias.txt"
 * <li>java org.safs.tools.logs.Mailer -to user@sas.com -sub "Don not  reply this email!" -msg "Helooooo mail coming" -type text/plain –attach "C:\yourpath\test.bat=test.bat.remove;C:\safs\lib\safsmail.jar=mail.jar.removeme;C:\safs\lib\windowsclassmap.dat"<br>
 * send to user a plain text "Helooooo mail coming" with subject "Don not  reply this email!" with multiple attached file as "test.bat.remove", "mail.jar.removeme" and "windowsclassmap.dat"
 * </ol>
 * 
 * @author (LeiWang)
 * <br> JUN,15 2015 Added SendMail call for and misc fixes.
 */
public class Mailer {
	
	//****************************** Used by main() **************************************/
	private static String param_host = "-host";
	private static String param_port = "-port";
	private static String param_protocol = "-protocol";
	
	private static String param_user = "-user";
	private static String param_pass = "-pass";
	
	private static String param_subject = "-subject";
	private static String param_sub = "-sub";

	/**the sender of this email, example user@company */
	private static String param_from = "-from";
	
	/**
	 * accept semi-colon separated string, example user1@company1;user2@company2
	 * @see #handleRecipients(String, List)
	 */
	private static String param_to = "-to";
	/**
	 * accept semi-colon separated string, example user1@company1;user2@company2
	 * @see #handleRecipients(String, List)
	 */
	private static String param_cc = "-cc";
	/**
	 * accept semi-colon separated string, example user1@company1;user2@company2
	 * @see #handleRecipients(String, List)
	 */
	private static String param_bcc = "-bcc";

	/**the message to send out*/
	private static String param_msg_head = "-msghead";
	private static String param_msg_object = "-msg";
	private static String param_msg_file = "-msgfile";
	private static String param_msg_foot = "-msgfoot";
	/**accept one of {@link MimeType#contentType}*/
	private static String param_msg_type = "-type";
	
	private static String param_help = "-help";
	private static String param_h = "-h";
	
	/**
	 * Accept semi-colon(;) separated string, there are the files to attach<BR>
	 * Each semi-colon separated token can be equal(=) separated string, 1th is file name, 2th is alias name
	 * used as attached file name.<br>
	 * Example:<BR>
	 * <UL>
	 * <LI>c:\folder\file.txt;d:\directory\building.jpg
	 * <LI>c:\folder\file.txt=alias.txt;d:\directory\compress.zip=compress.zip.unzipme
	 * </UL>
	 */
	private static String param_attachments = "-attachment";
	private static String param_attach = "-attach";
	
	public static String SEPARATOR_SEMI_COLON = ";";
	public static String SEPARATOR_EQUAL = "=";
	//****************************** Used by main() **************************************/
	
	public static final String DEFAULT_HOST = ""; 
	public static final int DEFAULT_PORT = 25;
	/** {@link Protocol#SMTP}*/
	public static final Protocol DEFAULT_PROTOCOL = Protocol.SMTP;
	public static boolean debug = false;

	private String sender = "noreply@sas.com";
	
	private Properties props = new Properties();
	private Authenticator auth = null;
	private Session session = null;
	
	/**
	 * Create a Mailer
	 * with default host {@value #DEFAULT_HOST}, port {@value #DEFAULT_PORT} and protocol {@link #DEFAULT_PROTOCOL}, 
	 * without Authentication (no need of user/password)
	 * @throws Exception
	 */
	public Mailer() throws Exception{
		prepareProperties(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PROTOCOL);
		getSession();
	}
	
	/**
	 * Create a Mailer
	 * with host, port and protocol, 
	 * without Authentication (no need of user/password)
	 * @param host , String, the host name of the mail server.
	 * @param port , int, the port number the mailer service is listening to.
	 * @param protocol , Protocol, the mail service's protocol
	 * @throws Exception
	 */
	public Mailer(String host, int port, Protocol protocol) throws Exception{
		prepareProperties(host, port, protocol);
		getSession();
	}
	
	/**
	 * Create a Mailer
	 * with default host {@value #DEFAULT_HOST}, port {@value #DEFAULT_PORT} and protocol {@link #DEFAULT_PROTOCOL}, 
	 * with Authentication (user/password)
	 * @param user ,	String, the user name used to login.
	 * @param password , String, the password of the user.
	 * @throws Exception
	 */
	public Mailer(String user, String password) throws Exception{
		prepareProperties(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PROTOCOL);
		prepareProperties(user, password);
		getSession();
	}

	/**
	 * Create a Mailer with Authentication (user/password) and host/port/protocol
	 * @param host , String, the host name of the mail server.
	 * @param port , int, the port number the mailer service is listening to.
	 * @param protocol , Protocol, the mail service's protocol
	 * @param user ,	String, the user name used to login.
	 * @param password , String, the password of the user.
	 * @throws Exception
	 */
	public Mailer(String host, int port, Protocol protocol, String user, String password) throws Exception{
		prepareProperties(host, port, protocol);
		prepareProperties(user, password);
		getSession();
	}
	
	/**
	 * Prepare the Properties {@link #props} for creating java mail session.
	 * @param host , String, the host name of the mail server.
	 * @param port , int, the port number the mailer service is listening to.
	 * @param protocol , Protocol, the mail service's protocol
	 * @see #getSession()
	 */
	private void prepareProperties(String host, int port, Protocol protocol){
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		switch (protocol) {
		case SMTPS:
			props.put("mail.smtp.ssl.enable", true);
			break;
		case TLS:
			props.put("mail.smtp.starttls.enable", true);
			break;
		}
	}
	
	/**
	 * Prepare the Properties {@link #props} for creating java mail session.
	 * @param user ,	String, the user name used to login.
	 * @param password , String, the password of the user.
	 * @see #getSession()
	 */
	private void prepareProperties(final String user, final String password){
		props.put("mail.smtp.auth", true);
		auth = new Authenticator() {
			private PasswordAuthentication pa = new PasswordAuthentication(user, password);
			public PasswordAuthentication getPasswordAuthentication() {
				return pa;
			}
		};
	}
	
	/**
	 * <em>Note: Before calling this method, we must call {@link #prepareProperties(String, String)} and {@link #prepareProperties(String, int, Protocol)}.</em>
	 * @return a java mail Session
	 * @see #prepareProperties(String, int, Protocol)
	 * @see #prepareProperties(String, String)
	 */
	protected Session getSession() throws Exception{
		if(session==null) session = Session.getInstance(props, auth);
		if(session==null) throw new Exception("Cannot get session!!!");
		
		session.setDebug(debug);
		return session;
	}
	
	/**
	 * Set the mail sender.
	 * @param sender
	 */
	public void setSender(String sender){
		if(sender!=null && sender.indexOf("@")>0){
			this.sender = sender;
		}
	}
	
	/**
	 * Prepare the MimeMessage for sending.
	 * 
	 * @param recipients , List<String>, a list of recipients (TO)
	 * @param subject , String, the mail's subject
	 * @return the MimeMessage
	 * @see #send(List, String, String)
	 * @see #send(List, String, String, MimeType)
	 * @see #send(List, String, String, MimeType, String) 
	 * 
	 * @throws Exception
	 */
	public MimeMessage prepareMimeMessage(List<String> recipients, String subject) throws Exception{
		if(session==null) throw new Exception("The mail Session is null!");
		return Mailer.prepareMimeMessage(session, recipients, sender, subject);
	}
	
	/**
	 * Prepare the MimeMessage for sending.
	 * 
	 * @param type_recipients , HashMap<Message.RecipientType, List<String>>
	 * @param subject , String, the mail's subject
	 * @return
	 * @see #send(List, String, String)
	 * @see #send(List, String, String, MimeType)
	 * @see #send(List, String, String, MimeType, String)
	 *  
	 * @throws Exception
	 */
	protected MimeMessage prepareMimeMessage(HashMap<Message.RecipientType, List<String>> type_recipients, String subject) throws Exception{
		if(session==null) throw new Exception("The mail Session is null!");
		return Mailer.prepareMimeMessage(session, type_recipients, sender, subject);
	}
	protected static MimeMessage prepareMimeMessage(Session session, List<String> recipients, String from, String subject) throws Exception{
		HashMap<Message.RecipientType, List<String>> type_recipients = new HashMap<Message.RecipientType, List<String>>();
		type_recipients.put(Message.RecipientType.TO, recipients);
		return Mailer.prepareMimeMessage(session, type_recipients, from, subject);
	}
	protected static MimeMessage prepareMimeMessage(Session session, HashMap<Message.RecipientType, List<String>> type_recipients, String from, String subject) throws Exception{
		String debugmsg = Mailer.class+".prepareMimeMessage(): ";
		MimeMessage message = new MimeMessage(session);
		
	    try {
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
			message.setSentDate(new Date());
			
			InternetAddress[] addresses = null;
			List<String> receievers = null;
			Set<Message.RecipientType> types = type_recipients.keySet();
			for(Message.RecipientType type:types){
				receievers = type_recipients.get(type);
				if(receievers!=null && !receievers.isEmpty()){
					addresses = new InternetAddress[receievers.size()];
					for(int i=0;i<receievers.size();i++){
						addresses[i] = new InternetAddress(receievers.get(i));
					}
					message.setRecipients(type, addresses);
				}else{
					continue;
				}
			}

	    } catch (Exception e) {
			IndependantLog.debug(debugmsg+"Met exception "+e.getClass().getName()+":"+e.getMessage(), e);
			throw new Exception(debugmsg+"Met exception "+e.getClass().getName()+":"+e.getMessage());
		}
	    
	    return message;
	}
	
	/**
	 * Mail text message.
	 * 
	 * @param recipients , List<String>, a list of recipients
	 * @param subject , String, the mail's subject
	 * @param body , String, the text message to mail.
	 */
	public void send(List<String> recipients, String subject, String body){
		try {
			MimeMessage message = prepareMimeMessage(recipients, subject);
		    message.setText(body);	         
		    Transport.send(message);
		    
		    IndependantLog.debug("Sent text message successfully....");
		} catch (Exception ex) {
		    IndependantLog.error("Fail to send mail.", ex);
		}
	}
	
	/**
	 * Mail object content, the content's type is defined by parameter format.
	 * 
	 * @param recipients , List<String>, a list of recipients
	 * @param subject , String, the mail's subject
	 * @param body , Object, the object to mail.
	 * @param type , the MimeType of the mail body.
	 */
	public void send(List<String> recipients, String subject, Object body, MimeType type){
		try {
		    MimeMessage message = prepareMimeMessage(recipients, subject);
	        message.setContent(body, type.contentType);
		    Transport.send(message);
		    IndependantLog.debug("Sent '"+type.contentType+"' message successfully....");
		} catch (Exception ex) {
			IndependantLog.error("Fail to send mail.", ex);
		}
	}
	
	/**
	 * Mail object, the content's type is defined by parameter format.<br>
	 * It will also send attachments provided by the last parameter 'attachment'.<br>
	 * 
	 * @param recipients , List<String>, a list of recipients
	 * @param subject , String, the mail's subject
	 * @param body , Object, the object to mail.
	 * @param type , the MimeType of the mail body.
	 * @param attachment , String, a  full path file name to attach
	 */
	public void send(List<String> recipients, String subject, Object body, MimeType type,
			String/*full path file name*/ attachment){
		List<String> files = new ArrayList<String>();
		files.add(attachment);
		send(recipients, subject, body, type, files);
	}
	/**
	 * Mail object, the content's type is defined by parameter format.<br>
	 * It will also send attachments provided by the last parameter 'attachments'.<br>
	 * 
	 * @param recipients , List<String>, a list of recipients
	 * @param subject , String, the mail's subject
	 * @param body , Object, the object to mail.
	 * @param type , the MimeType of the mail body.
	 * @param attachments , List<String>, a list of full path file name to attach
	 */
	public void send(List<String> recipients, String subject, Object body, MimeType type,
			List<String/*full path file name*/> attachments){
		
		HashMap<Integer/*the message order*/, MimeContent> contents = new HashMap<Integer, MimeContent>();
		HashMap<String/*full path file name*/,String/*alias*/> attchments_alias = new HashMap<String,String>();
		
		contents.put(new Integer(0), new MimeContent(body,type));
		if(attachments!=null){
			for(String file:attachments){
				attchments_alias.put(file, null);
			}
		}
		send(recipients, subject, contents, attchments_alias);
	}
	
	/**
	 * Mail an object, the content's type is defined by MimeType.<br>
	 * It will also send attachments provided by the last parameter 'attchments_alias'.<br>
	 * 
	 * @param recipients , List<String>, a list of recipients
	 * @param subject , String, the mail's subject
	 * @param body , Object, the object to mail.
	 * @param type , the MimeType of the mail body.
	 * @param attchments_alias , HashMap<String//full path file name,String//alias>
	 */	
	public void send(List<String> recipients, String subject, Object body, MimeType type,
			HashMap<String/*full path file name*/,String/*alias*/> attchments_alias){
		
		HashMap<Integer/*the message order*/, MimeContent> contents = new HashMap<Integer, MimeContent>();
		contents.put(new Integer(0), new MimeContent(body,type));
		
		send(recipients, subject, contents, attchments_alias);
	}
	
	/**
	 * Mail a set of objects, the content's type is defined by MimeType.<br>
	 * It will also send attachments provided by the last parameter 'attchments_alias'.<br>
	 * 
	 * @param recipients , List<String>, a list of recipients
	 * @param subject , String, the mail's subject
	 * @param contents , HashMap<Integer//message order, MimeContent>
	 *                   only the first element will be sent as main message, other will be sent as attachment.
	 * @param attchments_alias , HashMap<String//full path file name,String//alias>
	 */
	public void send(List<String> recipients, String subject, 
			HashMap<Integer/*the message order*/, MimeContent> contents,
			HashMap<String/*full path file name*/,String/*alias*/> attchments_alias){
		HashMap<Message.RecipientType, List<String>> type_recipients = new HashMap<Message.RecipientType, List<String>>();
		type_recipients.put(Message.RecipientType.TO, recipients);
		this.send(type_recipients, subject, contents, attchments_alias);
	}
	
	/**
	 * Mail a set of objects, the content's type is defined by MimeType.<br>
	 * It will also send attachments provided by the last parameter 'attchments_alias'.<br>
	 * 
	 * @param type_recipients , HashMap<Message.RecipientType, List<String>>, a map of recipients(to, cc, bcc)
	 * @param subject , String, the mail's subject
	 * @param contents , HashMap<Integer//message order, MimeContent>, 
	 *                   only the first element will be sent as main message, other will be sent as attachment.
	 * @param attchments_alias , HashMap<String//full path file name,String//alias>
	 */
	public void send(HashMap<Message.RecipientType, List<String>> type_recipients, String subject, 
			HashMap<Integer/*the message order*/, MimeContent> contents,
			HashMap<String/*full path file name*/,String/*alias*/> attchments_alias){
		try {
			MimeMessage message = prepareMimeMessage(type_recipients, subject);
						
			// Create a multiple-part message
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = null;
			MimeContent mimeContent = null;
			
			//First parts are object messages
			if(contents!=null && !contents.isEmpty()){
				Set<Integer> orders = contents.keySet();
				for(Integer order:orders){
					mimeContent = contents.get(order);
					if(mimeContent!=null && mimeContent.isValid()){
						messageBodyPart = new MimeBodyPart();
						messageBodyPart.setContent(mimeContent.object, mimeContent.type.contentType);
						multipart.addBodyPart(messageBodyPart);
					}else{
						continue;
					}
				}
			}else{
				IndependantLog.debug("No content.");
			}
			
			//Other Parts are attachments
			if(attchments_alias!=null && !attchments_alias.isEmpty()){
				Set<String> files = attchments_alias.keySet();
				File attchedFile = null;
				String alias = null;
				for(String filename:files){
					attchedFile = new File(filename);
					if(attchedFile.exists() && attchedFile.isFile()){
						messageBodyPart = new MimeBodyPart();
						DataSource source = new FileDataSource(filename);
						messageBodyPart.setDataHandler(new DataHandler(source));
						//Attached file name shown in email
						alias = attchments_alias.get(filename);
						messageBodyPart.setFileName((alias!=null? alias:attchedFile.getName()));
						multipart.addBodyPart(messageBodyPart);
					}else{
						IndependantLog.debug(filename+" doesn't exist or is not a file.");
						continue;
					}
				}
			}else{
				IndependantLog.debug("No attachment.");
			}
			
			if(multipart.getCount()>0){
				message.setContent(multipart );
				Transport.send(message);
				IndependantLog.debug("Sent objects with attachments successfully....");
			}else{
				IndependantLog.debug("Nothing to send, return successfully....");
			}
			
		} catch (Exception ex) {
			IndependantLog.error("Fail to send mail.", ex);
		}
	}
	
	/**
	 * @param recipients ,String , semi-colon separated string, example user1@company1;user2@company2
	 * @param list (out), List<String>, a list of email addresses.
	 */
	public static void handleRecipients(String recipients, List<String> list){
		try{
			if(recipients==null){
				IndependantLog.warn("parameter recipients is null.");
				return;
			}
			String[] tokens = recipients.split(SEPARATOR_SEMI_COLON);
			String recipient = null;
			for(String temp:tokens){
				recipient = temp.trim();
				if(!recipient.isEmpty()){
					list.add(recipient);
				}
			}
		}catch(Exception e){
			IndependantLog.error("Fail to handle recipients.", e);
		}
	}
	
	/**
	 * 
	 * @param attachments ,String , semi-colon separated string, example c:\folder\file.txt=alias.txt;d:\directory\compress.zip=compress.zip.unzipme
	 * @param attachments_alias (out), HashMap<String,String>, map containing a pair(filename, alias)
	 */
	public static void handleAttachments(String attachments, HashMap<String,String> attachments_alias){
		try{
			String[] tokens = attachments.split(SEPARATOR_SEMI_COLON);
			String attachment = null;
			String[] attachAlias = null;
			for(String temp:tokens){
				attachment = temp.trim();
				if(!attachment.isEmpty()){
					attachAlias = attachment.split(SEPARATOR_EQUAL);
					if(attachAlias.length==1){
						attachments_alias.put(attachAlias[0], null);
					}else if(attachAlias.length==2){
						attachments_alias.put(attachAlias[0], attachAlias[1]);
					}else{
						IndependantLog.warn("Error attachment format '"+attachment+"'");
						continue;
					}
				}
			}
		}catch(Exception e){
			IndependantLog.error("Fail to handle recipients.", e);
		}
	}
	
	/**
	 * 
	 * @param message , String, the message to send
	 * @param msg_type , MimeType, the message type
	 * @param contents (out), HashMap<Integer, MimeContent>, the HashMap to hold message to send.
	 *                        only the first element will be sent as main message, other will be sent as attachment.
	 * 
	 */
	public static void addMessag(String message, MimeType msg_type, HashMap<Integer, MimeContent> contents){
		try{
			if(message==null){
				IndependantLog.debug("The message is null, cannot send it.");
				return;
			}
			MimeContent content = new Mailer.MimeContent(message, msg_type);
			Integer order = new Integer(contents.size());
			contents.put(order, content);
		}catch(Exception e){
			IndependantLog.error("Fail to handle message.", e);
		}
	}
	
	public static void main(String[] args){
		Mailer mailer = null;
		List<String> recipientsTo = new ArrayList<String>();
		List<String> recipientsCc = new ArrayList<String>();
		List<String> recipientsBcc = new ArrayList<String>();
		HashMap<Message.RecipientType,List<String>> recipients = new HashMap<Message.RecipientType,List<String>>();
		HashMap<String,String> attachments_alias = new HashMap<String,String>();
		
		String host = null;
		String portStr = null;
		String protocolStr = null;
		
		String user = null;
		String pass = null;
		
		String subject = "Mailer sending message.";

		/**the sender of this email, example user@company */
		String from = null;
		
		/**accept semi-colon separated string, example user1@company1;user2@company2 */
		String to = null;
		/**accept semi-colon separated string, example user1@company1;user2@company2 */
		String cc = null;
		/**accept semi-colon separated string, example user1@company1;user2@company2 */
		String bcc = null;

		/**the message to send out*/
		//TODO HANDLE HEAD PARAMETER, HAS PROBLEM , HTML PLAIN
		String msg_head = null;
		String msg_object = "";
		//TODO HANDLE FILE PARAMETER
		String msg_file = null;
		//TODO HANDLE FOOT PARAMETER, HAS PROBLEM , HTML PLAIN
		String msg_foot = null;
		/**accept one of {@link MimeType}*/
		MimeType msg_type = MimeType.txt;
		
		/**accept semi-colon separated string, example c:\folder\file.txt;d:\directory\building.jpg */
		String attach = null;
		
		String usage = "\n" +
					   "java org.safs.tools.logs.Mailer parameters\n\n" +
					   "["+param_host+" mailServerName ]\n"+
					   "["+param_from+" from@mail.address] \n"+
					   "["+param_to+" to1@mail.address;to2@mail.address] \n" +
					   "["+param_subject+" mailSubject] \n"+
					   "["+param_msg_object+" emailMessage] \n"+ 
					   "["+param_attachments+" attachedfile1[=alias1];attachedfile2[=alias2]] \n\n\n"+
		
					   
					   "------ Extra parameters are as followings: ------\n" +
					   "["+param_host+" mailServerName "+param_port+" portNumber "+param_protocol+" protocol] \n" +
				       "["+param_user+" userMailAddress "+param_pass+" password] \n" +
				       "["+param_subject+" mailSubject] \n"+
				       "["+param_from+" from@mail.address] \n"+
				           param_to+" to1@mail.address;to2@mail.address \n" +
				       "["+param_cc+" cc1@mail.address;cc2@mail.address] \n" +
				       "["+param_bcc+" bcc1@mail.address;bcc2@mail.address] \n" +
				       "["+param_msg_head+" header_message] \n"+
				       "["+param_msg_object+" message ["+param_msg_type+" mimetype]] \n"+
				       "["+param_msg_file+" message_file ["+param_msg_type+" mimetype]] \n"+
				       "["+param_msg_foot+" footer_message] \n"+
					   "["+param_attachments+" attachedfile1[=alias1];attachedfile2[=alias2]] \n";
		
		try {
			for(int i=0;i<args.length;i++){
				if(param_host.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						host = args[++i].trim();
					}
					if(host==null || host.isEmpty()){
						System.out.println("ERROR: Missing mail server name!");
						System.out.println(usage);
						return;
					}
				}else if(param_port.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						portStr = args[++i].trim();
					}
					if(portStr==null || portStr.isEmpty()){
						System.out.println("ERROR: Missing mail server port number!");
						System.out.println(usage);
						return;
					}
				}else if(param_protocol.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						protocolStr = args[++i].trim();
					}
					if(protocolStr==null || protocolStr.isEmpty()){
						System.out.println("ERROR: Missing mail server protocol!");
						System.out.println(usage);
						return;
					}
				}else if(param_user.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						user = args[++i].trim();
					}
					if(user==null || user.isEmpty()){
						System.out.println("ERROR: Missing user name!");
						System.out.println(usage);
						return;
					}
				}else if(param_pass.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						pass = args[++i];
					}
					if(pass==null || pass.isEmpty()){
						System.out.println("ERROR: Missing user password!");
						System.out.println(usage);
						return;
					}
				}else if(param_subject.equalsIgnoreCase(args[i]) ||
						 param_sub.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						subject = args[++i].trim();
					}
					if(subject==null || subject.isEmpty()){
						System.out.println("WARNING: Missing mail subject!");
					}
				}else if(param_from.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						from = args[++i].trim();
					}
					if(from==null || from.isEmpty()){
						System.out.println("WARNING: Missing mail sender!");
					}
				}else if(param_to.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						to = args[++i].trim();
					}
					if(to==null || to.isEmpty()){
						System.out.println("ERROR: Missing mail 'to' recepients");
						System.out.println(usage);
						return;
					}
				}else if(param_cc.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						cc = args[++i].trim();
					}
					if(cc==null || cc.isEmpty()){
						System.out.println("ERROR: Missing mail 'cc' recepients");
						System.out.println(usage);
						return;
					}
				}else if(param_bcc.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						bcc = args[++i].trim();
					}
					if(bcc==null || bcc.isEmpty()){
						System.out.println("ERROR: Missing mail 'bcc' recepients");
						System.out.println(usage);
						return;
					}
				}else if(param_msg_head.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						msg_head = args[++i].trim();
					}
					if(msg_head==null || msg_head.isEmpty()){
						System.out.println("WARNING: Missing mail header message!");
					}
				}else if(param_msg_object.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						msg_object = args[++i].trim();
					}
					if(msg_object==null || msg_object.isEmpty()){
						System.out.println("ERROR: Missing mail content message!");
						System.out.println(usage);
						return;
					}
				}else if(param_msg_file.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						msg_file = args[++i].trim();
					}
					if(msg_file==null || msg_file.isEmpty()){
						System.out.println("ERROR: Missing mail content file name!");
						System.out.println(usage);
						return;
					}
				}else if(param_msg_foot.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						msg_foot = args[++i].trim();
					}
					if(msg_foot==null || msg_foot.isEmpty()){
						System.out.println("WARNING: Missing mail footer message!");
					}
				}else if(param_msg_type.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						try{ msg_type = MimeType.get(args[++i].trim()); }catch(ParseException e){}
					}
					if(msg_type==null){
						System.out.println("WARNING: Missing mail content mime type! The content will be considered as '"+MimeType.txt.contentType+"'");
					}
				}else if(param_attachments.equalsIgnoreCase(args[i]) ||
						 param_attach.equalsIgnoreCase(args[i])){
					if(i+1<args.length){
						attach = args[++i].trim();
					}
					if(attach==null || attach.isEmpty()){
						System.out.println("WARNING: Missing mail attached file list! No attachemnt will be sent.");
					}
				}else if(param_help.equalsIgnoreCase(args[i]) ||
						 param_h.equalsIgnoreCase(args[i])){
					System.out.println(usage);
					return;
				}else{
					IndependantLog.error("WARNING: Unknown parameter '"+args[i]+"'");
				}
			}

			//recipients to is required
			if(to==null || to.isEmpty()){
				System.out.println("Missing mail 'to' recepients, you must provide it!");
				System.out.println(usage);
				return;
			}

			//Handle host, port, protocol, user, password
			if(host!=null && portStr!=null && protocolStr!=null && user!=null && pass!=null){
				try{
					int port = Integer.parseInt(portStr);
					Protocol protocol = Protocol.get(protocolStr);
					mailer = new Mailer(host, port, protocol, user, pass);
				}catch(Exception e){
					IndependantLog.error("Fail create Mailer.", e);
					mailer = new Mailer(user, pass);
				}
			}else if(host !=null && portStr !=null){
				int port = Integer.parseInt(portStr);
				Protocol protocol = Protocol.get("SMTP");
				mailer = new Mailer(host, port, protocol);
			}else if(user!=null && pass!=null){
				mailer = new Mailer(user, pass);
			}else{
				mailer = new Mailer();
			}
			
			//Set the sender
			mailer.setSender(from);
					
			//Handle the recipients
			handleRecipients(to, recipientsTo);
			handleRecipients(cc, recipientsCc);
			handleRecipients(bcc, recipientsBcc);
			recipients.put(Message.RecipientType.TO, recipientsTo);
			recipients.put(Message.RecipientType.CC, recipientsCc);
			recipients.put(Message.RecipientType.BCC, recipientsBcc);
			
			//Handle attachments
			handleAttachments(attach, attachments_alias);
			
			//Handle message header, message footer, message content and its type
			HashMap<Integer/*the message order*/, MimeContent> contents = new HashMap<Integer/*the message order*/, MimeContent>();
			String newline = "\r\n";
			if(MimeType.txt.contentType.equalsIgnoreCase(msg_type.contentType)){
				newline = "\r\n";
			}else if(MimeType.htm.contentType.equalsIgnoreCase(msg_type.contentType)){
				newline = "<br>";
			}
			if(msg_head!=null){
				msg_object = msg_head+newline+msg_object;
			}
			if(msg_foot!=null){
				msg_object = msg_object+newline+msg_foot;
			}
			addMessag(msg_object, msg_type, contents);
			
			//Finally, send out the message and attachments
			mailer.send(recipients, subject, contents , attachments_alias);
			System.out.println("Email send successfully");			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static class MimeContent{
		public Object object;
		public MimeType type;
		
		public MimeContent(Object object, MimeType type) {
			super();
			this.object = object;
			this.type = type;
		}
		
		public boolean isValid(){
			boolean valid = object!=null && type!=null && type.contentType!=null;
			return valid;
		}
	}
	
	public static enum Protocol {
		SMTP, SMTPS, TLS;
		
		public static Protocol get(String name) throws ParseException{
			if("SMTP".equalsIgnoreCase(name)){
				return SMTP;
			}else if("SMTPS".equalsIgnoreCase(name)){
				return SMTPS;
			}else if("TLS".equalsIgnoreCase(name)){
				return TLS;
			}
			
			throw new ParseException("Unknown protocol name '"+name+"'");
		}
	}
	
	
	public static enum MimeType {
	    $323 ("text/h323"),
	    $3gp ("video/3gpp"),
	    $7z ("application/x-7z-compressed"),
	    abw ("application/x-abiword"),
	    ai ("application/postscript"),
	    aif ("audio/x-aiff"),
	    aifc ("audio/x-aiff"),
	    aiff ("audio/x-aiff"),
	    alc ("chemical/x-alchemy"),
	    art ("image/x-jg"),
	    asc ("text/plain"),
	    asf ("video/x-ms-asf"),
	    $asn ("chemical/x-ncbi-asn1"),
	    asn ("chemical/x-ncbi-asn1-spec"),
	    aso ("chemical/x-ncbi-asn1-binary"),
	    asx ("video/x-ms-asf"),
	    atom ("application/atom"),
	    atomcat ("application/atomcat+xml"),
	    atomsrv ("application/atomserv+xml"),
	    au ("audio/basic"),
	    avi ("video/x-msvideo"),
	    bak ("application/x-trash"),
	    bat ("application/x-msdos-program"),
	    b ("chemical/x-molconn-Z"),
	    bcpio ("application/x-bcpio"),
	    bib ("text/x-bibtex"),
	    bin ("application/octet-stream"),
	    bmp ("image/x-ms-bmp"),
	    book ("application/x-maker"),
	    boo ("text/x-boo"),
	    bsd ("chemical/x-crossfire"),
	    c3d ("chemical/x-chem3d"),
	    cab ("application/x-cab"),
	    cac ("chemical/x-cache"),
	    cache ("chemical/x-cache"),
	    cap ("application/cap"),
	    cascii ("chemical/x-cactvs-binary"),
	    cat ("application/vnd.ms-pki.seccat"),
	    cbin ("chemical/x-cactvs-binary"),
	    cbr ("application/x-cbr"),
	    cbz ("application/x-cbz"),
	    cc ("text/x-c++src"),
	    cdf ("application/x-cdf"),
	    cdr ("image/x-coreldraw"),
	    cdt ("image/x-coreldrawtemplate"),
	    cdx ("chemical/x-cdx"),
	    cdy ("application/vnd.cinderella"),
	    cef ("chemical/x-cxf"),
	    cer ("chemical/x-cerius"),
	    chm ("chemical/x-chemdraw"),
	    chrt ("application/x-kchart"),
	    cif ("chemical/x-cif"),
	    $class ("application/java-vm"),
	    cls ("text/x-tex"),
	    cmdf ("chemical/x-cmdf"),
	    cml ("chemical/x-cml"),
	    cod ("application/vnd.rim.cod"),
	    com ("application/x-msdos-program"),
	    cpa ("chemical/x-compass"),
	    cpio ("application/x-cpio"),
	    cpp ("text/x-c++src"),
	    $cpt ("application/mac-compactpro"),
	    cpt ("image/x-corelphotopaint"),
	    crl ("application/x-pkcs7-crl"),
	    crt ("application/x-x509-ca-cert"),
	    csf ("chemical/x-cache-csf"),
	    $csh ("application/x-csh"),
	    csh ("text/x-csh"),
	    csm ("chemical/x-csml"),
	    csml ("chemical/x-csml"),
	    css ("text/css"),
	    csv ("text/csv"),
	    ctab ("chemical/x-cactvs-binary"),
	    c ("text/x-csrc"),
	     ctx ("chemical/x-ctx"),
	    cu ("application/cu-seeme"),
	    cub ("chemical/x-gaussian-cube"),
	    cxf ("chemical/x-cxf"),
	    cxx ("text/x-c++src"),
	    dat ("chemical/x-mopac-input"),
	    dcr ("application/x-director"),
	    deb ("application/x-debian-package"),
	    diff ("text/x-diff"),
	    dif ("video/dv"),
	    dir ("application/x-director"),
	    djv ("image/vnd.djvu"),
	    djvu ("image/vnd.djvu"),
	    dll ("application/x-msdos-program"),
	    dl ("video/dl"),
	    dmg ("application/x-apple-diskimage"),
	    dms ("application/x-dms"),
	    doc ("application/msword"),
	    dot ("application/msword"),
	    d ("text/x-dsrc"),
	    dvi ("application/x-dvi"),
	    dv ("video/dv"),
	    dx ("chemical/x-jcamp-dx"),
	    dxr ("application/x-director"),
	    emb ("chemical/x-embl-dl-nucleotide"),
	    embl ("chemical/x-embl-dl-nucleotide"),
	    eml ("message/rfc822"),
	    $ent ("chemical/x-ncbi-asn1-ascii"),
	    ent ("chemical/x-pdb"),
	    eps ("application/postscript"),
	    etx ("text/x-setext"),
	    exe ("application/x-msdos-program"),
	    ez ("application/andrew-inset"),
	    fb ("application/x-maker"),
	    fbdoc ("application/x-maker"),
	    fch ("chemical/x-gaussian-checkpoint"),
	    fchk ("chemical/x-gaussian-checkpoint"),
	    fig ("application/x-xfig"),
	    flac ("application/x-flac"),
	    fli ("video/fli"),
	    fm ("application/x-maker"),
	    frame ("application/x-maker"),
	    frm ("application/x-maker"),
	    gal ("chemical/x-gaussian-log"),
	    gam ("chemical/x-gamess-input"),
	    gamin ("chemical/x-gamess-input"),
	    gau ("chemical/x-gaussian-input"),
	    gcd ("text/x-pcs-gcd"),
	    gcf ("application/x-graphing-calculator"),
	    gcg ("chemical/x-gcg8-sequence"),
	    gen ("chemical/x-genbank"),
	    gf ("application/x-tex-gf"),
	    gif ("image/gif"),
	    gjc ("chemical/x-gaussian-input"),
	    gjf ("chemical/x-gaussian-input"),
	    gl ("video/gl"),
	    gnumeric ("application/x-gnumeric"),
	    gpt ("chemical/x-mopac-graph"),
	    gsf ("application/x-font"),
	    gsm ("audio/x-gsm"),
	    gtar ("application/x-gtar"),
	    hdf ("application/x-hdf"),
	    hh ("text/x-c++hdr"),
	    hin ("chemical/x-hin"),
	    hpp ("text/x-c++hdr"),
	    hqx ("application/mac-binhex40"),
	    hs ("text/x-haskell"),
	    hta ("application/hta"),
	    htc ("text/x-component"),
	    $h ("text/x-chdr"),
	    html ("text/html"),
	    htm ("text/html"),
	    hxx ("text/x-c++hdr"),
	    ica ("application/x-ica"),
	    ice ("x-conference/x-cooltalk"),
	    ico ("image/x-icon"),
	    ics ("text/calendar"),
	    icz ("text/calendar"),
	    ief ("image/ief"),
	    iges ("model/iges"),
	    igs ("model/iges"),
	    iii ("application/x-iphone"),
	    inp ("chemical/x-gamess-input"),
	    ins ("application/x-internet-signup"),
	    iso ("application/x-iso9660-image"),
	    isp ("application/x-internet-signup"),
	    ist ("chemical/x-isostar"),
	    istr ("chemical/x-isostar"),
	    jad ("text/vnd.sun.j2me.app-descriptor"),
	    jar ("application/java-archive"),
	    java ("text/x-java"),
	    jdx ("chemical/x-jcamp-dx"),
	    jmz ("application/x-jmol"),
	    jng ("image/x-jng"),
	    jnlp ("application/x-java-jnlp-file"),
	    jpeg ("image/jpeg"),
	    jpe ("image/jpeg"),
	    jpg ("image/jpeg"),
	    js ("application/x-javascript"),
	    kar ("audio/midi"),
	    key ("application/pgp-keys"),
	    kil ("application/x-killustrator"),
	    kin ("chemical/x-kinemage"),
	    kml ("application/vnd.google-earth.kml+xml"),
	    kmz ("application/vnd.google-earth.kmz"),
	    kpr ("application/x-kpresenter"),
	    kpt ("application/x-kpresenter"),
	    ksp ("application/x-kspread"),
	    kwd ("application/x-kword"),
	    kwt ("application/x-kword"),
	    latex ("application/x-latex"),
	    lha ("application/x-lha"),
	    lhs ("text/x-literate-haskell"),
	    lsf ("video/x-la-asf"),
	    lsx ("video/x-la-asf"),
	    ltx ("text/x-tex"),
	    lyx ("application/x-lyx"),
	    lzh ("application/x-lzh"),
	    lzx ("application/x-lzx"),
	    $m3u ("audio/mpegurl"),
	    m3u ("audio/x-mpegurl"),
	    $m4a ("audio/mpeg"),
	    m4a ("video/mp4"),
	    m4b ("video/mp4"),
	    m4v ("video/mp4"),
	    maker ("application/x-maker"),
	    man ("application/x-troff-man"),
	    mcif ("chemical/x-mmcif"),
	    mcm ("chemical/x-macmolecule"),
	    mdb ("application/msaccess"),
	    me ("application/x-troff-me"),
	    mesh ("model/mesh"),
	    mid ("audio/midi"),
	    midi ("audio/midi"),
	    mif ("application/x-mif"),
	    mm ("application/x-freemind"),
	    mmd ("chemical/x-macromodel-input"),
	    mmf ("application/vnd.smaf"),
	    mml ("text/mathml"),
	    mmod ("chemical/x-macromodel-input"),
	    mng ("video/x-mng"),
	    moc ("text/x-moc"),
	    mol2 ("chemical/x-mol2"),
	    mol ("chemical/x-mdl-molfile"),
	    moo ("chemical/x-mopac-out"),
	    mop ("chemical/x-mopac-input"),
	    mopcrt ("chemical/x-mopac-input"),
	    movie ("video/x-sgi-movie"),
	    mov ("video/quicktime"),
	    mp2 ("audio/mpeg"),
	    mp3 ("audio/mpeg"),
	    mp4 ("video/mp4"),
	    mpc ("chemical/x-mopac-input"),
	    mpega ("audio/mpeg"),
	    mpeg ("video/mpeg"),
	    mpe ("video/mpeg"),
	    mpga ("audio/mpeg"),
	    mpg ("video/mpeg"),
	    ms ("application/x-troff-ms"),
	    msh ("model/mesh"),
	    msi ("application/x-msi"),
	    mvb ("chemical/x-mopac-vib"),
	    mxu ("video/vnd.mpegurl"),
	    nb ("application/mathematica"),
	    nc ("application/x-netcdf"),
	    nwc ("application/x-nwc"),
	    o ("application/x-object"),
	    oda ("application/oda"),
	    odb ("application/vnd.oasis.opendocument.database"),
	    odc ("application/vnd.oasis.opendocument.chart"),
	    odf ("application/vnd.oasis.opendocument.formula"),
	    odg ("application/vnd.oasis.opendocument.graphics"),
	    odi ("application/vnd.oasis.opendocument.image"),
	    odm ("application/vnd.oasis.opendocument.text-master"),
	    odp ("application/vnd.oasis.opendocument.presentation"),
	    ods ("application/vnd.oasis.opendocument.spreadsheet"),
	    odt ("application/vnd.oasis.opendocument.text"),
	    oga ("audio/ogg"),
	    ogg ("application/ogg"),
	    ogv ("video/ogg"),
	    ogx ("application/ogg"),
	    old ("application/x-trash"),
	    otg ("application/vnd.oasis.opendocument.graphics-template"),
	    oth ("application/vnd.oasis.opendocument.text-web"),
	    otp ("application/vnd.oasis.opendocument.presentation-template"),
	    ots ("application/vnd.oasis.opendocument.spreadsheet-template"),
	    ott ("application/vnd.oasis.opendocument.text-template"),
	    oza ("application/x-oz-application"),
	    p7r ("application/x-pkcs7-certreqresp"),
	    pac ("application/x-ns-proxy-autoconfig"),
	    pas ("text/x-pascal"),
	    patch ("text/x-diff"),
	    pat ("image/x-coreldrawpattern"),
	    pbm ("image/x-portable-bitmap"),
	    pcap ("application/cap"),
	    pcf ("application/x-font"),
	    pcx ("image/pcx"),
	    pdb ("chemical/x-pdb"),
	    pdf ("application/pdf"),
	    pfa ("application/x-font"),
	    pfb ("application/x-font"),
	    pgm ("image/x-portable-graymap"),
	    pgn ("application/x-chess-pgn"),
	    pgp ("application/pgp-signature"),
	    php3 ("application/x-httpd-php3"),
	    php3p ("application/x-httpd-php3-preprocessed"),
	    php4 ("application/x-httpd-php4"),
	    php ("application/x-httpd-php"),
	    phps ("application/x-httpd-php-source"),
	    pht ("application/x-httpd-php"),
	    phtml ("application/x-httpd-php"),
	    pk ("application/x-tex-pk"),
	    pls ("audio/x-scpls"),
	    pl ("text/x-perl"),
	    pm ("text/x-perl"),
	    png ("image/png"),
	    pnm ("image/x-portable-anymap"),
	    pot ("text/plain"),
	    ppm ("image/x-portable-pixmap"),
	    pps ("application/vnd.ms-powerpoint"),
	    ppt ("application/vnd.ms-powerpoint"),
	    prf ("application/pics-rules"),
	    prt ("chemical/x-ncbi-asn1-ascii"),
	    ps ("application/postscript"),
	    psd ("image/x-photoshop"),
	    p ("text/x-pascal"),
	    pyc ("application/x-python-code"),
	    pyo ("application/x-python-code"),
	    py ("text/x-python"),
	    qtl ("application/x-quicktimeplayer"),
	    qt ("video/quicktime"),
	    $ra ("audio/x-pn-realaudio"),
	    ra ("audio/x-realaudio"),
	    ram ("audio/x-pn-realaudio"),
	    rar ("application/rar"),
	    ras ("image/x-cmu-raster"),
	    rd ("chemical/x-mdl-rdfile"),
	    rdf ("application/rdf+xml"),
	    rgb ("image/x-rgb"),
	    rhtml ("application/x-httpd-eruby"),
	    rm ("audio/x-pn-realaudio"),
	    roff ("application/x-troff"),
	    ros ("chemical/x-rosdal"),
	    rpm ("application/x-redhat-package-manager"),
	    rss ("application/rss+xml"),
	    rtf ("application/rtf"),
	    rtx ("text/richtext"),
	    rxn ("chemical/x-mdl-rxnfile"),
	    sct ("text/scriptlet"),
	    sd2 ("audio/x-sd2"),
	    sda ("application/vnd.stardivision.draw"),
	    sdc ("application/vnd.stardivision.calc"),
	    sd ("chemical/x-mdl-sdfile"),
	    sdd ("application/vnd.stardivision.impress"),
	    $sdf ("application/vnd.stardivision.math"),
	    sdf ("chemical/x-mdl-sdfile"),
	    sds ("application/vnd.stardivision.chart"),
	    sdw ("application/vnd.stardivision.writer"),
	    ser ("application/java-serialized-object"),
	    sgf ("application/x-go-sgf"),
	    sgl ("application/vnd.stardivision.writer-global"),
	    $sh ("application/x-sh"),
	    shar ("application/x-shar"),
	    sh ("text/x-sh"),
	    shtml ("text/html"),
	    sid ("audio/prs.sid"),
	    sik ("application/x-trash"),
	    silo ("model/mesh"),
	    sis ("application/vnd.symbian.install"),
	    sisx ("x-epoc/x-sisx-app"),
	    sit ("application/x-stuffit"),
	    sitx ("application/x-stuffit"),
	    skd ("application/x-koan"),
	    skm ("application/x-koan"),
	    skp ("application/x-koan"),
	    skt ("application/x-koan"),
	    smi ("application/smil"),
	    smil ("application/smil"),
	    snd ("audio/basic"),
	    spc ("chemical/x-galactic-spc"),
	    $spl ("application/futuresplash"),
	    spl ("application/x-futuresplash"),
	    spx ("audio/ogg"),
	    src ("application/x-wais-source"),
	    stc ("application/vnd.sun.xml.calc.template"),
	    std ("application/vnd.sun.xml.draw.template"),
	    sti ("application/vnd.sun.xml.impress.template"),
	    stl ("application/vnd.ms-pki.stl"),
	    stw ("application/vnd.sun.xml.writer.template"),
	    sty ("text/x-tex"),
	    sv4cpio ("application/x-sv4cpio"),
	    sv4crc ("application/x-sv4crc"),
	    svg ("image/svg+xml"),
	    svgz ("image/svg+xml"),
	    sw ("chemical/x-swissprot"),
	    swf ("application/x-shockwave-flash"),
	    swfl ("application/x-shockwave-flash"),
	    sxc ("application/vnd.sun.xml.calc"),
	    sxd ("application/vnd.sun.xml.draw"),
	    sxg ("application/vnd.sun.xml.writer.global"),
	    sxi ("application/vnd.sun.xml.impress"),
	    sxm ("application/vnd.sun.xml.math"),
	    sxw ("application/vnd.sun.xml.writer"),
	    t ("application/x-troff"),
	    tar ("application/x-tar"),
	    taz ("application/x-gtar"),
	    $tcl ("application/x-tcl"),
	    tcl ("text/x-tcl"),
	    texi ("application/x-texinfo"),
	    texinfo ("application/x-texinfo"),
	    tex ("text/x-tex"),
	    text ("text/plain"),
	    tgf ("chemical/x-mdl-tgf"),
	    tgz ("application/x-gtar"),
	    tiff ("image/tiff"),
	    tif ("image/tiff"),
	    tk ("text/x-tcl"),
	    tm ("text/texmacs"),
	    torrent ("application/x-bittorrent"),
	    tr ("application/x-troff"),
	    tsp ("application/dsptype"),
	    ts ("text/texmacs"),
	    tsv ("text/tab-separated-values"),
	    txt ("text/plain"),	    
	    udeb ("application/x-debian-package"),
	    uls ("text/iuls"),
	    ustar ("application/x-ustar"),
	    val ("chemical/x-ncbi-asn1-binary"),
	    vcd ("application/x-cdlink"),
	    vcf ("text/x-vcard"),
	    vcs ("text/x-vcalendar"),
	    vmd ("chemical/x-vmd"),
	    vms ("chemical/x-vamas-iso14976"),
	    $vrml ("model/vrml"),
	    vrml ("x-world/x-vrml"),
	    vrm ("x-world/x-vrml"),
	    vsd ("application/vnd.visio"),
	    wad ("application/x-doom"),
	    wav ("audio/x-wav"),
	    wax ("audio/x-ms-wax"),
	    wbmp ("image/vnd.wap.wbmp"),
	    wbxml ("application/vnd.wap.wbxml"),
	    wk ("application/x-123"),
	    wma ("audio/x-ms-wma"),
	    wmd ("application/x-ms-wmd"),
	    wmlc ("application/vnd.wap.wmlc"),
	    wmlsc ("application/vnd.wap.wmlscriptc"),
	    wmls ("text/vnd.wap.wmlscript"),
	    wml ("text/vnd.wap.wml"),
	    wm ("video/x-ms-wm"),
	    wmv ("video/x-ms-wmv"),
	    wmx ("video/x-ms-wmx"),
	    wmz ("application/x-ms-wmz"),
	    wp5 ("application/wordperfect5.1"),
	    wpd ("application/wordperfect"),
	    $wrl ("model/vrml"),
	    wrl ("x-world/x-vrml"),
	    wsc ("text/scriptlet"),
	    wvx ("video/x-ms-wvx"),
	    wz ("application/x-wingz"),
	    xbm ("image/x-xbitmap"),
	    xcf ("application/x-xcf"),
	    xht ("application/xhtml+xml"),
	    xhtml ("application/xhtml+xml"),
	    xlb ("application/vnd.ms-excel"),
	    xls ("application/vnd.ms-excel"),
	    xlt ("application/vnd.ms-excel"),
	    xml ("application/xml"),
	    xpi ("application/x-xpinstall"),
	    xpm ("image/x-xpixmap"),
	    xsl ("application/xml"),
	    xtel ("chemical/x-xtel"),
	    xul ("application/vnd.mozilla.xul+xml"),
	    xwd ("image/x-xwindowdump"),
	    xyz ("chemical/x-xyz"),
	    zip ("application/zip"),
	    zmt ("chemical/x-mopac-input"),
	     ;
	    public String contentType;

	    MimeType(String contentType) { 
	        this.contentType = contentType;
	    }
	    
	    public static MimeType get(String mimetype) throws ParseException{
			for(MimeType m:MimeType.values()){
				if(m.contentType.equalsIgnoreCase(mimetype)) return m;
			}
			String debugmsg = "Unkown mime type '"+mimetype+"'";
			IndependantLog.error(debugmsg);
			throw new ParseException(debugmsg);
	    }
	}
}

