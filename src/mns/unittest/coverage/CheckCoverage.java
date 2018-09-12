package mns.unittest.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CheckCoverage {

	/**
	 * Singleton instance.
	 */
	private static CheckCoverage instance = new CheckCoverage();

	/**
	 * private default constructor.
	 */
	private CheckCoverage() {
	}

	/**
	 * This method returns the singleton instance for this class.
	 * 
	 * @return instance SonarUtils.
	 */
	public static CheckCoverage getInstance() {
		return instance;
	}

	public List<List<Coverage>> check(String key, Properties prop) throws ParseException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		List<List<Coverage>> list = new ArrayList<List<Coverage>>();
		String[] keys = key.split(",");
		for (String keyval : keys) {
			String url1 = "";
			String url2 = "";
			ArrayList<String> urls = new ArrayList<String>();

			String url="";
			if(null!=prop.getProperty("sonarPort"))
			{
			url = "http://" + prop.getProperty("sonarHost") + ":" + prop.getProperty("sonarPort")
					+ prop.getProperty("urlForGettingFiles") + keyval + "&depth=";
			}
			else{
			url = "http://" + prop.getProperty("sonarHost") + ":" + prop.getProperty("urlForGettingFiles") + keyval + "&depth=";   
			}
			if (keyval.equals("mns:wcs-master"))
				url2 = url + "2";
			else
				url2 = url + 3;
			urls.add(url2);
			if (keyval.equals("com.mns.portal:mns-parent")) {
				url1 = url + "4";
				urls.add(url1);
			}

			for (String url_individual : urls) {
				HttpGet request1 = new HttpGet(url_individual);
				request1.addHeader("Authorization", prop.getProperty("authorization"));
				HttpResponse response = client.execute(request1);
				String result = EntityUtils.toString(response.getEntity());
				String emptyString = "[]";
				Boolean endval = "]".equals((result.substring(result.length() - 1)));
				if ((null != result) && !(result.equals(emptyString)) && endval) {
					List<Coverage> populatedData = populateVO(result, prop);
					if (!populatedData.isEmpty())
						list.add(populatedData);
				}
			}
		}
		return list;
	}
	
	public List<List<NewCoverage>> newCheck(String key, Properties prop) throws ParseException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		List<List<NewCoverage>> list = new ArrayList<List<NewCoverage>>();
		ArrayList<String> urls = new ArrayList<String>();
		String[] keys = key.split(",");
		for (String keyval : keys) {
			String url="";
			if(null!=prop.getProperty("sonarPort"))
			{
			url = "https://" + prop.getProperty("sonarHost") + ":" + prop.getProperty("sonarPort")
					+ prop.getProperty("urlForGettingFiles") + keyval + "&metricKeys=new_coverage&strategy=leaves";  
			}
			else{
			url = "https://" + prop.getProperty("sonarHost") +  prop.getProperty("urlForGettingFiles") + keyval + "&metricKeys=new_coverage&strategy=leaves";   
			}
			urls.add(url);
			}

			for (String url_individual : urls) {
				HttpGet request1 = new HttpGet(url_individual);
				request1.addHeader("Authorization", prop.getProperty("authorization"));
				HttpResponse response = client.execute(request1);
				String result = EntityUtils.toString(response.getEntity());
				String emptyString = "{}";
				Boolean endval = "}".equals((result.substring(result.length() - 1)));
				if ((null != result) && !(result.equals(emptyString)) && endval) {
					List<NewCoverage> populatedData = populateNewVO(result, prop);
					if (!populatedData.isEmpty())
						list.add(populatedData);
				}
		}
		return list;
	}

	public List<Coverage> populateVO(String result, Properties prop) throws IOException {
		ArrayList<Coverage> coverages = new ArrayList<>();
		JSONArray ja = new JSONArray(result);
		String deltadays = prop.getProperty("deltaDays");
		String param = getVariable(deltadays);
		for (int i = 0; i < ja.length(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			long id = jo.getLong("id");
			String key = jo.getString("key");
			String name = jo.getString("name");
			String scope = jo.getString("scope");
			if (scope.equals("DIR"))
				continue;
			String qualifier = jo.getString("qualifier");
			String date = jo.getString("date");
			String creationdate = jo.getString("creationDate");
			String lname = jo.getString("lname");
			boolean retVal = false;
			String exclusion = prop.getProperty("exclusion");
			String[] exclusionList = exclusion.split(",");
			for (String exl : exclusionList) {
				retVal = (name.toUpperCase().endsWith(exl));
				if (retVal)
					break;
			}
			JSONArray ja1 = jo.getJSONArray("msr");
			if (ja1 == null || ja1.isNull(0) || retVal)
				continue;
			JSONObject jo1 = ja1.getJSONObject(0);
			String msr_key = jo1.getString("key");
			if (!jo1.has(param))
				continue;
			Double msr_val = jo1.getDouble(param);
			if (msr_val == null || msr_val > Double.parseDouble(prop.getProperty("boundaryValue")))
				continue;
			String msr_frmt_val = jo1.getString("f" + param);
			Coverage coverage = new Coverage(id, key, name, scope, qualifier, date, creationdate, lname, msr_key,
					msr_val, msr_frmt_val);
			coverages.add(coverage);
		}
		return coverages;
	}
	
	public List<NewCoverage> populateNewVO(String result, Properties prop) throws IOException {
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(result);
		JsonObject jsonObject = (JsonObject) obj;
		JsonArray coverageList = (JsonArray) jsonObject.get("components");
		ArrayList<NewCoverage> coverages = new ArrayList<>();
		Iterator<JsonElement> iterator = coverageList.iterator();
		String deltadays = prop.getProperty("deltaDays");
		String param = getVariable(deltadays);
		while (iterator.hasNext()) {
			JsonElement je = iterator.next();
            JsonObject jo =  je.getAsJsonObject();
            NewCoverage newCoverage = new NewCoverage();
            newCoverage.setId(jo.get("id").getAsString());
            newCoverage.setKey(jo.get("key").getAsString());
            newCoverage.setName(jo.get("name").getAsString());
            newCoverage.setQualifier(jo.get("qualifier").getAsString());
            JsonArray measures = jo.getAsJsonArray("measures");
            Iterator<JsonElement> measure = measures.iterator();
            JsonObject measureObj =  measure.next().getAsJsonObject();
            newCoverage.setMetric( measureObj.get("metric").getAsString());
            JsonArray peroids = measureObj.getAsJsonArray("periods");
            Iterator<JsonElement> period = peroids.iterator();
            JsonObject periodObj =  period.next().getAsJsonObject();
            newCoverage.setValue(periodObj.get("value").getAsString());
            if (Double.parseDouble(periodObj.get("value").getAsString()) > Double.parseDouble(prop.getProperty("boundaryValue")))
				continue;
			coverages.add(newCoverage);
		}
		return coverages;
	}

	private String getVariable(String deltadays) {

		String param;
		switch (deltadays) {
		case "1":
			param = "var1";
			break;
		case "3":
			param = "var2";
			break;
		case "7":
			param = "var3";
			break;
		case "30":
			param = "var4";
			break;
		default:
			param = "var5";
			break;
		}

		return param;

	}
}
