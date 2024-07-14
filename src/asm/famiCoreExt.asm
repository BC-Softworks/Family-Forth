;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

.include "famiCoreExt.asm"

;; Core Word Extensions
;; Primitives are defined as macros when possible for performance

; ( x1 x2 -- x2 )
; Remove the second cell on the stack
.proc NIP
  lda $00,X    ; Load the lowByte from the TOS
  sta $02,X    ; Overwrite lowByte of second cell
  lda $01,X    ; Load the highByte from the TOS
  sta $03,X    ; Overwrite highByte of second cell
  DROP
  rts
.endproc


; ( x1 x2-- x2 x1 x2 )
; Copy the first (top) stack item below the second stack item. 
.proc TUCK
  jsr SWAP
  jsr OVER
  rts
.endproc
