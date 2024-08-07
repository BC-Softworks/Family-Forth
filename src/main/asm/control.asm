;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words
; DO LOOP +LOOP 
; BEGIN WHILE REPEAT
; IF ELSE THEN I J

.ifndef CONTROL_GUARD
	CONTROL_GUARD = 1
.endif

.ifndef MEMORY_GUARD
	.include "memory.asm"
.endif

.ifndef MATH_GUARD
	.include "math.asm"
.endif

.ifndef CORE_GUARD
	.include "core.asm"
.endif

; Stores the program counter in W2
.proc GET_PC
	SAVE_RETURN
	LOAD_RETURN
	rts
.endproc

; ( C: -- orig )
; Put the location of a new unresolved forward reference orig onto the control flow stack.
; Append the run-time semantics given below to the current definition. 
; The semantics are incomplete until orig is resolved, e.g., by THEN or ELSE. 

; ( x -- )
; If all bits of x are zero, continue execution
; at the location specified by the resolution of orig. 
.macro IF orig
		lda $00,X
		and $01,X
		beq orig
.endmacro

; ( C: orig1 -- orig2 )
; Put the location of a new unresolved forward reference orig2 onto the control flow stack. 
; Append the run-time semantics given below to the current definition. 
; The semantics will be incomplete until orig2 is resolved (e.g., by THEN). 
; Resolve the forward reference orig1 using the location following the appended run-time semantics. 

; ( -- )
; Continue execution at the location given by the resolution of orig2.
; In this implmentation it is the following block of code so a nop suffices
.macro ELSE
		nop
.endmacro

; ( C: orig -- )
; Append the run-time semantics given below to the current definition.
; Resolve the forward reference orig using the location of the appended run-time semantics. 
;
; ( -- )
; Continue execution.
; Provide an address for jmp
.macro THEN
		nop
.endmacro


; ( n1 | u1 n2 | u2 -- ) ( R: -- loop-sys )
; Set up loop control parameters with index n2 | u2 and limit n1 | u1.
; An ambiguous condition exists if n1 | u1 and n2 | u2 are not both the same type.
; Anything already on the return stack becomes unavailable until the loop-control 
; parameters are discarded. 
.macro DO
		jsr TWOTOR	; Place n1 | u1 n2 | u2 onto the return stack
		jsr GET_PC	; Places the address of this line in W2
		LOAD_RETURN ; Places the previous line on the return stack
.endmacro

; ( -- ) ( R: loop-sys1 -- | loop-sys2 ) 
; Add one to the loop index. If the loop index is then equal to the loop limit, 
; discard the loop parameters and continue execution immediately following the loop. 
; Otherwise continue execution at the beginning of the loop. 
.proc LOOP
		pla           	; Save the fall through address
		sta lowByteW  	; Store the lowbyte of the addr in the W register
		pla           	; Repeat for the high byte
		sta hiByteW
		jsr RFROM		; Load the tol addr to the data stack
		jsr TWORFROM	; Load the limit and counter
		jsr TWODUP		; Duplicate the limit and counter
		jsr EQUAL		; Check if equal
		bne @end		; Numbers are equal end loop
		jsr DROP		; Drop the flag
		jsr ONEADD		; Add one to the counter
		jsr TWOTOR		; push the limit and counter back onto the return stack
		lda hiByteW		; Load the highByte of the addr in the W register
		pla           	; Push the highByte of the start of the loop
		lda lowByteW  	; Load the lowbyte of the addr in the W register
		pla           	; Push the lowbyte of the start of the loop
		rts				; Return to the top of the loop
@end:	jsr TWODROP		; Set the return to address end of loop
		jmp TWODROP		; Drop address, limit, counter, and flag
.endproc

; ( -- ) ( R: loop-sys -- )
;
; Discard the current loop control parameters.
; An ambiguous condition exists if they are unavailable.
; Continue execution immediately following the innermost syntactically enclosing DO...LOOP or DO...+LOOP. 
.macro LEAVE addr
		ldy #5
@loop:	pla
		dey			; Load loop-sys and drop on the floor
		bne @loop	; Six bytes, three cells
		clc
		bcc addr	; Branch to after LOOP or +LOOP
.endmacro

; ( C: do-sys -- )
; Append the run-time semantics given below to the current definition.
; Resolve the destination of all unresolved occurrences of LEAVE between the location given 
; by do-sys and the next location for a transfer of control, to execute the words following +LOOP.
;
; ( n -- ) ( R: loop-sys1 -- | loop-sys2 )
; An ambiguous condition exists if the loop control parameters are unavailable.
; Add n to the loop index. If the loop index did not cross the boundary between the
; loop limit minus one and the loop limit, continue execution at the beginning of the loop.
; Otherwise, discard the current loop control parameters and continue execution immediately following the loop. 
.proc PLUSLOOP
		pla				; Save the fall through address
		sta lowByteW  	; Store the lowbyte of the addr in the W register
		pla				; Repeat for the high byte
		sta hiByteW
		jsr RFROM		; Load the tol addr to the data stack
		jsr TWORFROM	; Load the limit and counter
		jsr ONEADD		; Add one to the counter
		jsr TWODUP		; Duplicate the limit and counter
		jsr EQUAL		; Check if equal
		bne @end		; Numbers are equal end loop
		jsr DROP		; Drop the flag
		jsr TWOTOR		; push the limit and counter back onto the return stack
		lda hiByteW		; Load the highByte of the addr in the W register
		pla           	; Push the highByte of the start of the loop
		lda lowByteW  	; Load the lowbyte of the addr in the W register
		pla           	; Push the lowbyte of the start of the loop
		rts				; Return to the top of the loop			
@end:	jsr TWODROP		; Set the return to address end of loop
		jmp TWODROP		; Drop address, limit, counter, and flag
.endproc

; ( C: -- dest )
; Put the next location for a transfer of control, dest, onto the control flow stack. 
; Append the run-time semantics given below to the current definition.
;
; ( -- )
; Continue execution. 
.macro BEGIN
		nop
.endmacro

; ( C: dest -- orig dest )
; Put the location of a new unresolved forward reference orig onto the control flow stack, under the existing dest.
; Append the run-time semantics given below to the current definition.
; The semantics are incomplete until orig and dest are resolved (e.g., by REPEAT).
;
; ( x -- )
; If all bits of x are zero, continue execution at the location specified by the resolution of orig. 
.macro WHILE orig
		lda $00,X
		and $01,X
		beq orig
.endmacro

; ( C: orig dest -- )
; Append the run-time semantics given below to the current definition, resolving the backward reference dest. 
; Resolve the forward reference orig using the location following the appended run-time semantics.
;
; ( -- )
; Continue execution at the location given by dest. 
.macro REPEAT dest
		jsr dest	; Jumps to dest
.endmacro
