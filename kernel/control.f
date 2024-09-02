\ Core control flow words
\ author: Edward Conn

\ Defines the following core words
\ DO LOOP +LOOP 
\ BEGIN WHILE REPEAT
\ IF ELSE THEN I J

require "memory.f"

segment "CODE"

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
MACRO IF orig
	lda $00,X
	and $01,X
	beq orig
ENDMACRO

( C: orig1 -- orig2 )
\ Put the location of a new unresolved forward reference orig2 onto the control flow stack. 
\ Append the run-time semantics given below to the current definition. 
\ The semantics will be incomplete until orig2 is resolved (e.g., by THEN). 
\ Resolve the forward reference orig1 using the location following the appended run-time semantics. 

( -- )
\ Continue execution at the location given by the resolution of orig2.
\ In this implmentation it is the following block of code so a nop suffices
MACRO ELSE
	nop
ENDMACRO

( C: orig -- )
\ Append the run-time semantics given below to the current definition.
\ Resolve the forward reference orig using the location of the appended run-time semantics. 
\
\ ( -- )
\ Continue execution.
\ Provide an address for jmp
MACRO THEN
	nop
ENDMACRO


\ ( n1 | u1 n2 | u2 -- ) ( R: -- loop-sys )
\ Set up loop control parameters with index n2 | u2 and limit n1 | u1.
\ An ambiguous condition exists if n1 | u1 and n2 | u2 are not both the same type.
\ Anything already on the return stack becomes unavailable until the loop-control 
\ parameters are discarded. 
MACRO DO
	jsr >R		\ Place n2 | u2 onto the return stack
	jsr >R		\ Place n1 | u1 onto the return stack
	jsr GET_PC	\ Places the address of this line in W2
	LOAD_RETURN \ Places the previous line on the return stack
ENDMACRO

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
MACRO BEGIN
	nop
ENDMACRO

\ ( C: dest -- orig dest )
\ Put the location of a new unresolved forward reference orig onto the control flow stack, under the existing dest.
\ Append the run-time semantics given below to the current definition.
\ The semantics are incomplete until orig and dest are resolved (e.g., by REPEAT).
\
\ ( x -- )
\ If all bits of x are zero, continue execution at the location specified by the resolution of orig. 
MACRO WHILE orig
	lda $00,X
	and $01,X
	beq orig
ENDMACRO

\ ( C: orig dest -- )
\ Append the run-time semantics given below to the current definition, resolving the backward reference dest. 
\ Resolve the forward reference orig using the location following the appended run-time semantics.
\
\ ( -- )
\ Continue execution at the location given by dest. 
MACRO REPEAT dest
		jsr dest	\ Jumps to dest
ENDMACRO

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