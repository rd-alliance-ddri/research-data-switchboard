package org.grants.exports.export;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.entity.RestNode;

public class Export {

	protected static final String FIELD_DOI = "doi";
	protected static final String FIELD_NAME = "name";
	protected static final String FIELD_SCIENTIFIC_TITLE = "scientific_title";
	protected static final String FIELD_FULL_NAME = "full_name";
		
	protected static enum RelTypes implements RelationshipType
    {
    	AdminInstitute, Investigator, KnownAs, RelatedTo
    }
	
	protected CompiledNode CreateInstitution(RestNode node) {
		return new CompiledNode(node, 
				CompiledNode.NodeType.Institution, 
				(String) node.getProperty(FIELD_NAME));
	}
	
	protected CompiledNode CreateGrant(RestNode node) {
		return new CompiledNode(node, 
				CompiledNode.NodeType.Grant, 
				(String) node.getProperty(FIELD_SCIENTIFIC_TITLE));
	}
	
	protected CompiledNode CreateResearcher(RestNode node) {
		return new CompiledNode(node, 
				CompiledNode.NodeType.Researcher, 
				(String) node.getProperty(FIELD_FULL_NAME));
	}
	
	protected CompiledNode CreateDataset(RestNode node) {
		return new CompiledNode(node, 
				CompiledNode.NodeType.Dataset, 
				(String) node.getProperty(FIELD_DOI));
	}
	

	protected void QueryAllKnownAsNodes(RestNode node, CompiledNode compiledNode) {
		// Get all KnownAs Relationships
		Iterable<Relationship> rels = node.getRelationships(RelTypes.KnownAs);
		if (null != rels) 
			for (Relationship rel : rels) {
				// Extract start and end node
				RestNode startNode = (RestNode) rel.getStartNode();
				if (!compiledNode.isNodeExists(startNode.getId())) {
					compiledNode.addRestNode(startNode);						
					QueryAllKnownAsNodes(startNode, compiledNode);
				}
				
				RestNode endNode = (RestNode) rel.getEndNode();
				if (!compiledNode.isNodeExists(endNode.getId())) {
					compiledNode.addRestNode(endNode);						
					QueryAllKnownAsNodes(endNode, compiledNode);
				}
			}
	}		
	
	protected List<CompiledNode> QueryAllDatasetResearchers(CompiledNode dataset) {
		List<CompiledNode> researchers = null;
		
		for (RestNode nodeDataset : dataset.getNodes()) {
			Iterable<Relationship> relResearchers = nodeDataset.getRelationships(
					RelTypes.RelatedTo, Direction.INCOMING);
			
			if (null != relResearchers) 
				for (Relationship relResearcher : relResearchers) {
					
					// Extract only start node because we know direction
					RestNode nodeResearcher = (RestNode) relResearcher.getStartNode();
					
					// create list if needed
					if (null == researchers)
						researchers = new ArrayList<CompiledNode>();
					
					// check what we do not have this researcher
					CompiledNode researcher = CompiledNode.findNodeInList(researchers, nodeResearcher.getId());
					if (null == researcher)
					{
						// create new researcher node
						researcher = CreateResearcher(nodeResearcher);
						
						// Query all known as researchers
						QueryAllKnownAsNodes(nodeResearcher, researcher);
						
						// add it to the list
						researchers.add(researcher);
					}
				}			
		} 
		
		return researchers;
	}
	
	protected List<CompiledNode> QueryAllResearcherGrants(CompiledNode researcher) {
		List<CompiledNode> grants = null;
		
		for (RestNode nodeResearcher : researcher.getNodes()) {
			Iterable<Relationship> relGrants = nodeResearcher.getRelationships(
					RelTypes.Investigator, Direction.OUTGOING);
			
			if (null != relGrants) 
				for (Relationship relGrant : relGrants) {
					
					// Extract only end node because we know direction
					RestNode nodeGrant = (RestNode) relGrant.getEndNode();
					
					// create list if needed
					if (null == grants)
						grants = new ArrayList<CompiledNode>();
					
					// check what we do not have this researcher
					CompiledNode grant = CompiledNode.findNodeInList(grants, nodeGrant.getId());
					if (null == grant)
					{
						// add it to the list
						grants.add(CreateGrant(nodeGrant));
					}
				}	
		}
		
		return grants;
	}
	
