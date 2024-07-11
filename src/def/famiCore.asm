;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

;; Primitives are defined as macros when possible for performance

;; Working register locations
hiByteW   = $00
lowByteW  = $01
hiByteW2  = $02
lowByteW2 = $03

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
  lda $0002,X
  sta $0000,X
  lda $0003,X
  sta $0001,X
  rts
.endproc

; ( x1 x2 -- x2 x1 )
; Swaps the position of x1 and x2
.proc SWAP
  lda $0000,X    ; Load the hiByte from the TOS
  sta hibyte_W   
  lda $0001,X    ; Load the lowByte from the TOS
  sta lowbyte_W  
  
  lda $0002,X    ; Load the hiByte
  sta $0000,X    
  lda $0003,X    ; Load the lowByte
  sta $0001,X    
  
  lda hibyte_W   ; Load the hiByte from W
  sta $0002,X   
  lda lowbyte_W  ; Load the lowByte from W
  sta $0003,X  
  
  rts
.endproc

; ( x1 x2 -- x1 x2 x1 )
; Duplicate the TOS and 
; place it onto the bottom
.proc OVER

  rts
.endproc

; ( x1 x2 x3 -- x2 x3 x1 )
; Places the top of the stack onto the bottom
.proc ROT

  rts
.endproc

; ( x1 x2 -- x2 )
; Remove the second cell on the stack
.proc NIP
  CALL SWAP
  DROP
  RETURN
.endproc

; ( x1 x2 x3 -- x2 x3 x1 )
; Places the top of the stack onto the bottom
.proc TUCK
  CALL SWAP
  CALL OVER
  RETURN
.endproc


;; Logical operations

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "and" of x1 with x2. 
.proc AND
  lda $0000,X
  sta $00
  lda $0001,X
  DROP
  and $0001,X
  sta $0001,X
  lda $00
  and $0000,X
  sta $0000,X
  rts
.endproc


; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "or" of x1 with x2. 
.proc OR
  lda $0000,X
  sta $00
  lda $0001,X
  DROP
  ora $0001,X
  sta $0001,X
  lda $00
  ora $0000,X
  sta $0000,X
  rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "xor" of x1 with x2. 
.proc XOR
  lda $0000,X
  sta $00
  lda $0001,X
  DROP
  eor $0001,X
  sta $0001,X
  lda $00
  eor $0000,X
  sta $0000,X
  rts
.endproc

;; Arithmetic operations

; ( n1 n2 -- n3 )
; n3 = n1 + n2
.proc ADD
  
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 = n1 - n2
.proc SUB
  
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 is lesser value
.proc MIN
  
  rts
.endproc

; ( n1 n2 -- n3 )
; n3 is greater value
.proc MAX
  
  rts
.endproc
