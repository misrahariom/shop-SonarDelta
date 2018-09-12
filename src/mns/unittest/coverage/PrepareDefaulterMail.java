package mns.unittest.coverage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.internet.AddressException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mns.report.FileUtils;
import mns.report.SendEmail;

public class PrepareDefaulterMail {

	public static boolean sendEmail = false;

	public static Set<String> defaultersEmail = new HashSet<String>();

	public static void main(String[] args)
			throws org.apache.http.ParseException, JSONException, IOException, ParseException, AddressException {

		FileUtils fu = new FileUtils();
		Properties prop = fu.getPropValues("junit_config.properties");
		//List<Map<Coverage, Set<String>>> defaulters = findDefaulters(prop);
		List<Map<NewCoverage, Set<String>>> defaulters = findNewDefaulters(prop);
		String emails = getEmailList(defaultersEmail);
		if ((defaulters != null) && !defaulters.isEmpty()) {
			sendEmail = true;
		}
		if ("true".equalsIgnoreCase(prop.getProperty("emailEnabled"))) {

			CreateDefaultersReportHtml report = new CreateDefaultersReportHtml();
			StringBuilder html = new StringBuilder();
			html.append(report.createNewReport(defaulters));
			fu.writeFile(html.toString(), prop.getProperty("attchmentFilePath"));
			SendEmail se = new SendEmail();
			System.out.println(emails.toString());
			if (sendEmail) {
				if ("yes".equalsIgnoreCase(prop.getProperty("developMode"))) {
					se.sendEmail(html.toString(), "", prop);
				} else {
					try {
						se.sendEmail(html.toString(), emails, prop);
					} catch (AddressException e) {
						System.out.println("Problem in sending email to violators, sending to default address");
						se.sendEmail(html.toString(), "", prop);
					}
				}
				System.out.println("Coverage breach List report generated successfully.");
			} else {
				System.out.println("Coverage breach List report completed. No issues found");
			}
		}

	}

	public static List<Map<Coverage, Set<String>>> findDefaulters(Properties prop)
			throws org.apache.http.ParseException, IOException, JSONException, ParseException {
		List<List<Coverage>> faulty_s = CheckCoverage.getInstance().check(prop.getProperty("projectKey"), prop);
		List<Map<Coverage, Set<String>>> defaulters = new ArrayList<Map<Coverage, Set<String>>>();
		for (List<Coverage> faulty : faulty_s) {
			HttpClient client = HttpClientBuilder.create().build();
			Coverage coverage;
			Integer deltaDays = Integer.parseInt(prop.getProperty("deltaDays"));
			for (int i = 0; i < faulty.size(); i++) {
			    Map<Coverage, Set<String>> defaulters_individual = new HashMap<Coverage, Set<String>>();
				String key = faulty.get(i).getKey();
				String urlForScm = "https://" + prop.getProperty("sonarHost") + ":" + prop.getProperty("sonarPort")
						+ prop.getProperty("urlForScm") + key;
				HttpGet request1 = new HttpGet(urlForScm);
				request1.addHeader("Authorization", prop.getProperty("authorization"));
				HttpResponse response = client.execute(request1);
				String result = EntityUtils.toString(response.getEntity());
				Set<String> defaulter = getListOfDefaulters(result, deltaDays);
				for (String email : defaulter)
					defaultersEmail.add(email);
				Set<String> defaulterName = getUserName(prop, defaulter);
				if (!defaulterName.isEmpty()) {
					coverage = faulty.get(i);
					defaulters_individual.put(coverage, defaulterName);
					defaulters.add(defaulters_individual);
				} else {
					coverage = faulty.get(i);
					defaulters_individual.put(coverage, defaulter);
					defaulters.add(defaulters_individual);
				}
			}
		}
		return defaulters;
	}
	
