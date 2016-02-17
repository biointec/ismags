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
package algorithm;

import datastructures.NodeIterator;

import java.util.ArrayList;
//import datastructures.SymProp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import datastructures.PriorityObject;
import datastructures.PriorityQueueMap;
import java.util.Map;
import motifs.Motif;
import motifs.MotifLink;
import network.Node;

/**
 * This class is responsible for analysing the motif and providing the
 * constraints to the MotifFinder class
 */
class SymmetryHandler {

    Set<Integer> mappedPositions;
    private Map<Integer, Set<Integer>> smaller;
    private Map<Integer, Set<Integer>> larger;
    NodeIterator[] mapping;
    private Node[] mappedNodes;
    private PriorityQueueMap PQmap;
    private Motif motif;

    /**
     * Constructs a SymmetryHandler object to deal with the specified motif
     *
     * @param mapping handle to NodeIterators containing constraining neighbour
     * lists
     * @param motif motif to be analysed
     * @param mappedNodes handle to partial node mapping
     */
    public SymmetryHandler(NodeIterator[] mapping, Motif motif, Node[] mappedNodes) {
        PQmap = new PriorityQueueMap(mapping.length);
        this.mapping = mapping;
        this.motif = motif;
        mappedPositions = new HashSet<Integer>(mappedNodes.length);
        this.mappedNodes = mappedNodes;
        smaller = new HashMap<Integer, Set<Integer>>();
        larger = new HashMap<Integer, Set<Integer>>();
//        SymProp sp = analyseMotif(motif);
    }

    /**
     * Determines the next motif nodes and candidates to be mapped
     *
     * @param unmappedMotifNodes unmapped motif nodes from which to select the
     * next node
     * @return next motif node and graph node candidates
     */
    NodeIterator getNextBestIterator(Set<Integer> unmappedMotifNodes) {
        //get next node to be mapped by polling priority map
        PriorityObject poll = PQmap.poll(unmappedMotifNodes);
        int motifNodeID = poll.getTo();
        NodeIterator r = mapping[motifNodeID];
        //determine lower bound for graph node candidates
        Set<Integer> minset = larger.get(motifNodeID);
        Node minNode = new Node(-1,"");
//        Node minNode=null;
        if (minset != null) {
            for (Integer integer : minset) {
                if (mappedPositions.contains(integer) && minNode.compareTo(mappedNodes[integer])<0) {
                    minNode = mappedNodes[integer];
                }
            }
        }
        //determine upper bound for graph node candidates
        Set<Integer> maxset = smaller.get(motifNodeID);
        Node maxNode = new Node(Integer.MAX_VALUE,"");
        if (maxset != null) {
            for (Integer integer : maxset) {
                if (mappedPositions.contains(integer) && maxNode.compareTo(mappedNodes[integer])>0) {
                    maxNode = mappedNodes[integer];
                    //abort when bounds conflict
                    if (minNode.compareTo(maxNode)>0) {
                        return null;
                    }
                }
            }
        }
        //determine nodes by intersecting using the bounds
        NodeIterator intersect = r.intersect(minNode, maxNode);
        return intersect;
    }

    /**
     * Maps a graph node to a motif node and updates the neighbour lists used
     * for intersecting
     *
     * @param motifNode motif node to be mapped on
     * @param n graph node to be mapped
     * @return returns true if node is suitable for mapping on the motif node
     */
    boolean mapNode(int motifNode, Node n) {
        int[] connections = motif.getConnectionsOfMotifNode(motifNode);
        MotifLink[] restrictions = motif.getLinksOfMotifNode(motifNode);
        int nrConnections = connections.length;
        for (int j = 0; j < nrConnections; j++) {
            int i = connections[j];
            if (mappedNodes[i] != null) {
                continue;
            }
            MotifLink motifLink = restrictions[j];
//            NodeSet ln = n.neighboursPerType[motifLink.getMotifLinkID()];
            ArrayList<Node> ln = n.neighboursPerType.get(motifLink.getMotifLinkID());
            if (ln == null) {
                return false;
            } else {
                mapping[i].addRestrictionList(ln, n);
                int size = ln.size();
                PQmap.add(new PriorityObject(n, motifNode, i, size));
            }
        }
        return true;
    }

    /**
     * Un-maps a graph node previously mapped to a motif node, ensuring
     * consistency in constraining neighbour lists
     *
     * @param motifNode motif node mapped to
     * @param graphNode graph node mapped to motif node
     */
    void removeNodeMapping(int motifNode, Node graphNode) {
        int[] neighbours = motif.getConnectionsOfMotifNode(motifNode);
        for (int i : neighbours) {
            mapping[i].removeRestrictionList(graphNode);
            PQmap.remove(motifNode, i);
        }
    }

    
}
