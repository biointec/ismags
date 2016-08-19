package junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.Test;

import ISMAGS.CommandLineInterface;
import algorithm.MotifFinder;
import motifs.Motif;
import motifs.MotifInstance;
import network.LinkType;
import network.Network;
import network.Node;

public class ISMAGSTest {
	final String inputFiles = "X u A A Xu.txt Y u B B Yu.txt Z d A B Zd.txt";
	final String motifspec = "XXXZ000Z0Y00ZYY";
	final String dataFolder = "./networks/";
	
	final int expectedNumberOfInstances = 28362;

	
	/**
	 * Run the algorithm on test data for all tests in this unit
	 */
	public ISMAGSTest() {
		doTestRun();
	}

	/**
	 * Found Motif instances
	 */
	private Set<MotifInstance> motifInstances;

	/**
	 * Used motif
	 */
	private Motif motif;

	/**
	 * MotifFinder that does this test run
	 */
	private MotifFinder mf;

	/**
	 * Run a sample test on included data.
	 */
	private void doTestRun() {
		ArrayList<String> linkfiles = new ArrayList<String>();
		ArrayList<String> linkTypes = new ArrayList<String>();
		ArrayList<String> sourcenetworks = new ArrayList<String>();
		ArrayList<String> destinationnetworks = new ArrayList<String>();
		ArrayList<Boolean> directed = new ArrayList<Boolean>();
		StringTokenizer st = new StringTokenizer(inputFiles, " ");
		while (st.hasMoreTokens()) {
			linkTypes.add(st.nextToken());
			directed.add(st.nextToken().equals("d"));
			sourcenetworks.add(st.nextToken());
			destinationnetworks.add(st.nextToken());
			linkfiles.add(dataFolder + st.nextToken());
		}
		ArrayList<LinkType> allLinkTypes = new ArrayList<LinkType>();
		HashMap<Character, LinkType> typeTranslation = new HashMap<Character, LinkType>();
		for (int i = 0; i < linkTypes.size(); i++) {
			String n = linkTypes.get(i);
			char nn = n.charAt(0);
			LinkType t = typeTranslation.get(nn);
			if (t == null) {
				t = new LinkType(directed.get(i), n, i, nn, sourcenetworks.get(i), destinationnetworks.get(i));
			}
			allLinkTypes.add(t);
			typeTranslation.put(nn, t);
		}

		try {
			Network network = Network.readNetworkFromFiles(linkfiles, allLinkTypes);
			this.motif = CommandLineInterface.getMotif(motifspec, typeTranslation);

			this.mf = new MotifFinder(network);
			this.motifInstances = mf.findMotif(motif, true);
			System.out.println("Found motif instances: "+this.motifInstances.size());
			
		} catch (FileNotFoundException e) {
			fail("Could not find test data");
		} catch (IOException e) {
			fail("Could not read test data");
		}

	}

	/**
	 * Basic check if the output is as expected for this test data
	 */
	@Test
	public void testGeneralRun(){
		assertEquals(expectedNumberOfInstances, this.motifInstances.size());		
	}
	
	/**
	 * Checks if the list of edges stored in {@link MotifFinder.usedLinks} are
	 * the same edges as used by the instances from
	 * {@link MotifFinder.findMotif()}
	 */
	@Test
	public void testResultSet() {
		Set<Set<Node>> usedLinks = mf.getUsedLinks();
		assertTrue("usedLinks should be stored", 
				usedLinks !=null && usedLinks.size() > 0);
		System.out.println("Used links: "+usedLinks.size());
		
		Set<Set<Node>> resultLinks = this.findResultSet();
		
		assertEquals("links found in the motifs should be the same as stored in usedLinks", 
				resultLinks, usedLinks);
		
	}
	
	
	
	/**
	 * Access point for recursively going over nodes and edges of a {@link MotifInstance} 
	 * 
	 * @param motif the {@link Motif}, used to know the structure of the {@link MotifInstance}
	 * @param nodeArray array of {@link Node}s in a {@link MotifInstance}
	 * @param resultConnections {@link Set} of found edges
	 */
	private void iterateMotifInstance(Motif motif, Node[] nodeArray, HashSet<Set<Node>> resultConnections){
		ArrayList<Integer> visited = new ArrayList<Integer>(nodeArray.length);
		registerNodeAndNeighbors(0, motif, nodeArray, resultConnections, visited);
	}
	
	/**
	 * Recursively visiting a node of a {@link MotifInstance} and its neighbors 
	 * 
	 * @param nodeNr position of the {@link Node} in the node array
	 * @param motif the {@link Motif}, used to know the structure of the {@link MotifInstance}
	 * @param nodeArray array of {@link Node}s in a {@link MotifInstance}
	 * @param resultConnections {@link Set} of found edges
	 * @param visited list of nodes already visited in this {@link MotifInstance} iteration
	 */
	private void registerNodeAndNeighbors(int nodeNr, Motif motif, Node[] nodeArray, HashSet<Set<Node>> resultConnections, ArrayList<Integer> visited){
		visited.add(nodeNr);
		Node node = nodeArray[nodeNr];
		
		for (int j : motif.getConnectionsOfMotifNode(nodeNr)){
			HashSet<Node> nodePair = new HashSet<Node>();
			Node toNode = nodeArray[j];
			nodePair.add(node);
			nodePair.add(toNode);
			resultConnections.add(nodePair);
			if (!visited.contains(j)){
				registerNodeAndNeighbors(j, motif, nodeArray, resultConnections, visited);
			}
		}
	}
	
	/**
	 * Iterate the {@link MotifInstance}s from a run and list all edges as node sets.
	 * 
	 * @return a Set of node pairs
	 */
	private Set<Set<Node>> findResultSet(){
		HashSet<Set<Node>> resultConnections = new HashSet<Set<Node>>();
				
		for (MotifInstance mi : motifInstances){
			Node[] nodeArray = mi.getNodeArray();
			iterateMotifInstance(motif, nodeArray, resultConnections);
		}
		return resultConnections;
	}


}
