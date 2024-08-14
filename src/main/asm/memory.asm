;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words core words
; @ C@ ! C! ALIGN ALIGNED 
; CELL+ CELLS R> >R R@ 
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
.proc FETCH
		jsr SAVETOS
		ldy #0
		lda ($00),Y
		sta $00,X
		ldy #1
		lda ($00),Y
		sta $01,X
		rts  
.endproc

; ( c-addr -- char )
; Fetch the character stored at c-addr. 
; When the cell size is greater than character size, 
; the unused high-order bits are all zeroes. 
; Fetch command 'C@'
.proc CFETCH
		jsr SAVETOS
		ldy #0
		lda ($00),Y
		sta $00,X
		sty $01,X
		rts  
.endproc

; ( a-addr -- x1 x2 ) 
; Returns two full cells
; Fetch command '2@'
.proc TWOFETCH
		jsr DUP		; ( a-addr -- a-addr a-addr ) 
		jsr FETCH	; ( a-addr a-addr -- a-addr x1 ) 
		jsr SWAP	; ( a-addr x1 -- x1 a-addr )
		PUSH #2		; ( x1 a-addr -- x1 a-addr 2 )
		jsr ADD		; ( x1 a-addr 2 -- x1 b-addr )
		jmp FETCH	; ( x1 b-addr -- x1 x2 ) 
.endproc

; ( -- )
; If the data-space pointer is not aligned, reserve enough space to align it.
.proc ALIGN
		lda lowByteDSP
		and #$01
		beq @end
		PUSH #1
		jmp ALLOT
@end:	rts
.endproc

;( addr -- a-addr )
; a-addr is the first aligned address greater than or equal to addr. 
.proc ALIGNED
		lda $00,X
		and #1
		beq @push
		PUSH #1
		bne @add	; Always branches
@push:	PUSH #2
@add:	jmp ADD
.endproc

; ( x a-addr -- )
; Store the cell x1 at a-addr,
; Tokenized "!"
.proc STORE
		jsr SAVETOS
		jsr DROP		; Drop the address from the stack
		ldy #0			; Set offset to zero
		lda $00,X
		sta ($00),Y		; Store the lowbyte at a-addr
		ldy #1
		lda $01,X		; Store the highbyte at a-addr + 1
		sta ($00),Y
		jmp DROP		; Drop x from the stack
.endproc

; ( char c-addr -- )
; Store char at c-addr. 
; When character size is smaller than cell size, 
; only the number of low-order bits corresponding to character size are transferred. 
; Tokenized "C!"
.proc CSTORE
	jsr SAVETOS
	jsr DROP		; Drop the address from the stack
	ldy #0			; Set offset to zero
	lda $00,X
	sta ($00),Y		; Store the lowbyte at a-addr
	jmp DROP		; Drop x from the stack
.endproc

; ( x1 x2 a-addr -- )
; Store the cell pair x1 x2 at a-addr, with x2 at a-addr and x1 at the
; next consecutive cell. "2!"
.proc TWOSTORE
		jsr DUP		; ( x1 x2 a-addr -- x1 x2 a-addr a-addr )
		jsr DROP
		jsr SWAP	; ( x1 x2 a-addr a-addr -- x1 a-addr x2 a-addr )
		PUT
		jsr STORE	; ( x1 a-addr x2 a-addr -- x1 a-addr)
		PUSH #2
		jsr ADD
		jmp STORE
.endproc


; ( a-addr1 -- a-addr2 )
; Add the size in address units of a cell to a-addr1, giving a-addr2.
; Tokenized CELL+
.proc CELLPLUS
		PUSH #2
		jmp ADD
.endproc

; ( n1 -- n2 )
; n2 is the size in address units of n1 cells. 
.proc CELLS
		jmp LSHIFT
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
		lda $00,X
		ora $01,X
		bne @sub		; Only zero is both bytes are zero
		jmp DROP		; Both bytes zero
	
@sub:	sec
		lda lowByteDSP
		sbc $00,X
		sta lowByteDSP
		lda hiByteDSP
		sbc $01,X
		sta hiByteDSP
		jmp DROP
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
		PUSH #2
		jsr ALLOT
		jsr HERE
		jmp STORE
.endproc

; ( char -- )
; Reserve space for one character in the data space and store char in the space.
; If the data-space pointer is character aligned when C, begins execution, it will remain character aligned when C, finishes execution.
; An ambiguous condition exists if the data-space pointer is not character-aligned prior to execution of C,. 
.proc CCOMMA
		PUSH #1
		jsr ALLOT
		jsr HERE
		jmp CSTORE
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
