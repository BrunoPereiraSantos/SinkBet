package projects.wsn.nodes.messages;

import java.util.List;
import java.util.Map;

import sinalgo.nodes.messages.Message;

public class Proper extends Message {
	private Map<Integer, Integer> values;
	private Integer senderID;
	
	
	/**
	 * @param values
	 */
	public Proper(Map<Integer, Integer>  values, Integer senderID) {
		this.values = values;
		this.senderID = senderID;
	}

	@Override
	public Message clone() {
		Proper msg = new Proper(this.values, this.senderID);
		return msg;
	}
	
	public Map<Integer, Integer>  getValues() {
		return values;
	}
	public void setValues(Map<Integer, Integer> values) {
		this.values = values;
	}

	public int getSenderID() {
		return senderID;
	}

	public void setSenderID(Integer senderID) {
		this.senderID = senderID;
	}
	
	
}
