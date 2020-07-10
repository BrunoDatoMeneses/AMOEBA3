package ros;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import agents.percept.Percept;
import kernel.ELLSA;

public class RosListenDelegateForAmoeba implements RosListenDelegate {
	
	private ELLSA ellsa;
	private Publisher pub;
	
	public RosListenDelegateForAmoeba(ELLSA ellsa, Publisher pub) {
		this.ellsa = ellsa;
		this.pub = pub;
	}

	@Override
	public void receive(JsonNode data, String stringRep) {
		HashMap<String, Double> out = buildOutputFromMsg(data);
		double res;
		boolean learn = data.get("msg").get("learn").asBoolean();
		if(learn) {
			ellsa.learn(out);
			res = ellsa.getAction();
		} else {
			res = ellsa.request(out);
		}
		((ObjectNode)data.get("msg")).set("oracle", JsonNodeFactory.instance.numberNode(res));
		pub.publishJsonMsg(data.get("msg").toString());
	}
	
	private HashMap<String, Double> buildOutputFromMsg(JsonNode data) {
		HashMap<String, Double> output = new HashMap<>();
		for(Percept p : ellsa.getPercepts()) {
			String name = p.getName();
			Double value = data.get("msg").get(name).asDouble();
			output.put(name, value);
		}
		output.put("oracle", data.get("msg").get("oracle").asDouble());
		return output;
	}
	

}
