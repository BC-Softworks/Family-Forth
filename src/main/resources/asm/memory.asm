;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words core words
; @ C@ 2@ ALIGNED 2! 2@ R> R@

.include "math.asm"

; ( a-addr -- x ) 
; Returns the full cell
; Fetch command '@'
.proc SYM_AT
		lda $00,X
		sta lowByteW
		lda $01,X
		sta hiByteW
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
		sta lowByteW
		lda $01,X
		sta hiByteW
		ldy #0
		lda ($01),Y
		sta $00,X
		sty $01,X
		rts  
.endproc

; ( a-addr -- x1 x2 ) 
; Returns two full cells
; Fetch command '2@'
.proc TWO_SYM_AT
  jsr SYM_AT    ; Leaves addr in W
  inx			; Inline PUT
  inx
  lda #2	    ; Add 2 to the address
  clc
  adc lowByteW
  sta $00,X     ; Then call @ again
  lda #0
  adc hiByteW	; Add zero and use carry
  sta $01,X     ; 
  jsr SYM_AT
  jsr SWAP		; Then swap
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

; TODO: TEST
; ( addr -- d)
; Leave on the stack the contents of the four consecutive  bytes
; beginning at addr, as for a double number.
.proc TWOFETCH
		ldy $00,X     ; Load the low byte of the address into Y
		lda $01,X     ; Load the high byte of the address into A
		sta hiByteW2  ; Store the high byte in W2
		
loop:	lda ($00),Y   ; Load and store the low byte from the first byte
		sta $00,X     ; pointed to by the address
		clc
		tya		      ; Increment the address and repeat 3x
		adc #1
		tay
		bne loop
no_add: 
		rts
.endproc

; Save return value in W2
.macro SAVE_RETURN
		pla
		sta lowByteW2
		pla
		sta hiByteW2
.endmacro

; Load return value from W2
.macro LOAD_RETURN
		lda lowByteW2
		pha
		lda hiByteW2
		pha
.endmacro


; ( -- n)
; Transfer n from the return stack to the data stack.
; Tokenized R>
.proc RFROM
		SAVE_RETURN
		dex           ; Inline Put
		dex
		pla
		sta $00,X     ; Push lower byte
		pla
		sta $01,X	  ; Push higher byte
		LOAD_RETURN
		rts
.endproc

; ( -- n)
; Copy the number on top of the return stack to the data stack.
; Tokenized R@
.proc RFETCH
		SAVE_RETURN
		dex           ; Inline Put
		dex
		pla
		sta $00,X     ; Push lower byte
		sta $00		  ; Store lower byte
		pla
		sta $01,X	  ; Push higher byte
		pha
		lda $00		  ; Load lowbyte to push back onto the stack
		pha
		LOAD_RETURN
		rts
.endproc



