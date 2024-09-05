\ Core Word Extensions
\ author: Edward Conn

\ Defines the following words core extension words:
\ NIP PICK ROLL ERASE TRUE FALSE <> 0> 0<> 
\ TUCK PAD U> 2>R 2R> 2R@ UNUSED VALUE

require "memory.f"

segment "CODE"

( x1 x2 -- x2 )
( Remove the second cell on the stack )
: NIP SWAP DROP ;

( xu...x1 x0 u -- xu...x1 x0 xu )
\ Remove u. Copy the xu to the top of the stack. 
\ An ambiguous condition exists if there are less 
\ than u+2 items on the stack before PICK is executed.
\ Implementation note: Since the stack is 60 cells max u is only using the lower byte
CODE PICK
	clc
	lda $00,X
	tay
	adc $00,X   \ Convert cells to bytes
	stx $00		\ Store TOS address
	adc $00    	\ Add from top of stack
	ldy #0    	\ Clear Y
	sta $00   	\ Save addr at $00    
	lda ($00),Y \ Load xu's low byte indirectly
	sta $00,X
	iny			\ Increment y to get high byte
	lda ($00),Y \ Load xu's high byte indirectly
	sta $01,X
	rts        	\ Don't need to drop anything
ENDCODE

( xu xu-1 ... x0 u -- xu-1 ... x0 xu )
\ Remove u. Rotate u+1 items on the top of the stack.
\ An ambiguous condition exists if there are less than u+2 items on the stack before ROLL is executed. 
CODE ROLL
	clc
	lda $00,X
	tay 		\ Load u into y
	iny
	tya
	sta $00,X
	txa			\ Transfer pointer to A
	adc $00,X	\ Move index to location of xu in the stack
	adc $00,X	\ Add twice since cells are two bytes
	tax			\ Overwrite pointer
	dey			\ Predecrement y
@l:	
	jsr SWAP	\ Swap xn and xn+1
	PUT
	dey
	bpl @l
	rts
ENDCODE

( addr u -- )
\ If u is greater than zero, clear all bits
\ in each of u consecutive address units of memory beginning at addr. 
CODE ERASE
	PUT
	ldy #0
	sty $00,X
	sty $01,X
	jmp FILL
ENDCODE

( -- true )
\ Return a true flag, a single-cell value with all bits set. 
CODE TRUE
	PUT
	lda #true
	jmp SETTOS
ENDCODE

( -- false )
\ Return a false flag, a single-cell value with no bits set.
\ Same as pushing 0 onto the stack
CODE FALSE
	PUT
	lda #false
	jmp SETTOS
ENDCODE


( x1 x2 -- flag )
\ flag is true if and only if x1 is not bit-for-bit the same as x2.
: <> = 0= ;


( n -- flag )
\ flag is true if and only if n is greater than zero.
CODE 0>
	lda $01,X
	bmi @neg
	eor $00,X
	beq @neg
	lda #true
	jmp SETTOS
@neg:
	lda #false
	jmp SETTOS
ENDCODE

( x -- flag )
\ flag is true if and only if x is not equal to zero. 
: 0<> 0 <> ;

( x1 x2-- x2 x1 x2 )
( Copy the first stack item below the second stack item. )
: TUCK SWAP OVER ;

( -- c-addr )
\ c-addr is the address of a transient region that can be used to hold data for intermediate processing.
\ $0400 => 1024
: PAD 1024 ;

( u1 u2 -- flag )
\ flag is true if and only if u1 is greater than u2. 
: U> + 2/ 0> ;

( x1 x2 -- ) ( R: -- x1 x2 )
\ Transfer cell pair x1 x2 to the return stack.
: 2>R SWAP >R >R ;

( -- x1 x2 ) ( R: x1 x2 -- )
\ Transfer cell pair x1 x2 from the return stack.
: 2R> R> R> SWAP ;

( -- x1 x2 ) ( R: x1 x2 -- x1 x2 )
\ Copy cell pair x1 x2 from the return stack
: 2R@ R> R> 2DUP >R >R SWAP ;

( -- u )
\ u is the amount of space remaining in the region addressed by HERE, in address units. 
: UNUSED HERE 4 - 2/ ;
