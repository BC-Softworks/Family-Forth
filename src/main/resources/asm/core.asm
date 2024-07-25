;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words
; DROP DUP SWAP OVER ROT 0= 0< 0>
; AND OR XOR 2* 2/ LSHIFT RSHIFT < > = 

; Defines the following words core extension words
; NIP TUCK TRUE FALSE PICK U> U<

;; Kernel "register" locations
lowByteW   = $00
hiByteW    = $01
lowByteW2  = $02
hiByteW2   = $03
lowByteDSP = $04
hiByteDSP = $05

;Boolean constant
false = 0
true  = 255

; Used to load constants
; onto the stack
.macro PUSH arg1, arg2
		.if (.blank(arg))
            .error "Syntax error"
        .endif
		dex
		dex
		lda arg1
		sta $00,X
		lda arg2
		sta $01,X
		rts
.endmacro

; ( x -- )
; Drop x from the stack
.proc DROP
		inx
		inx
		rts
.endproc

; ( x -- xx )
; Copy x and place it on the stack
.proc DUP
		dex          ; Inline Put
		dex
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
		sta lowbyte_W
		lda $01,X    ; Load the hiByte from the TOS
		sta hibyte_W

		lda $02,X    ; Load the lowByte
		sta $00,X
		lda $03,X    ; Load the hiByte
		sta $01,X

		lda lowbyte_W   ; Load the lowByte from W
		sta $02,X
		lda hibyte_W  ; Load the hiByte from W
		sta $03,X

		rts
.endproc

; ( x1 x2 -- x1 x2 x1 )
; Duplicate the second cell on  the stack
; place it on top
.proc OVER
		dex       ; Inline Put
		dex
		lda $04,X ; Load now third in stack
		sta $00,X ; Store low byte of second
		lda $05,X
		sta $01,X ; Store high byte of second
		rts
.endproc

; ( x1 x2 x3 -- x2 x3 x1 )
; Rotate the top three stack entries.
; Optimized back moving the stack pointer
; instead of the objects.
.proc ROT
		inx          ; Inline Drop
		inx
		jsr SWAP     ; Doesn't affect x3
		dex          ; Inline Put
		dex
		jsr SWAP
		rts
.endproc

;; Logical operations

; ( x -- flag )
; flag is true if and only if x is equal to zero.
; Tokenized 0=
.proc ZEROEQU
		lda false
		cmp $00,X
		bne @not_zero
		cmp $01,X
		bne @not_z
		lda true   ;true = #255
		sta $00,X
		sta $01,X
		rts
@not_z: sta $00,X
		sta $01,X
		rts
.endproc

; Synonym of 0=
.macro NOT
		ZEROEQU
.endmacro

; ( n -- flag )
; flag is true if and only if n is less then zero.
; Tokenized 0<
.proc ZEROLESS
		lda $01,X
		bmi neg
		lda false
		jmp set
neg: 	lda true
set: 	sta $00,X
		sta $01,X
		rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "and" of x1 with x2.
.proc ANDD
		lda $00,X
		sta $00
		lda $01,X
		inx
		inx
		and $01,X
		sta $01,X
		lda $00
		and $00,X
		sta $00,X
		rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "or" of x1 with x2.
.proc OR
		lda $00,X
		sta lowByteW
		lda $01,X
		inx
		inx
		ora $01,X
		sta $01,X
		lda lowByteW
		ora $00,X
		sta $00,X
		rts
.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "xor" of x1 with x2.
.proc XOR
		lda $00,X
		sta lowByteW
		lda $01,X
		inx          ;Inline drop
		inx
		eor $01,X
		sta $01,X
		lda lowByteW
		eor $00,X
		sta $00,X
		rts
.endproc

;; Bit Shifts

; ( x1 -- x2 )
; x2 is the result of shifting x1 one bit toward the most-significant bit, 
; filling the vacated least-significant bit with zero. 
.proc TWOSTAR
		clc
		asl $00,X
		asl $01,X
		rts
.endproc

; ( x1 -- x2 )
; x2 is the result of shifting x1 one bit toward the least-significant bit, 
; leaving the most-significant bit unchanged. 
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
		inx             ; Inline Drop
		inx
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
		inx             ; Inline Drop
		inx
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

; TODO: Finish implmenting LESS and GREATER
;       Account for sign


.macro CMP16
		lda $00,X
		cmp $02,X
		lda $01,X
		sbc $03,X
		bvc skip ; N eor V
		eor #$80
