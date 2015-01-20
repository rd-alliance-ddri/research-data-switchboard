package org.grants.scopus.response;

import java.util.List;

import org.grants.scopus.deserilaize.AuthorArrayDeserializer;
import org.grants.scopus.deserilaize.DateArrayDeserializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Entry extends ScopusObject {
	private String scopusEid;
	private String scopusId;
	private String identifier;
	private String title;
	private String crteator;
	private String description;
	private String authkeywords;
	private String openaccess;
	private String pii;
	private String aggregationType;
	private String copyright;
	private String coverDisplayDate;
	private String doi;
	private String isbn;
	private String issn;
	private String eIssn;
	private String volume;
	private String pageRange;
	private String startingPage;
	private String endingPage;
	private String issueIdentifier;
	private String issueName;
	private String publicationName;
	private String teaser;
	private String url;
	private String pubType;
	private String citedbyCount;
	private String subtype;
	private String subtypeDescription;
	private boolean openaccessFlag;
	private List<Author> authors;
	private List<Link> links;
	private List<Date> coverDates;
	private List<Affiliation> affiliations;

	@JsonCreator
	public Entry(
			@JsonProperty("@_fa") final boolean _fa) {
		super(_fa);
	}
	
	public String getScopusEid() {
		return scopusEid;
	}

	@JsonProperty("scopus-eid")
	public void setScopusEid(String scopusEid) {
		this.scopusEid = scopusEid;
	}

	// aliase for scopusEid
	@JsonProperty("eid")
	public void setEid(String eid) {
		this.scopusEid = eid;
	}

	
	public String getScopusId() {
		return scopusId;
	}

	@JsonProperty("scopus-id")
	public void setScopusId(String scopusId) {
		this.scopusId = scopusId;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	@JsonProperty("dc:identifier")
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getTitle() {
		return title;
	}
	
	@JsonProperty("dc:title")
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getCrteator() {
		return crteator;
	}

	@JsonProperty("dc:creator")
	public void setCrteator(String crteator) {
		this.crteator = crteator;
	}

	public String getDescription() {
		return description;
	}

	@JsonProperty("dc:description")
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAuthkeywords() {
		return authkeywords;
	}

	public void setAuthkeywords(String authkeywords) {
		this.authkeywords = authkeywords;
	}
	
	public String getOpenaccess() {
		return openaccess;
	}

	public void setOpenaccess(String openaccess) {
		this.openaccess = openaccess;
	}
	
	public String getPii() {
		return pii;
	}

	public void setPii(String pii) {
		this.pii = pii;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	@JsonProperty("prism:aggregationType")
	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}
	
	public String getCopyright() {
		return copyright;
	}

	@JsonProperty("prism:copyright")
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getCoverDisplayDate() {
		return coverDisplayDate;
	}

	@JsonProperty("prism:coverDisplayDate")
	public void setCoverDisplayDate(String coverDisplayDate) {
		this.coverDisplayDate = coverDisplayDate;
	}
	
	public String getDoi() {
		return doi;
	}
	
	@JsonProperty("prism:doi")
	public void setDoi(String doi) {
		this.doi = doi;
	}
	
	public String getIsbn() {
		return isbn;
	}

	@JsonProperty("prism:isbn")
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getIssn() {
		return issn;
	}

	@JsonProperty("prism:issn")
	public void setIssn(String issn) {
		this.issn = issn;
	}
	
	public String geteIssn() {
		return eIssn;
	}

	@JsonProperty("prism:eIssn")
	public void seteIssn(String eIssn) {
		this.eIssn = eIssn;
	}
	
	public String getVolume() {
		return volume;
	}

	@JsonProperty("prism:volume")
	public void setVolume(String volume) {
		this.volume = volume;
	}
	
	public String getPageRange() {
		return pageRange;
	}

	@JsonProperty("prism:pageRange")
	public void setPageRange(String pageRange) {
		this.pageRange = pageRange;
	}

	public String getStartingPage() {
		return startingPage;
	}

	@JsonProperty("prism:startingPage")
	public void setStartingPage(String startingPage) {
		this.startingPage = startingPage;
	}
	
	public String getEndingPage() {
		return endingPage;
	}

	@JsonProperty("prism:endingPage")
	public void setEndingPage(String endingPage) {
		this.endingPage = endingPage;
	}
	
	public String getIssueIdentifier() {
		return issueIdentifier;
	}

	@JsonProperty("prism:issueIdentifier")
	public void setIssueIdentifier(String issueIdentifier) {
		this.issueIdentifier = issueIdentifier;
	}
	
	public String getIssueName() {
		return issueName;
	}

	@JsonProperty("prism:issueName")
	public void setIssueName(String issueName) {
		this.issueName = issueName;
	}
	
	public String getPublicationName() {
		return publicationName;
	}

	@JsonProperty("prism:publicationName")
	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}
	
	public String getTeaser() {
		return teaser;
	}

	@JsonProperty("prism:teaser")
	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}
	
	public String getUrl() {
		return url;
	}

	@JsonProperty("prism:url")
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getPubType() {
		return pubType;
	}

	public void setPubType(String pubType) {
		this.pubType = pubType;
	}
	
	public String getCitedbyCount() {
		return citedbyCount;
	}

	@JsonProperty("citedby-count")
	public void setCitedbyCount(String citedbyCount) {
		this.citedbyCount = citedbyCount;
	}
	
	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getSubtypeDescription() {
		return subtypeDescription;
	}

	public void setSubtypeDescription(String subtypeDescription) {
		this.subtypeDescription = subtypeDescription;
	}

	public boolean isOpenaccessFlag() {
		return openaccessFlag;
	}
	
	public void setOpenaccessFlag(boolean openaccessFlag) {
		this.openaccessFlag = openaccessFlag;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	@JsonDeserialize(using = AuthorArrayDeserializer.class)
	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}
	
	public List<Link> getLinks() {
		return links;
	}

	@JsonProperty("link")
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	
	public List<Date> getCoverDates() {
		return coverDates;
	}

	@JsonProperty("prism:coverDate")
	@JsonDeserialize(using = DateArrayDeserializer.class)
	public void setCoverDates(List<Date> coverDates) {
		this.coverDates = coverDates;
	}
	
	public List<Affiliation> getAffiliations() {
		return affiliations;
	}

	@JsonProperty("affiliation")
	public void setAffiliations(List<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}

	@Override
	public String toString() {
		return "Entry [scopusEid=" + scopusEid + ", scopusId=" + scopusId
				+ ", identifier=" + identifier + ", title=" + title
				+ ", crteator=" + crteator + ", authkeywords=" + authkeywords
				+ ", openaccess=" + openaccess + ", pii=" + pii
				+ ", aggregationType=" + aggregationType + ", copyright="
				+ copyright + ", coverDisplayDate=" + coverDisplayDate
				+ ", doi=" + doi + ", isbn=" + isbn + ", issn=" + issn
				+ ", volume=" + volume + ", startingPage=" + startingPage
				+ ", endingPage=" + endingPage + ", issueIdentifier="
				+ issueIdentifier + ", issueName=" + issueName
				+ ", publicationName=" + publicationName + ", teaser=" + teaser
				+ ", url=" + url + ", openaccessFlag=" + openaccessFlag
				+ ", authors=" + authors + ", links=" + links + ", coverDates="
				+ coverDates + "]";
	}
}
