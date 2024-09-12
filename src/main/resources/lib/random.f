\ NES RNG library using an XORshift generator
\ Author: Edward Conn

require "memory.f"

segment "CODE"

: seed $3000 ;

: setseed ( n -- ) seed ! ;

: rnd ( -- n ) 
    seed @             ( Push the value at seed to TOS )
    DUP 07 LSHIFT XOR
    DUP 09 RSHIFT XOR
    DUP 13 LSHIFT XOR  ( Generate the next seed )
    DUP seed ! ;       ( Duplicate the result and store a copy )

: random ( n -- n ) RND SWAP MOD ;
