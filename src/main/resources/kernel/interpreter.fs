\ Forth Interpreter
\ author: Edward Conn

\ Defines the following words
\ : \ [ ] STATE QUIT ABORT
\ BASE BIN DECIMEL HEX

require "core.f"

segment "CODE"

( i * x -- i * x ) ( R: -- nest-sys )
\ Save implementation-dependent information nest-sys about the calling definition. The stack effects i * x represent arguments to name.

\ ( i * x -- j * x )
\ Execute the definition name. The stack effects i * x and j * x represent arguments to and results from name, respectively. 
MACRO COLON
	lda #true
	sta mode
ENDMACRO

( C: colon-sys -- )
\ Append the run-time semantics below to the current definition.
\ End the current definition, allow it to be found in the dictionary and enter interpretation state, consuming colon-sys.
\ If the data-space pointer is not aligned, reserve enough data space to align it.

( -- ) ( R: nest-sys -- )
\ Return to the calling definition specified by nest-sys. 
MACRO SEMICOLON
	lda #false
	sta mode
	jsr ALIGN
ENDMACRO

( -- )
\ Enter interpretation state. [ is an immediate word.
\ Tokenized [
MACRO LBRACK
	lda #false
	sta mode
ENDMACRO

( -- )
\ Enter compilation state. [ is an immediate word. 
\ Tokenized ]
MACRO RBRACK
	lda #true
	sta mode
ENDMACRO

( -- a-addr )
\ a-addr is the address of a cell containing the compilation-state flag.
\ STATE is true when in compilation state, false otherwise. The true value in STATE is non-zero, but is otherwise implementation-defined.
\ Only the following standard words alter the value in STATE: : (colon), \ (semicolon), ABORT, QUIT, :NONAME, [ (left-bracket), ] (right-bracket).
MACRO STATE
	PUT
	lda #mode
	sta 00,X
	lda #0
	sta 01,X
ENDMACRO

( -- ) ( R: i * x -- )
\ Empty the return stack, store zero in SOURCE-ID if it is present, 
\ make the user input device the input source, 
\ and enter interpretation state. 
\ Do not display a message. Repeat the following:
\    * Accept a line from the input source into the input buffer, set >IN to zero, and interpret.
\    * Display the implementation-defined system prompt if in interpretation state, all processing has been completed, and no ambiguous condition exists.
\
\ TODO: Finish
MACRO QUIT
	ldx #$FF
	txs			\ Reset return stack
	lda #false
	sta mode	\ Set interpreter state
ENDMACRO

( i * x -- ) ( R: j * x -- )
\ Empty the data stack and perform the function of QUIT, which includes emptying the return stack, without displaying a message.
\ TODO: Finish
MACRO ABORT
	ldx #$FF
	QUIT
ENDMACRO

( -- a-addr )
\ a-addr is the address of a cell containing the current number-conversion radix {{2...36}}. 
CODE BASE
	PUT
	lda #0
	sta $01,X
	lda #radix
	sta $00,x
	rts
ENDCODE

( -- )
\ Set the numeric conversion radix to 2 (binary).
MACRO BIN
	lda #2
	sta radix
	rts
ENDMACRO

( -- )
\ Set the numeric conversion radix to ten (decimal).
MACRO DECIMAL
	lda #10
	sta radix
	rts
ENDMACRO

( -- )
\ Set the numeric conversion radix to 16 (hexidecimal).
MACRO HEX
	lda #16
	sta radix
	rts
ENDMACRO

