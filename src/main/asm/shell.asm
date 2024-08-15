;=======================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)				;
;=======================================================;

; Defines the following words
; [ ]

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