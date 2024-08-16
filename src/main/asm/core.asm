;=======================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)				;
;=======================================================;

; Defines the following words
; DROP 2DROP DUP SWAP OVER 2DUP 2OVER ?DUP ROT 2SWAP 
; 0= 0< AND OR XOR 2* 2/ < > = U< 
; LSHIFT RSHIFT DEPTH S>D EMIT


; Include guard
.ifndef CORE_GUARD
	CORE_GUARD = 1
.endif

;; Kernel "register" locations

lowByteW   = $00
hiByteW    = $01
lowByteW2  = $02
hiByteW2   = $03
lowByteDSP = $04
hiByteDSP  = $05
radix  	   = $06
mode  	   = $07

; Input buffer
heap = $0100

;Boolean constant
false = %00000000
true  = %11111111

;; Global Macros

.macro PUT
		dex
		dex
.endmacro

; Save return value in W2
.macro SAVE_RETURN
		pla
		sta hiByteW2
		pla
		sta lowByteW2
.endmacro

; Load return value from W2
.macro LOAD_RETURN
		lda lowByteW2
		pha
		lda hiByteW2
		pha
.endmacro


; Used to load constants onto the stack
.macro PUSH arg1
		.if (.blank(arg))
            .error "Syntax error"
        .endif
		PUT
		lda #0
		sta $01,X
		lda arg1
		sta $00,X
.endmacro

.macro PUSHCELL arg1, arg2
		.if (.blank(arg))
            .error "Syntax error"
        .endif
		PUT
		lda arg1
		sta $00,X
		lda arg2
		sta $01,X
.endmacro

.segment "CODE"

; Helper procs

.proc LDW
	lda $00,X
	sta lowByteW
	lda $01,X
	sta hiByteW
	rts
.endproc

.proc LDW2
	lda $00,X
	sta lowByteW2
	lda $01,X
	sta hiByteW2
	rts
.endproc

; Set Top of Stack to value in A
.proc SETTOS
	sta $00,X
	sta $01,X
	rts
.endproc

.segment "CODE"

; ( x -- )
; Drop x from the stack
; Underflow prevention
.proc DROP
		cpx #0
		beq @end
		inx
		inx
@end:	rts
.endproc

; ( x1 x2 -- )
; Drop x1 and x2 from the stack
; Tokwnized 2DROP
.proc TWODROP
		txa
		clc
		adc #4
		tax
		rts
.endproc

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
		lda $00,X    ; Load the lowByte from the TOS
		sta lowByteW
		lda $01,X    ; Load the hiByte from the TOS
		sta hiByteW

		lda $02,X    ; Load the lowByte
		sta $00,X
		lda $03,X    ; Load the hiByte
		sta $01,X

		lda lowByteW   ; Load the lowByte from W
		sta $02,X
		lda hiByteW  ; Load the hiByte from W
		sta $03,X

		rts
.endproc

; ( x1 x2 -- x1 x2 x1 )
; Duplicate the second cell on  the stack
; place it on top
.proc OVER
		PUT
		lda $04,X ; Load now third in stack
		sta $00,X ; Store low byte of second
		lda $05,X
		sta $01,X ; Store high byte of second
		rts
.endproc

; ( x1 x2 -- x1 x2 x1 x2 )
; Duplicate cell pair x1 x2. 
; Tokenized 2DUP
.proc TWODUP
		jsr OVER 
		jmp OVER
.endproc


; ( x1 x2 x3 x4 -- x1 x2 x3 x4 x1 x2 )
; Copy cell pair x1 x2 to the top of the stack. 
.proc TWOOVER
		ldy #2
@loop:	PUT
		lda $08,X
		sta $00,X
		lda $09,X
		sta $01,X
		dey
		bne @loop
		rts
.endproc

; ( x -- 0 | x x )
; Duplicate x if it is non-zero.
; Tokenized ?DUP
.proc QDUP
		lda $00,X
		and $01,X
		beq @skip
		jmp DUP
@skip:	rts
.endproc


; ( x1 x2 x3 -- x2 x3 x1 )
; Rotate the top three stack entries.
; Optimized by moving the stack pointer instead of the objects.
.proc ROT
		jsr DROP	; ( x1 x2 x3 -- x1 x2	)
		jsr SWAP	; ( x1 x2 	 -- x2 x1	)
		PUT			; ( x1 x2 	 -- x2 x1 x3)
		jmp SWAP	; ( x1 x2 	 -- x2 x3 x1)
.endproc

;( x1 x2 x3 x4 -- x3 x4 x1 x2 )
; Exchange the top two cell pairs. 
.proc TWOSWAP
		jsr ROT		; ( x1 x2 x3 x4 -- x1 x3 x4 x2 )
		jsr DROP
		jsr ROT
		PUT
		rts
.endproc

;; Logical operations

; ( x -- flag )
; flag is true if and only if x is equal to zero.
; Tokenized 0=
.proc ZEROEQUALS
		lda $00,X
		ora $01,X
		beq @true
		lda #false
		jmp SETTOS
@true: 	lda #true
		jmp SETTOS
.endproc

; Synonym of 0=
.proc NOT
		jmp ZEROEQUALS
.endproc

; ( n -- flag )
; flag is true if and only if n is less than zero.
; Tokenized 0<
.proc ZEROLESS
		lda $01,X
		bmi @neg
		lda #false
		jmp @set
