;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

;; Primitives are defined as macros when possible for performance

;; Working register locations
hiByteW   = $00
lowByteW  = $01
hiByteW2  = $02
lowByteW2 = $03

; Macros for 1:1 tokens
.macro CALL
  jsr
.endmacro

.macro RETURN
  rts
.endmacro

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
  lda $00,X    ; Load the hiByte from the TOS
  sta hibyte_W   
  lda $01,X    ; Load the lowByte from the TOS
  sta lowbyte_W  
  
  lda $02,X    ; Load the hiByte
  sta $00,X    
  lda $03,X    ; Load the lowByte
  sta $01,X    
  
  lda hibyte_W   ; Load the hiByte from W
  sta $02,X   
  lda lowbyte_W  ; Load the lowByte from W
  sta $03,X  
  
  rts
.endproc

; ( x1 x2 -- x1 x2 x1 )
; Duplicate the TOS and 
; place it onto the bottom
.proc OVER
  lda $00,X
  
  rts
.endproc

; ( x1 x2 x3 -- x2 x3 x1 )
; Places the top of the stack onto the bottom
.proc ROT
  lda $00,X
  
  rts
.endproc

; ( x1 x2 -- x2 )
; Remove the second cell on the stack
.proc NIP
  jsr SWAP
  DROP
  rts
.endproc

; ( x1 x2 x3 -- x2 x3 x1 )
; Places the top of the stack onto the bottom
.proc TUCK
  jsr SWAP
  jsr OVER
  rts
.endproc


;; Logical operations

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
  sta hiByteW
  lda $01,X
  DROP
  ora $01,X
  sta $01,X
  lda hiByteW
  ora $00,X
  sta $00,X
  rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "xor" of x1 with x2. 
.proc XOR
  lda $00,X
  sta hiByteW
  lda $01,X ; Keep the low byte in register
  DROP
  eor $01,X
  sta $01,X
  lda hiByteW
  eor $00,X
  sta $00,X
  rts
.endproc

;; Arithmetic operations

; ( n1 n2 -- n3 )
; n3 = n2 + n1
.proc ADD
  clc
  lda $01,X
  adc $03,X
  sta $03,X
  lda $00,X  
  adc $02,X
  sta $02,X
  DROP
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n2 - n1
.proc SUB
  sec
  .repeat 2   ; Decrementing between subtractions
    lda $00,X ; allows a repeat to be used
    sbc $02,X ; Doesn't work with ADD because of carry
    sta $02,X
    dex
  .endrep
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 is the lesser value
.proc MIN
  lda $00,X
  
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 is the greater value
.proc MAX
  lda $00,X
  
  rts
.endproc
