package webcrawler;

import java.util.HashSet;

public class Node {

	private static int count = 0;
	
	public int id=0;	
	public String url;
	public HashSet<Node> incoming;
	public HashSet<Node> outgoing;

	public Node(String link) {
		this.incoming = new HashSet<Node>();
		this.outgoing = new HashSet<Node>();
		this.url = link.toLowerCase();
		this.id = count++;
	}
	
	public void addOutgoingEdge(Node outgoing){
		this.outgoing.add(outgoing);
	}
	
	public void addIncomingEdge(Node incmoing){
		this.incoming.add(incmoing);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node rhs = (Node) obj;
			return rhs.url == this.url;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}
}
