

import java.util.TimerTask;

public class SendAliveTask extends TimerTask {
	Router router;
	public SendAliveTask(Router router) {
		this.router = router;
	}
	
	@Override
	public void run() {
		router.sendAlive();  // 定时向邻居发送信号，告知本router开机
	}
}
