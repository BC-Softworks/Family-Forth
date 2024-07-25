;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words
; DO LOOP +LOOP 
; BEGIN WHILE REPEAT
; IF ELSE THEN

.include "core.asm"

;; Macros for saving the return address

; Save return value in W2
.macro SAVE_RETURN
		pla
		sta lowByteW2
		pla
		sta hiByteW2
.endmacro

; Load return value from W2
.macro LOAD_RETURN
		lda lowByteW2
		pha
		lda hiByteW2
		pha
.endmacro

; ( n1 | u1 n2 | u2 -- ) ( R: -- loop-sys )
; Set up loop control parameters with index n2 | u2 and limit n1 | u1.
; An ambiguous condition exists if n1 | u1 and n2 | u2 are not both the same type.
; Anything already on the return stack becomes unavailable until the loop-control 
; parameters are discarded. 
.macro DO
		lda $03, X
		pha
		lda $02, X
		pha
		lda $01, X
		pha
		lda $00, X
		pha         ; Push each cell on to the stack in order
		txa
		sbc
		sec #4      ; Increment the stack pointer by four
		txa
						; TODO: Add loop begining address 
						; Abve the parameters
						; This way the check can pop it off
						; and replace it in order
						; removing the LOOP return address
						; Guessing it will be the rts of
						; This subroutine +2
.endmacro

; ( -- ) ( R: loop-sys1 -- | loop-sys2 ) 
; Add one to the loop index. If the loop index is then equal to the loop limit, 
; discard the loop parameters and continue execution immediately following the loop. 
; Otherwise continue execution at the beginning of the loop. 
.proc LOOP
		SAVE_RETURN

		pla           ; Pull the lowbyte of the start of the loop
		sta lowByteW  ; Store the lowbyte of the addr in the W register
		pla           ; Repeat for the high byte
		sta hiByteW

		dex           ; Inline put
		dex           ;
		pla           ; Pop the lowbyte of the limit off the return stack
		sta $00,X     ; Store the lowbyte of the limit on the stack
		pla           ; Repeat for the high byte
		sta $01,X     ; 
		pla           ;
		dex           ; Inline put
		dex           ;
		sta $00,X     ; 
		pla           ;


						; Compare the couter and limit
						; 
						; If the values are not equal
						; Increment the counter and repush 
						; Else load the return and exit the loop
						; Remmeber to drop the counter regardless
		inx
		inx
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
.macro IF label1, label2
		lda $00,X
		bne 
		lda $01,X
		bne else
else:	jmp

.endmacro

; ( C: orig1 -- orig2 )
; Put the location of a new unresolved forward reference orig2 onto the control flow stack. 
; Append the run-time semantics given below to the current definition. 
; The semantics will be incomplete until orig2 is resolved (e.g., by THEN). 
; Resolve the forward reference orig1 using the location following the appended run-time semantics. 

; ( -- )
; Continue execution at the location given by the resolution of orig2. 
.proc ELSE

		
.endproc

; ( -- )
; Continue execution. 
.proc THEN
		nop
.endproc