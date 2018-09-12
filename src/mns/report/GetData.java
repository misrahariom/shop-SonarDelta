package mns.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;


public class GetData {

	public String fetchReport(String urlStr) {
		
		FileUtils fu = new FileUtils();
		try {
			Properties prop = fu.getPropValues("config.properties");
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(urlStr);
			request.addHeader("Authorization", prop.getProperty("authorization"));
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

		return result.toString();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

}
