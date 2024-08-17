;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words core words
; @ C@ ! +! C! ALIGN ALIGNED 
; CELL+ CELLS R> >R R@ 
; ALLOT HERE , C, MOVE FILL

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
		jsr LDW
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
		jsr LDW
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
		jsr LDW
		jsr DROP		; Drop the address from the stack
		ldy #0			; Set offset to zero
		lda $00,X
		sta ($00),Y		; Store the lowbyte at a-addr
		ldy #1
		lda $01,X		; Store the highbyte at a-addr + 1
		sta ($00),Y
		jmp DROP		; Drop x from the stack
.endproc

; ( x a-addr -- )
; Add x to the single-cell number at a-addr. 
; Tokenized "+!"
.proc PLUSSTORE
		jsr SWAP	; ( x a-addr		-- a-addr x 		)
		jsr OVER	; ( a-addr x		-- a-addr x a-addr	)
		jsr FETCH	; ( a-addr x a-addr	-- a-addr x1 x2		)
		jsr ADD		; ( a-addr x1 x2  	-- a-addr x3		)
		jsr SWAP	; ( a-addr x3		-- x3 a-addr 		)
		jmp STORE	; ( x3 a-addr		--					)
.endproc

; ( char c-addr -- )
; Store char at c-addr. 
; When character size is smaller than cell size, 
; only the number of low-order bits corresponding to character size are transferred. 
; Tokenized "C!"
.proc CSTORE
	jsr LDW
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
		sta $00,X     ; Pull low byte
		pla
		sta $01,X	  ; Pull high byte
		LOAD_RETURN
		rts
.endproc

; ( x -- ) ( R: -- x )
; Move x to the return stack.
; Tokenized >R
; IMPORTANT: Doesn't use W
.proc TOR
		SAVE_RETURN
		lda $01,X     ; Push higher byte
		pha
		lda $00,X	  ; Push lower byte
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
		sta $00,X		; Push lower byte
		pla
		sta $01,X		; Push higher byte
		pha
		lda $00,X		; Load lowbyte to push back onto the stack
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
		ora $01,X		; Compare low and high bytes
		bne @rot		; Skip if u = 0
		jsr TWODROP		; Drop addr1, addr2, and u
		jmp DROP

@rot:	jsr SWAP		; Swap u and addr2
		jsr LDW2		; Copy addr2 to W2
		jsr DROP		; Drop addr2
		jsr SWAP		; Swap u and addr1
		jsr LDW			; Copy addr1 to W
		jsr DROP		; Drop addr1

		ldy #0			; Init Y
@loop:	
		lda ($00),Y		; Load byte from addr1 + y
		sta ($02),Y		; Store byte at addr2 + y
		iny				; Increment Y
		jsr ONESUB		; Subtract one from u ; TODO: Use unsigned sub
		lda $00,X
		ora $01,X		; Compare low and high bytes
		bne @loop		; Break if zero
		jmp DROP		; Drop u
.endproc

; ( c-addr u char -- )
; If u is greater than zero, store char in each of u consecutive characters of memory beginning at c-addr. 
.proc FILL
		lda $02,X
		ora $03,X		; Compare low and high bytes
		bne start		; Skip if u = 0
		jsr DROP		; Drop addr1, addr2, and u
		jmp TWODROP

start:	lda $00,X		; Load char into W2
		sta $02
		jsr DROP		; Drop char
		jsr SWAP		; Swap c-addr and u 
		jsr LDW			; Copy c-addr to W
		jsr DROP		; Drop c-addr
		
		ldy #0			; Init Y
@loop:
		lda $02			; Load byte from addr1 + y
		sta ($00),Y		; Store byte at addr2 + y
		iny				; Increment Y
		jsr ONESUB		; Subtract one from u
		lda $00,X
		ora $01,X		; Compare low and high bytes
		bne @loop		; Break if zero
		jmp DROP		; Drop u
.endproc

; ( c-addr1 -- c-addr2 u )
; Return the character string specification for the counted string stored at c-addr1.
; c-addr2 is the address of the first character after c-addr1.
; u is the contents of the character at c-addr1, which is the length in characters of the string at c-addr2.
; TODO: Finish
.proc COUNT
		jsr LDW		; Store c-addr1 in W
		ldy #$00	; Set y to 0

@loop:	iny
		jsr ONEADD	; Increment c-addr1
		lda ($00),Y	; Load current address
		bne @loop	; Loop if char is not zero

		PUT
		tya			; Set top of stack to Y
		sta $00,X
		lda #0
		sta $01,X	; TODO: Decide if 256+ strings are acceptable
		rts
.endproc