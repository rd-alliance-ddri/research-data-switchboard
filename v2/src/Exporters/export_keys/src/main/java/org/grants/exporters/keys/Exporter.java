package org.grants.exporters.keys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import scala.Option;
import scala.collection.immutable.Map;

import org.grants.neo4j.local.Neo4jUtils;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import au.com.bytecode.opencsv.CSVWriter;

public class Exporter {
	private GraphDatabaseService graphDb;
	private ExecutionEngine engine;
	
	private File exportFolder;
	
	public Exporter(String dbFolder, final String outputFolder) {
		System.out.println("Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
	
		graphDb = Neo4jUtils.getReadOnlyGraphDb(dbFolder);
		engine = Neo4jUtils.getExecutionEngine(graphDb);
		
		// Set output folder
		exportFolder= new File(outputFolder);
		exportFolder.mkdirs();
	}	
	
	public void process(String label1, String label2, int minRels, int maxRels, int pagination, boolean header) throws IOException {
		long keysCounter = 0;
		long rowCounter = 0;
		long fileCounter = 0;
		
		CSVWriter writer = null;

		long beginTime = System.currentTimeMillis();
		
		StringBuilder sb = new StringBuilder();
		sb.append("MATCH (n1:`");
		sb.append(label1);
		sb.append("`)-[*");
		if (minRels > 0 || maxRels > 0) {
			if (minRels == maxRels) 
				sb.append(minRels);
			else {
				if (minRels > 0)
					sb.append(minRels);
				sb.append("..");
				if (maxRels > 0)
					sb.append(maxRels);
			}
		}
		sb.append("]-(n2:`");
		sb.append(label2);
		sb.append("`) RETURN n1.key AS key1, n2.key AS key2");
		
		String cypher = sb.toString();
		System.out.println(cypher);
				
		
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ExecutionResult result = engine.execute( cypher );
			while (result.hasNext()) {
				Map<String, Object> row = result.next();
				
				if (null == writer) {
					String fileName =  label1 + "_" + label2;
					if (pagination > 0)
						fileName += "_" + fileCounter++;
					fileName += ".csv";
					File file = new File(exportFolder, fileName);
					writer = new CSVWriter(new FileWriter(file));

					if (header)
						writer.writeNext(new String[] {label1, label2});
				}					
				
				String key1 = (String) row.get("key1").get();
				String key2 = (String) row.get("key2").get();

				System.out.println(key1 + " - " + key2);
				
				++rowCounter;
				
				writer.writeNext(new String[] {key1, key2});
				
				if (pagination > 0 && rowCounter >= pagination) {
					writer.close();
					writer = null;
					
					keysCounter += rowCounter;
					rowCounter = 0;
				}
			}
		}
		
		if (null != writer)
			writer.close();
		
		keysCounter += rowCounter;
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d keys over %d ms. Average %f ms per key", 
				keysCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)keysCounter));
	}
}
