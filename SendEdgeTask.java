// 这个每隔一秒向所有邻居发送数据中的边的信息

import java.util.TimerTask;

public class SendEdgeTask extends TimerTask {
	Router router;
	public SendEdgeTask(Router router) {
		this.router = router;
	}
	
	@Override
	public void run() {
		router.sendEdges();
	}
}
