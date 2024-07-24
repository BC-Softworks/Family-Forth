;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words
; DO LOOP +LOOP 
; BEGIN WHILE REPEAT
; IF ELSE THEN

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
.proc DO
        SAVE_RETURN ; Store the return address

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

        LOAD_RETURN ; Restore the return address

        rts
.endproc

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