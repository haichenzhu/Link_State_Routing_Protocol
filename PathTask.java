// 定时计算和输出最短路径


import java.util.TimerTask;

public class PathTask extends TimerTask {
	Router router;
	public PathTask(Router router) {
		this.router = router;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		router.updatePath();
	}

}
