var serverUri = "http://localhost:7474/db/data/";
var cypherUri = serverUri + "cypher";
var nodeUri = serverUri + "node/";
var relationshipUri = serverUri + "relationship/";

function getVersion(callback) {
	$.getJSON( serverUri )
		.done(function( data ) {
			callback(data.neo4j_version);
		});
}

/*
function getVersion(callback) {
//.header(" Access-Control-Allow-Origin:", "*")
	d3.json(serverUri, function(error, data) {
		if (error != null)
			alert(error);
		else
			callback(data.neo4j_version);
	 });
}
*/

// MATCH (n1:Identifier)-[r1:IDENTIFIES]->(n2:Institution)<-[r2:ADMIN_INSTITUTE]-(n3)<-[r3:INVESTIGATOR]-(n4) 
// WHERE n2.name =~ ".*Monash.*" 
// RETURN n1, n2, n3, n4, r1, r2, r3 
// LIMIT 25


function cypher(query, params, callback) {
	/*
	d3.json(cypherUri)
		.header("Content-Type", "application/json")
		.on("error", function(data) {
			alert("x: " + JSON.stringify(data));
		})
		.post(JSON.stringify({ query : query, params : params }), function(error, data) {
			if (error != null) {
				alert(JSON.stringify(error));
			}
			else
				callback(data);
		});
	*/
	
	$.post(cypherUri, { query : query, params : params }, callback, "json")
		.error( function (jqXHR, status, error) {
			alert("Cypher error: " + jqXHR.responseJSON.message);
		})
}

//"http://localhost:7474/db/data/node/171",
function isNode(obj) {
	return obj.self != undefined && obj.self.indexOf(nodeUri) == 0;		
}

//"http://localhost:7474/db/data/relationship/508"
function isRelationship(obj) {
	return obj.self != undefined && obj.self.indexOf(relationshipUri) == 0;	
}

function parseInstitutionResponse(response) {
	var nodes = [];
	
	var columns = response.data;
	var columnCount = columns.length;
	
	for (var nColumn = 0; nColumn < columnCount; ++nColumn) {
		var data = columns[nColumn];
		var current = null;
		
		// we expect to have 3 object for this replay: institution, grant and researcher
		if (isNode(data[0]) && isNode(data[1]) && isNode(data[2])) {
			
			// lets find our institution in the array
			var instCount = nodes.length;
			var instIndex = -1;
			for (var n = 0; n < instCount; ++n) 
				if (nodes[n].self == data[0].self) {
					instIndex = n;
					break;
				}	
			
			// if institution does not exists, create it
			if (instIndex < 0) {
				instIndex = instCount;
				var name = data[0].data.hasOwnProperty("name") ? 
						data[0].data.name : "Institution";
				
				nodes.push({ 
					self : data[0].self, 
					name : name,
					class : "Institution",
					size : 100,
					color : "#0066FF",
					children : []
				});
			}
			
			// now lets find grant in the institution array
			var grantsCount = nodes[instIndex].children.length;
			var grantIndex = -1;
			for (var n = 0; n < grantsCount; ++n) 
				if (nodes[instIndex].children[n].self == data[1].self) {
					grantIndex = n;
					break;
				}
			
			// if grant does not exists, create it as well
			if (grantIndex < 0) {
				grantIndex = grantsCount;
				var name = data[1].data.hasOwnProperty("simplified_title") ? 
						data[1].data.simplified_title : 
							data[1].data.hasOwnProperty("scientific_title") ?
						data[1].data.scientific_title : "Grant";
						
				nodes[instIndex].children.push({
					self : data[1].self, 
					name : name,
					class : "Grant",
					size : 100,
					color : "#FF4D4D",
					children : []
				});
			}
			
			// now lets find researcher
			var resCount = nodes[instIndex].children[grantIndex].children.length;
			var resIndex = -1;
			for (var n = 0; n < resCount; ++n) 
				if (nodes[instIndex].children[grantIndex].children[n].self == data[2].self) {
					resIndex = n;
					break;
				}
			
			// if researcher does not exists, create it as well
			if (resIndex < 0) {
				resIndex = resCount;
				var name = data[2].data.hasOwnProperty("full_name") ? 
						data[2].data.full_name : "Researcher";
						
				nodes[instIndex].children[grantIndex].children.push({
					self : data[2].self, 
					name : name,
					class : "Researcher",
					size : 100,
					color : "#00CC00"
					
				});
			}
		}
	}
	
	return nodes;	
}


