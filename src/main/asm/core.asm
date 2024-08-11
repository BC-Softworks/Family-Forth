;=======================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)				;
;=======================================================;

; Defines the following words
; DROP 2DROP DUP SWAP OVER 2DUP 2OVER ?DUP ROT 0= 0< 0>
; AND OR XOR 2* 2/ LSHIFT RSHIFT < > = DEPTH BASE
; U> U<


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
		lda arg1
		sta $00,X
		lda #0
		sta $01,X
		rts
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
		rts
.endmacro

.segment "CODE"

; ( x -- )
; Drop x from the stack
.proc DROP
		inx
		inx
		rts
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
		jsr DROP
		jsr SWAP     ; Doesn't affect x3
		PUT
		jmp SWAP
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
		sta $00,X
		sta $01,X
		rts
@true: 	lda #true
		sta $00,X
		sta $01,X
		rts
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

; ( n -- flag )
; flag is true if and only if n is greater than zero.
; Tokenized 0>
.proc ZEROGREAT
		lda $01,X
		bmi @neg
		eor $00,X
		beq @neg
		lda #true
		jmp @set
@neg: 	lda #false
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
		asl $01,X
		rts
.endproc

; ( x1 -- x2 )
; x2 is the result of shifting x1 one bit toward the least-significant bit, 
; leaving the most-significant bit unchanged.
; Tokenized 2/
.proc TWOSLASH
		clc
		lda #%10000000 ; Store the 7th bit
		and $01,X
		lsr $01,X
		lsr $00,X
		ora $01,X      ; Restore the 7th bit
		rts
.endproc


; ( x1 u -- x2 )
; Perform a logical left shift of u bit-places on x1, giving x2.
; Put zeroes into the least significant bits vacated by the shift.
; If u is greater or equal to cell size return 0.
.proc LSHIFT
		ldy $00,X       ; Load low bit of u
		cmp #%00010000  
		bmi r_zero
		jsr DROP
		lda $00,X
@loop:	asl A
		sta $00, X
		; TODO: Finish
		dey
		bne @loop
		rts
r_zero: lda #0
		sta $00
		rts
.endproc

; ( x1 u -- x2 )
; Perform a logical right shift of u bit-places on x1, giving x2. 
; Put zeroes into the most significant bits vacated by the shift.
.proc RSHIFT
		ldy $00,X       ; Load low bit of u
		cmp #%00010000  
		bmi r_zero
		jsr DROP
		lda $00,X
@loop:	lsr A
		sta $00, X
		; TODO: Finish
		dey
		bne @loop
		rts
r_zero: lda #0
		sta $00
		rts
.endproc

; Helper proc
.proc CMP16
		lda $00,X
		cmp $02,X
		lda $01,X
		sbc $03,X
		bvc skip 	; N eor V
		eor #$80
skip:	rts
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
		sta $00,X
		sta $01,X
		rts
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
		sta $00,X
		sta $01,X
		rts
.endproc

; ( n1 n2 -- flag )
; flag is true if and only if n1 is equal to n2. 
; Tokenized =
.proc EQUAL
		jsr XOR    		; Flips bits to all zeros if equal
		jmp ZEROEQUALS	; Check if flag is zero
.endproc

; ( -- +n )
; +n is the number of single-cell values contained in 
; the data stack before +n was placed on the stack
.proc DEPTH
		stx lowByteW	; Store addr before dex
		dex				; Add an empty highbyte
		lda #0		
		sta $00,X
		dex
		sec				; Clear sub flag
		lda #255
		sbc lowByteW
		sta $00,X
		jsr RSHIFT
		rts
.endproc

; ( -- a-addr )
; a-addr is the address of a cell containing the current number-conversion radix {{2...36}}. 
.proc BASE
	lda radix
	sta $00,x
	lda #0
	sta $01,X
	rts
.endproc

; ( u1 u2 -- flag )
; flag is true if and only if u1 is greater than u2. 
; U>
.proc UGREATER
		lda $01,X
		cmp $03,X
		bpl @true	; High byte greater
		bmi @false	; High byte lesser
		
		lda $00,X	; Lowbyte check
		cmp $02,X
		bpl @true	; Low byte greater
		
@false:	lda false
		beq @drop
		
@true:	lda true		
		
@drop:  sta $02,X	; Store true or false
		sta $03,X	; to the TOS
		jmp DROP
.endproc

; ( u1 u2 -- flag )
; flag is true if and only if u1 is less than u2. 
; U<
.proc ULESS
		lda $01,X
		cmp $03,X
		bmi @true	; High byte lesser
		bpl @false	; High byte greater
		
		lda $00,X	; Lowbyte check
		cmp $02,X
		bmi @true	; Low byte lesser
		
@false:	lda false
		beq @drop
		
@true:	lda true
		
@drop:  jsr DROP
		sta $00,X	; Store true or false
		sta $01,X	; to the TOS
		rts
.endproc

