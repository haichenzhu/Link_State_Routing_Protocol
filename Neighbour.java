// 从config文件读入邻居信息
// 初始每个邻居的状态为false
// 每个节点每0.5秒向所有邻居发一个信号，告诉邻居我还活着
// 每个节点每隔1.5秒检查每个邻居上一次发信号的时间


public class Neighbour {
	
	char id;  
	int port;
	double cost;
	
	
	long last_alive_time = 0;  // 表示最近一次受到邻居的信号的时间
	long last_check_time = 0;
	boolean alive = false;  //表示邻居的状态
	
	public Neighbour(char id,int port,double cost) {
		this.id = id;
		this.port = port;
		this.cost = cost;
	}
	
	String getStatus() {
		return alive?"live":"died";
	}
	
	@Override
	public String toString() {
		return String.format("%c is %s,after %d",id,getStatus(),last_check_time);
	}
	
	void setStatus(boolean status,long check_time) {
		if(last_check_time < check_time)
			last_check_time = check_time;
		alive = status;
		if (status && last_alive_time <check_time) {
			last_alive_time = check_time;
		}
	}
	
	// 如果这个时间和当前时间差大于1.5
	// 就认为这个邻居已经关机
	// 此时要把邻居的状态设为false
	// 然后邻居的相关边的状态也要设为false
	// A就要把本地数据库里D的状态设为fasle
	// 然后把D的相关边DE DC DB的状态都改为closed
	boolean exceedTime(long check_time) {
		return (check_time - last_alive_time >= Router.DIE_INTERVAL);
	}
	
	boolean isDied() {
		return !alive;
	}

	
	boolean isLive() {
		return alive;
	}
}
