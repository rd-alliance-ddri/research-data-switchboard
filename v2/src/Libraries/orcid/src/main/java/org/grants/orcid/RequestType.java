package org.grants.orcid;

/**
 * RequestType Enum 
 * <p>
 * Supporterd Types:
 * <br>bio - Default Orcid request, return bio information only
 * <br>works - Return works list
 * <br>record - Return record information. Currently not supported.
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public enum RequestType {
	bio, works, record
}
