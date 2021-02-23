package agents.head;

import java.io.Serializable;

public enum REQUEST implements Serializable {

	ACTIVE,
	RDM,
	SELF,
	CONFLICT,
	CONCURRENCE,
	FRONTIER,
	VOID,
	SUBVOID,
	MODEL,
	DREAM,
	NEIGHBOR,
	FUSION,
	RESTRUCTURE,
	CREATION_WITH_NEIGHBOR,
	EXOGENOUS,
	ENDOGENOUS,

	NCS_BAD_PREDICTION,
	NCS_USELESSNESS,
	NCS_CONFLICT,
	NCS_CONCURRENCY,
	NCS_UNPRODUCTIVITY,
	NCS_EXPANSION,
	NCS_CREATION

	
}

	

	

