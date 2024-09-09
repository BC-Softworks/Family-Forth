\ Core Words
\ author: Edward Conn

\ Defines the following words
\ DROP 2DROP DUP SWAP OVER 
\ 2DUP 2OVER ?DUP ROT 2SWAP 
\ 0= 0< AND OR XOR 2* 2/ 
\ LSHIFT RSHIFT DEPTH S>D

\ Kernel "register" locations
CONST
	lowByteW	= $00
	hiByteW		= $01
	lowByteW2	= $02
	hiByteW2	= $03
	lowByteDSP	= $04
	hiByteDSP	= $05
	radix		= $06
	mode		= $07
	lowByteDP	= $08
	hiByteDP	= $09
	false		= %00000000
	true		= %11111111
ENDCONST

\ Global Macros

MACRO BRANCH
	rts
ENDMACRO

( x -- )
( R: addr -- | addr )
\ Branch to the next address on the
\ return stack if teh TOS is 0
CODE 0BRANCH
	lda $00,X
	and $01,X
	beq @end
	rts
@end:
ENDCODE

MACRO NONE
	nop
ENDMACRO

MACRO PUT
	dex
	dex
ENDMACRO

MACRO PUSH arg0, arg1
	PUT
    lda arg0
    sta 00,X
	lda arg1
    sta 01,X
ENDMACRO

\ Save return value in W2
MACRO SAVE_RETURN
	pla
	sta lowByteW2
	pla
	sta hiByteW2
ENDMACRO

\ Load return value from W2
MACRO LOAD_RETURN
	lda hiByteW2
	pha
	lda lowByteW2
	pha
ENDMACRO

\ Helper procs

CODE LDW
	lda $00,X
	sta lowByteW
	lda $01,X
	sta hiByteW
	rts
ENDCODE

\ Set Top of Stack to value in A
CODE SETTOS
	sta $00,X
	sta $01,X
	rts
ENDCODE

segment "CODE"

( x -- )
\ Drop x from the stack
\ Underflow prevention
CODE DROP
	cpx #0
	beq @end
	inx
	inx
@end:	
	rts
ENDCODE

( x1 x2 -- )
\ Drop x1 and x2 from the stack
CODE 2DROP
	txa
	clc
	adc #4
	tax
	rts
ENDCODE

( x -- x x )
\ Copy x and place it on the stack
CODE DUP
	PUT
	lda $02,X
	sta $00,X
	lda $03,X
	sta $01,X
	rts
ENDCODE

( x1 x2 -- x2 x1 )
\ Swaps the position of x1 and x2
CODE SWAP
	lda $00,X    \ Load the lowByte from the TOS
	sta lowByteW
	lda $01,X    \ Load the hiByte from the TOS
	sta hiByteW

	lda $02,X    \ Load the lowByte
	sta $00,X
	lda $03,X    \ Load the hiByte
	sta $01,X

	lda lowByteW \ Load the lowByte from W
	sta $02,X
	lda hiByteW  \ Load the hiByte from W
	sta $03,X

	rts
ENDCODE

( x1 x2 -- x1 x2 x1 )
\ Duplicate the second cell on  the stack
\ place it on top
CODE OVER
	PUT
	lda $04,X \ Load now third in stack
	sta $00,X \ Store low byte of second
	lda $05,X
	sta $01,X \ Store high byte of second
	rts
ENDCODE

( x1 x2 -- x1 x2 x1 x2 )
\ Duplicate cell pair x1 x2. 
: 2DUP OVER OVER ;

( x1 x2 x3 x4 -- x1 x2 x3 x4 x1 x2 )
\ Copy cell pair x1 x2 to the top of the stack. 
CODE 2OVER
	ldy #2
@loop:
	PUT
	lda $08,X
	sta $00,X
	lda $09,X
	sta $01,X
	dey
	bne @loop
	rts
ENDCODE

( x -- 0 | x x )
\ Duplicate x if it is non-zero.
CODE ?DUP
	lda $00,X
	and $01,X
	beq @skip
	jmp DUP
@skip:	
	rts
ENDCODE

( x1 x2 x3 -- x2 x3 x1 )
\ Rotate the top three stack entries.
\ Optimized by moving the stack pointer instead of the objects.
CODE ROT
	jsr DROP	\ ( x1 x2 x3 -- x1 x2	)
	jsr SWAP	\ ( x1 x2 	 -- x2 x1	)
	PUT			\ ( x1 x2 	 -- x2 x1 x3)
	jmp SWAP	\ ( x1 x2 	 -- x2 x3 x1)
ENDCODE

