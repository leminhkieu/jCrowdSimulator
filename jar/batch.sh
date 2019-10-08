#!/bin/bash

# Run the crowd simulator a load of times
# This is done in a batch script, rather than through a Java class, because each time a new
# simulation starts it needs a fresh jvm, otherwise there are variables left over from the
# previous simulatino run that will break future runs.
for i in {1..10}
do
 printf " \n\n ********************* \n\n  *** EXPERIMENT $i *** \n\n ********************* \n\n"
 java -jar crowdsimui1.jar 
done 

