;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words core words
; @ C@ ! C! 2! 2@ ALIGN ALIGNED 
; CELLS CELL+ R> 2R> >R 2>R R@ 
; ALLOT HERE , C, MOVE

; Include guard
.ifndef MEMORY_GUARD
	MEMORY_GUARD = 1
.endif

.ifndef MATH_GUARD
	.include "math.asm"
.endif

.ifndef CORE_GUARD
	.include "core.asm"
.endif


.segment "CODE"

; ( a-addr -- x ) 
; Returns the full cell
; Fetch command '@'
.proc SYM_AT
		jsr SAVETOS
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
		jsr SAVETOS
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
		jsr DUP
		jsr CELLPLUS
		jsr SYM_AT
		jsr SWAP
		jmp SYM_AT
.endproc

; ( -- )
; If the data-space pointer is not aligned, 
; reserve enough space to align it.
.proc ALIGN
		lda lowByteDSP
		and #%00000001
		cmp #%00000001
		bne @sub
		rts
@sub:	jmp ONESUB
.endproc

;( addr -- a-addr )
; a-addr is the first aligned address greater than or equal to addr. 
.proc ALIGNED
		lda $00,X
		and #%00000001
		beq @add
		jsr ONEADD
@add:	jmp ONEADD
.endproc

; ( x a-addr -- )
; Store the cell x1 at a-addr,
; Tokenized "!"
.proc STORE
		lda $00,X
		sta lowByteW	; Store the low byte of the address
		lda $01,X
		sta hiByteW		; Store the high byte of the address
		ldy #0			; Set offset to zero
		lda $02,X
		sta ($00),Y		; Store the lowbyte at a-addr
		iny
		lda $03,X		; Store the lowbyte at a-addr + 1
		sta ($00),Y
		jmp TWODROP		; Drop the address and x from the stack
.endproc

; ( char c-addr -- )
; Store char at c-addr. 
; When character size is smaller than cell size, 
; only the number of low-order bits corresponding to character size are transferred. 
.proc C_STORE
	rts
.endproc

; ( x1 x2 a-addr -- )
; Store the cell pair x1 x2 at a-addr, with x2 at a-addr and x1 at the
; next consecutive cell. "2!"
.proc TWOSTORE
		jsr DUP
		jsr ROT
		jsr SWAP
		jsr STORE
		jsr CELLPLUS
		jmp STORE
.endproc

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
no_add: rts
.endproc

; ( n1 -- n2 )
; n2 is the size in address units of n1 cells. 
.proc CELLS
		jmp LSHIFT
.endproc

; ( a-addr1 -- a-addr2 )
; Add the size in address units of a cell to a-addr1, giving a-addr2.
; Tokenized CELL+
.proc CELLPLUS
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
; IMPORTANT: Doesn't use W
.proc RFROM
		SAVE_RETURN
		PUT
		pla
		sta $01,X     ; Pull high byte
		pla
		sta $00,X	  ; Pull low byte
		LOAD_RETURN
		rts
.endproc

; ( -- x1 x2 ) ( R: x1 x2 -- )
; Transfer cell pair x1 x2 from the return stack.
.proc TWORFROM
		jsr RFROM
		jsr RFROM
		jmp SWAP
.endproc

; ( x -- ) ( R: -- x )
; Move x to the return stack.
; Tokenized >R
; IMPORTANT: Doesn't use W
.proc TOR
		SAVE_RETURN
		sta $01,X     ; Push higher byte
		pha
		sta $00,X	  ; Push lower byte
		pha
		LOAD_RETURN
		jmp DROP
.endproc

; ( x1 x2 -- ) ( R: -- x1 x2 )
; Transfer cell pair x1 x2 to the return stack. 
; Semantically equivalent to SWAP >R >R. 
; Tokenized 2>R
.proc TWOTOR
		jsr SWAP
		jsr TOR
		jmp TOR
.endproc

; ( -- n)
; Copy the number on top of the return stack to the data stack.
; Tokenized R@
.proc RFETCH
		SAVE_RETURN
		PUT
		pla
		sta $00,X     ; Push lower byte
		sta lowByteW  ; Store lower byte
		pla
		sta $01,X	  ; Push higher byte
		pha
		lda lowByteW  ; Load lowbyte to push back onto the stack
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

; ( char -- )
; Reserve space for one character in the data space and store char in the space.
; If the data-space pointer is character aligned when C, begins execution, it will remain character aligned when C, finishes execution.
; An ambiguous condition exists if the data-space pointer is not character-aligned prior to execution of C,. 
.proc C_COMMA
	jsr HERE
	jsr C_STORE
	PUSH #1
	jmp ALLOT
.endproc

; ( addr1 addr2 u -- )
; If u is greater than zero, copy the contents of u 
; consecutive address units at addr1 to the u consecutive address units at addr2.
; After MOVE completes, the u consecutive address units 
; at addr2 contain exactly what the u consecutive address units at addr1 contained before the move. 
.proc MOVE
		lda $00,X
		and $01,X		; Compare low and high bytes
		beq @end		; Skip if u = 0
@loop:	ldy $04,X		; Load addr1 into Y
		lda ($00),Y
		ldy $02,X		; Load addr2 into Y
		sta ($00),Y
		jsr ONESUB		; Subtract one from u
		inx				; Add one to addr1 and addr2
		inx
		jsr ONEADD		
		inx
		inx
		jsr ONEADD		; Add one to addr1 and addr2
		txa				; Reset stack pointer
		sec
		sbc #4
		tax
		lda $01,X		; Check if u = 0
		bne @loop
		lda $00,X
		bne @loop
@end:	jsr TWODROP		; Drop addr1, addr2, and u
		jmp DROP
.endproc
