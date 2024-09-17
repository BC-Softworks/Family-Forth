\ Core Words
\ author: Edward Conn

\ Core words
\ DROP 2DROP DUP SWAP OVER 
\ 2DUP 2OVER ?DUP ROT 2SWAP 
\ 0= 0< AND OR XOR 2* 2/ 
\ LSHIFT RSHIFT DEPTH S>D

\ Arithmetic words
\ + - < > = 1+ 1- ABS * / */ MOD/ 
\ MIN MAX NEGATE INVERT CHAR+
\ UM* UM/MOD U< 

\ Memory manipulation words
\ @ C@ 2@ ! +! C! ALIGN ALIGNED 
\ CELL+ CELLS R> >R R@ 
\ ALLOT HERE , C, MOVE FILL
\ EXECUTE CREATE VARIABLE CONSTANT

\ Control flow words
\ DO LOOP +LOOP 
\ BEGIN WHILE REPEAT
\ IF ELSE THEN I J

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

\  Arithmetic words

( n1 n2 -- n3 )
CODE +
    clc
    lda $00,X
    adc $02,X
    sta $02,X
    lda $01,X
    adc $03,X
    sta $03,X
    jmp DROP
ENDCODE

( n1 n2 -- n3 )
CODE -
    sec			\ Set carry bit
    lda $02,X
    sbc $00,X
    sta $02,X	\ Store high byte
    lda $03,X
    sbc $01,X
    sta $03,X	\ Store low byte
    jmp DROP
ENDCODE

( n1 | u1 -- n2 | u2 )
\ Add one (1) to n1 | u1 giving the sum n2 | u2.
: 1+ 1 + ;

( n1 | u1 -- n2 | u2 )
\ Sub one (1) to n1 | u1 giving the differene n2 | u2.
: 1- 1 - ;

( n1 n2 -- flag )
\ flag is true if and only if n1 is less than n2. 
: < - 0< ; 

( n1 n2 -- flag )
\ flag is true if and only if n1 is greater than n2. 
: > SWAP - 0< ;

( n1 n2 -- flag )
\ flag is true if and only if n1 is equal to n2. 
: = XOR 0= ;

( n -- u )
\ u is the absolute value of n. 
CODE ABS
    lda $01,X
    and #%01111111
    sta $01,X
    rts
ENDCODE

( n1 n2 -- d )
\ Modified version that uses the Y register from 6502.org
CODE M*
    jsr LDW		    \ Load the first cell into W

    lda $02,X		\ Load the second cell into W2
    sta lowByteW2
    lda $03,X
    sta hiByteW2

mult16: 
    lda #$00
    sta $02,X	 	\ clear upper bits of product
    sta $03,X 
    ldy #$10		\ set binary count to 16 
		
shift:	
    lsr lowByteW+1 	\ divide lowByteW by 2 
    ror lowByteW
    bcc rot_r 
    lda $02,X	 	\ get upper half of product and add lowByteW2
    clc
    adc lowByteW2
    sta $02,X
    lda $03,X
    adc lowByteW2+1

rot_r:	
    ror			 	\ rotate partial product
    sta $03,X
    ror $02,X
    ror $01,X 
    ror $00,X
    dey
    bne shift
    rts
ENDCODE

( n1 n2 -- n3 )
: * M* SWAP DROP ;

\ ( n1 n2 -- n3 n4 )
\ n3rn4 = n2 / n1
CODE /MOD
    ldy #0			\ Clear W register
    sty lowByteW	\ Used to store quotient
    sty hiByteW
@loop:	
    jsr 2DUP		\ If R < D fall through
    jsr <   		\ Check if lesser	
    lda $00,X		\ Load low byte of flag
    bne @end		\ Break if true
    jsr DROP		\ Drop flag
    jsr -			\ R = R - D
    PUT 			\ Restore stack pointer

    clc				
    lda lowByteW	\ Increment W
    adc #1
    sta lowByteW
    lda hiByteW
    adc #0
    sta hiByteW		\ Check if high byte is greater then zero
    bpl @loop		\ will be for small value
    lda lowByteW	\ but necessary if over 255
    bne @loop

