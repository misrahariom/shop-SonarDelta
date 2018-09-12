package mns.unittest.coverage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import mns.report.FileUtils;

public class CreateDefaultersReportHtml {

	public String startHTML() {
		return "<!doctype html><html>";
	}

	public String startTop() {
		return "<centre><h1>Unit Test Coverage Breach</h1></centre>";
	}

	public String closeHTML() {
		return "</html>";
	}

	public String returnHead() {
		String head = "<head><style>body{margin:20 auto;font-family:Arial,Helvetica,sans-serif;color:#000}p{background:#6600FF;font-size:2em;"
				+ "margin-bottom:2%;margin-top:2%;text-align:center;width:90%}table,tr{width:90%;border-collapse:collapse;border-spacing:0;"
				+ "font-family:Arial,Helvetica,sans-serif;color:#000;border:1px solid #000;-moz-border-radius:3px;-webkit-border-radius"
				+ ":3px;border-radius:3px;-moz-box-shadow:0 1px 2px #d1d1d1;-webkit-box-shadow:0 1px 2px #d1d1d1;box-shadow:0 1px 2px "
				+ "#d1d1d1;background:#eaebec}tr{border-bottom:1px solid #000}th{padding:15px;border-top:1px solid #000;text-transform:"
				+ "uppercase}td{padding:10px;text-align:center}.critical{background-color:red}.major{background-color:orange}.minor{"
				+ "background-color:#ff0}.info{background-color:#88f879}.tableheader{background-color:#3399FF}</style></head>";

		return head;
	}

	public String writeTableHead() {
		String html = "<tr class=\"tableheader\">" + "<th>FileName</th>" + "<th>Coverage</th>" + "<th>Assigned To</th>"
				+ "</tr>";

		return html;
	}

	public String startTable() {

		return "<table><tbody>";
	}

	public String closeTable() {

		return "</tbody></table>";
	}

	public String createRow(Map<Coverage, Set<String>> result) throws IOException {
		StringBuilder sb = new StringBuilder();
		SetConvertor setconv = new SetConvertor();
		FileUtils fu = new FileUtils();
		Properties prop = fu.getPropValues("junit_config.properties");
		String deltadays = prop.getProperty("deltaDays");
		String link = getLink(deltadays);
		for (Coverage x : result.keySet()) {
			// String key=x.getKey();
			String filename = x.getName();
			String names = setconv.setConvertor(result.get(x));
			sb.append("<tr>");
			sb.append("<td>" + "<a href=" + "http://" + prop.getProperty("sonarHost") + ":"
					+ prop.getProperty("sonarPort") + prop.getProperty("urlForLinkHtml") + link + " >"
					+ filename.toString() + "</a></td>");
			sb.append("<td>" + x.getMsr_frmt_val().toString());
			sb.append("<td>" + names + "</td>");
			sb.append("</tr>");
		}
		return sb.toString();
	}
	
	public String createNewRow(Map<NewCoverage, Set<String>> result) throws IOException {
		StringBuilder sb = new StringBuilder();
		SetConvertor setconv = new SetConvertor();
		FileUtils fu = new FileUtils();
		Properties prop = fu.getPropValues("junit_config.properties");
		String deltadays = prop.getProperty("deltaDays");
		String link = getLink(deltadays);
		for (NewCoverage x : result.keySet()) {
			 String key=x.getKey();
			String filename = x.getName();
			String names = setconv.setConvertor(result.get(x));
			sb.append("<tr>");
			sb.append("<td>" + "<a href=" + "http://" + prop.getProperty("sonarHost") + ":"
					+ prop.getProperty("sonarPort") + prop.getProperty("urlForLinkHtml") + link + " >"
					+ filename.toString() + "</a></td>");
			sb.append("<td>" + String.format("%.2f", Double.parseDouble(x.getValue())));
			sb.append("<td>" + names + "</td>");
			sb.append("</tr>");
		}
		return sb.toString();
	}

	private String getLink(String deltadays) {
		String link;
		switch (deltadays) {
		case "1":
			link = "1";
			break;
		case "3":
			link = "2";
			break;
		case "7":
			link = "3";
			break;
		case "30":
			link = "4";
			break;
		default:
			link = "5";
			break;
		}

		return link;

	}

	public String createHeading(String title, int count) {
		return "<p>" + title + " DEFAULTERS: " + count + "</p><br/>";
	}

	public String createReport(List<Map<Coverage, Set<String>>> results) throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append(htmlInitialiser());
		for (Map<Coverage, Set<String>> result : results) {
			sb.append(this.createRow(result));
		}
		sb.append(this.closeTable());
		sb.append(this.closeHTML());
		return sb.toString();
	}
	
	public String createNewReport(List<Map<NewCoverage, Set<String>>> results) throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append(htmlInitialiser());
		for (Map<NewCoverage, Set<String>> result : results) {
			sb.append(this.createNewRow(result));
		}
		sb.append(this.closeTable());
		sb.append(this.closeHTML());
		return sb.toString();
	}

	private String htmlInitialiser() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.startHTML());
		sb.append(this.returnHead());

		sb.append(this.startTop());
		sb.append(this.startTable());
		sb.append(this.writeTableHead());
		return sb.toString();
	}

}
