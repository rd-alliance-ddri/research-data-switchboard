package org.grants.scopus.response;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "intid" })
public class Coredata {
	private final String citedByCount;
	private final Author[] creators;
	private final String description;
	private final String identifier;
	private final String title;
	private final Link[] links;
	private final String aggregationType;
	private final String coverDate;
	private final String doi;
	private final String endingPage;
	private final String issn;
	private final String issueIdentifier;
	private final String pageRange;
	private final String publicationName;
	private final String startingPage;
	private final String url;
	private final String volume;
	private final String pubmedId;
	private final String srcType;

	@JsonCreator
	public Coredata(@JsonProperty("citedby-count") final String citedByCount, 
			@JsonProperty("dc:creator") final Author[] creators, 
			@JsonProperty("dc:description") final String description,
			@JsonProperty("dc:identifier") final String identifier, 
			@JsonProperty("dc:title") final String title,
			@JsonProperty("link") final Link[] links, 
			@JsonProperty("prism:aggregationType") final String aggregationType,
			@JsonProperty("prism:coverDate") final String coverDate, 
			@JsonProperty("prism:doi") final String doi, 
			@JsonProperty("prism:endingPage") final String endingPage, 
			@JsonProperty("prism:issn") final String issn,
			@JsonProperty("prism:issueIdentifier") final String issueIdentifier, 
			@JsonProperty("prism:pageRange") final String pageRange, 
			@JsonProperty("prism:publicationName") final String publicationName,
			@JsonProperty("prism:startingPage") final String startingPage, 
			@JsonProperty("prism:url") final String url, 
			@JsonProperty("prism:volume") final String volume, 
			@JsonProperty("pubmed-id") final String pubmedId,
			@JsonProperty("srctype") final String srcType) {
		
		this.citedByCount = citedByCount;
		this.creators = creators;
		this.description = description;
		this.identifier = identifier;
		this.title = title;
		this.links = links;
		this.aggregationType = aggregationType;
		this.coverDate = coverDate;
		this.doi = doi;
		this.endingPage = endingPage;
		this.issn = issn;
		this.issueIdentifier = issueIdentifier;
		this.pageRange = pageRange;
		this.publicationName = publicationName;
		this.startingPage = startingPage;
		this.url = url;
		this.volume = volume;
		this.pubmedId = pubmedId;
		this.srcType = srcType;
	}

	public String getCitedByCount() {
		return citedByCount;
	}

	public Author[] getCreators() {
		return creators;
	}

	public String getDescription() {
		return description;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public String getTitle() {
		return title;
	}

	public Link[] getLinks() {
		return links;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	public String getCoverDate() {
		return coverDate;
	}

	public String getDoi() {
		return doi;
	}

	public String getEndingPage() {
		return endingPage;
	}

	public String getIssn() {
		return issn;
	}

	public String getIssueIdentifier() {
		return issueIdentifier;
	}

	public String getPageRange() {
		return pageRange;
	}

	public String getPublicationName() {
		return publicationName;
	}

	public String getStartingPage() {
		return startingPage;
	}

	public String getUrl() {
		return url;
	}

	public String getVolume() {
		return volume;
	}

	public String getPubmedId() {
		return pubmedId;
	}

	public String getSrcType() {
		return srcType;
	}

	@Override
	public String toString() {
		return "Coredata [citedByCount=" + citedByCount + ", creators="
				+ creators + ", description=" + description + ", identifier="
				+ identifier + ", links=" + Arrays.toString(links)
				+ ", aggregationType=" + aggregationType + ", coverDate="
				+ coverDate + ", doi=" + doi + ", endingPage=" + endingPage
				+ ", issn=" + issn + ", issueIdentifier=" + issueIdentifier
				+ ", pageRange=" + pageRange + ", publicationName="
				+ publicationName + ", startingPage=" + startingPage + ", url="
				+ url + ", volume=" + volume + ", pubmedId=" + pubmedId
				+ ", srcType=" + srcType + "]";
	}
}
