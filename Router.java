import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Router {
	public static final int UPDATE_INTERNAL = 1000;
	public static final int ROUTER_UPDATE_INTERNAL = 20*1000;
	public static final int SEND_ALIVE_INTERVAL = 400;
	public static final int DIE_INTERVAL = 3*(SEND_ALIVE_INTERVAL);
	
	
	static final int ROUTER_ALIVE = 0;
	static final int EDGE_SEND = 1;
	static final int EDGE_REPLY = 2;
	
	List<Neighbour> neighbours = new ArrayList<Neighbour>();  //邻居节点
	List<EdgeInfo> edges = new ArrayList<EdgeInfo>();  //所有边
	
	
	String ip="";
	long start_time = 0;
	long cur_time = 0;
	int port;
	char router_id;
	PrintWriter writer = null;
	
	
	final static boolean debug = false;
	
	public Router(char id,int port,String config) {
		getIp();
		this.router_id = id;
		this.port = port;
		start_time = getTime();
		
		try {
			//writer = new PrintWriter("log"+router_id+".txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		read(config);
	}
	
	synchronized void quit() {
		if (writer != null) { 	
			writer.close();
			writer = null;
		}
	}
	
	synchronized void updatePath() {
		Graph graph = new Graph();
		for(EdgeInfo e:edges) {
			if (e.opened()) {
				graph.processEdge(e);
			}
		}
		graph.calShortestCost(router_id);
		graph.print(router_id);
	}
		
	synchronized void process(String message) {
		updateTime();
		logEvent("at time %d ,recv message:%s",cur_time, message);
		Scanner sc = new Scanner(message);
		char sender = sc.next().charAt(0);
		int message_type = sc.nextInt();
		if (message_type == Router.ROUTER_ALIVE) 
			setNeighbourAlive(sender);
		else if (message_type == EDGE_SEND) {
			boolean opened = (sc.nextInt()!=0);
			char start = sc.next().charAt(0);
			char end = sc.next().charAt(0);
			double cost = sc.nextDouble();
			String visitors = sc.next();
			long check_time = sc.nextLong();
			setEdgeStatus(sender, start, end, cost, visitors, opened, check_time);
		}
		
		sc.close();
	}
	
	synchronized void checkNeighbours() {
		updateTime();
		for(Neighbour node:neighbours) {
			EdgeInfo edge = findEdge(node.id);
			assert(edge != null);
			
			boolean status = node.isLive();
			assert(edge.opened() ==status);
			
			if (node.isLive()) {
				if (node.exceedTime(cur_time)) {
					print("neightbour %c changes to die at %d",node.id,cur_time);
					status = false;
					edge.resetVistors();
				}
			}
			
			node.setStatus(status, cur_time);
			edge.setStatus(status, cur_time);
		}
	}
	
	void setEdgeStatus(char sender, char start,char end
			,double cost,String visitors,boolean opened,long check_time) {
		assert(visitors.indexOf(router_id) < 0);
		
		assert(visitors.indexOf(sender) >=0 );
		assert(visitors.indexOf(start)>=0);
		assert(visitors.indexOf(end)>=0);
		
		EdgeInfo e = findEdge(start, end);
		if (e == null) {
			print("neightbour %c send a new edge %s between %c --> %c,cost =%f, "
					,sender
					,opened?"opend":"closed"
					,start,end,cost);
			e = new EdgeInfo(start, end, cost);
			edges.add(e);
		}
		else {
			if (e.opened() != opened) {
				assert(e.getCheckTime() <= check_time);
				print("neightbour %c send a new edge status %s between %c --> %c "
						,sender
						,opened?"opened":"closed"
						,start,end);
			}
		}
		
		e.setStatus(opened, check_time);
		e.setVistor(visitors);
		e.addVistor(router_id);
	}
	
	
	synchronized void sendAlive() {
		String message = String.format("%c %d", router_id,ROUTER_ALIVE);
		for(Neighbour node: neighbours) {
			send(node, message);
		}
	}
	
	synchronized void sendEdges() {
		for(EdgeInfo e:edges)
			send(e);
	}
	
	
	
	void send(EdgeInfo e) {
		String message = String.format("%c %d %s",router_id,EDGE_SEND,e.toMessage());
		logEvent("prepare to send edge info:%s to all neighbours",message);
		for(Neighbour node:neighbours) {
			if (!e.hasVistor(node.id)) {
				if (!node.isDied()) {
					logEvent("will send edge message to %c",node.id);
					send(node, message);
					e.addVistor(node.id);
				}
				else
					logEvent("will not send edge message to %c because it is died",node.id);
			}
			else {
				logEvent("will not send edge message to %c because it is a visitor",node.id);
			}
		}
	}
	
	
	void send(Neighbour node,String message) {
		assert(node != null);
		try {
			int port = node.port;
			DatagramSocket socket = new DatagramSocket();
			message.getBytes();
			byte[] buf  = message.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length
					,new InetSocketAddress(ip , port));
			socket.send(packet);
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	void updateTime() {
		cur_time = getTime();
	}
		
	void setNeighbourAlive(char id) {
		logEvent("neighbour %c send active message at time %d",id, cur_time);
		
		Neighbour node = findNeighbour(id);
		assert(node != null);
		
		EdgeInfo edge = findEdge(id);
		assert(edge != null);
		
		assert(edge.opened() ==node.isLive());
		
		logEvent("at time %d,status of edge %c->%c is %s,last_check_time=%d",cur_time, router_id, id,edge.opened()?"open":"close",
				edge.last_check_time
				);
		
		if (node.isDied()) {
			print("node %c change to live at time %d",id,cur_time);
			edge.resetVistors();
		}
		
		//logEvent("edge %c->%c changed to opened at time %d because of active message send from %c",router_id, id,cur_time,id);
		edge.setStatus(true, cur_time);
		node.setStatus(true, cur_time);
	}
	
	EdgeInfo findEdge(char start,char end) {
		for(EdgeInfo e:edges) {
			if (e.equals(start, end))
				return e;
		}
		return null;
	}
	
	EdgeInfo findEdge(char id) {
		return findEdge(router_id, id);
	}
	
	Neighbour findNeighbour(char id) {
		for(Neighbour e:neighbours) {
			if (e.id == id) {
				return e;
			}
		}
		return null;
	}
		
	
	
	void logEvent(String format, Object ...args) {
		if (writer != null) {
			writer.println(String.format(format, args));
		}
	}
	
	
	
	long getTime() {
		return System.currentTimeMillis();
	}
	
	void getIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
           // System.out.println("ip=" + ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	void read(String config) {
		try {
			File file = new java.io.File(config);
			Scanner sc = new Scanner(file);
			int n = sc.nextInt();
			for(int i=0;i<n;++i) {
				char id =  sc.next().charAt(0);
				double cost = sc.nextDouble();
				int port =  sc.nextInt();
				addNeighbour(id, port, cost);
				addNeighbourEdge(id,cost);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	void addNeighbour(char id,int port,double cost) {
		Neighbour neighbour = new Neighbour(id, port, cost);
		neighbour.last_check_time = start_time;
		neighbours.add(neighbour);
	}
	
	void addNeighbourEdge(char end,double cost) {
		EdgeInfo e = new EdgeInfo(router_id, end, cost);
		e.setStatus(false, start_time);
		edges.add(e);
	}
	
	void print() {
		logEvent("router %c starts at %d", router_id, start_time);
		logEvent("ip=%s", ip);
	}
	
	synchronized void printNodes() {
		for(Neighbour node:neighbours) {
			System.out.println(node.toString());
		}
	}
	
	static void print(String format,Object...args) {
		if (debug) {
			System.out.println(String.format(format, args));
		}
	}
	
	synchronized void printEdges() {
		for(EdgeInfo e:edges) {
			print(e.toString());
		}
	}
			
	void logNeighbours() {
		for(Neighbour node:neighbours) {
			logEvent(node.toString());
		}
	}
	
	void logEdges() {
		for(EdgeInfo e:edges) {
			logEvent(e.toString());
		}
	}
		
}
