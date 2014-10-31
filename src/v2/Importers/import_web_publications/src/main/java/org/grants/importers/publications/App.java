package org.grants.importers.publications;

import javax.xml.bind.JAXBException;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			Importer importer = new Importer();
			importer.importData();
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
