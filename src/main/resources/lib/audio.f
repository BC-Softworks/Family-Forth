\ NES audio library
\ author: Edward Conn

\ Defines the following words
\ SILENCE

require "control.f"

segment "CODE"

CONST
    pulse1  = $4000	    \ Pulse 1 	Timer, length counter, envelope, sweep
    pulse2  = $4004 	\ Pulse 2 	Timer, length counter, envelope, sweep
    tri     = $4008 	\ Triangle 	Timer, length counter, linear counter
    noise   = $400C	    \ Noise 	Timer, length counter, envelope, linear feedback shift register
    dmc     = $4010 	\ DMC 	    Timer, memory reader, sample buffer, output unit
    counter = $4015 	\ Channel enable and length counter status
    frame   = $4017 	\ Frame counter
ENDCONST

: SILENCE $13 0 
    DO 
        I           (  -- i )
        DUP         ( i -- i i )
        4 MOD       ( i i -- i b )
        IF
            $30     
        ELSE 
            $00 
        THEN        ( i b -- i x )
        $4000 +     ( i x -- i x+$4000 )
        !           ( x addr  -- )
    LOOP
    15 $4015 !
    64 $4017 !
;
