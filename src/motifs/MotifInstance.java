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
package motifs;

import java.util.Arrays;
import network.Node;

/**
 * Represents a motif instance in the graph
 */
public class MotifInstance {

    private Node[] nodeMapping;

    public MotifInstance(Node[] mapping) {
        nodeMapping = Arrays.copyOf(mapping, mapping.length);
    }

    /**
     * Get the array of nodes in this motif instance
     * @return array of nodes in this motif instance
     */
    public Node[] getNodeArray() {
		return nodeMapping;
	}

	@Override
    public String toString() {
        String r = "";
        for (int i = 0; i < nodeMapping.length; i++) {
            Node node = nodeMapping[i];
            r += node.getDescription();
            if (i < nodeMapping.length - 1) {
                r += ";";
            }
        }
        return r;
    }
}