@end:	
    jsr DROP		\ Drop flag
    lda lowByteW	\ Set TOS to contents of W
    sta $00,X
    lda hiByteW
    sta $01,X
    rts
ENDCODE


\ ( n1 n2 -- n3 )
: / /MOD SWAP DROP ;

\ ( n1 n2 -- n3 )
\ Divide n1 by n2, giving the single-cell remainder n3.
: MOD /MOD DROP ;

\ ( n1 n2 n3 -- n4 )
\ Multiply n1 by n2 producing the intermediate double-cell result d.
\ Divide d by n3 giving the single-cell quotient n4.
: */ M* / ;

\ ( n1 n2 n3 -- n4 n5 )
\ Multiply n1 by n2 producing the intermediate double-cell result d. 
\ Divide d by n3 producing the single-cell remainder n4 and the single-cell quotient n5.
CODE */MOD
    jsr DROP	\ Move pointer to multiply x1 and x2
    jsr M*      \ ( n1 n2 n3 -- d1 n3 )
    PUT

    jsr /MOD	\ ( d1 n3 -- x1 x2 x3 )
    lda $00,X   \ TODO: Replace with 32 x 16 division
    sta $04,X
    lda $01,X
    sta $05,X
    jmp DROP
ENDCODE

\ ( n1 n2 -- n3 )
\ n3 is the lesser value
CODE MIN
    lda $01,X \ Compare high bytes first
    cmp $03,X
    bne @end  \ If unequal skip checking low byte
    lda $00,X \ Compare low bytes
    cmp $02,X \ If highest equal
@end:	
    bpl	@drop \ If n1 is less then n2 call drop
	jsr SWAP  \ Else call swap then drop
@drop:
    jmp DROP
ENDCODE

\ ( n1 n2 -- n3 )
\ n3 is the greater value
CODE MAX
    lda $01,X \ Compare high bytes first
    cmp $03,X
    bne @end  \ If unequal skip checking low byte
    lda $00,X \ Compare low bytes
    cmp $02,X \ If high bytes are equal
@end:	
    bmi @drop  \ If n1 is greater then n2 call drop
	jsr SWAP
@drop:
    jmp DROP
ENDCODE

\ ( x1 -- x2 )
\ Invert all bits of x1, giving its logical inverse x2. 
CODE INVERT
    lda $00,X
    eor #%11111111
    sta $00,X
    lda $01,X
    eor #%11111111
    sta $01,X
    rts
ENDCODE

\ ( n1 -- n2 )
\ Negate n1, giving its arithmetic inverse n2. 
: NEGATE INVERT 1+ ;

\ ( c-addr1 -- c-addr2 )
\ Add the size in address units of a character to c-addr1, giving c-addr2.
: CHAR+ 1+ ;

\ ( u1 u2 -- ud )
\ Multiply u1 by u2, giving the unsigned double-cell p ud.
\ All values and arithmetic are unsigned. 
\
\ From The Merlin 128 Macro Assembler disk, via 'The Fridge': http://www.ffd2.com/fridge/math/mult-div.s
CODE UM*
    PUT				\ Increase stack pointer
    lda #0
    sta $01,X		\ Clear the highByte of TOS
    ldy #17
    clc
		
@loop:	
    ror $01,X		\ ext+1
    ror
    ror $03,X		\ acc+1
    ror $02,X		\ acc
    bcc @mul

    clc
    adc $04,X		\ aux
    pha
    lda $05,X		\ aux+1
    adc $01,X		\ ext+1
    sta $01,X		\ ext+1
    pla
@mul:	
    dey
    bne @loop		\ When breaks ( u1 u2 -- u1 ud )

    sta $00,X		\ Store low byte
    jsr ROT			\ Swap result and u1
    jsr DROP
    jmp SWAP
ENDCODE

\ ( ud u1 -- u2 u3 )
\ Divide ud by u1, giving the quotient u3 and the remainder u2. 
\ All values and arithmetic are unsigned. 
\ An ambiguous condition exists if u1 is zero or if the quotient lies outside the range of a single-cell unsigned integer.
\
\ From The Merlin 128 Macro Assembler disk, via 'The Fridge': http://www.ffd2.com/fridge/math/mult-div.s
CODE UM/MOD
    PUT				\ Increase stack pointer
    lda #0
    sta $01,X		\ Clear the highByte of TOS
    ldy #16

