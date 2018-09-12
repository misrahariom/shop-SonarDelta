package mns.report;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SJsonParser {

	public HashMap<String, User> getUserDetails(String json) {

		JsonParser parser = new JsonParser();
		Object obj = parser.parse(json);
		JsonObject jsonObject = (JsonObject) obj;
		JsonArray issuesList = (JsonArray) jsonObject.get("users");
		Iterator<JsonElement> iterator = issuesList.iterator();
		HashMap<String, User> users = new HashMap<String, User>();
		while (iterator.hasNext()) {
			JsonElement je = iterator.next();
			JsonObject jo = je.getAsJsonObject();
			if (null != jo.get("email")) {
				User user = new User();
				user.setEmail(jo.get("email").getAsString());
				user.setName(jo.get("name").getAsString());
				user.setLogin(jo.get("login").getAsString());
				users.put(jo.get("email").getAsString(), user);
			}
		}
		return users;
	}

	public int getNoOfPages(String json) {

		JsonParser parser = new JsonParser();
		Object obj = parser.parse(json);
		JsonObject jsonObject = (JsonObject) obj;

		JsonObject paging = jsonObject.getAsJsonObject("paging");
		int total = paging.get("total").getAsInt();
		int ps = jsonObject.get("ps").getAsInt();
		if(total > ps){
			return total/ps +1;
		}else if (total > 0 & total <= ps){
			return 1;
		}else
		return 0;
	}
	
	public HashMap<String, String> getRuleList(String json){
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(json);
		ArrayList<ArrayList<Issue>> allIssues = new ArrayList<ArrayList<Issue>>();
		HashMap<String, String> ruleMap = new HashMap<String, String>();
		JsonObject jsonObject = (JsonObject) obj;
		if(null!=jsonObject.get("rules")){
		JsonArray issuesList = (JsonArray) jsonObject.get("rules");
		Iterator<JsonElement> iterator = issuesList.iterator();
		
        while (iterator.hasNext()) {
            JsonElement je = iterator.next();
            JsonObject jo =  je.getAsJsonObject();
            
            String ruleKey = jo.get("key").getAsString();
            String ruleName = jo.get("name").getAsString();
            ruleMap.put(ruleKey, ruleName);
        }
		}
        return ruleMap;
	}
	
	public ArrayList<ArrayList<Issue>> jsnParser(String json,HashMap<String, String> ruleMap, HashMap<String, User> users, boolean isClosed) throws ParseException, IOException {
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(json);
		JsonObject jsonObject = (JsonObject) obj;
		ArrayList<ArrayList<Issue>> allIssues = new ArrayList<ArrayList<Issue>>();
		JsonArray issuesList = (JsonArray) jsonObject.get("issues");
		
		Iterator<JsonElement> iterator = issuesList.iterator();
		ArrayList<Issue> cIssues = new ArrayList<Issue>();
		ArrayList<Issue> bIssues = new ArrayList<Issue>();
		ArrayList<Issue> majorIsues = new ArrayList<Issue>();
		ArrayList<Issue> minorIssues = new ArrayList<Issue>();
		ArrayList<Issue> iIssues = new ArrayList<Issue>();
        while (iterator.hasNext()) {
            JsonElement je = iterator.next();
            JsonObject jo =  je.getAsJsonObject();
            Issue issue = new Issue();
            
            if(isClosed){
            	FileUtils fu = new FileUtils();
            	Properties prop = fu.getPropValues("config.properties");
            	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    			Calendar cal = Calendar.getInstance();
    			cal.add(Calendar.DATE, -Integer.parseInt(prop.getProperty("noOfDeltaDays", "1")));
            	String closedDateStr = jo.get("closeDate").getAsString();
            	
            	Date issueClosedDate = dateFormat.parse(closedDateStr);
            	if(issueClosedDate.before(cal.getTime())){
            		continue;
            	}
            	
            }
            
            issue.setSeverity(jo.get("severity").getAsString());
            issue.setProject(jo.get("project").getAsString());
            String fileStr = jo.get("component").getAsString();
            issue.setClassName(fileStr.substring(fileStr.lastIndexOf("/")+1));
            //System.out.println(fileStr.substring(fileStr.lastIndexOf("/")+1)+","+fileStr.substring(fileStr.indexOf("WebSphereCommerceServerExtensionsLogic")+39, fileStr.lastIndexOf("/")));
            String key = jo.get("rule").getAsString();
            issue.setMessage(jo.get("message").getAsString());
            issue.setVoilation(ruleMap.get(key));
            issue.setStatus(jo.get("status").getAsString());
            
            if (null != jo.get("line")){
            	issue.setLineNo(jo.get("line").getAsInt());
            }else{
            	issue.setLineNo(0);
            }
            
            if (null != jo.get("author")){
            	issue.setAssignee(users.get(jo.get("author").getAsString()));
            }
            
            switch (issue.getSeverity()) {
			case "CRITICAL":
				cIssues.add(issue);
				break;
			case "BLOCKER":
				bIssues.add(issue);
				break;
			case "MAJOR":
				majorIsues.add(issue);
				break;
			case "MINOR":
				minorIssues.add(issue);
				break;
			case "INFO":
				iIssues.add(issue);
				break;

			default:
				break;
			}
            
        }
        //ArrayList<ArrayList<Issue>> allIssues = new ArrayList<ArrayList<Issue>>();
        
        allIssues.add(bIssues);
        allIssues.add(cIssues);
        allIssues.add(majorIsues);
        allIssues.add(minorIssues);
        allIssues.add(iIssues);
		
		return allIssues;
	}

}
