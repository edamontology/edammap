package edammapper.input.json;

import java.util.ArrayList;
import java.util.List;

public class Biotools {

	private int count;

	private String previous;

	private String next;

	private List<ToolInput> list = new ArrayList<>();

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public String getPrevious() {
		return previous;
	}
	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public String getNext() {
		return next;
	}
	public void setNext(String next) {
		this.next = next;
	}

	public List<ToolInput> getList() {
		return list;
	}
	public void setList(List<ToolInput> list) {
		this.list = list;
	}

	public void addTools(List<ToolInput> list) {
		this.list.addAll(list);
	}
}
