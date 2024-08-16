;=======================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)				;
;=======================================================;

; Defines the following words
; : ; [ ] STATE QUIT ABORT
; IMMEDIATE POSTPONE
; BASE BIN DECIMEL HEX

; Include guard
.ifndef SHELL_GUARD
	SHELL_GUARD = 1
.endif

.ifndef MEMORY_GUARD
	.include "memory.asm"
.endif

.ifndef CORE_GUARD
	.include "core.asm"
.endif

.segment "CODE"

; ( i * x -- i * x ) ( R: -- nest-sys )
;
; Save implementation-dependent information nest-sys about the calling definition. The stack effects i * x represent arguments to name.
;
; ( i * x -- j * x )
; Execute the definition name. The stack effects i * x and j * x represent arguments to and results from name, respectively. 
.macro COLON
		lda #true
		sta mode
.endmacro

; ( C: colon-sys -- )
; Append the run-time semantics below to the current definition.
; End the current definition, allow it to be found in the dictionary and enter interpretation state, consuming colon-sys.
; If the data-space pointer is not aligned, reserve enough data space to align it.
;
; ( -- ) ( R: nest-sys -- )
; Return to the calling definition specified by nest-sys. 
.macro SEMICOLON
		lda #false
		sta mode
		jmp ALIGN
.endmacro

; ( -- )
; Enter interpretation state. [ is an immediate word.
; Tokenized [
.macro LBRACK
		lda #false
		sta mode
.endmacro

; ( -- )
; Enter compilation state. [ is an immediate word. 
; Tokenized ]
.macro RBRACK
		lda #true
		sta mode
.endmacro

; ( -- a-addr )
; a-addr is the address of a cell containing the compilation-state flag.
; STATE is true when in compilation state, false otherwise. The true value in STATE is non-zero, but is otherwise implementation-defined.
; Only the following standard words alter the value in STATE: : (colon), ; (semicolon), ABORT, QUIT, :NONAME, [ (left-bracket), ] (right-bracket).
.macro STATE
		PUSH #mode
.endmacro

; ( -- ) ( R: i * x -- )
; Empty the return stack, store zero in SOURCE-ID if it is present, 
; make the user input device the input source, 
; and enter interpretation state. 
; Do not display a message. Repeat the following:
;    * Accept a line from the input source into the input buffer, set >IN to zero, and interpret.
;    * Display the implementation-defined system prompt if in interpretation state, all processing has been completed, and no ambiguous condition exists.
;
; TODO: Finish
.macro QUIT
		ldx #$FF
		txs			; Reset return stack
		lda #false
		sta mode	; Set interpreter state
.endmacro

; ( i * x -- ) ( R: j * x -- )
; Empty the data stack and perform the function of QUIT, which includes emptying the return stack, without displaying a message.
; TODO: Finish
.macro ABORT
	ldx #$FF
	QUIT
.endmacro

; ( -- a-addr )
; a-addr is the address of a cell containing the current number-conversion radix {{2...36}}. 
.proc BASE
		PUT
		lda #0
		sta $01,X
		lda #radix
		sta $00,x
		rts
.endproc

; ( -- )
; Set the numeric conversion radix to 2 (binary).
.macro BIN
		lda #2
		sta radix
		rts
.endmacro

; ( -- )
; Set the numeric conversion radix to ten (decimal).
.macro DECIMAL
		lda #10
		sta radix
		rts
.endmacro

; ( -- )
; Set the numeric conversion radix to 16 (hexidecimal).
.macro HEX
		lda #16
		sta radix
		rts
.endmacro

