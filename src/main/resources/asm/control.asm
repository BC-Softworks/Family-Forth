;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words
; DO LOOP +LOOP 
; BEGIN WHILE REPEAT
; IF ELSE THEN I J

.include "memory.asm"

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
.macro IF label
		lda $00,X
		and $01,X
		beq label
.endmacro

; ( C: orig1 -- orig2 )
; Put the location of a new unresolved forward reference orig2 onto the control flow stack. 
; Append the run-time semantics given below to the current definition. 
; The semantics will be incomplete until orig2 is resolved (e.g., by THEN). 
; Resolve the forward reference orig1 using the location following the appended run-time semantics. 

; ( -- )
; Continue execution at the location given by the resolution of orig2. 
.proc ELSE
		nop
.endproc

; ( -- )
; Continue execution.
; Provide an address for jmp
.proc THEN
		nop
.endproc


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
		lda highByteW  	; Load the highByte of the addr in the W register
		pla           	; Push the highByte of the start of the loop
		lda lowByteW  	; Load the lowbyte of the addr in the W register
		pla           	; Push the lowbyte of the start of the loop
		rts				; Return to the top of the loop
@end:					; Set the return to address end of loop
		jsr TWODROP		; Drop address, limit, counter, and flag
		jmp TWODROP
.endproc

; ( C: -- dest )
; Put the next location for a transfer of control, dest, onto the control flow stack. 
; Append the run-time semantics given below to the current definition.
;
; ( -- )
; Continue execution. 
.proc BEGIN

	rts
.endproc

; ( C: dest -- orig dest )
; Put the location of a new unresolved forward reference orig onto the control flow stack, under the existing dest. 
; Append the run-time semantics given below to the current definition. 
; The semantics are incomplete until orig and dest are resolved (e.g., by REPEAT).
;
; ( x -- )
; If all bits of x are zero, continue execution at the location specified by the resolution of orig. 
.proc WHILE

	rts
.endproc

; ( C: orig dest -- )
; Append the run-time semantics given below to the current definition, resolving the backward reference dest. 
; Resolve the forward reference orig using the location following the appended run-time semantics.
;
; ( -- )
; Continue execution at the location given by dest. 
.proc REPEAT
	
	rts
.endproc
