package mns.unittest.coverage;

public class Coverage {

	private long id;
	private String key;
	private String name;
	private String scope;
	private String qualifier;
	private String date;
	private String creationdate;
	private String lname;
	private String msr_key;
	private Double msr_val;
	private String msr_frmt_val;

	public Coverage(long id, String key, String name, String scope, String qualifier, String date, String creationdate,
			String lname, String msr_key, Double msr_val, String msr_frmt_val) {
		super();
		this.id = id;
		this.key = key;
		this.name = name;
		this.scope = scope;
		this.qualifier = qualifier;
		this.date = date;
		this.creationdate = creationdate;
		this.lname = lname;
		this.msr_key = msr_key;
		this.msr_val = msr_val;
		this.msr_frmt_val = msr_frmt_val;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
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

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCreationdate() {
		return creationdate;
	}

	public void setCreationdate(String creationdate) {
		this.creationdate = creationdate;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getMsr_key() {
		return msr_key;
	}

	public void setMsr_key(String msr_key) {
		if (msr_key.isEmpty() || msr_key.equals(null))
			this.msr_key = "";
		this.msr_key = msr_key;
	}

	public Double getMsr_val() {
		return msr_val;
	}

	public void setMsr_val(Double msr_val) {
		if (msr_val.equals(null))
			this.msr_val = 0.0;
		this.msr_val = msr_val;
	}

	public String getMsr_frmt_val() {
		return msr_frmt_val;
	}

	public void setMsr_frmt_val(String msr_frmt_val) {
		if (msr_frmt_val.isEmpty() || msr_frmt_val.equals(null))
			this.msr_frmt_val = "";
		this.msr_frmt_val = msr_frmt_val;
	}

	@Override
	public String toString() {
		return "Coverage [id=" + id + ", key=" + key + ", name=" + name + ", scope=" + scope + ", qualifier="
				+ qualifier + ", date=" + date + ", creationdate=" + creationdate + ", lname=" + lname + ", msr_key="
				+ msr_key + ", msr_val=" + msr_val + ", msr_frmt_val=" + msr_frmt_val + "]";
	}

}
