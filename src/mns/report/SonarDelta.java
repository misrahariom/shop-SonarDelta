
package mns.report;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author nverm5
 *
 */
public class SonarDelta {

	/**
	 * @param args
	 */
	
	static HashMap<String,String> users;
	static HashMap<String, String> sapVoilators;
	
	public static void main(String[] args) {
		
		sapVoilators = new HashMap<String, String>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		date.setDate(date.getDate()-1);                                                                                      
		System.out.println(dateFormat.format(date));
		//String url = "http://10.207.249.136:9000/api/issues/search?componentRoots=mns:wcs-master&format=json&createdAfter="+dateFormat.format(date)+"&pageIndex=";
		//String url = "http://10.207.249.136:9000/api/issues/search?componentRoots=mns:wcs-master&format=json&createdAfter=2015-12-24&pageIndex=";
		String url = "http://10.151.4.37:9000/api/issues/search?componentRoots=WCS_Report&format=json&createdAfter="+dateFormat.format(date)+"&pageIndex=0";
		//String url = "http://10.151.4.37:9000/api/issues/search?componentRoots=WCS_Report&format=json&createdAfter=2018-09-01&pageIndex=0";
		getUserDetails();
		String json = fetchReport(url,1);
		String html = findPages(json,url);
		System.out.println(html);
		// Get a set of the entries
	      Set set = sapVoilators.entrySet();
	      // Get an iterator
	      Iterator i = set.iterator();
	      // Display elements
	      StringBuffer sb = new StringBuffer();
	      while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print("key-"+me.getKey() + ": ");
	         System.out.println("value-"+me.getValue());
	         sb.append((String)me.getKey());
	         sb.append(",");	         
	      }
		
