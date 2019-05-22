package bowen;

import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Cert {
	private static final Logger LOGGER = LogManager.getLogger(Cert.class);
	private static final String FROM_EMAIL = "voicemail@xfinityvoice.com";
	
	private DateFormat dateFormat;
	private List<String> recipients;
	private List<String> urls;
	
	public Cert(List<String> _recipients, List<String> _urls) {
		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		recipients = _recipients;
		urls = _urls;
	}
	
	public void run() {
		send(checkExpiration(urls), recipients);
	}
	
	public Map<String, CertInfo> checkExpiration(List<String> urls) {
		Map<String, CertInfo> res = new HashMap<>();
		Date oneMonthLater = getOneMonthLaterDate();
		LOGGER.info("One month later date: " + dateFormat.format(oneMonthLater));
		
		for (String _url: urls) {
			LOGGER.info("Endpoint URL: " + _url);
			
			try {
		        URL url = new URL(_url);
		        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		        connection.connect();
		        
		        Certificate certificate = connection.getServerCertificates()[0];
	        	X509Certificate x509Certificate = (X509Certificate) certificate;
	        	String subject = x509Certificate.getSubjectDN().toString();
	        	Date endDate = x509Certificate.getNotAfter();
	        	LOGGER.info("SSL Certificate subject: " + subject);
	        	LOGGER.info("SSL Certificate end date: " + endDate);
	        	
	        	if (endDate.before(oneMonthLater)) {
	        		res.put(_url, new CertInfo(subject, endDate));
	        		LOGGER.info("Expire soon!!! " + subject.split(",")[0].substring(3) + " will expire on " + dateFormat.format(endDate));
	        	}
		      
		        connection.disconnect();
			} catch(Exception e) {
				LOGGER.error("URL: " + _url + " not able to connect. " + e.getMessage());
			}
		}
		return res;
	}
	
	public void send(Map<String, CertInfo> urlToCertInfo, List<String> recipients) {
		if (urlToCertInfo == null || urlToCertInfo.isEmpty()) {
			return;
		}
		Date currentDate = getCurrentDate();
		
		String host = "ulamailrelay.g.comcast.com";
		String port = "25";

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.socketFactory.port", port); //SSL Port
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        
		Session session = Session.getDefaultInstance(properties);

		try {
			InternetAddress[] sendTo = new InternetAddress[recipients.size()]; 
	        for (int i = 0; i < recipients.size(); i++) {  
	        	LOGGER.info("Send to: " + recipients.get(i));
	            sendTo[i] = new InternetAddress(recipients.get(i));  
	        }
	        
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(FROM_EMAIL));

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, sendTo);

			// Set Subject: header field
			message.setSubject("NextVM: SSL Certification expiry notification");

			// Now set the actual message
			StringBuilder sb = new StringBuilder();
			int days = daysBetween(currentDate, urlToCertInfo.get(urls.get(0)).getEndDate());
			sb.append("Please note that the following SSL certificate(s) will expire in the next <font color=\"red\"><strong>" + days + 
					"</strong></font> day(s). If you have already taken action, please ignore this email.<br>");
			for (String url: urlToCertInfo.keySet()) {
				String subject = urlToCertInfo.get(url).getSubject();
				Date endDate = urlToCertInfo.get(url).getEndDate();
				
				sb.append("<ul><li>End point: <font color=\"red\"><strong>" + url + "</strong></font></li>" +
						      "<li>Hostname: <font color=\"red\"><strong>" + subject.split(",")[0].substring(3) + "</strong></font></li>" +
							  "<li>Expire date: <font color=\"red\"><strong>" + dateFormat.format(endDate) + "</strong></font></li>" +
							  "<li>" + subject + "</li></ul>");
			}
			sb.append("<br>NextVM Team");
			message.setContent(sb.toString(), "text/html");

			// Send message
			Transport.send(message);  
			LOGGER.info("Sent message successfully....");
		} catch (MessagingException mex) {
			LOGGER.error(mex.getMessage());
		}
	}
	
	private Date getOneMonthLaterDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH , 1);
		return cal.getTime();
	}
	
	private Date getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}

    public int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }
    
    class CertInfo {
    	private String subject;
    	private Date endDate;
    	
    	public CertInfo(String subject, Date endDate) {
    		this.subject = subject;
    		this.endDate = endDate;
    	}
    	public String getSubject() {
    		return subject;
    	}
    	
    	public Date getEndDate() {
    		return endDate;
    	}
    }
    
}
