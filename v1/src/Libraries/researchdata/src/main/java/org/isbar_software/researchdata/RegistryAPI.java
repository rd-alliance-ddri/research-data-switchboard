package org.isbar_software.researchdata;

import org.isbar_software.rest.RestInterface;

/**
 * Base class for any RDA registry API
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class RegistryAPI extends RestInterface {
	protected static final String APP_KEY = "79b4cd40cde8";
	protected static final String BASE_URL = "http://researchdata.ands.org.au/registry/services/" + APP_KEY + "/";
	
}