( x1 x2 x3 x4 -- x3 x4 x1 x2 )
\ Exchange the top two cell pairs.
\ Optimized by moving the stack pointer instead of the objects.
CODE 2SWAP
	jsr ROT		\ ( x1 x2 x3 x4 -- x1 x3 x4 x2 )
	jsr DROP
	jsr ROT
	PUT
	rts
ENDCODE

\ Logical operations

\ ( x -- flag )
\ flag is true if and only if x is equal to zero.
CODE 0=
	lda $00,X
	ora $01,X
	beq @true
	lda #false
	jmp SETTOS
@true: 	
	lda #true
	jmp SETTOS
ENDCODE

\ ( x -- flag )
\ Synonym of 0=
: NOT 0= ;

( n -- flag )
\ flag is true if and only if n is less than zero.
CODE 0<
	lda $01,X
	bmi @neg
	lda #false
	jmp @set
@neg: 	
	lda #true
@set: 	
	sta $00,X
	sta $01,X
	rts
ENDCODE

( x1 x2 -- x3 )
\ x3 is the bit-by-bit logical "and" of x1 with x2.
CODE AND
	lda $00,X
	and $02,X
	sta $02,X
	lda $01,X
	and $03,X
	sta $03,X
	jmp DROP
ENDCODE

( x1 x2 -- x3 )
\ x3 is the bit-by-bit logical "or" of x1 with x2.
CODE OR
	lda $00,X
	ora $02,X
	sta $02,X
	lda $01,X
	ora $03,X
	sta $03,X
	jmp DROP
ENDCODE

( x1 x2 -- x3 )
\ x3 is the bit-by-bit logical "xor" of x1 with x2.
CODE XOR
	lda $00,X
	eor $02,X
	sta $02,X
	lda $01,X
	eor $03,X
	sta $03,X
	jmp DROP
ENDCODE

\ Bit Shifts

\ ( x1 -- x2 )
\ x2 is the result of shifting x1 one bit toward the most-significant bit, 
\ filling the vacated least-significant bit with zero. 
CODE 2*
	clc
	asl $00,X
	rol $01,X
	rts
ENDCODE

\ ( x1 -- x2 )
\ x2 is the result of shifting x1 one bit toward the least-significant bit, 
\ leaving the most-significant bit unchanged.
CODE 2/
	clc
	lda #$80		\ Store the 7th bit
	and $01,X
	lsr $01,X
	ror $00,X
	ora $01,X		\ Restore the 7th bit
	sta $01,X
	rts
ENDCODE

( x1 u -- x2 )
\ Perform a logical left shift of u bit-places on x1, giving x2.
\ Put zeroes into the least significant bits vacated by the shift.
\ If u is greater or equal to cell size return 0.
CODE LSHIFT
	lda $01,X	\ Check if the number is greater then 16
	bne @over
	lda $00,X	\ Check if the number is greater then 16
	and %11110000
	beq @start
@over:	
	jsr DROP
	lda #false
	jmp SETTOS
@start:
	ldy $00,X
	jsr DROP	\ Drop u
@loop:
	jsr 2*
	dey
	bne @loop
	rts
ENDCODE

( x1 u -- x2 )
\ Perform a logical right shift of u bit-places on x1, giving x2. 
\ Put zeroes into the most significant bits vacated by the shift.
\ If u is greater or equal to cell size return 0.
CODE RSHIFT
	lda $01,X		\ Check if the number is greater then 16
	bne @over
	lda $00,X		\ Check if the number is greater then 16
	and %11110000
	beq @start
@over:	
	jsr DROP
	lda #false
	jmp SETTOS

@start: 
	ldy $00,X
	jsr DROP		\ Drop u
@loop:
	jsr 2/
	dey
	bne @loop
	rts
ENDCODE

( -- +n )
\ +n is the number of single-cell values contained in 
\ the data stack before +n was placed on the stack
CODE DEPTH
	txa				\ Transfer stack pointer to A
	bne	@put		\ If empty return zero
	lda #0
	sta $01,X
	sta $00,X
	rts
@put:	
	PUT
	eor #$FF		\ Inverse pointer
	adc #1
	sta $00,X
	lda #0			\ Clear A
	sta $01,X
	jmp 2/
ENDCODE

( n -- d )
\ Convert the number n to the double-cell number d with the same numerical value. 
CODE S>D
	lda $00,x
	bmi @neg	\ Push $FFFF if negative
	lda #false
	beq @set
@neg:
	lda #true
@set:	
	PUT
	jsr SETTOS
	jmp SWAP
ENDCODE
