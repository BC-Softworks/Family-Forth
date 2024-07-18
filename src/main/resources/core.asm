;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Defines the following words core words
; PUT DROP DUP SWAP OVER ROT 0= 0< 0>
; AND OR XOR 2* 2/ LSHIFT < >

; Defines the following words core extension words
; NIP TUCK TRUE FALSE U> U<

;; Working register locations
lowByteW   = $00
hiByteW    = $01
lowByteW2  = $02
hiByteW2   = $03

;Boolean constant
false = #0
true  = #255


; Should always be called before
; adding a value to the stack
.proc PUT
		dex
		dex
		rts
.endproc

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
		PUT
		lda $04,X
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
loop:	asl A
		sta $00, X
		; TODO: Finish
		dey
		bne loop
		rts
r_zero: lda #0
		sta $00
		rts
.endproc

; TODO: Finish implmenting LESS and GREATER

; ( n1 n2 -- flag )
; flag is true if and only if n1 is less than n2. 
; Tokenized <
.proc LESS
		lda $03,X
		cmp $01,X
		bmi neg


		rts
.endproc

; ( n1 n2 -- flag )
; flag is true if and only if n1 is greater than n2. 
; Tokenized >
.proc GREATER
		lda $03,X
		cmp $01,X
		bmi neg


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
		dex
		dex
		lda true
		sta $00,X
		sta $01,X
.endproc

; ( -- false )
; Return a false flag, a single-cell value with no bits set.
; Same as pushing 0 onto the stack
.proc FALSE
		dex
		dex
		lda false
		sta $00,X
		sta $01,X
.endproc

; ( u1 u2 -- flag )
; flag is true if and only if u1 is greater than u2. 
.proc UGREATER
		
		rts
.endproc

.proc ULESS
		
		rts
.endproc

