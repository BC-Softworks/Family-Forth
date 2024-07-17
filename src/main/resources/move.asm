;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

.include "famiCore.asm"

;; Working register locations
lowByteW   = $00
hiByteW    = $01
lowByteW2  = $02
hiByteW2   = $03

; ( a-addr -- x ) 
; Returns the full cell
; Fetch command '@'
.proc F_SYM_AT
  lda $00,X
  sta $00,X
  lda $01,X
  sta $01,X
  ldy #0
  lda ($01),Y
  sta $00,X
  ldy #1
  lda ($01),Y
  sta $01,X
  rts  
.endproc

; ( c-addr -- char )
; Fetch the character stored at c-addr. 
; When the cell size is greater than character size, 
; the unused high-order bits are all zeroes. 
; Fetch command 'C@'
.proc F_C_SYM_AT
  lda $00,X
  sta $00,X
  lda $01,X
  sta $01,X
  ldy #0
  lda ($01),Y
  sta $00,X
  sty $01,X
  rts  
.endproc

;( addr -- a-addr )
; a-addr is the first aligned address greater than or equal to addr. 
.proc F_ALIGNED
  lda $00,X
  and #%00000001
  beq add_one
  jsr ONEADD
add_one:  
  jsr ONEADD
  rts
.endproc
