import java.util.Scanner;
import java.util.Timer;

public class Lsr {
	
	
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		char id = args[0].charAt(0);
		int port = Integer.parseInt(args[1]);
		String config = args[2];
		Router router = new Router(id, port, config);
		
		Router.print("router %c starts at %d",id,router.start_time);
		
		router.print();
		
		router.logNeighbours();
		router.logEdges();
		
		UdpServer server = new UdpServer(router);
		Thread serverThread = new Thread(server);
		serverThread.start();
		
		// scheduleAtFixedRate定时发送信息
		Timer aliveTimer = new Timer(true);
		aliveTimer.scheduleAtFixedRate(new SendAliveTask(router), 0, Router.SEND_ALIVE_INTERVAL);
		
		Timer checkTimer = new Timer(true);
		checkTimer.scheduleAtFixedRate(new CheckNodesTask(router), 0,Router.DIE_INTERVAL);
		
		Timer edgeTimer = new Timer(true);
		edgeTimer.scheduleAtFixedRate(new SendEdgeTask(router), 0, Router.UPDATE_INTERNAL);
		
		Timer pathTimer = new Timer(true);
		pathTimer.scheduleAtFixedRate(new PathTask(router), 0, Router.ROUTER_UPDATE_INTERNAL);
		
		Scanner scanner = new Scanner(System.in);
		while(true){
			String cmd = scanner.next();
			if (cmd.equals("quit")) {
				break;
			}
			else if (cmd.equals("path")) {
				router.updatePath();
			}
			else if (cmd.equals("node")) {
				router.printNodes();
			}
			
			else if (cmd.equals("edge")) {
				router.printEdges();
			}
		}
		scanner.close();
		
		server.quit();
		router.quit();
		aliveTimer.cancel();
		checkTimer.cancel();
		pathTimer.cancel();
		
		System.exit(0);
	}

}