	public static List<Map<NewCoverage, Set<String>>> findNewDefaulters(Properties prop)
			throws org.apache.http.ParseException, IOException, JSONException, ParseException {
		List<List<NewCoverage>> faulty_s = CheckCoverage.getInstance().newCheck(prop.getProperty("projectKey"), prop);
		List<Map<NewCoverage, Set<String>>> defaulters = new ArrayList<Map<NewCoverage, Set<String>>>();
		for (List<NewCoverage> faulty : faulty_s) {
			HttpClient client = HttpClientBuilder.create().build();
			NewCoverage coverage;
			Integer deltaDays = Integer.parseInt(prop.getProperty("deltaDays"));
			for (int i = 0; i < faulty.size(); i++) {
			    Map<NewCoverage, Set<String>> defaulters_individual = new HashMap<NewCoverage, Set<String>>();
				String key = faulty.get(i).getKey();
				String urlForScm = "https://" + prop.getProperty("sonarHost") + ":" + prop.getProperty("sonarPort")
						+ prop.getProperty("urlForScm") + key;
				HttpGet request1 = new HttpGet(urlForScm);
				request1.addHeader("Authorization", prop.getProperty("authorization"));
				HttpResponse response = client.execute(request1);
				String result = EntityUtils.toString(response.getEntity());
				Set<String> defaulter = getListOfDefaulters(result, deltaDays);
				for (String email : defaulter)
					defaultersEmail.add(email);
				Set<String> defaulterName = getUserName(prop, defaulter);
				if (!defaulterName.isEmpty()) {
					coverage = faulty.get(i);
					defaulters_individual.put(coverage, defaulterName);
					defaulters.add(defaulters_individual);
				} else {
					coverage = faulty.get(i);
					defaulters_individual.put(coverage, defaulter);
					defaulters.add(defaulters_individual);
				}
			}
		}
		return defaulters;
	}

	private static HashSet<String> getListOfDefaulters(String result, Integer deltaDays)
			throws JSONException, ParseException {
		JSONObject jsonObj = new JSONObject(result);
		HashSet<String> defaulters = new HashSet<String>();
		JSONArray jsonArray = jsonObj.getJSONArray("scm");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONArray jo = jsonArray.getJSONArray(i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date currentDate = new Date();
			Date d1 = sdf.parse(jo.getString(2));
			Date d2 = sdf.parse(sdf.format(currentDate));
			DateTime dt1 = new DateTime(d1);
			DateTime dt2 = new DateTime(d2);
			if (Math.abs(Days.daysBetween(dt2, dt1).getDays()) <= deltaDays) {
				defaulters.add(jo.getString(1));
			}

		}
		return defaulters;
	}

	private static HashSet<String> getUserName(Properties prop, Set<String> defaulters)
			throws ClientProtocolException, IOException {

		HttpClient client = HttpClientBuilder.create().build();
		HashSet<String> defaultersName = new HashSet<String>();
		String urlForUsername = "https://" + prop.getProperty("sonarHost") + ":" + prop.getProperty("sonarPort")
				+ prop.getProperty("urlForUser");
		HttpGet request1 = new HttpGet(urlForUsername);
		request1.addHeader("Authorization", prop.getProperty("authorization"));
		HttpResponse response = client.execute(request1);
		String result = EntityUtils.toString(response.getEntity());
		JSONObject jo = new JSONObject(result);
		JSONArray ja = jo.getJSONArray("users");
		for (String email : defaulters) {
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo1 = ja.getJSONObject(i);
				if (jo1.has("email")) {
					if (jo1.getString("email").equals(email)) {
						String name = jo1.getString("name").toUpperCase();
						defaultersName.add(name);
					}
				}
			}
		}
		return defaultersName;

	}

	public static String getEmailList(Set<String> defaulters) {

		String separator = ", ";
		int total = defaulters.size() * separator.length();
		for (String email : defaulters) {
			total += email.length();
		}

		StringBuilder sb = new StringBuilder(total);
		for (String email : defaulters) {
			sb.append(separator).append(email);
		}

		String result = sb.substring(separator.length());
		return result;
	}

}
