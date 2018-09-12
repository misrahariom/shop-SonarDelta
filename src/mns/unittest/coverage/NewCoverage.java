package mns.unittest.coverage;

public class NewCoverage {
	
	private String id;
	private String key;
	private String name;
	
	private String qualifier;
	private String metric;
	private String value;
	
	
	
	public NewCoverage(String id, String key, String name, String scope, String qualifier, String metric,
			String value) {
		super();
		this.id = id;
		this.key = key;
		this.name = name;
	
		this.qualifier = qualifier;
		this.metric = metric;
		this.value = value;
	}
	
	public NewCoverage() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	public String getMetric() {
		return metric;
	}
	public void setMetric(String metric) {
		this.metric = metric;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
	

}