@neg: 	lda #true
@set: 	sta $00,X
		sta $01,X
		rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "and" of x1 with x2.
.proc ANDD
		lda $00,X
		and $02,X
		sta $02,X
		lda $01,X
		and $03,X
		sta $03,X
		jmp DROP
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "or" of x1 with x2.
.proc OR
		lda $00,X
		ora $02,X
		sta $02,X
		lda $01,X
		ora $03,X
		sta $03,X
		jmp DROP
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "xor" of x1 with x2.
.proc XOR
		lda $00,X
		eor $02,X
		sta $02,X
		lda $01,X
		eor $03,X
		sta $03,X
		jmp DROP
.endproc

;; Bit Shifts

; ( x1 -- x2 )
; x2 is the result of shifting x1 one bit toward the most-significant bit, 
; filling the vacated least-significant bit with zero. 
; Tokenized 2*
.proc TWOSTAR
		clc
		asl $00,X
		rol $01,X
		rts
.endproc

; ( x1 -- x2 )
; x2 is the result of shifting x1 one bit toward the least-significant bit, 
; leaving the most-significant bit unchanged.
; Tokenized 2/
.proc TWOSLASH
		clc
		lda #$80		; Store the 7th bit
		and $01,X
		lsr $01,X
		ror $00,X
		ora $01,X		; Restore the 7th bit
		sta $01,X
		rts
.endproc

; Helper proc
.proc CMP16
		lda $02,X
		cmp $00,X
		lda $03,X
		sbc $01,X
		bvc @skip 	; N eor V
		eor #$80
@skip:	rts
.endproc

; ( n1 n2 -- flag )
; flag is true if and only if n1 is less than n2. 
; Tokenized <
.proc LESS
		jsr CMP16
		bpl @pos 	; If N not set 
		lda #true
		jmp @store
@pos: 	lda #false  ; A bit was set to 1
@store:	jsr DROP
		jmp SETTOS
.endproc

; ( n1 n2 -- flag )
; flag is true if and only if n1 is greater than n2. 
; Tokenized >
.proc GREATER
		jsr CMP16
		bmi @neg 	; N set
		lda #true
		jmp @store
@neg: 	lda #false  ; A bit was set to 1
@store:	jsr DROP
		jmp SETTOS
.endproc

; ( n1 n2 -- flag )
; flag is true if and only if n1 is equal to n2. 
; Tokenized =
.proc EQUAL
		jsr XOR    		; Flips bits to all zeros if equal
		jmp ZEROEQUALS	; Check if flag is zero
.endproc

; ( u1 u2 -- flag )
; flag is true if and only if u1 is less than u2. 
; <U
.proc ULESS
		lda $03,X
		cmp $01,X
		bcc @true
		bne @false
		lda $02,X
		cmp $00,X
		bcc @true
@false:	lda #false
		beq @end
@true:	lda #true
@end:	jsr DROP
		jmp SETTOS
.endproc

; ( x1 u -- x2 )
; Perform a logical left shift of u bit-places on x1, giving x2.
; Put zeroes into the least significant bits vacated by the shift.
; If u is greater or equal to cell size return 0.
.proc LSHIFT
		lda $01,X	; Check if the number is greater then 16
		bne @over
		lda $00,X	; Check if the number is greater then 16
		and %11110000
		beq @start
@over:	jsr DROP
		lda #false
		jmp SETTOS
@start: ldy $00,X
		jsr DROP	; Drop u
@loop:	jsr TWOSTAR
		dey
		bne @loop
		rts
.endproc

; ( x1 u -- x2 )
; Perform a logical right shift of u bit-places on x1, giving x2. 
; Put zeroes into the most significant bits vacated by the shift.
; If u is greater or equal to cell size return 0.
.proc RSHIFT
		lda $01,X		; Check if the number is greater then 16
		bne @over
		lda $00,X		; Check if the number is greater then 16
		and %11110000
		beq @start
@over:	jsr DROP
		lda #false
		jmp SETTOS

@start: ldy $00,X
		jsr DROP		; Drop u
@loop:	jsr TWOSLASH
		dey
		bne @loop
		rts
.endproc

; ( -- +n )
; +n is the number of single-cell values contained in 
; the data stack before +n was placed on the stack
.proc DEPTH
		txa				; Transfer stack pointer to A
		bne	@put		; If empty return zero
		PUSH #0
		rts
@put:	PUT
		eor #$FF		; Inverse pointer
		adc #1
		sta $00,X
		lda #0			; Clear A
		sta $01,X		
		jmp TWOSLASH
.endproc

; ( n -- d )
; Convert the number n to the double-cell number d with the same numerical value. 
.proc STOD
		lda $00,x
		bmi @neg	; Push $FFFF if negative
		lda #false
		beq @set
@neg:	lda #true
@set:	PUT
		jsr SETTOS
		jmp SWAP
.endproc

; ( x -- )
; If x is a graphic character in the implementation-defined character set, display x. 
; The effect of EMIT for all other values of x is implementation-defined.
;
; When passed a character whose character-defining bits have a value between hex 20 and 7E inclusive,
; the corresponding standard character, specified by 3.1.2.1 Graphic characters, is displayed.
; Because different output devices can respond differently to control characters,
; programs that use control characters to perform specific functions have an environmental dependency.
; Each EMIT deals with only one character.
.proc EMIT
	
	rts
.endproc