skip:
.endmacro

; ( n1 n2 -- flag )
; flag is true if and only if n1 is less than n2. 
; Tokenized <
.proc LESS
		CMP16
		bpl branch ; If N not set 
		lda true
		sta $00,X
		sta $01,X
		rts
branch: lda false  ; A bit was set to 1
		sta $00,X
		sta $01,X
		rts
.endproc

; ( n1 n2 -- flag )
; flag is true if and only if n1 is greater than n2. 
; Tokenized >
.proc GREATER
		CMP16
		bmi branch ; N set
		lda true
		sta $00,X
		sta $01,X
		rts
branch: lda false  ; A bit was set to 1
		sta $00,X
		sta $01,X
		rts
.endproc

; ( n1 n2 -- flag )
; flag is true if and only if n1 is equal to n2. 
; Tokenized =
.proc EQUAL
		jsr XOR    ; Flips bits to all zeros if equal
		lda $00,X
		bne branch
		lda $01,X
		bne branch ; If false 
		lda true
		sta $00,X
		sta $01,X
		rts
branch: lda false  ; A bit was set to 1
		sta $00,X
		sta $01,X
		rts
.endproc

;; Core Word Extensions

; ( x1 x2 -- x2 )
; Remove the second cell on the stack
.proc NIP
		lda $00,X    ; Load the lowByte from the TOS
		sta $02,X    ; Overwrite lowByte of second cell
		lda $01,X    ; Load the highByte from the TOS
		sta $03,X    ; Overwrite highByte of second cell
		inx
		inx
		rts
.endproc


; ( x1 x2-- x2 x1 x2 )
; Copy the first (top) stack item below the second stack item. 
.proc TUCK
		jsr SWAP
		jsr OVER
		rts
.endproc

; ( n -- flag )
; flag is true if and only if n is greater then zero.
; Tokenized 0>
.proc ZEROGREATER
		lda $01,X
		bpl neg
		lda false
		jmp set
neg: 	lda true
set: 	sta $00,X
		sta $01,X
		rts
.endproc

; ( -- true )
; Return a true flag, a single-cell value with all bits set. 
.proc TRUE
		dex			; Inline Put
		dex
		lda true
		sta $00,X
		sta $01,X
.endproc

; ( -- false )
; Return a false flag, a single-cell value with no bits set.
; Same as pushing 0 onto the stack
.proc FALSE
		dex			; Inline Put
		dex
		lda false
		sta $00,X
		sta $01,X
.endproc

; ( xu...x1 x0 u -- xu...x1 x0 xu )
; Remove u. Copy the xu to the top of the stack. 
; An ambiguous condition exists if there are less 
; than u+2 items on the stack before PICK is executed.
; Since the stack is 60 cells max u is only using the lower byte
.proc PICK
		clc
		lda $00,X
		tay
		adc $00,X   ; Convert cells to bytes
		stx $00		; Store TOS address
		adc $00    	; Add from top of stack
		ldy #0    	; Clear Y
		sta $00   	; Save addr at $00    
		lda ($00),Y ; Load xu's low byte indirectly
		sta $00,X
		iny			; Increment y to get high byte
		lda ($00),Y ; Load xu's high byte indirectly
		sta $01,X
		rts        	; Don't need to drop anything
.endproc

; ( u1 u2 -- flag )
; flag is true if and only if u1 is greater than u2. 
; U>
.proc UGREATER
		lda $01,X
		cmp $03,X
		bpl _true	; High byte greater
		bmi _false	; High byte lesser
		
		lda $00,X	; Lowbyte check
		cmp $02,X
		bpl _true	; Low byte greater
		
_false:	lda false
		jmp @drop
		
_true:	lda true		
		
@drop:  inx
		inx		
		sta $00,X	; Store true or false
		sta $01,X	; to the TOS
		rts
.endproc

; ( u1 u2 -- flag )
; flag is true if and only if u1 is less than u2. 
; U<
.proc ULESS
		lda $01,X
		cmp $03,X
		bmi _true	; High byte lesser
		bpl _false	; High byte greater
		
		lda $00,X	; Lowbyte check
		cmp $02,X
		bmi _true	; Low byte lesser
		
_false:	lda false
		jmp @drop
		
_true:	lda true		
		
@drop:  inx
		inx		
		sta $00,X	; Store true or false
		sta $01,X	; to the TOS
		rts
.endproc

