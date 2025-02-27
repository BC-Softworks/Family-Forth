\ author: Edward Conn

\ Defines the following words
\ CTRL

require "core.f"

variable input

segment "CODE"

( -- x )
\ Place the controller bytes on the data stack
\ Player 1's in the low byte
\ Player 2's in the high byte
CODE CTRL
    PUT
    lda #1
    sta $4016
    lda #0
    sta $4016
    ldy #8
@loop:
    lda $4016
    lsr
    ror $00,X
    lda $4017
    lsr
    ror $01,X
    dey
    bne @loop
    rts
ENDCODE

( -- )
\ Repoll user input
: POLL CTRL input ! ;

( x -- true | false )
\ Return true if and only if the button has been pressed
\ POLL must be called first
: PRESSED DUP input @ AND = ;

( x -- true | false )
\ Return true if and only if 
\ the A button has been pressed
: A_PRESSED $01 PRESSED ;

( x -- true | false )
\ Return true if and only if 
\ the B button has been pressed
: B_PRESSED $02 PRESSED ;

( x -- true | false )
\ Return true if and only if
\ the select button has been pressed
: SELECT $04 PRESSED ;

( x -- true | false )
\ Return true if and only if
\ the START button has been pressed
: START $08 PRESSED ;

( x -- true | false )
\ Return true if and only if
\ the up button has been pressed
: UP $0010 PRESSED ;

( x -- true | false )
\ Return true if and only if
\ the down button has been pressed
: DOWN $0020 PRESSED ;

( x -- true | false )
\ Return true if and only if
\ the down button has been pressed
: LEFT $0040 PRESSED ;

( x -- true | false )
\ Return true if and only if
\ the down button has been pressed
: RIGHT $0080 PRESSED ;