@loop:  
    asl $04,X
    rol $05,X
    rol
    rol $01,X
    pha
    cmp $02,X
    lda $01,X
    sbc $03,X
    bcc @div
    sta $01,X
    pla
    sbc $02,X
    pha
    inc $04,X
@div:	
    pla
    dey
    bne @loop

    sta $00,X		\ Store low byte
    jsr SWAP		\ Swap result and u1
    jsr DROP		\ Drop u1
    jmp SWAP		\ Swap remainder and quotient
ENDCODE


( u1 u2 -- flag )
\ flag is true if and only if u1 is less than u2. 
CODE U<
	lda $03,X
	cmp $01,X
	bcc @true
	bne @false
	lda $02,X
	cmp $00,X
	bcc @true
@false:	
	lda #false
	beq @end
@true:
	lda #true
@end:
	jsr DROP
	jmp SETTOS
ENDCODE

( a-addr -- x ) 
\ Returns the full cell
CODE @
	jsr LDW
	ldy #0
	lda ($00),Y
	sta $00,X
	ldy #1
	lda ($00),Y
	sta $01,X
	rts  
ENDCODE

( c-addr -- char )
\ Fetch the character stored at c-addr. 
\ When the cell size is greater than character size, 
\ the unused high-order bits are all zeroes. 
CODE C@
	jsr LDW
	ldy #0
	lda ($00),Y
	sta $00,X
	sty $01,X
	rts  
ENDCODE

\ Returns two full cells
: 2@ ( a-addr -- x1 x2 )  DUP @ SWAP 2 + @ ;

( x a-addr -- )
\ Store the cell x1 at a-addr,
CODE !
	jsr LDW			\ Store the address in W
	jsr DROP		\ Drop the address from the stack
	ldy #0			\ Set offset to zero
	lda $00,X
	sta ($00),Y		\ Store the lowbyte at a-addr
	ldy #1
	lda $01,X		\ Store the highbyte at a-addr + 1
	sta ($00),Y
	jmp DROP		\ Drop x from the stack
ENDCODE

\ Add x to the single-cell number at a-addr.
: +! ( x a-addr -- ) SWAP OVER @ + SWAP	! ;

( char c-addr -- )
\ Store char at c-addr. 
\ When character size is smaller than cell size, 
\ only the number of low-order bits corresponding to character size are transferred. 
CODE C!
	jsr LDW
	jsr DROP		; Drop the address from the stack
	ldy #0			; Set offset to zero
	lda $00,X
	sta ($00),Y		; Store the lowbyte at a-addr
	jmp DROP		; Drop x from the stack
ENDCODE

\ Store the cell pair x1 x2 at a-addr, with x2 at a-addr and x1 at the
\ next consecutive cell.
: 2! ( x1 x2 a-addr -- ) SWAP OVER ! 2 + ! ;

\ Add the size in address units of a cell to a-addr1, giving a-addr2.
: CELL+ ( a-addr1 -- a-addr2 ) 2 + ;

\ n2 is the size in address units of n1 cells. 
: CELLS ( n1 -- n2 ) LSHIFT ;

\ ( -- n)
\ Transfer n from the return stack to the data stack.
\ IMPORTANT: Doesn't use W
CODE R>
	SAVE_RETURN
	PUT
	pla
	sta $00,X
	pla
	sta $01,X
	LOAD_RETURN
	rts
ENDCODE

( x -- ) ( R: -- x )
\ Move x to the return stack.
\ Tokenized >R
\ IMPORTANT: Doesn't use W
CODE >R
	SAVE_RETURN
	lda $01,X
	pha
	lda $00,X
	pha
	LOAD_RETURN
	jmp DROP
ENDCODE

( -- n )
\ Copy the number on top of the return stack to the data stack.
CODE R@
	jsr R> 
	jsr >R 
	PUT 
	rts
ENDCODE

