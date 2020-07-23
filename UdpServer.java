// 开启一个udp服务器
// 传递的信息有两种
// 一种是路由器开机信息，此时的数据格式为 发送者 消息类型=0，b告知a，b现在开机，就是b 0
// 另一种数据结构为（发送者， 消息状态， 边状态， 起点， 终点， 距离， 本消息的已告知者， 检查时间）
// 例如             A       1        0     A     F    2.2         AF   1549121930138ms
// router b 在收到这个信息后，可以根据这个来更新自己的数据库，把a加到自己的边列表里


import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServer implements Runnable{
	Router router;
	public UdpServer(Router r) {
		router = r;
	}
	
	boolean stopped = false;
	synchronized void quit() {
		stopped = true;
	}
	
	synchronized boolean toStop() {
		return stopped;
	}
	
	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(router.port);
			byte[] buffer = new byte[512];
			while(!toStop()) {
				DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
				socket.receive(packet);
				
				String message = new String(packet.getData(), 0, packet.getData().length);
				message = message.trim();  //important !!
				
				router.process(message);
			}
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
