# Scala ActiveMQ Generator
Simple message generator based on xml message files. 

**Note: This project is built for ActiveMQ 5.7. Change the activemq-core dependency version and rebuild the jar if you need a different version**

####Building the project
If you are using **ActiveMQ 5.7 simply download the released jar [Here](https://github.com/ShannonHolgate/ScalaActiveMQGenerator/releases/download/v0.1/messagegenerator_2.11-1.0-one-jar.jar)**. Otherwise, follow the steps below:
	
- change the `"org.apache.activemq" % "activemq-core" % "5.7.0"` dependency to whatever version you are using
- run `sbt one-jar` to build the über jar

Please be aware that the sole purpose of this project is to stick some messages in a queue, there are no tests and the code is slightly messy. It can't do any damage but please read the code first.

####Usage
Java/Scala built über jar which simply iterates through a directory ./xml-messages and publishes them a number of times on a queue specified in ./queue-connection.conf

1. Build the jar if needed using `sbt one-jar` or download the released jar [Here](https://github.com/ShannonHolgate/ScalaActiveMQGenerator/releases/download/v0.1/messagegenerator_2.11-1.0-one-jar.jar)
2. Add a file called queue-connection.conf to the same directory as the built jar
	3. This is a Typesafe config written using HOCON if need be.
	4. Define the following 3 properties:
		5.	`activemq.url="tcp://queue:port"`
      	6.	`activemq.queue="Queue"`
      	7.	`activemq.client="ClientId"`
3. Add a folder, called "xml-messages", to the same directory as the built jar. This directory should be full of sample xml messages to post to the queue specified. 	
4. Run the jar **from the same directory as the jar** using the following command:
		
		Java -jar ./messagegenerator_2.11-1.0-one-jar.jar arg0 arg1 arg2
Where:

        arg0<BigInt> number of messages to produce per iteration
        arg1<Int> number of iterations
        arg2<Long> seconds between iterations