( n -- )
\ If n is greater than zero, reserve n address units of data space. 
\ If n is less than zero, release | n | address units of data space. 
\ If n is zero, leave the data-space pointer unchanged.

\ If the data-space pointer is aligned and n is a multiple of the size of a cell when ALLOT begins execution, 
\ it will remain aligned when ALLOT finishes execution.

\ If the data-space pointer is character aligned and n is a multiple of the size of a character when ALLOT begins execution, 
\ it will remain character aligned when ALLOT finishes execution. 
CODE ALLOT
	sec
	lda lowByteDSP
	sbc $00,X
	sta lowByteDSP
	lda hiByteDSP
	sbc $01,X
	sta hiByteDSP
	jmp DROP
ENDCODE

( -- addr )
\ addr is the data-space pointer. 
CODE HERE
	dex
	lda hiByteDSP
	sta $00,X
	dex
	lda lowByteDSP
	sta $00,X
	rts
ENDCODE

( addr -- a-addr )
\ a-addr is the first aligned address greater than or equal to addr. 
CODE ALIGNED
	lda $00,X
	and #1
	beq @aligned
	jmp CHAR+
@aligned:
	rts
ENDCODE

\ If the data-space pointer is not aligned, 
\ reserve enough space to align it.
: ALIGN ( -- ) HERE ALIGNED HERE - ALLOT ;

\ Reserve one cell of data space and store x in the cell. 
: , ( x -- ) 2 ALLOT HERE ! ;

\ Reserve space for one character in the data space and store char in the space.
\ If the data-space pointer is character aligned when C, begins execution, it will remain character aligned when C, finishes execution.
\ An ambiguous condition exists if the data-space pointer is not character-aligned prior to execution of C,. 
: C, ( char -- ) 1 ALLOT HERE C! ;

( addr1 addr2 u -- )
\ If u is greater than zero, copy the contents of u 
\ consecutive address units at addr1 to the u consecutive address units at addr2.
\ After MOVE completes, the u consecutive address units 
\ at addr2 contain exactly what the u consecutive address units at addr1 contained before the move. 
CODE MOVE
	lda $00,X
	ora $01,X		\ Compare low and high bytes
	bne @rot		\ Skip if u = 0
	jsr 2DROP		\ Drop addr1, addr2, and u
	jmp DROP

@rot:	
	jsr SWAP		\ Swap u and addr2
	lda $00,X		\ Copy addr2 to W2
	sta lowByteW2
	lda $01,X
	sta hiByteW2
	jsr DROP		\ Drop addr2
	jsr SWAP		\ Swap u and addr1
	jsr LDW			\ Copy addr1 to W
	jsr DROP		\ Drop addr1

	ldy #0			\ Init Y
@loop:	
	lda ($00),Y		\ Load byte from addr1 + y
	sta ($02),Y		\ Store byte at addr2 + y
	iny				\ Increment Y
	jsr 1-			\ Subtract one from u \ TODO: Use unsigned sub
	lda $00,X
	ora $01,X		\ Compare low and high bytes
	bne @loop		\ Break if zero
	jmp DROP		\ Drop u
ENDCODE

( c-addr u char -- )
\ If u is greater than zero, store char in each of u consecutive characters of memory beginning at c-addr. 
CODE FILL
	lda $02,X
	ora $03,X		\ Compare low and high bytes
	bne start		\ Skip if u = 0
	jsr DROP		\ Drop addr1, addr2, and u
	jmp 2DROP

start:	
	lda $00,X		\ Load char into W2
	sta $02
	jsr DROP		\ Drop char
	jsr SWAP		\ Swap c-addr and u 
	jsr LDW			\ Copy c-addr to W
	jsr DROP		\ Drop c-addr
	
	ldy #0			\ Init Y
@loop:
	lda $02			\ Load byte from addr1 + y
	sta ($00),Y		\ Store byte at addr2 + y
	iny				\ Increment Y
	jsr 1-			\ Subtract one from u
	lda $00,X
	ora $01,X		\ Compare low and high bytes
	bne @loop		\ Break if zero
	jmp DROP		\ Drop u
ENDCODE

