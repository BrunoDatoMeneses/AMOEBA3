package mas.agents.head;

public class DynamicPerformance {
	
	
	
	
	int successesBeforeDiminution;
	int conflictsBeforeAugmentation;
	double performanceIndicator;
	int performanceCounter;
	
	double augmentationFactor;
	double diminutionFactor;
	
	double minPerformanceIndicator;
	
	public DynamicPerformance(int nbSuccessesBeforeDiminution, int nbConflictsBeforeAugmentation, double performanceIndicatorValue, double augmentationFactorValue, double diminutionFactorValue, double minPerformanceIndicatorValue) {
		
		successesBeforeDiminution = nbSuccessesBeforeDiminution;
		conflictsBeforeAugmentation = nbConflictsBeforeAugmentation;
		performanceIndicator = performanceIndicatorValue;
		performanceCounter = 0;
		
		augmentationFactor = augmentationFactorValue;
		diminutionFactor = diminutionFactorValue;
		
		minPerformanceIndicator = minPerformanceIndicatorValue;
		
	}
	
	
	public void update(double currentCriticalityMean) {
		
		if (currentCriticalityMean > performanceIndicator) {
			performanceCounter--;
		} else {
			performanceCounter++;
		}
		

		
		if (performanceCounter <= conflictsBeforeAugmentation * (-1)) {
			performanceCounter = 0;
			performanceIndicator += augmentationFactor * performanceIndicator;
		}

		if (performanceCounter >= successesBeforeDiminution) {
			performanceCounter = 0;
			performanceIndicator -= diminutionFactor * performanceIndicator;
			performanceIndicator = Math.max(minPerformanceIndicator, performanceIndicator);
		}
		
	}
	
	public void setPerformanceIndicator(double value) {
		performanceIndicator = value;
	}
	
	public double getPerformanceIndicator() {
		return performanceIndicator;
	}

}













