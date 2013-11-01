#!/usr/bin/perl

$numRounds = 5000; # number of rounds to perform per simulation
$numNodes=500; # number of nodes
$numExec=5;
$EV=50;
$intervalAggr=10;
$dimX=200;
$dimY=200;

for($i=0 ; $i<$numExec; $i+=1) {
  system("./sinalgo " .
  "-batch ".
  "-project etxBet " .             # choose the project
  "-gen $numNodes etxBet:NodeEtxBet Random " . # generate nodes
  "-rounds $numRounds ".
  "-overwrite ".
  "NumberNodes=$numNodes " .
  "EV=$EV ".
  "intervalAggr=$intervalAggr ".                   # Overwrite configuration file parameters
  "dimX=$dimX ".
  "dimY=$dimY "
  );
  
  system("./sinalgo " .
  "-batch ".
  "-project hopBet " .             # choose the project
  "-gen $numNodes hopBet:NodeHopSbet Random " . # generate nodes
  "-rounds $numRounds ".
  "-overwrite ".
  "NumberNodes=$numNodes " .
  "EV=$EV ".
  "intervalAggr=$intervalAggr ".                  # Overwrite configuration file parameters
  "dimX=$dimX ".
  "dimY=$dimY "
  );
}


#for($i=0 ; $i<$numExec; $i+=1) {
 # system("./sinalgo " .
 # "-batch ".
 # "-project hopBet " .             # choose the project
 # "-gen $numNodes hopBet:NodeHopSbet Random " . # generate nodes
 # "-rounds $numRounds ".
 # "-overwrite ".
 # "NumberNodes=$numNodes " .
 # "EV=$EV ".
 # "intervalAggr=$intervalAggr ".                  # Overwrite configuration file parameters
 # "dimX=$dimX ".
 # "dimY=$dimY "
 # );
#}