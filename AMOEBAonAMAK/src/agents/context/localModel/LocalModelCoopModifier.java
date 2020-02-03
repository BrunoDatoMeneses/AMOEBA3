package agents.context.localModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;
import utils.Pair;

public class LocalModelCoopModifier extends LocalModel {
	private LocalModel localModel;
	private TypeLocalModel type;
	
	public LocalModelCoopModifier(LocalModel localModel, TypeLocalModel type) {
		this.localModel = localModel;
		localModel.setModifier(this);
		setType(type);
	}
	
	@Override
	public Context getContext() {
		return localModel.getContext();
	}
	
	@Override
	public void setContext(Context context) {
		localModel.setContext(context);
	}

	@Override
	public double getProposition() {
		return localModel.getProposition();
	}
	
	public double getPropositionCoop() {
		
		return localModel.getProposition();
	}

	@Override
	public double getMaxProposition() {
		return localModel.getMaxProposition();
	}

	@Override
	public HashMap<String, Double> getMaxWithConstraint(HashMap<String, Double> fixedPercepts) {
		ArrayList<Percept> percepts = getContext().getAmas().getPercepts();
		
		HashMap<String, Double> result = new HashMap<String, Double>();
		Double[] coefs = getCoefCoop();
		result.put("oracle", coefs[0]);

		if (coefs[0] == Double.NaN)
			throw new ArithmeticException("First coeficient of model cannot be NaN");
		
		for (int i = 1 ; i < coefs.length ; i++) {
			double coef = coefs[i];
			if (Double.isNaN(coef)) coef = 0.0;
			double pos;
			Percept p = percepts.get(i-1);
			if(fixedPercepts.containsKey(p.getName())) {
				pos = fixedPercepts.get(p.getName());
			} else {
				if(coef>0) {
					pos = getContext().getRanges().get(p).getEnd();
				}
				else {
					pos = getContext().getRanges().get(p).getStart();
				}
			}
			double value = coef * pos;
			result.put("oracle", result.get("oracle") + value);
			result.put(p.getName(), pos);
		}
		
		return result;
	}

	@Override
	public double getMinProposition() {
		return localModel.getMinProposition();
	}

	@Override
	public void updateModel(Experiment newExperiment, double weight) {
		localModel.updateModel(newExperiment, weight);
	}

	@Override
	public String coefsToString() {
		return localModel.coefsToString();
	}

	@Override
	public double distance(Experiment experiment) {
		return localModel.distance(experiment);
	}

	@Override
	public ArrayList<Experiment> getFirstExperiments() {
		return localModel.getFirstExperiments();
	}

	@Override
	public void setFirstExperiments(ArrayList<Experiment> frstExp) {
		localModel.setFirstExperiments(frstExp);
	}

	@Override
	public boolean finishedFirstExperiments() {
		return localModel.finishedFirstExperiments();
	}
	
	@Override
	public Double[] getCoef() {
		return localModel.getCoef();
	}
	
	@Override
	public String getCoefsFormula() {
		Double[] coefs = getCoefCoop();
		String result = "" +coefs[0];
		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			
			result += "\t" + coefs[i] + " (" + getContext().getAmas().getPercepts().get(i-1) +")";
			
		}
		result += "\nFrom " +localModel.getType()+" : "+localModel.getCoefsFormula(); 
		
		return result;
	}
	
	public Double[] getCoefCoop() {
		Set<Context> neighbors = getNeighbors();
		Double[] coef = localModel.getCoef().clone();
		int i = 0;
		for(Percept p : getContext().getRanges().keySet()) {
			for(Context c : neighbors) {
				LocalModel model = c.getLocalModel();
				while(model.hasModified()) {
					model = model.getModified();
				}
				Double[] coef2 = model.getCoef();
				coef[i] += coef2[i]/neighbors.size()*getCommonFrontierCoef(p, getContext(), c);
			}
			i++;
		}
		return coef;
	}
	
	private Set<Context> getNeighbors() {
		Set<Percept> percepts = getContext().getRanges().keySet();
		Set<Context> contexts = new HashSet<>(getContext().getAmas().getContexts());
		
		for(Percept p : percepts) {
			contexts.removeIf(c -> !(p.inNeighborhood(c, getContext().getRangeByPercept(p).getStart()) || p.inNeighborhood(c, getContext().getRangeByPercept(p).getEnd())));
		}
		return contexts;
	}
	
	private double getCommonFrontierCoef(Percept p, Context c1, Context c2) {
		ArrayList<Pair<Context, Double>> sorted = new ArrayList<>();
		sorted.add(new Pair<Context, Double>(c1, c1.getRangeByPercept(p).getStart()));
		sorted.add(new Pair<Context, Double>(c2, c2.getRangeByPercept(p).getStart()));
		sorted.add(new Pair<Context, Double>(c1, c1.getRangeByPercept(p).getEnd()));
		sorted.add(new Pair<Context, Double>(c2, c2.getRangeByPercept(p).getEnd()));
		
		sorted.sort(new Comparator<Pair<Context, Double>>() {
			@Override
			public int compare(Pair<Context, Double> o1, Pair<Context, Double> o2) {
				return o1.b < o2.b ? -1 : 1;
			}
		});
		
		if(sorted.get(0).a == sorted.get(1).a ) {
			return 0.0;
		} else {
			return (Math.abs(sorted.get(2).b-sorted.get(1).b)+1)/(Math.abs(sorted.get(3).b-sorted.get(0).b)+1);
		}
	}

	@Override
	public void setCoef(Double[] coef) {
		localModel.setCoef(coef);
	}

	@Override
	public TypeLocalModel getType() {
		return type;
	}

	@Override
	public void setType(TypeLocalModel type) {
		this.type = type;
	}

}
