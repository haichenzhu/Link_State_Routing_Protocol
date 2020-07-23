// 


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Graph {
	List<Vertex> vertexes = new ArrayList<Vertex>();
	
	void processEdge(Edge e) {
		processEdge(e.start,e.end,e.cost);
	}
	
	void processEdge(char start,char end,double cost) {
		Vertex u = getVertex(start);
		Vertex v = getVertex(end);
		u.addNeighbour(end, cost);
		v.addNeighbour(start, cost);
	}
	
	
	Vertex getVertex(char name) {
		Vertex u =null;
		for(Vertex v:vertexes) {
			if (v.name ==name) {
				return v;
			}
		}
		
		u = new Vertex(name);
		vertexes.add(u);
		return u;
	}
	
	Vertex getMinUnvisited() {
		double cost = Double.POSITIVE_INFINITY;
		Vertex u = null;
		for(Vertex v:vertexes) {
			if (!v.visited && v.min_cost < cost) {
				u = v;
				cost = v.min_cost;
			}
		}
		return u;
	}
	
	// djj算法求单源最短路径
	void calShortestCost(char start) {
		Vertex u = getVertex(start);
		u.min_cost = 0;
		int n = vertexes.size();
		for (int i = 0; i < n; ++i) {
			u = getMinUnvisited();
			if (u == null) {
				break;
			}
			//System.out.println(String.format("min =%c", u.name));
			u.visited = true;
			updateNeighbourCost(u);
		}

	}
		
	void updateNeighbourCost(Vertex u) {
		for(Map.Entry<Character, Double> entry:u.neighbours.entrySet()) {
			char id = entry.getKey();
			double cost = entry.getValue();
			Vertex v = getVertex(id);
			if (!v.visited) {
				double new_cost = u.min_cost + cost;
				String new_path = u.path + u.name;
				if (v.needUpdate(new_cost, new_path)) {
					v.min_cost = new_cost;
					v.path = new_path;
					//System.out.println(String.format("find new cost for %c",v.name));
					//System.out.println(v.toString());
				}
			}
		}
	}
	
	void print(char start) {
		System.out.println("I am Router " + start);
		for (Vertex u:vertexes) {
			if (u.name != start) {
				System.out.println(u.toString());
			}
		}
	}
}
