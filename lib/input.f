\ author: Edward Conn

\ Defines the following words
\ CTRL

require "core.f"

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

( x -- true | false )
\ Return true if and only if 
\ the A button has been pressed
: A 1 DUP CTRL AND = ;

( x -- true | false )
\ Return true if and only if 
\ the B button has been pressed
: B 2 DUP CTRL AND = ;

( x -- true | false )
\ Return true if and only if
\ the select button has been pressed
: SELECT 4 DUP CTRL AND = ;

( x -- true | false )
\ Return true if and only if
\ the START button has been pressed
: START 4 DUP CTRL AND = ;

( x -- true | false )
\ Return true if and only if
\ the up button has been pressed
: UP 8 DUP CTRL AND = ;

( x -- true | false )
\ Return true if and only if
\ the down button has been pressed
: DOWN 16 DUP CTRL AND = ;

( x -- true | false )
\ Return true if and only if
\ the down button has been pressed
: LEFT 32 DUP CTRL AND = ;

( x -- true | false )
\ Return true if and only if
\ the down button has been pressed
: RIGHT 64 DUP CTRL AND = ;