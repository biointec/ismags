/* 
 * Copyright (C) 2013 Maarten Houbraken
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Software available at https://github.com/mhoubraken/ISMAGS
 * Author : Maarten Houbraken (maarten.houbraken@intec.ugent.be)
 */
package network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import motifs.MotifLink;
import java.util.Map;

/**
 *
 * @author mhoubraken
 */
public class Network {
//    Map<Integer,Link> links;
//    Map<Integer,Node> nodesByID;

    private Map<String, Node> nodesByDescription;
    private int nrLinks = 0;
    private Map<MotifLink, Set<Node>> nodeSetsDepartingFromLink;
    private Map<MotifLink, ArrayList<Node>> nodesWithLink;
    
    
    
    /**
     * Creates a new network without edges or nodes
     */
    public Network() {
//        nodesByID=new HashMap<Integer,Node>();
        nodesByDescription = new HashMap<String, Node>();
//        links=new HashMap<Integer,Link>();
        nodeSetsDepartingFromLink = new HashMap<MotifLink, Set<Node>>();

    }

    public void addNode(Node n) {
//        nodesByID.put(n.getID(), n);
        nodesByDescription.put(n.getDescription(), n);
    }

    public Node getNodeByDescription(String s) {
        return nodesByDescription.get(s);
    }

    
    
    public Map<String, Node> getNodesByDescription() {
		return nodesByDescription;
	}

	/**
     * Adds a link to the network and updates the sets of nodes with edges of
     * the related type
     *
     * @param link
     */
    public void addLink(Link link) {
//        links.put(l.getID(), l);
        nrLinks++;
        int typeID = link.getType().getMotifLink().getMotifLinkID();
        Set<Node> setOfLink = getSetOfType(link.getType().getMotifLink());
        setOfLink.add(link.getStart());

        if (link.getType().isDirected()) {
            Set<Node> reverseSet = getSetOfType(link.getType().getInverseMotifLink());
            reverseSet.add(link.getEnd());
        } else {
            setOfLink.add(link.getEnd());
        }
        //adding to nodes
        ArrayList<Node> nodeList = link.getStart().neighboursPerType.get(typeID);
        if (nodeList == null) {
            nodeList = new ArrayList<Node>();
            link.getStart().neighboursPerType.set(typeID,nodeList);
        }
        if (!nodeList.contains(link.getEnd())) {
            nodeList.add(link.getEnd());
//            link.getStart().nrNeighboursPerType[typeID]++;
        }
        if (link.getType().isDirected()) {
            typeID = link.getType().getInverseMotifLink().getMotifLinkID();
        }
        nodeList = link.getEnd().neighboursPerType.get(typeID);
        if (nodeList == null) {
            nodeList = new ArrayList<Node>();
            link.getEnd().neighboursPerType.set(typeID,nodeList);
        }
        if (!nodeList.contains(link.getStart())) {
            nodeList.add(link.getStart());
//            link.getEnd().nrNeighboursPerType[typeID]++;
        }
    }

    private Set<Node> getSetOfType(MotifLink type) {
        Set<Node> set = nodeSetsDepartingFromLink.get(type);
        if (set == null) {
            set = new HashSet<Node>();
            nodeSetsDepartingFromLink.put(type, set);
        }
        return set;
    }
    /**
     * Optimises network structure for further processing
     */
    public void finalizeNetworkConstruction() {
        Set<MotifLink> keySet = nodeSetsDepartingFromLink.keySet();
        nodesWithLink = new HashMap<MotifLink, ArrayList<Node>>(keySet.size());
        for (MotifLink motifLink : keySet) {
            Set<Node> nodes = nodeSetsDepartingFromLink.get(motifLink);
            ArrayList<Node> n = new ArrayList<Node>(nodes);
            Collections.sort(n);
            nodesWithLink.put(motifLink, n);
        }
        nodeSetsDepartingFromLink = null;
    }

    public ArrayList<Node> getNodesOfType(MotifLink m) {
    	ArrayList<Node> nodeList = nodesWithLink.get(m);
    	return nodeList!=null? nodeList : new ArrayList<Node>();
    }
    
    /**
     * Reads a network
     *
     * @param filenames list of files containing edges and nodes
     * @param linkTypes for each filename, a linktype is specified
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Network readNetworkFromFiles(ArrayList<String> filenames, ArrayList<LinkType> linkTypes) throws FileNotFoundException, IOException {
        Network network = new Network();
        int nodeID = 0;
        int linkID = 0;
        for (int i = 0; i < filenames.size(); i++) {
            String filename = filenames.get(i);
            LinkType linkType = linkTypes.get(i);

            BufferedReader in = new BufferedReader(new FileReader(filename));

            String line = in.readLine();
            int linksi = 0;
            while (line != null) {
                int t = line.indexOf('\t');
                if (t <= 0 || line.contains("#")) {
                    line = in.readLine();
                    continue;
                }
                String n1 = line.substring(0, t) + linkType.getSourceNetwork();
                String n2 = line.substring(t + 1) + linkType.getDestinationNetwork();

                if (n1.equals(n2)) {
//                if(n1.equalsIgnoreCase(n2)){
                    line = in.readLine();
                    continue;
                }
                Node origin = network.getNodeByDescription(n1);
                if (origin == null) {
                    origin = new Node(nodeID++, n1);
                    network.addNode(origin);
                }
                Node destination = network.getNodeByDescription(n2);
                if (destination == null) {
                    destination = new Node(nodeID++, n2);
                    network.addNode(destination);
                }

                ArrayList<Node> nodes = origin.neighboursPerType.get(linkType.getMotifLink().getMotifLinkID());
                line = in.readLine();
                if (nodes != null && nodes.contains(destination)) {
                    continue;
                }
                Link l = new Link(linkID++, origin, destination, linkType);
                linksi++;
                network.addLink(l);
            }
            for (Node node : network.nodesByDescription.values()) {
                for (ArrayList<Node> nodeSet : node.neighboursPerType) {
                    if (nodeSet != null) {
                        Collections.sort(nodeSet);
                        nodeSet.trimToSize();
                    }
                }
            }
            System.out.println("Read: " + filename + " : links: " + linksi);
            in.close();
        }
        network.finalizeNetworkConstruction();
        System.out.println("Nodes: " + network.nodesByDescription.size());
//        System.out.println("Links: "+network.links.size());
        System.out.println("Links: " + network.nrLinks);
        return network;
    }
}
