;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words
; ADD SUB 1+ 1- ABS * / MOD/ 
; MIN MAX NEGATE INVERT

.include "core.asm"

; ( n1 n2 -- n3 )
; n3 = n2 + n1
.proc ADDD
		clc
		.repeat 2   ; Decrementing between additions
			lda $00,X ; allows a repeat to be used
			adc $02,X ; Doesn't work with SUB
			sta $02,X
			inx       ; inx in place to eliminate drop
		.endrep
		rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 - n1
.proc SUBB
		lda $01,X
		sec
		sbc $03,X
		sta $03,X  ; Store high byte
		lda $00,X
		sbc $02,X
		sta $02,X  ; Store low byte
		jmp DROP
.endproc

; ( n1 | u1 -- n2 | u2 )
; Add one (1) to n1 | u1 giving the sum n2 | u2.
; Tokenized 1+
.proc ONEADD
		clc
		lda $00,X
		adc #1
		sta $00,X
		lda $01,X ; Should handle the carry
		adc #0
		sta $01,X
		rts
.endproc

; ( n1 | u1 -- n2 | u2 )
; Sub one (1) to n1 | u1 giving the differene n2 | u2.
; Tokenized 1-
.proc ONESUB
		sec
		lda $00,X
		sbc #1
		sta $00,X
		lda $01,X ; Should handle the carry
		sbc #0
		sta $01,X
		rts
.endproc


; ( n -- u )
; u is the absolute value of n. 
.proc ABS
		lda $01,X
		and #%01111111
		sta $01,X
		rts
.endproc


; Modified Mul8 optimized by Tepples
; Runs in 120 cycles or less
.proc MUL8
		lda $00,X
		ldy $02,X
		lsr
		sta lowByteW
		tya
		beq @ret
		dey
		sty hiByteW
		lda #0
		.repeat 8, i
			.if i > 0
				ror lowByteW
			.endif
			bcc @rot
			adc hiByteW
@rot:	ror
		.endrepeat
		tay
		lda lowByteW
		ror
@ret: 	rts
.endproc

; ( n1 n2 -- d )
; n3 = n2 * n1
; Tokenized M*
; Modified version that uses the Y register from 6502.org
.proc M_STAR
		lda $00,X			; Load the first cell into W
		sta $00,X
		lda $01,X
		sta $01,X

		lda $02,X			; Load the first cell into W2
		sta $02,X
		lda $03,X
		sta $03,X

		multiplier = lowByteW
		multiplicand = lowByteW2
		product = $00,X		; Store directly onto the stack
							; Overwrites both factors
mult16: lda #$00
		sta product+2	 	; clear upper bits of product
		sta product+3 
		ldy #$10		   	; set binary count to 16 
shift_r:
		lsr multiplier+1 	; divide multiplier by 2 
		ror multiplier
		bcc rot_r 
		lda product+2	 	; get upper half of product and add multiplicand
		clc
		adc multiplicand
		sta product+2
		lda product+3 
		adc multiplicand+1
rot_r:	ror			 		; rotate partial product 
		sta product+3 
		ror product+2
		ror product+1 
		ror product 
		dey
		bne shift_r
		rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 * n1
.proc MULT
		lda $01,X
		ora $03,X ; If the highbyte's or is zero 
		beq @mul8 ; then use 8x8 multiplication
		jsr M_STAR
		jsr SWAP
		jmp DROP
@mul8:  jmp MUL8  ; Spliting the mul functions 
.endproc


//   R(0) := N(i)          -- Set the least-significant bit of R equal to bit i of the numerator
//   if R ≥ D then
//     R := R − D
//     Q(i) := 1
//   end

; ( n1 n2 -- n4 n3 )
; n3rn4 = n2 / n1
; Tokenized MOD/
.proc MODDIV
		lda $00,X		; Move the top of stack into W and W2
		sta lowByteW
		lda $01,X
		sta hiByteW
		lda $01,X
		sta lowByteW2
		lda $02,X
		sta hiByteW2
		lda #0			; Clear the top of the stack
		sta $00,X
		sta $01,X
		sta $02,X
		sta $03,X
		ldy #15			; Set to cell size minus 1
@loop:	jsr LSHIFT		; Left-shift remainder by 1 bit
		
						; Set the least-significant bit of R 
						; equal to bit i of the numerator

						; If the reamainder is greater
						; then the denominator
						; Subtract the reamainder by the denominator
						; set Quotinet bit i to one
		dey
		bpl @loop
		rts
.endproc


; ( n1 n2 -- n3 )
; n3 = n2 / n1
.proc DIV
		jsr MODDIV
		jsr SWAP	; Swap Q and R
		jmp DROP	; Drop the remainder
.endproc

; ( n1 n2 -- n3 )
; Divide n1 by n2, giving the single-cell remainder n3.
.proc MOD
		jsr MODDIV
		jmp DROP	; Drop the quotient
.endproc


; ( n1 n2 -- n3 )
; n3 is the lesser value
.proc MIN
		lda $01,X ; Compare high bytes first
		cmp $03,X
		bne @end  ; If unequal skip checking low byte
		lda $00,X ; Compare low bytes
		cmp $02,X ; If high ytes equal
		bpl @end  ; If n1 is less then n2 call drop
		jsr NIP   ; Else call nip
		rts
@end: 	inx       ; Inline Drop
		inx
		rts
.endproc

; ( n1 n2 -- n3 )
; n3 is the greater value
.proc MAX
		lda $01,X ; Compare high bytes first
		cmp $03,X
		bne @end  ; If unequal skip checking low byte
		lda $00,X ; Compare low bytes
		cmp $02,X ; If high bytes are equal
		bmi @end  ; If n1 is greater then n2 call drop
		jsr NIP   ; Else call nip
		rts
@end: 	inx       ; Inline Drop
		inx
		rts
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
		jsr ONEADD
		rts
.endproc
