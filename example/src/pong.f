\ NES port of PONG

require "audio.f"
require "graphics.f"
require "input.f"
require "startup.f"

SEGMENT "CODE"

variable GAMEPAD

\ Graphics update loop
: NMI 

; INTERRUPT

\ Check for user input
: CHECKGP ;

\ Main game logic loop
: MAIN 
    1 0 DO
        CHECKGP
    0 +LOOP
;

\ Reset console and jump to main loop on startup
: STARTUP RESET MAIN ;

SEGMENT "STARTUP"

STARTUP
