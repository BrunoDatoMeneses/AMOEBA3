import sys
import os

if (len(sys.argv) < 4) :
    print("Usage : %s outFileName isEnum percept1 [percept2 ...]")
    print("Generate a .msg for ROS and a .xml for AMOEBA based on the percepts names provided.")
    exit(0)

fileName = sys.argv[1][0].upper()+sys.argv[1][1:] 

with open(fileName+".msg", "w") as msgFile:
    for percept in sys.argv[3:] :
        msgFile.write("float64 %s\n" % percept)
    msgFile.write("float64 oracle\n")
    msgFile.write("bool learn")

with open(fileName+".xml", "w") as xmlFile:
    start = """
<?xml version="1.0" encoding="UTF-8"?>
<System>

    <Configuration>	
        <Learning allowed = "true" creationOfNewContext = "true" loadPresetContext = "false"></Learning>	
    </Configuration>
    
    <StartingAgents>
"""
    xmlFile.write(start)
    for percept in sys.argv[3:] :
        xmlFile.write("\t\t<Sensor Name=\"%s\" Enum=\"%s\" />\n" % (percept, sys.argv[2]))
    end = """
        <Controller Name="Controller">
            <ErrorMargin ErrorAllowed="2000.0" AugmentationFactorError="5.0" DiminutionFactorError="0.4" MinErrorAllowed="0.1" NConflictBeforeAugmentation="40" NSuccessBeforeDiminution="80" />
            
        </Controller> 
    </StartingAgents>
    
</System>
"""
    xmlFile.write(end)

exit(0)
