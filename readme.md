#Distributed Rock Paper Scissors
This is a multiplayer distributed Rock-paper-scissors game built using CORBA and written in Java. Eclipse project files are included but eclipse is not required.  

###Running 

All included java files must be compiled before proceeding. The idlj can be
used to recreate the CORBA proxy code.

For Windows make sure you have java in your path. 

####Start the tnameserv on port 1050

Linux/OS X: ```sudo tnameserv -ORBInitialPort 1050```
Windows: ```tnameserv -ORBInitialPort 1050```

Port 1050 is not required and can be changed by modifying the code on the
clients and servers.

####Run the server 

```
java RPSDistServer
```

####Run two clients (CLI or GUI clients, any combination)

CLI: ```java RPSDistClient```

GUI: ``java MainJFrame`` 

####Play the game

Play a simple distributed version of rock paper scissors from the two clients.
