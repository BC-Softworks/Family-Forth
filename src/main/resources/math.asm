;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

.include "famiCore.asm"

;; Working register locations
lowByteW   = $00
hiByteW    = $01
lowByteW2  = $02
hiByteW2   = $03


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
  inx        ; Inline Drop
  inx
  rts
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
.proc _ABS
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
    beq mul8_return
    dey
    sty hiByteW
    lda #0
.repeat 8, i
    .if i > 0
        ror lowByteW
    .endif
    bcc rotate
    adc hiByteWF_
rotate:
    ror
.endrepeat
    tay
    lda lowByteW
    ror
mul8_return:
    rts
.endproc

; 16-bit multiply with 32-bit product
; High bytes are discarded
; Modified version that uses the Y register
; from 6502.org
.proc MUL16
lda $00,X
sta $00,X
lda $01,X
sta $01,X

lda $02,X
sta $02,X
lda $03,X
sta $03,X

multiplier = lowByteW
multiplicand = lowByteW2
product = $00,X
 
mult16: 
  lda #$00
  sta product+2	 ; clear upper bits of product
  sta product+3 
  ldy #$10		   ; set binary count to 16 
shift_r:
  lsr multiplier+1 ; divide multiplier by 2 
  ror multiplier
  bcc rotate_r 
  lda product+2	 ; get upper half of product and add multiplicand
  clc
  adc multiplicand
  sta product+2
  lda product+3 
  adc multiplicand+1
rotate_r:
  ror			 ; rotate partial product 
  sta product+3 
  ror product+2
  ror product+1 
  ror product 
  dey
  bne shift_r
  jsr SWAP
  inx
  inx
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 * n1
.proc MULT
  lda $01,X
  ora $03,X ; If the highbyte's or is zero 
  beq @mul8 ; then use 8x8 multiplication
  jsr MUL16
  rts
@mul8:  
  jsr MUL8  ; Spliting the mul functions 
  rts       ; Saves cycles on low numbers
.endproc


; ( n1 n2 -- n3 )
; n3 = n2 / n1
.proc DIVI
  sec

  rts
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
@end:
  inx       ; Inline Drop
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
@end:
  inx       ; Inline Drop
  inx
  rts
.endproc
