\ NES port of PONG
\ Uses the honkeypongtiles char

require "audio.f"
require "graphics.f"
require "input.f"
require "startup.f"

SEGMENT "CODE"

\ Graphics update loop
: NMI 

; INTERRUPT

\ Check for user input
\ Return directions to stack
( -- up down left right )
: GAMEPAD POLL UP DOWN LEFT RIGHT ;

\ Main game logic loop
: MAIN 
    0 0 DO
        GAMEPAD

    0 +LOOP
;

\ Reset console and jump to main loop on startup
: STARTUP RESET MAIN ;

SEGMENT "STARTUP"

STARTUP