// {nodeId : parseInt(institutionId), maxNodes: parseInt(maxNodes)}
function getInstitutionGraphById(institutionId, maxNodes, callback) {
//	cypher("START n1=node({id}) MATCH (n1:Institution)<-[r1:ADMIN_INSTITUTE]-(n2)<-[r2:INVESTIGATOR]-(n3) RETURN n1, n2, n3, r1, r2 LIMIT {l}", {id : parseInt(institutionId), l : parseInt(maxNodes)}, function(data) {
	cypher("START n1=node(" + institutionId + ") MATCH (n1:Institution)<-[r1:ADMIN_INSTITUTE]-(n2)<-[r2:INVESTIGATOR]-(n3) RETURN n1, n2, n3 LIMIT " + maxNodes, {}, function(data) {
		callback(parseInstitutionResponse(data));
	});
}

function getInstitutionGraphByNla(institutionNla, maxNodes, callback) {
/*	cypher("START n1=node({nodeId}) MATCH (n1)<-[:IDENTIFIES]-(n2:Identifier) WHERE n2.type = {type} RETURN n2.identifier",
			nodeId : parseInt(institutionId), maxNodes: parseInt(maxNodes), function (data) {
				alert("Data returned: " + JSON.stringify(data));
			});*/
}

function getInstitutionGraphByName(institutionNla, maxNodes, callback) {
	cypher("START n1=node({nodeId}) MATCH (n1)<-[:IDENTIFIES]-(n2:Identifier) WHERE n2.type = {type} RETURN n2.identifier",
			{ nodeId : institutionId, type : identifierType }, function (data) {
				alert("Data returned: " + JSON.stringify(data));
			});
}


function getAnyInstitutionGraph(maxNodes, callback) {
	cypher("START n1=node({nodeId}) MATCH (n1)<-[:IDENTIFIES]-(n2:Identifier) WHERE n2.type = {type} RETURN n2.identifier",
			{ nodeId : institutionId, type : identifierType }, function (data) {
				alert("Data returned: " + JSON.stringify(data));
			});
}


// START n1=node(29) MATCH (n1)<-[:IDENTIFIES]-(n2:Identifier) WHERE n2.type = "NLA" RETURN n2.identifier 
function getInstitutionIdetifier(institutionId, identifierType, callback) {
	cypher("START n1=node({nodeId}) MATCH (n1)<-[:IDENTIFIES]-(n2:Identifier) WHERE n2.type = {type} RETURN n2.identifier",
			{ nodeId : institutionId, type : identifierType }, function (data) {
				alert("Data returned: " + JSON.stringify(data));
			});
}

/*
function createRequest() {
  var result = null;
  if (window.XMLHttpRequest) {
    // FireFox, Safari, etc.
    result = new XMLHttpRequest();
    if (typeof xmlhttp.overrideMimeType != 'undefined') {
      result.overrideMimeType('text/xml'); // Or anything else
    }
  }
  else if (window.ActiveXObject) {
    // MSIE
    result = new ActiveXObject("Microsoft.XMLHTTP");
  } 
  else {
    // No known mechanism -- consider aborting the application
  }
  return result;
}

function getData() {
	var req = createRequest(); // defined above
	// Create the callback:
	req.onreadystatechange = function() {
		if (req.readyState != 4) return; // Not there yet
		if (req.status != 200) {
		// Handle request failure here...
			return;
		}
	    	  // Request successful, read the response
	   var resp = req.responseText;
	   // ... and use it as needed by your app.
	}

	/*req.open("GET", url, true);
	req.send();

	req.open("POST", url, true);
	req.setRequestHeader("Content-Type",
	                     "application/x-www-form-urlencoded");
	req.send(form-encoded request body);* /

}*/

