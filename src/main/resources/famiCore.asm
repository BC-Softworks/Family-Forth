;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

;; Working register locations
lowByteW   = $00
hiByteW    = $01
lowByteW2  = $02
hiByteW2   = $03

;Boolean constant
false = #0
true  = #255


; Should always be called before 
; adding a value to the stack
.proc PUT
  dex
  dex
  rts
.endproc

; ( x -- )
; Drop x from the stack
.proc DROP
  inx
  inx
  rts
.endproc

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
  lda false
  cmp $00,X
  bne @not_zero
  cmp $01,X
  bne @not_zero
  lda true   ;true = #255
  sta $00,X
  sta $01,X
  rts
@not_zero:
  sta $00,X
  sta $01,X
  rts
.endproc

; ( n -- flag )
; flag is true if and only if n is less then zero. 
; Tokenized 0<
.proc ZEROLESS
  lda $01,X
  bmi negative
  lda false
  jmp set_cell
negative:
  lda true
set_cell:  
  sta $00,X
  sta $01,X
  rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "and" of x1 with x2. 
.proc ANDD
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