	protected List<CompiledNode> QueryAllResearchersDatasets(CompiledNode researcher) {
		List<CompiledNode> datasets = null;
		
		for (RestNode nodeResearcher : researcher.getNodes()) {
			Iterable<Relationship> relDatatsets = nodeResearcher.getRelationships(
					RelTypes.RelatedTo, Direction.OUTGOING);
			
			if (null != relDatatsets) 
				for (Relationship relDatatset : relDatatsets) {
					
					// Extract only end node because we know direction
					RestNode nodeDataset = (RestNode) relDatatset.getEndNode();
					
					// create list if needed
					if (null == datasets)
						datasets = new ArrayList<CompiledNode>();
					
					// check what we do not have this researcher
					CompiledNode dataset = CompiledNode.findNodeInList(datasets, nodeDataset.getId());
					if (null == dataset)
					{
						// create new researcher node
						dataset = CreateDataset(nodeDataset);
						
						// Query all known as researchers
						QueryAllKnownAsNodes(nodeDataset, dataset);
						
						// add it to the list
						datasets.add(dataset);					}
				}	
		}
		
		return datasets;
	}
	
	protected List<CompiledNode> QueryAllGrantInstitutions(CompiledNode grant) {
		List<CompiledNode> insitutions = null;
		
		for (RestNode nodeGrant : grant.getNodes()) {
			Iterable<Relationship> relInsitutions = nodeGrant.getRelationships(
					RelTypes.AdminInstitute, Direction.OUTGOING);
			
			if (null != relInsitutions) 
				for (Relationship relInsitution : relInsitutions) {
					
					// Extract only end node because we know direction
					RestNode nodeInstitution = (RestNode) relInsitution.getEndNode();
					
					// create list if needed
					if (null == insitutions)
						insitutions = new ArrayList<CompiledNode>();
					
					// check what we do not have this researcher
					CompiledNode insitution = CompiledNode.findNodeInList(insitutions, nodeInstitution.getId());
					if (null == insitution)
					{
						// create new institution node
						insitution = CreateInstitution(nodeInstitution);
						
						// Query all known as insitutions
						QueryAllKnownAsNodes(nodeInstitution, insitution);
						
						// add it to the list
						insitutions.add(insitution);
					}
				}	
		}
		
		return insitutions;
	}
	
	protected List<CompiledNode> QueryAllGrantResearchers(CompiledNode grant) {
		List<CompiledNode> researchers = null;
		
		for (RestNode nodeGrant : grant.getNodes()) {
			Iterable<Relationship> relResearchers = nodeGrant.getRelationships(
					RelTypes.Investigator, Direction.INCOMING);
			
			if (null != relResearchers) 
				for (Relationship relResearcher : relResearchers) {
					
					// Extract only start node because we know direction
					RestNode nodeResearcher = (RestNode) relResearcher.getStartNode();
					
					// create list if needed
					if (null == researchers)
						researchers = new ArrayList<CompiledNode>();
					
					// check what we do not have this researcher
					CompiledNode researcher = CompiledNode.findNodeInList(researchers, nodeResearcher.getId());
					if (null == researcher)
					{
						// create new researcher node
						researcher = CreateResearcher(nodeResearcher);
						
						// Query all known as researchers
						QueryAllKnownAsNodes(nodeResearcher, researcher);
						
						// add it to the list
						researchers.add(researcher);
					}
				}	
		}
		
		return researchers;
	}
	
	
}
