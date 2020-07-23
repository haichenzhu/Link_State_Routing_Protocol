//



public class Edge {
	char start;
	char end;
	double cost;
	
	public Edge(char u,char v,double cost) {
		this.start = u;
		this.end =v;
		this.cost = cost;
	}

	boolean contains(char id) {
		return start == id|| end ==id;
	}
	
	boolean equals(char u,char v) {
		return contains(u) && contains(v);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("%c -- %c : %f", start,end,cost);
	}
	
	String toMessage() {
		return String.format("%c %c %.1f", start, end, cost);
	}
}
