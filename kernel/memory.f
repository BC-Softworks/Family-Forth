\ Core memory manipulation words
\ author: Edward Conn

\ Defines the following words core words
\ @ C@ 2@ ! +! C! ALIGN ALIGNED 
\ CELL+ CELLS R> >R R@ 
\ ALLOT HERE , C, MOVE FILL
\ EXECUTE CREATE VARIABLE CONSTANT

require "math.f"

segment "CODE"

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

\ Runtime code only TODO: Add compile time parsing
\ a-addr is the address of name's data field.
: CREATE ( -- a-addr ) HERE 1 ALLOT HERE ;

( -- a-addr )
: VARIABLE CREATE 0 , ;

( -- x )
: COSNTANT VARIABLE @ ;