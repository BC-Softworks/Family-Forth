\ NES port of PONG

require "audio.f"
require "graphics.f"
require "input.f"

\ Graphics update loop
: NMI 

;

\ Check for user input
: GAMEPAD ;

\ Main game logic loop
: MAIN 
    1 0 DO
        GAMEPAD
    0 +LOOP
;

MAIN