	      String emailList = sb.toString();
	      if(!"".equalsIgnoreCase(emailList)){
	    	  System.out.println("email list is-"+emailList.substring(0,emailList.length()-1));
	      }
	      if(emailList.indexOf(",") > 0){
	    	  sendEmail(html,emailList);
	      }else{
	    	  sendEmail(html,"");
	      }
	      
	}
	
		
	public static void sendEmail(String html, String emailTo){// Recipient's email ID needs to be mentioned.
	      String to = "hmisra@sapient.com";
	      if(!"".equalsIgnoreCase(emailTo)){
	    	//to = emailTo;
	      }
	      String cc = "sgupta40@sapient.com,hmisra@sapient.com,anshul.gupta3@sapient.com,mrastogi2@sapient.com";
	   //   String replyTo = "nverma5@sapient.com,aamol@sapient.com,csharma7@sapient.com";
	   //   String cc = "hmisra@sapient.com";
	      String replyTo = "hmisra@sapient.com";
	      InternetAddress[] iAdressArray = null;
	      InternetAddress[] ccArray = null;
	      InternetAddress[] rToArray = null;
	      try {
			iAdressArray = InternetAddress.parse(to);
			ccArray = InternetAddress.parse(cc);
			rToArray = InternetAddress.parse(replyTo);
		} catch (AddressException e){
			e.printStackTrace();
		}
	      
	      
	      // Sender's email ID needs to be mentioned
	      String from = "tcpsonarissueswatcher@sapient.com";

	      // Assuming you are sending email from localhost
	      String host = "180.235.155.220";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);

	      try{
	          // Create a default MimeMessage object.
	          MimeMessage message = new MimeMessage(session);

	          // Set From: header field of the header.
	          message.setFrom(new InternetAddress(from));

	          // Set To: header field of the header.
	          //message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	          message.setRecipients(Message.RecipientType.TO, iAdressArray);
	          message.setRecipients(Message.RecipientType.CC, ccArray);
	          message.setReplyTo(rToArray);

	          DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	  		  Date date = new Date();
	  		  date.setDate(date.getDate()-1);
	  		  
	          // Set Subject: header field
	          message.setSubject("Sonar Voilations Added | "+dateFormat.format(date));
	          
	          // Send the actual HTML message, as big as you like
	          //message.setContent(html, "text/html" );
	          
	       // Create the message part
	          BodyPart messageBodyPart = new MimeBodyPart();

	          // Now set the actual message
	          messageBodyPart.setContent(html,"text/html");

	          // Create a multipar message
	          Multipart multipart = new MimeMultipart();

	          // Set text message part
	          multipart.addBodyPart(messageBodyPart);

	          // Part two is attachment
	          messageBodyPart = new MimeBodyPart();
	          String osName = System.getProperty("os.name").toLowerCase();
	          String filename = "C:\\sonarDelta.html";
	          if (!"".equalsIgnoreCase(osName) && !osName.contains("window")) {
	        	  filename = "/tmp/sonarDelta.html";
              }
	          DataSource source = new FileDataSource(filename);
	          messageBodyPart.setDataHandler(new DataHandler(source));
	          messageBodyPart.setFileName("SonarDelta.html");
	          multipart.addBodyPart(messageBodyPart);
	          

	          // Send the complete message parts
	          message.setContent(multipart);

	          // Send message
	          Transport.send(message);
	          //System.out.println("Sent message successfully....");
	       }catch (MessagingException mex) {
	          mex.printStackTrace();
	       }
	}
	
	public static String findPages(String json, String url){
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(json);
		JsonObject jsonObject = (JsonObject) obj;
		
		JsonObject paging = jsonObject.getAsJsonObject("paging");
		
		JsonElement issues = jsonObject.get("issues");
		System.out.println(issues.getAsJsonArray().size());
        String html = null;
        StringBuffer sb = new StringBuffer();
		sb.append("<!doctype html><html><head><style>table td:first-child,table th:first-child{text-align:left;padding-left:20px}table td,table th{border-bottom:1px solid #e0e0e0}table{font-family:Arial,Helvetica,sans-serif;color:#666;text-shadow:1px 1px 0 #fff;background:#eaebec;margin:20px;border:1px solid #ccc;-moz-border-radius:3px;-webkit-border-radius:3px;border-radius:3px;-moz-box-shadow:0 1px 2px #d1d1d1;-webkit-box-shadow:0 1px 2px #d1d1d1;box-shadow:0 1px 2px #d1d1d1}table th{padding:21px 25px 22px;border-top:1px solid #fafafa;background:#ededed;background:-webkit-gradient(linear,left top,left bottom,from(#ededed),to(#ebebeb));background:-moz-linear-gradient(top,#ededed,#ebebeb)}table tr:first-child th:first-child{-moz-border-radius-topleft:3px;-webkit-border-top-left-radius:3px;border-top-left-radius:3px}table tr:first-child th:last-child{-moz-border-radius-topright:3px;-webkit-border-top-right-radius:3px;border-top-right-radius:3px}table tr{text-align:center;padding-left:20px}table td:nth-child(2){max-width:900px;border-left:0}table td{word-wrap:break-word;max-width:250px;padding:18px;border-top:1px solid #fff;border-left:1px solid #e0e0e0;background:#fafafa;background:-webkit-gradient(linear,left top,left bottom,from(#fbfbfb),to(#fafafa));background:-moz-linear-gradient(top,#fbfbfb,#fafafa)}table tr.even td{background:#f6f6f6;background:-webkit-gradient(linear,left top,left bottom,from(#f8f8f8),to(#f6f6f6));background:-moz-linear-gradient(top,#f8f8f8,#f6f6f6)}table tr:last-child td{border-bottom:0}table tr:last-child td:first-child{-moz-border-radius-bottomleft:3px;-webkit-border-bottom-left-radius:3px;border-bottom-left-radius:3px}table tr:last-child td:last-child{-moz-border-radius-bottomright:3px;-webkit-border-bottom-right-radius:3px;border-bottom-right-radius:3px}table tr:hover td{background:#f2f2f2;background:-webkit-gradient(linear,left top,left bottom,from(#f2f2f2),to(#f0f0f0));background:-moz-linear-gradient(top,#f2f2f2,#f0f0f0)}</style></head>");
		sb.append("<table><tbody><tr><th>Severity</th><th>Class</th><th>Voilation</th><th>Line No</th><th>Assigned To</th></tr>");
		ArrayList<String> arr = null;
        JsonElement totalViolationsJson = paging.get("total");
        int totalViolations = 0;
        if(null !=totalViolationsJson)
        	totalViolations =	totalViolationsJson.getAsInt();
        
    	if(0==totalViolations){
    		System.out.println("No voilations on last day!!");
    		System.exit(1);
    	}
    	StringBuffer sbs = jsnParser(json);
    	sb.append(sbs);
        sb.append("</tbody></table></html>");
        writeFile(sb.toString());
        System.out.print(sb.toString());
        return sb.toString();
	}
	
	public static void getUserDetails(){
		String url = "http://10.151.4.37:9000/api/users/search?format=json";
		String json  = fetchReport(url, 0);
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(json);
		JsonObject jsonObject = (JsonObject) obj;
		JsonArray issuesList = (JsonArray) jsonObject.get("users");
		Iterator<JsonElement> iterator = issuesList.iterator();
		users = new HashMap<String, String>();
		int i=0;
        while (iterator.hasNext()) {
            JsonElement je = iterator.next();
            JsonObject jo =  je.getAsJsonObject();
            if (null != jo.get("login")){
            	users.put(jo.get("login").getAsString(), jo.get("name").getAsString());
            	i++;
            }
        }
        
        //System.out.println("Total users count is - "+i);

	}
	
	public static void writeFile(String html){
		Writer writer = null;

		String osName = System.getProperty("os.name").toLowerCase();
        String filename = "C:\\sonarDelta.html";
        if (!"".equalsIgnoreCase(osName) && !osName.contains("window")) {
      	  filename = "/tmp/sonarDelta.html";
        }
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(filename), "utf-8"));
		    writer.write(html);
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	
	public static StringBuffer jsnParser(String json) {
		   ArrayList<String> arr = new ArrayList<String>();
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(json);
		JsonObject jsonObject = (JsonObject) obj;
		JsonArray issuesList = (JsonArray) jsonObject.get("issues");
		StringBuffer sf = new StringBuffer();
		Iterator<JsonElement> iterator = issuesList.iterator();
        while (iterator.hasNext()) {
            JsonElement je = iterator.next();
            JsonObject jo =  je.getAsJsonObject();
            sf.append("<tr><td>"+jo.get("severity").getAsString()+"</td><td>");
            String fileStr = jo.get("component").getAsString();
            sf.append(fileStr.substring(fileStr.lastIndexOf("/")+1)+"</td><td>");
            String key = jo.get("rule").getAsString();
            
            sf.append("<a href=\"#\" data-toggle=\"tooltip\" title=\""+jo.get("message").getAsString()+"\">"+jo.get("message").getAsString()+"</a></td>");
            if (null != jo.get("line")){
            	sf.append("<td>"+jo.get("line").toString()+"</td>");
            	//System.out.println("Line - " + jo.get("line").toString());
            }else{
            	sf.append("<td>NA</td>");
            }
            
            if (null != jo.get("author")){
            	if(null!= users.get(jo.get("author").getAsString()))
            		sf.append("<td>"+users.get(jo.get("author").getAsString())+"</td>");
            	else
            		sf.append("<td>"+jo.get("author").getAsString()+"</td>");
            	
            	//add sapient people to voilator's list
            	String email = jo.get("author").getAsString();
            	System.out.println(email);
            	if (null != email && email.indexOf("sapient") > 0){
            		sapVoilators.put(jo.get("author").getAsString(), users.get(jo.get("author").getAsString()));
            	}
            }else{
            	sf.append("<td>Not Assigned</td>");
            }
            sf.append("</tr>");
        }
		return sf;
	}
	
	public static String fetchReport(String urlStr, int index) {
		
		try {
            // get URL content

			if (index > 0)
				urlStr = urlStr+index;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));

            StringBuffer sbuff = new StringBuffer();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                    sbuff.append(inputLine);
            }
            System.out.println("response:  "+sbuff.toString());
            br.close();

            //System.out.println("URL Response - "+sbuff.toString());
            return sbuff.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

		return null;
		
	}

}