\ ( c-addr1 -- c-addr2 u )
\ Return the character string specification for the counted string stored at c-addr1.
\ c-addr2 is the address of the first character after c-addr1.
\ u is the contents of the character at c-addr1, which is the length in characters of the string at c-addr2.
\ TODO: Handle empty strings
CODE COUNT
	jsr LDW		\ Store c-addr1 in W
	ldy #$00	\ Set y to 0
@loop:	
	iny
	jsr 1+		\ Increment c-addr1
	lda ($00),Y	\ Load current address
	bne @loop	\ Loop if char is not zero

	PUT
	tya			\ Set top of stack to Y
	sta $00,X
	lda #0
	sta $01,X
	rts
ENDCODE

( i * x xt -- j * x )
\ Remove xt from the stack and perform the semantics identified by it.
\ Other stack effects are due to the word EXECUTEd. 
CODE EXECUTE
	jsr >R
	rts	      \ Return to address pushed
ENDCODE

\ Reserving the words in the library, 
\ definitions unused until paring is finished
\ Runtime parsing not supported

\ a-addr is the address of name's data field.
: CREATE ( -- a-addr ) NONE ;

\ a-addr is the address of the reserved cell.
: CONSTANT ( -- x ) NONE ;

\ a-addr is the address of the reserved cell.
: VARIABLE ( -- a-addr ) ALIGN HERE 0 , CONSTANT ;

\ Core control flow words

\ Put the location of a new unresolved forward reference orig onto the control flow stack.
MACRO PUSHORIG orig
	lda orig + 1
	pha
	lda orig
	pha
ENDMACRO

\ Pull the location of a forward reference orig onto the data stack.
MACRO POPORIG orig
	pla
	sta orig
	pla
	sta orig + 1
ENDMACRO

\ Stores the program counter in W2
CODE GET_PC
	SAVE_RETURN
	LOAD_RETURN
	rts
ENDCODE

( C: -- orig )
\ Put the location of a new unresolved forward reference orig onto the control flow stack.
\ Append the run-time semantics given below to the current definition. 
\ The semantics are incomplete until orig is resolved, e.g., by THEN or ELSE. 

( x -- )
\ If all bits of x are zero, continue execution
\ at the location specified by the resolution of orig. 
: IF 0BRANCH ;

( C: orig1 -- orig2 )
\ Put the location of a new unresolved forward reference orig2 onto the control flow stack. 
\ Append the run-time semantics given below to the current definition. 
\ The semantics will be incomplete until orig2 is resolved (e.g., by THEN). 
\ Resolve the forward reference orig1 using the location following the appended run-time semantics. 

( -- )
\ Continue execution at the location given by the resolution of orig2.
: ELSE NONE ;

( C: orig -- )
\ Append the run-time semantics given below to the current definition.
\ Resolve the forward reference orig using the location of the appended run-time semantics. 
\
\ ( -- )
\ Continue execution.
: THEN R> DROP ;

\ ( n1 | u1 n2 | u2 -- ) ( R: -- loop-sys )
\ Set up loop control parameters with index n2 | u2 and limit n1 | u1.
\ An ambiguous condition exists if n1 | u1 and n2 | u2 are not both the same type.
\ Anything already on the return stack becomes unavailable until the loop-control 
\ parameters are discarded. 
: DO >R	>R GET_PC LOAD_RETURN ;

\ ( C: do-sys -- )
\ Append the run-time semantics given below to the current definition.
\ Resolve the destination of all unresolved occurrences of LEAVE between the location given 
\ by do-sys and the next location for a transfer of control, to execute the words following +LOOP.
\
\ ( n -- ) ( R: loop-sys1 -- | loop-sys2 )
\ An ambiguous condition exists if the loop control parameters are unavailable.
\ Add n to the loop index. If the loop index did not cross the boundary between the
\ loop limit minus one and the loop limit, continue execution at the beginning of the loop.
\ Otherwise, discard the current loop control parameters and continue execution immediately following the loop. 
CODE +LOOP
		pla				\ Save the fall through address
		sta lowByteW  	\ Store the lowbyte of the addr in the W register
		pla				\ Repeat for the high byte
		sta hiByteW

		jsr R>			( n -- n addr )
		jsr SWAP		( n addr -- addr n )
		jsr R>			( addr n -- addr n c )
		jsr +			( addr n c -- addr n+c )
		jsr R>			( addr n+c -- addr n+c l )
		jsr 2DUP		\ Duplicate the limit and counter
		jsr =			\ Check if equal
		lda $00,X
		bne @end		\ Numbers are equal end loop
		jsr DROP		\ Drop the flag
		jsr SWAP		( addr n+c l -- addr l n+c )
		jsr >R			( addr n+c l -- addr l )
		jsr >R			( addr l -- addr )
		jsr DUP			( addr -- addr addr )
		jsr >R			( addr addr -- addr )
		jmp >R 			\ Return to the top of the loop			
