#!/usr/bin/perl

$numRounds = 4000; # number of rounds to perform per simulation
$numNodes=200; # number of nodes
$numExec=30;
for($i=0 ; $i<=$numExec; $i+=1) {
  system("./sinalgo " .
  "-batch ".
  "-project etxBet " .             # choose the project
  "-gen $numNodes etxBet:NodeEtxBet Random " . # generate nodes
  "-overwrite " .                   # Overwrite configuration file parameters
  "exitAfter=true exitAfter/Rounds=$numRounds " . # number of rounds to perform & stop
#  "exitOnTerminationInGUI=true " .  # Close GUI when hasTerminated() returns true
  "AutoStart=true " .               # Automatically start communication protocol
#  "outputToConsole=false " .        # Create a framework log-file for each run
#  "extendedControl=false " .        # Don't show the extended control in the GUI
  "-rounds $numRounds " .           # Number of rounds to start simulation
#  "-refreshRate 20");               # Don't draw GUI often
  "NumberNodes=$numNodes " . #numero de nodes
  "EV=50 " . # % de nodes que vao emitir eventos
  "intervalAggr=10 " # intervalo de agregracao
  
  );
}

for($i=0 ; $i<=$numExec; $i+=1) {
  system("./sinalgo " .
  "-batch ".
  "-project hopBet " .             # choose the project
  "-gen $numNodes hopBet:NodeHopSbet Random " . # generate nodes
  "-overwrite " .                   # Overwrite configuration file parameters
  "exitAfter=true exitAfter/Rounds=$numRounds " . # number of rounds to perform & stop
#  "exitOnTerminationInGUI=true " .  # Close GUI when hasTerminated() returns true
  "AutoStart=true " .               # Automatically start communication protocol
#  "outputToConsole=false " .        # Create a framework log-file for each run
#  "extendedControl=false " .        # Don't show the extended control in the GUI
  "-rounds $numRounds " .           # Number of rounds to start simulation
#  "-refreshRate 20");               # Don't draw GUI often
  "NumberNodes=$numNodes " . #numero de nodes
  "EV=50 " . # % de nodes que vao emitir eventos
  "intervalAggr=10 " # intervalo de agregracao
  
  );
}