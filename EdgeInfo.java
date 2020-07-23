// 在egde的基础上


public class EdgeInfo extends Edge {
	boolean active = false; // 打开还是中断
	String visitors = "";  // 有哪些邻居已经知道本节点信息，这样下次就不用发
	long last_check_time = 0;  // 上次被检查的时间，表示边的状态最近一次被更新的时间
	
	public EdgeInfo(char start,char end,double cost) {
		super(start, end, cost);
		resetVistors();
	}
	
	void resetVistors() {
		visitors ="" + start + end;
	}
	
	void addVistor(char router_id) {
		if (visitors.indexOf(router_id)< 0 )
			visitors = visitors + router_id;
	}
	
	void setVistor(String vistors) {
		this.visitors = vistors;
		addVistor(start);
		addVistor(end);
	}
	
	void addVistor(String visitors) {
		for(int i=0;i<visitors.length();++i) {
			char c =visitors.charAt(i);
			addVistor(c);
		}
	}
	
	long getCheckTime() {
		return last_check_time;
	}
	
	void setStatus(boolean status, long check_time) {
		active = status;
		if(last_check_time <= check_time)
			last_check_time =check_time;
	}
	
	String getStatus() {
		return active?"opened":"closed";
	}
	
	
	boolean opened() {
		return active;
	}
	
	boolean closed() {
		return !active;
	}
	
	
	
	boolean hasVistor(char id) {
		return visitors.indexOf(id) >= 0;
	}
	
	public String toMessage() {
		return String.format("%d %-20s %-15s %d",active?1:0,super.toMessage(),visitors
				,last_check_time);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("%c --> %c is %s after %d,visitors=%s", start,end,getStatus(),last_check_time,visitors);
	}
	
}
