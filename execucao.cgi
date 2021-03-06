#!/usr/bin/perl

$numRounds = 3500; # number of rounds to perform per simulation
$numNodes=500; # number of nodes
$numExec=1;
$EV=50;
$intervalAggr=10;
$dimX=200;
$dimY=200;
$rMax=30;

for(;$numNodes<=800; $numNodes+=300){
  for($id=0; $id<10; $id+=1){
    for($i=0 ; $i<$numExec; $i+=1) {
      system("./sinalgo " .
      "-batch ".
      "-project etxBet " .             # choose the project
      "-gen $numNodes etxBet:NodeEtxBet etxBet:ReadTopology " . # generate nodes
      #"-gen $numNodes etxBet:NodeEtxBet Random " . # generate nodes
      "-rounds $numRounds ".
      "UDG ".
      "-overwrite ".
      "UDG/rMax=$rMax ".
      "NumberNodes=$numNodes " .
      "EV=$EV ".
      "intervalAggr=$intervalAggr ".                   # Overwrite configuration file parameters
      "dimX=$dimX ".
      "dimY=$dimY ".
      "idTopology=$id"
      );
      
      system("./sinalgo " .
      "-batch ".
      "-project hopBet " .             # choose the project
      "-gen $numNodes hopBet:NodeHopSbet hopBet:ReadTopology " . # generate nodes
      "-rounds $numRounds ".
      "UDG ".
      "-overwrite ".
      "UDG/rMax=$rMax ".
      "NumberNodes=$numNodes " .
      "EV=$EV ".
      "intervalAggr=$intervalAggr ".                  # Overwrite configuration file parameters
      "dimX=$dimX ".
      "dimY=$dimY ".
      "idTopology=$id"
      );
    }
  }
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
