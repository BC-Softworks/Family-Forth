;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words core words
; @ C@ 2@ ALIGN ALIGNED 2! 2@ 
; CELLS CELL+ R> R@ ALLOT HERE , 

; We plan to implement the
; optional Memory-Allocation word set ar a later date
; ALLOCATE FREE RESIZE


.include "core.asm"

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

; ( -- )
; If the data-space pointer is not aligned, 
; reserve enough space to align it.
.proc ALIGN
		lda lowByteDSP
		and #%00000001
		cmp #%00000001
		beq @end
		jsr ONESUB
@end:	rts
.endproc

;( addr -- a-addr )
; a-addr is the first aligned address greater than or equal to addr. 
.proc ALIGNED
		lda $00,X
		and #%00000001
		beq @addOne
		clc
		lda $00,X
		adc #2
		sta $00,X
		lda $01,X
		adc #0
		sta $01,X
		rts
@addOne:  
		clc
		lda $00,X
		adc #1
		sta $00,X
		lda $01,X
		adc #0
		sta $01,X
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
; beginning at addr, as for a double number. '2@'
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

; ( n1 -- n2 )
; n2 is the size in address units of n1 cells. 
.proc CELLS
	jsr LSHIFT
	rts
.endproc

; ( a-addr1 -- a-addr2 )
; Add the size in address units of a cell to a-addr1, giving a-addr2. 
.proc CELL+
	clc
	lda $00,X
	adc #2
	sta $00,X
	lda $01,X ; Should handle the carry
	adc #0
	sta $01,X
	rts
.endproc

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

; ( n -- )
; If n is greater than zero, reserve n address units of data space. 
; If n is less than zero, release | n | address units of data space. 
; If n is zero, leave the data-space pointer unchanged.

; If the data-space pointer is aligned and n is a multiple of the size of a cell when ALLOT begins execution, 
; it will remain aligned when ALLOT finishes execution.

; If the data-space pointer is character aligned and n is a multiple of the size of a character when ALLOT begins execution, 
; it will remain character aligned when ALLOT finishes execution. 
.proc ALLOT
		lda $01,X		; Check if high byte is zeroed
		bne @add
		lda $00,X		; Check if low byte is zeroed
		bne @add
		jmp @drop		; Both bytes zero
	
@add:	clc				; Clear carry flag
		lda $00,X
		adc lowByteDSP
		sta lowByteDSP
		lda $01,X 
		adc hiByteDSP
		sta hiByteDSP
@drop:  jmp DROP
.endproc

; ( -- addr )
; addr is the data-space pointer. 
.proc HERE
		dex
		lda hiByteDSP
		sta $00,X
		dex
		lda lowByteDSP
		sta $00,X
		rts
.endproc

; ( x -- )
; Reserve one cell of data space and store x in the cell. 
; Tokenized ,
.proc COMMA
		dex					; Inline Put
		lda #0
		sta $00,X			; Push 2 onto the stack
		dex
		lda #2
		sta $00,X
		jsr ALLOT			; Allot one cell
		ldy #0
		lda $00,X   
		sta (lowByteDSP),Y	; Store the lowByte
		iny
		lda $01,X   
		sta (lowByteDSP),Y	; Store the highbyte
		jmp DROP			; Drop x from the parameter stack
.endproc




