package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Class to store Publication Date
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class PublicationDate {
	private String year;
	private String month;
	private String day;
	private String mediaType;

	public String getYear() {
		return year;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setMonth(String month) {
		this.month = month;
	}

	public String getDay() {
		return day;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setDay(String day) {
		this.day = day;
	}

	@JsonProperty("media-type")
	public String getMediaType() {
		return mediaType;
	}

	@JsonProperty("media-type")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	@Override
	public String toString() {
		return "PublicationDate [year=" + year + ", month=" + month + ", day="
				+ day + ", mediaType=" + mediaType + "]";
	}	
}
