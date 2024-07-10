;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

;; Primitives are defined as macros when possible for performance

; ( x -- x)
; Add x to the stack
.macro PUT
	dex
	dex
.endmacro

; ( x -- )
; Drop x from the stack
.macro DROP
	inx
	inx
.endmacro

; ( x -- xx )
; Copy x and place it on the stack
.macro DUP
	lda 0,X
	pha
	lda 1,X
	PUT
.endmacro

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "and" of x1 with x2. 
.macro AND

and 

.endmacro


; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "or" of x1 with x2. 
.proc OR

.endproc

; ( x1 x2 -- x3 )
; x3 is the bit-by-bit logical "xor" of x1 with x2. 
.proc XOR

.endproc

; ( n1 n2 -- n3 )
; n3 is lesser value
.proc MIN

.endproc

; ( n1 n2 -- n3 )
; n3 is greater value
.proc MAX

.endproc
