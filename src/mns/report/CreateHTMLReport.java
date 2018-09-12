package mns.report;

import java.util.ArrayList;

public class CreateHTMLReport {

	public String startHTML() {
		return "<!doctype html><html>";
	}

	public String closeHTML() {
		return "</html>";
	}

	public String returnHead() {
		String head = "<head><style>body{margin:20 auto;font-family:Arial,Helvetica,sans-serif;color:#000}p{background:#6600FF;font-size:2em;"
				+ "margin-bottom:2%;margin-top:2%;text-align:center;width:90%}table{width:90%;border-collapse:collapse;border-spacing:0;"
				+ "font-family:Arial,Helvetica,sans-serif;color:#000;border:1px solid #000;-moz-border-radius:3px;-webkit-border-radius"
				+ ":3px;border-radius:3px;-moz-box-shadow:0 1px 2px #d1d1d1;-webkit-box-shadow:0 1px 2px #d1d1d1;box-shadow:0 1px 2px "
				+ "#d1d1d1;background:#eaebec}tr{border-bottom:1px solid #000}th{padding:15px;border-top:1px solid #000;text-transform:"
				+ "uppercase}td{padding:10px;text-align:center}.critical{background-color:red}.major{background-color:orange}.minor{"
				+ "background-color:#ff0}.info{background-color:#88f879}.tableheader{background-color:#3399FF}</style></head>";

		return head;
	}

	public String writeTableHead() {
		String html = "<tr class=\"tableheader\">" + "<th>Severity</th>" + "<th>Class</th>"
				+ "<th>Violation</th>" + "<th>Line no</th>" + "<th>Assigned To</th>" + "<th>Project</th>" + "</tr>";

		return html;
	}

	public String startTable() {

		return "<table><tbody>";
	}

	public String closeTable() {

		return "</tbod></table>";
	}

	public String createRow(Issue issue) {
		StringBuilder sb = new StringBuilder();
		sb.append("<tr class=\"");
		sb.append(issue.getSeverity().toLowerCase());
		sb.append("\">");
		sb.append("<td>" + issue.getSeverity().toUpperCase() + "</td>");
		sb.append("<td>" + issue.getClassName() + "</td>");
		sb.append("<td><a href=\"#\" data-toggle=\"tooltip\" title=\"" + issue.getMessage() + "\">"
			//	+ issue.getVoilation() + "</a></td>");
				+ issue.getMessage() + "</a></td>");
		
		if(issue.getLineNo() > 0) {
			sb.append("<td>" + issue.getLineNo() + "</td>");
		} else {
			sb.append("<td>NA</td>");
		}
		if(null != issue.getAssignee()) {
			sb.append("<td>" + issue.getAssignee().getName() + "</td>");
		} else {
			sb.append("<td>Not Assigned</td>");
		}
		sb.append("<td>" + issue.getProject().substring(4).toUpperCase() + "</td>");
		sb.append("</tr>");
		return sb.toString();
	}
	
	public String createHeading(String title, int count) {
		return "<p>"+title+" ISSUES: " + count + "</p><br/>";
	}
	
	public String createReport(ArrayList<ArrayList<Issue>> arr) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(this.startHTML());
		sb.append(this.returnHead());

		StringBuilder open = new StringBuilder();
		StringBuilder close = new StringBuilder();

		open.append(this.startTable());
		open.append(this.writeTableHead());
		close.append(this.startTable());
		close.append(this.writeTableHead());
		int openIssues = 0;
		int closeIssues = 0;
		for (int i = 0; i < arr.size(); i++) {
			for (int j = 0; j < arr.get(i).size(); j++) {
				Issue issue = arr.get(i).get(j);
				if("open".equalsIgnoreCase(issue.getStatus())) {
					open.append(this.createRow(issue));
					openIssues++;
				} else if("closed".equalsIgnoreCase(issue.getStatus())) {
					close.append(this.createRow(issue));
					closeIssues++;
				}
			}
		}
		
		open.append(this.closeTable());
		close.append(this.closeTable());
		if(openIssues > 0) {
			sb.append(createHeading("OPEN",openIssues));
			sb.append(open.toString());
			sb.append("<br/>");
		}
		
		if(closeIssues > 0) {
			sb.append(createHeading("CLOSED", closeIssues));
			sb.append(close.toString());	
		}

		sb.append(this.closeHTML());
		return sb.toString();
	}

}
