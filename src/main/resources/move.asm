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
.proc SYM_AT
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
.proc C_SYM_AT
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

; ( -- )
; If the data-space pointer is not aligned,
; reserve enough space to align it.
.proc ALIGN
	txa
	and #%00000001
	beq end 
	dex
end:	
	rts
.endproc

;( addr -- a-addr )
; a-addr is the first aligned address greater than or equal to addr. 
.proc ALIGNED
  lda $00,X
  and #%00000001
  beq add_one
  jsr ONEADD
add_one:  
  jsr ONEADD
  rts
.endproc

; ( x1 x2 a-addr -- )
; Store the cell pair x1 x2 at a-addr, with x2 at a-addr and x1 at the
; next consecutive cell. "2!"
.proc TWOSTORE
  ldy #0
  lda $02,X
  lda $03,X
  sta ($00),Y
  sta ($01),Y
  ldy #2
  lda $04,X
  lda $05,X
  sta ($00),Y
  sta ($01),Y
  rts
.endproc






