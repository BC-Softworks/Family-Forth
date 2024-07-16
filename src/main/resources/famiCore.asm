;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

;; Primitives are defined as macros when possible for performance
;; TODO: Create JSON file mapping primatives to subroutines/macros

;; Working register locations
lowByteW   = $00
hiByteW    = $01
lowByteW2  = $02
hiByteW2   = $03

; Macros for 1:1 tokens
.macro CALL
  jsr
.endmacro

.macro RETURN
  rts
.endmacro

; Should always be called before 
; adding a value to the stack
.macro PUT
  dex
  dex
.endmacro

; ( x -- )
; Drop x from the stack
.macro DROP
  inx
  inx
.endmacro

; ( x -- xx )
; Copy x and place it on the stack
.proc DUP
  PUT
  lda $02,X
  sta $00,X
  lda $03,X
  sta $01,X
  rts
.endproc

; ( x1 x2 -- x2 x1 )
; Swaps the position of x1 and x2
.proc SWAP
  lda $00,X    ; Load the lowByte from the TOS
  sta lowbyte_W   
  lda $01,X    ; Load the hiByte from the TOS
  sta hibyte_W  
  
  lda $02,X    ; Load the lowByte
  sta $00,X    
  lda $03,X    ; Load the hiByte
  sta $01,X    
  
  lda lowbyte_W   ; Load the lowByte from W
  sta $02,X   
  lda hibyte_W  ; Load the hiByte from W
  sta $03,X  
  
  rts
.endproc

; ( x1 x2 -- x1 x2 x1 )
; Duplicate the second cell on  the stack
; place it on top
.proc OVER
  PUT
  lda $04,X
  sta $00,X ; Store low byte of second
  lda $05,X
  sta $01,X ; Store high byte of second
  rts
.endproc

; ( x1 x2 x3 -- x2 x3 x1 )
; Rotate the top three stack entries. 
.proc ROT
  lda $00,X
  
  rts
.endproc

;; Logical operations


; ( x -- flag )
; flag is true if and only if x is equal to zero. 
; Tokenized 0=
.proc ZEROEQU
  lda $00,X
  
  rts
.endproc

; ( n -- flag )
; flag is true if and only if n is less then zero. 
; Tokenized 0<
.proc ZEROLESS
  lda $01,X
  
  rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "and" of x1 with x2. 
.proc AND
  lda $00,X
  sta $00
  lda $01,X
  DROP
  and $01,X
  sta $01,X
  lda $00
  and $00,X
  sta $00,X
  rts
.endproc


; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "or" of x1 with x2. 
.proc OR
  lda $00,X
  sta lowByteW
  lda $01,X
  DROP
  ora $01,X
  sta $01,X
  lda lowByteW
  ora $00,X
  sta $00,X
  rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "xor" of x1 with x2. 
.proc XOR
  lda $00,X
  sta lowByteW
  lda $01,X
  DROP
  eor $01,X
  sta $01,X
  lda lowByteW
  eor $00,X
  sta $00,X
  rts
.endproc

;; Arithmetic operations

; ( n1 n2 -- n3 )
; n3 = n2 + n1
.proc ADD
  clc
  .repeat 2   ; Decrementing between additions
    lda $00,X ; allows a repeat to be used
    adc $02,X ; Doesn't work with SUB
    sta $02,X
    dex
  .endrep
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 - n1
.proc SUB
  sec
  lda $01,X
  sbc $03,X
  sta $03,X
  lda $00,X
  sbc $02,X
  sta $02,X
  DROP
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
    adc hiByteW
rotate:
    ror
.endrepeat
    tay
    lda lowByteW
    ror
mul8_return:
    rts
.endproc

.proc MUL16
  clc

  rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 * n1
.proc MUL
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
.proc DIV
  sec

  rts
.endproc

; TODO: Speed up with direct addressing
; ( n1 -- n2 )
; n2 = n1 ^ 2
.proc SQR
  jsr DUP
  jsr MUL
  rts
.endproc

; ( n1 -- n2 )
; n2 = n1 ^ 3
.proc CUB
  jsr DUP
  jsr SQR
  jsr MUL
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
  DROP
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
  DROP
  rts
.endproc
