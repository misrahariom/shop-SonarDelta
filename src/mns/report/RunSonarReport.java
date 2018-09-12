package mns.report;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.internet.AddressException;

public class RunSonarReport {

	public static boolean sendEmail = false;

	public static void main(String[] args) throws AddressException {

		FileUtils fu = new FileUtils();
		try {
			Properties prop = fu.getPropValues("config.properties");
			sendEmail = false;
			String url = "https://" + prop.getProperty("sonarHost")
					+ "/api/users/search?format=json&pageSize=500";
			System.out.println("Users - "+url);
			GetData gd = new GetData();
			String json = gd.fetchReport(url);
			SJsonParser jp = new SJsonParser();
			HashMap<String, User> users = jp.getUserDetails(json);
			ArrayList<ArrayList<Issue>> arr = null;
			arr = openedIssues(users);
			ArrayList<ArrayList<Issue>> tempArr = null;
			tempArr = closedIssues(users);

			for (int x = 0; x < arr.size(); x++) {
				for (int y = 0; y < tempArr.get(x).size(); y++) {
					Issue issue = tempArr.get(x).get(y);
					arr.get(x).add(issue);
					sendEmail = true;

				}
			}

			CreateHTMLReport report = new CreateHTMLReport();
			String html = report.createReport(arr);
			fu.writeFile(html, prop.getProperty("attchmentFilePath"));

			if ("true".equalsIgnoreCase(prop.getProperty("emailEnabled"))) {
				StringBuilder sb = new StringBuilder();
				for (int x = 0; x < arr.size(); x++) {
					for (int y = 0; y < arr.get(x).size(); y++) {
						Issue issue = arr.get(x).get(y);
						if (!sb.toString().isEmpty() && null != issue.getAssignee()
								&& sb.toString().contains(issue.getAssignee().getEmail())) {
							continue;
						}
						if (null != issue.getAssignee() && null != issue.getAssignee().getEmail()) {
							sb.append(issue.getAssignee().getEmail());
							sb.append(",");
						}
					}
				}

				SendEmail se = new SendEmail();

				if (sendEmail) {
					System.out.println("Email list - " + sb.toString());
					if ("yes".equalsIgnoreCase(prop.getProperty("developMode"))) {
						se.sendEmail(html, "", prop);
					} else {
						se.sendEmail(html, sb.toString(), prop);
					}
					System.out.println("Sonar report generated successfully.");
				} else {
					System.out.println("Sonar report completed. No issues found");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<ArrayList<Issue>> closedIssues(HashMap<String, User> users) {

		FileUtils fu = new FileUtils();
		try {
			Properties prop = fu.getPropValues("config.properties");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -Integer.parseInt(prop.getProperty("noOfDeltaDays")));
			String url = "https://" + prop.getProperty("sonarHost") + "/api/issues/search?format=json&createdAfter="
					+ dateFormat.format(cal.getTime()) + "&asc=true&statuses=CLOSED&resolved=true&pageSize=100&pageIndex=";
			GetData gd = new GetData();
			String json = gd.fetchReport(url + "1");
			SJsonParser jp = new SJsonParser();
			int totalPages = jp.getNoOfPages(json);
			if (0 == totalPages) {
				System.out.println("Closed issues are not found");
				// System.exit(1);
			}
			ArrayList<ArrayList<Issue>> arr = null;
			ArrayList<Issue> cIssues = new ArrayList<Issue>();;
			ArrayList<Issue> bIssues = new ArrayList<Issue>();;
			ArrayList<Issue> majorIsues = new ArrayList<Issue>();;
			ArrayList<Issue> minorIssues = new ArrayList<Issue>();;
			ArrayList<Issue> iIssues = new ArrayList<Issue>();;
			for (int i = 1; i <= totalPages;) {
			    
				HashMap<String, String> ruleMap = jp.getRuleList(json);
				// parsing page 1
				if (null == arr) {
					arr = jp.jsnParser(json, ruleMap, users, true);
					bIssues = arr.get(0);
					cIssues = arr.get(1);
					majorIsues = arr.get(2);
					minorIssues = arr.get(3);
					iIssues = arr.get(4);
				} else {
					json = gd.fetchReport(url + i);
					ruleMap = jp.getRuleList(json);
					
					arr = jp.jsnParser(json, ruleMap, users, true);

					for (int j = 0; j < arr.get(0).size(); j++) {
						bIssues.add(arr.get(0).get(j));
					}
					for (int j = 0; j < arr.get(1).size(); j++) {
						cIssues.add(arr.get(1).get(j));
					}
					for (int j = 0; j < arr.get(2).size(); j++) {
						majorIsues.add(arr.get(2).get(j));
					}
					for (int j = 0; j < arr.get(3).size(); j++) {
						minorIssues.add(arr.get(3).get(j));
					}
					for (int j = 0; j < arr.get(4).size(); j++) {
						iIssues.add(arr.get(4).get(j));
					}
				}
				if (i == totalPages)
					break;
				i++;
			}

			arr = new ArrayList<ArrayList<Issue>>();
			arr.add(bIssues);
			arr.add(cIssues);
			arr.add(majorIsues);
			arr.add(minorIssues);
			arr.add(iIssues);

			return arr;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;

	}

	

	public static ArrayList<ArrayList<Issue>> openedIssues(HashMap<String, User> users) {
		try {
			FileUtils fu = new FileUtils();
			Properties prop = fu.getPropValues("config.properties");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -Integer.parseInt(prop.getProperty("noOfDeltaDays")));
			String url = "https://" + prop.getProperty("sonarHost") + "/api/issues/search?format=json&createdAfter="
					+ dateFormat.format(cal.getTime()) + "&asc=true&statuses=OPEN&resolved=false&pageSize=100&pageIndex=";
			System.out.println("URL="+url);
			GetData gd = new GetData();
			String json = gd.fetchReport(url + "1");
			SJsonParser jp = new SJsonParser();
			int totalPages = jp.getNoOfPages(json);
			if (0 == totalPages) {
				System.out.println("No open voilations on last day!!");
				// System.exit(1);
			} else {
				sendEmail = true;
			}
			ArrayList<ArrayList<Issue>> arr = null;
			ArrayList<Issue> cIssues = new ArrayList<Issue>();
			ArrayList<Issue> bIssues = new ArrayList<Issue>();
			ArrayList<Issue> majorIsues = new ArrayList<Issue>();
			ArrayList<Issue> minorIssues = new ArrayList<Issue>();
			ArrayList<Issue> iIssues = new ArrayList<Issue>();
			for (int i = 1; i <= totalPages;) {
				HashMap<String, String> ruleMap = jp.getRuleList(json);
				// parsing page 1
				if (null == arr) {
					arr = jp.jsnParser(json, ruleMap, users, false);
					bIssues = arr.get(0);
					cIssues = arr.get(1);
					
					majorIsues = arr.get(2);
					minorIssues = arr.get(3);
					iIssues = arr.get(4);
				}else {
					json = gd.fetchReport(url + i);
					ruleMap = jp.getRuleList(json);
					arr = jp.jsnParser(json, ruleMap, users, false);

					for (int j = 0; j < arr.get(0).size(); j++) {
						bIssues.add(arr.get(0).get(j));
					}
					for (int j = 0; j < arr.get(1).size(); j++) {
						cIssues.add(arr.get(1).get(j));
					}
					for (int j = 0; j < arr.get(2).size(); j++) {
						majorIsues.add(arr.get(2).get(j));
					}
					for (int j = 0; j < arr.get(3).size(); j++) {
						minorIssues.add(arr.get(3).get(j));
					}
					for (int j = 0; j < arr.get(4).size(); j++) {
						iIssues.add(arr.get(4).get(j));
					}
				}
				if (i == totalPages)
					break;
				i++;
			}

			arr = new ArrayList<ArrayList<Issue>>();
			arr.add(bIssues);
			arr.add(cIssues);
			arr.add(majorIsues);
			arr.add(minorIssues);
			arr.add(iIssues);

			return arr;

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return null;

	}
}
