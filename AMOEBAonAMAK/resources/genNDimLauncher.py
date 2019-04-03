# A simple script to generate a launcher that can be used with NDimCube

NDim = 100

start = """<?xml version="1.0" encoding="UTF-8"?>
<System>

	<!-- General config options -->
	<Configuration>	
		<Learning allowed = "true" creationOfNewContext = "true" loadPresetContext = "true"></Learning>	
	</Configuration>
	
	
	<StartingAgents>
"""
sensor = '\t\t<Sensor Name="px%d" Source="x%d"></Sensor>\n'

end = """
	  	<Controller Name="Controller" Oracle="test">
              <ErrorMargin ErrorAllowed="2000.0" AugmentationFactorError="5.0" DiminutionFactorError="0.4" MinErrorAllowed="0.1" NConflictBeforeAugmentation="40" NSuccessBeforeDiminution="80" />
              <InexactMargin InexactAllowed="500.0" AugmentationInexactError="2.5" DiminutionInexactError="0.2" MinInexactAllowed="0.05" NConflictBeforeInexactAugmentation="40" NSuccessBeforeInexactDiminution="80" />
        </Controller> 


			
	</StartingAgents>
	
	
</System>
"""
print("Creating launcher with %d sensors."%NDim)
with open("%dDimensionsLauncher.xml"%NDim, "w") as xmlfile:
    xmlfile.write(start)
    for i in range(1, NDim+1):
        xmlfile.write(sensor%(i,i))
    xmlfile.write(end)
print("Done.")
