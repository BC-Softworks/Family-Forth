;=======================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)				;
;=======================================================;

; Defines the following words
; [ ] BASE

; Include guard
.ifndef SHELL_GUARD
	SHELL_GUARD = 1
.endif

.ifndef CORE_GUARD
	.include "core.asm"
.endif

.segment "CODE"

; ( -- )
; Enter interpretation state. [ is an immediate word.
; Tokenized [
.proc LBRACK
    lda #0
    sta mode
    rts
.endproc

; ( -- )
; Enter compilation state. [ is an immediate word. 
; Tokenized ]
.proc RBRACK
    lda #255
    sta mode
    rts
.endproc


; ( -- a-addr )
; a-addr is the address of a cell containing the current number-conversion radix {{2...36}}. 
.proc BASE
	lda radix
	sta $00,x
	lda #0
	sta $01,X
	rts
.endproc

; ( -- )
; Set the numeric conversion radix to ten (decimal).
.macro DECIMAL
	lda #10
	sta radix
	rts
.endmacro