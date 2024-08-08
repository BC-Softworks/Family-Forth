;=======================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)				;
;=======================================================;

;; Core Word Extensions

; Defines the following words core extension words
; NIP TUCK TRUE FALSE PICK <> 0<>

.ifndef CORE_GUARD
	.include "core.asm"
.endif

.segment "CODE"

; ( x1 x2 -- x2 )
; Remove the second cell on the stack
.proc NIP
		lda $00,X    ; Load the lowByte from the TOS
		sta $02,X    ; Overwrite lowByte of second cell
		lda $01,X    ; Load the highByte from the TOS
		sta $03,X    ; Overwrite highByte of second cell
		jmp DROP
.endproc


; ( x1 x2-- x2 x1 x2 )
; Copy the first (top) stack item below the second stack item. 
.proc TUCK
		jsr SWAP
		jmp OVER
.endproc

; ( n -- flag )
; flag is true if and only if n is greater then zero.
; Tokenized 0>
.proc ZEROGREATER
		lda $01,X
		bpl @neg
		lda false
		jmp @set
@neg: 	lda true
@set: 	sta $00,X
		sta $01,X
		rts
.endproc

; ( -- true )
; Return a true flag, a single-cell value with all bits set. 
.proc TRUE
		PUSHCELL true, true
		rts
.endproc

; ( -- false )
; Return a false flag, a single-cell value with no bits set.
; Same as pushing 0 onto the stack
.proc FALSE
		PUSH false
		rts
.endproc

; ( xu...x1 x0 u -- xu...x1 x0 xu )
; Remove u. Copy the xu to the top of the stack. 
; An ambiguous condition exists if there are less 
; than u+2 items on the stack before PICK is executed.
; Since the stack is 60 cells max u is only using the lower byte
.proc PICK
		clc
		lda $00,X
		tay
		adc $00,X   ; Convert cells to bytes
		stx $00		; Store TOS address
		adc $00    	; Add from top of stack
		ldy #0    	; Clear Y
		sta $00   	; Save addr at $00    
		lda ($00),Y ; Load xu's low byte indirectly
		sta $00,X
		iny			; Increment y to get high byte
		lda ($00),Y ; Load xu's high byte indirectly
		sta $01,X
		rts        	; Don't need to drop anything
.endproc

; ( x1 x2 -- flag )
; flag is true if and only if x1 is not bit-for-bit the same as x2.
; Tokenized <>
.proc NOTEQUALs
		lda $00,X
		cmp $02,X
		bne @false
		lda $01,X
		cmp $03,X
		bne @false
		jsr TWODROP ; Drop x1 and x2
		jmp TRUE	; x1 == x2
@false:	jsr TWODROP
		jmp FALSE
.endproc

; ( x -- flag )
; flag is true if and only if x is not equal to zero. 
; Tokenized 0<>
.proc ZERONOTEQUALS
		lda false
		cmp $00,X
		bne @not_z
		cmp $01,X
		bne @not_z
		lda false
		jmp @store
@not_z:	lda true
@store: sta $00,X
		sta $01,X
		rts
.endproc
