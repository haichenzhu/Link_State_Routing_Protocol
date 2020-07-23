import java.util.TimerTask;

public class CheckNodesTask extends TimerTask {
	Router router;

	// 检查邻居状态
	public CheckNodesTask(Router router) {
		// TODO Auto-generated constructor stub
		this.router = router;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		router.checkNeighbours();
	}
}
