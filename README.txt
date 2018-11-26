CS 372 Introduction to Computer Networks
Programming Assignment #2
Due Sunday, end of Week 9, by 11:59pm

To start ftserver.cpp, run the command g++ -o ftserver ftserver.cpp. 
After that, simple type ./ftserver 25000 to start a server listening on port 25000.

To get a directory list using ftclient.java, run the command javac ftclient.java.
After that, type java ftclient flip3.engr.oregonstate.edu 25000 -l 25001 to get the 
directory using data port 25001. 

To get a directory list using ftclient.java, run the command javac ftclient.java.
After that, type java ftclient flip3.engr.oregonstate.edu 25000 -g 25001 filename.txt to 
get filename.txt through data port 25001.

If the server says "bind error: bad value for ai_flags" I believe the selected port is in use.
Try a different one.

This hasn't been tested if the data port is already in use, but I'm sure it will end gracefully.