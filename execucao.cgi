#!/usr/bin/perl

$numRounds = 4000; # number of rounds to perform per simulation
$numNodes=200; # number of nodes
$numExec=25;
$EV=50;
$intervalAggr=10;
$dimX=300;
$dimY=300;

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
}


for($i=0 ; $i<$numExec; $i+=1) {
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