package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Class to store Orcid History
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class OrcidHistory {
	private String creationMethod;
	private String completionDate;
	private String submissionDate;
	private String lastModifiedDate;
	private String claimed;
	private String source;
	private String visibility;
	
	@JsonProperty("creation-method")
	public String getCreationMethod() {
		return creationMethod;
	}
	
	@JsonProperty("creation-method")
	public void setCreationMethod(String creationMethod) {
		this.creationMethod = creationMethod;
	}
	
	@JsonProperty("completion-date")
	public String getCompletionDate() {
		return completionDate;
	}
	
	@JsonProperty("completion-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCompletionDate(String completionDate) {
		this.completionDate = completionDate;
	}
	
	@JsonProperty("submission-date")
	public String getSubmissionDate() {
		return submissionDate;
	}
	
	@JsonProperty("submission-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setSubmissionDate(String submissionDate) {
		this.submissionDate = submissionDate;
	}
	
	@JsonProperty("last-modified-date")
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	@JsonProperty("last-modified-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public String getClaimed() {
		return claimed;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setClaimed(String claimed) {
		this.claimed = claimed;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getVisibility() {
		return visibility;
	}
	
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	
	@Override
	public String toString() {
		return "OrcidHistory [creationMethod=" + creationMethod
				+ ", completionDate=" + completionDate + ", submissionDate="
				+ submissionDate + ", lastModifiedDate=" + lastModifiedDate
				+ ", claimed=" + claimed + ", source=" + source
				+ ", visibility=" + visibility + "]";
	}
}
