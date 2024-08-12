;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words
; ADD SUB 1+ 1- ABS * / */ MOD/ 
; MIN MAX NEGATE INVERT CHAR+
; UM* UM/MOD

.ifndef MATH_GUARD
	MATH_GUARD = 1
.endif

.ifndef CORE_GUARD
	.include "core.asm"
.endif

.segment "CODE"

; ( n1 n2 -- n3 )
; n3 = n2 + n1
.proc ADD
		clc
		.repeat 2	  ; Decrementing between additions
			lda $00,X ; allows a repeat to be used
			adc $02,X ; Doesn't work with SUB
			sta $02,X
			inx       ; inx in place to eliminate drop
		.endrep
		rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 - n1
.proc SUB
		sec			; Set carry bit
		lda $02,X
		sbc $00,X
		sta $02,X	; Store high byte
		lda $03,X
		sbc $01,X
		sta $03,X	; Store low byte
		jmp DROP
.endproc

; ( n1 | u1 -- n2 | u2 )
; Add one (1) to n1 | u1 giving the sum n2 | u2.
; Tokenized 1+
.proc ONEADD
		PUSH #1
		jmp ADD
.endproc

; ( n1 | u1 -- n2 | u2 )
; Sub one (1) to n1 | u1 giving the differene n2 | u2.
; Tokenized 1-
.proc ONESUB
		PUSH #1
		jsr SWAP ; TODO: FIX
		jmp SUB
.endproc


; ( n -- u )
; u is the absolute value of n. 
.proc ABS
		lda $01,X
		and #%01111111
		sta $01,X
		rts
.endproc

; ( n1 n2 -- d )
; n3 = n2 * n1
; Tokenized M*
; Modified version that uses the Y register from 6502.org
.proc M_STAR
		lda $00,X		; Load the first cell into W
		sta lowByteW
		lda $01,X
		sta hiByteW

		lda $02,X		; Load the first cell into W2
		sta lowByteW2
		lda $03,X
		sta hiByteW2

		n1 = lowByteW
		n2 = lowByteW2
mult16: lda #$00
		sta $02,X	 	; clear upper bits of product
		sta $03,X 
		ldy #$10		; set binary count to 16 
		
shift:	lsr n1+1 		; divide n1 by 2 
		ror n1
		bcc rot_r 
		lda $02,X	 	; get upper half of pproduct and add n2
		clc
		adc n2
		sta $02,X
		lda $03,X
		adc n2+1

rot_r:	ror			 	; rotate partial product
		sta $03,X
		ror $02,X
		ror $01,X 
		ror $00,X
		dey
		bne shift
		rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 * n1
.proc MULT
		jsr M_STAR
		jsr SWAP
		jmp DROP
.endproc

; ( n1 n2 -- n3 n4 )
; n3rn4 = n2 / n1
; Tokenized /MOD
.proc DIVMOD
		ldy #0			; Clear W register
		sty lowByteW	; Used to store quotient
		sty hiByteW
@loop:	jsr TWODUP		; If R < D fall through
		jsr LESS		; Check if lesser	
		lda $00,X		; Load low byte of flag
		bne @end		; Break if true
		jsr DROP		; Drop flag
		jsr SUB			; R = R - D
		PUT 			; Restore stack pointer

		clc				
		lda lowByteW	; Increment W
		adc #1
		sta lowByteW
		lda hiByteW
		adc #0
		sta hiByteW		; Check if high byte is greater then zero
		bpl @loop		; will be for small value
		lda lowByteW	; but necessary if over 255
		bne @loop

@end:	jsr DROP		; Drop flag
		lda lowByteW	; Set TOS to contents of W
		sta $00,X
		lda hiByteW
		sta $01,X
		rts
.endproc


; ( n1 n2 -- n3 )
; n3 = n2 / n1
.proc DIV
		jsr DIVMOD
		jsr SWAP	; Swap Q and R
		jmp DROP	; Drop the remainder
.endproc

; ( n1 n2 -- n3 )
; Divide n1 by n2, giving the single-cell remainder n3.
.proc MOD
		jsr DIVMOD
		jmp DROP	; Drop the quotient
.endproc

; ( n1 n2 n3 -- n4 )
; Multiply n1 by n2 producing the intermediate double-cell result d.
; Divide d by n3 giving the single-cell quotient n4. 
; Tokenized */
.proc MULDIV
		jsr M_STAR
		jmp DIV
.endproc

; ( n1 n2 n3 -- n4 n5 )
; Multiply n1 by n2 producing the intermediate double-cell result d. 
; Divide d by n3 producing the single-cell remainder n4 and the single-cell quotient n5.
; Tokenized */MOD
.proc MULDIVMOD
		jsr M_STAR
		jmp DIVMOD
.endproc

; ( n1 n2 -- n3 )
; n3 is the lesser value
.proc MIN
		lda $01,X ; Compare high bytes first
		cmp $03,X
		bne @end  ; If unequal skip checking low byte
		lda $00,X ; Compare low bytes
		cmp $02,X ; If high ytes equal
@end:	bpl	@drop ; If n1 is less then n2 call drop
		jsr SWAP  ; Else call swap then drop
@drop: 	jmp DROP
.endproc

; ( n1 n2 -- n3 )
; n3 is the greater value
.proc MAX
		lda $01,X ; Compare high bytes first
		cmp $03,X
		bne @end  ; If unequal skip checking low byte
		lda $00,X ; Compare low bytes
		cmp $02,X ; If high bytes are equal
@end:	bmi @drop  ; If n1 is greater then n2 call drop
		jsr SWAP
@drop: 	jmp DROP
.endproc

; ( x1 -- x2 )
; Invert all bits of x1, giving its logical inverse x2. 
.proc INVERT
		lda $00,X
		eor #%11111111
		sta $00,X
		lda $01,X
		eor #%11111111
		sta $01,X
		rts
.endproc

; ( n1 -- n2 )
; Negate n1, giving its arithmetic inverse n2. 
.proc NEGATE
		jsr INVERT
		jmp ONEADD
.endproc

; ( c-addr1 -- c-addr2 )
; Add the size in address units of a character to c-addr1, giving c-addr2.
; Tokenized 'CHAR+'
.proc CHAR_PLUS
		jmp ONEADD
.endproc

; ( u1 u2 -- ud )
; Multiply u1 by u2, giving the unsigned double-cell p ud.
; All values and arithmetic are unsigned. 
.proc UNSIGNEDMULT
		
.endproc

; ( ud u1 -- u2 u3 )
; Divide ud by u1, giving the quotient u3 and the remainder u2. 
; All values and arithmetic are unsigned. 
; An ambiguous condition exists if u1 is zero or if the quotient lies outside the range of a single-cell unsigned integer.
; Tokenized UM/MOD
.proc UNSIGNEDDIVMOD

.endproc

