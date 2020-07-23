// 


import java.util.HashMap;
import java.util.Map;

public class Vertex {
	public Vertex(char name) {
		this.name = name;
	}
	
	char name;  // 名字
	double min_cost = Double.POSITIVE_INFINITY;  // 最短路径
	String path = ""; //current shortest path
	boolean visited = false;  // 是否求短路径已经求完

	// 存储本节点到其他邻居的距离
	Map<Character, Double> neighbours = new HashMap<Character, Double>();
	
	void addNeighbour(char dest,double cost) {
		neighbours.put(dest, cost);
	}
	
	@Override
	public String toString() {
		return String.format("Least cost path to router %c:%s and the cost is %.1f"
				, name,path + name,min_cost);
	}
	
	boolean needUpdate(double new_cost,String new_path) {
		if (new_cost < min_cost) {
			return true;
		}
		else if (new_cost >min_cost){
			return false;
		}
		else {
			if (new_path.length() < path.length()) {
				return true;
			}
			else if (new_path.length() > path.length()) {
				return false;
			}
			else {
				return new_path.compareTo(path) < 0;
			}
		}
	}
}