@end:	
		lda hiByteW		\ Load the highByte of the addr in the W register
		pha           	\ Push the highByte of the start of the loop
		lda lowByteW  	\ Load the lowbyte of the addr in the W register
		pha           	\ Push the lowbyte of the start of the loop
		jsr 2DROP		( addr n+c l f -- addr n+c )
		jmp 2DROP		( addr n+c -- )
ENDCODE

\ ( -- ) ( R: loop-sys1 -- | loop-sys2 ) 
\ Add one to the loop index. If the loop index is then equal to the loop limit, 
\ discard the loop parameters and continue execution immediately following the loop. 
\ Otherwise continue execution at the beginning of the loop. 
: LOOP 1 +LOOP ;

\ ( -- ) ( R: loop-sys -- )
\
\ Discard the current loop control parameters.
\ An ambiguous condition exists if they are unavailable.
\ Continue execution immediately following the innermost syntactically enclosing DO...LOOP or DO...+LOOP. 
MACRO LEAVE addr
		ldy #5
@loop:	
		pla
		dey			\ Load loop-sys and drop on the floor
		bne @loop	\ Six bytes, three cells
		clc
		bcc addr	\ Branch to after LOOP or +LOOP
ENDMACRO

\ ( C: -- dest )
\ Put the next location for a transfer of control, dest, onto the control flow stack. 
\ Append the run-time semantics given below to the current definition.
\
\ ( -- )
\ Continue execution. 
: BEGIN >R DUP R> R> ;

\ ( C: dest -- orig dest )
\ Put the location of a new unresolved forward reference orig onto the control flow stack, under the existing dest.
\ Append the run-time semantics given below to the current definition.
\ The semantics are incomplete until orig and dest are resolved (e.g., by REPEAT).
\
\ ( x -- )
\ If all bits of x are zero, continue execution at the location specified by the resolution of orig. 
: WHILE 0BRANCH ;

\ ( C: orig dest -- )
\ Append the run-time semantics given below to the current definition, resolving the backward reference dest. 
\ Resolve the forward reference orig using the location following the appended run-time semantics.
\
\ ( -- )
\ Continue execution at the location given by dest. 
: REPEAT BRANCH ;

\ Helper for I and J
\ Roughly halves bytes used
CODE LPARM
		ldy lowByteW2	\ Pull loop parameters
@pull:	dex
		pla
		sta $00,X
		dey
		bne @pull		\ Break when Y is zero
		jsr LDW			\ Store counter in W
		ldy lowByteW2	\ Push loop parameters back onto the stack
@push:	pla
		sta $00,X
		inx
		dey
		bne @push		\ Break when Y is zero
		PUT				\ Copy W to TOS
		lda	lowByteW
		sta $00,X
		lda	hiByteW
		sta $01,X
		rts
ENDCODE

\ ( -- n | u ) ( R: loop-sys -- loop-sys )
\ n | u is a copy of the current (innermost) loop index. 
\ An ambiguous condition exists if the loop control parameters are unavailable. 
CODE I
	lda #8
	sta lowByteW2
	jmp LPARM
ENDCODE

\ ( -- n | u ) ( R: loop-sys1 loop-sys2 -- loop-sys1 loop-sys2 )
\ n | u is a copy of the next-outer loop index.
\ An ambiguous condition exists if the loop control parameters of the next-outer loop, loop-sys1, are unavailable.
CODE J
	lda #14
	sta lowByteW2
	jmp LPARM
ENDCODE
