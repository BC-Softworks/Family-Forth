\ Core arithmetic words
\ author: Edward Conn

\ Defines the following core words
\ ADD SUB 1+ 1- ABS * / */ MOD/ 
\ MIN MAX NEGATE INVERT CHAR+
\ UM* UM/MOD

require "core.f"

( n1 n2 -- n3 )
CODE +
		clc
		.repeat 2	  \ Decrementing between additions
			lda $00,X \ allows a repeat to be used
			adc $02,X \ Doesn't work with SUB
			sta $02,X
			inx       \ inx in place to eliminate drop
		.endrep
		rts
ENDCODE

( n1 n2 -- n3 )
CODE -
		sec			\ Set carry bit
		lda $02,X
		sbc $00,X
		sta $02,X	\ Store high byte
		lda $03,X
		sbc $01,X
		sta $03,X	\ Store low byte
		jmp DROP
ENDCODE

( n1 | u1 -- n2 | u2 )
\ Add one (1) to n1 | u1 giving the sum n2 | u2.
: 1+ 1 + ;

( n1 | u1 -- n2 | u2 )
\ Sub one (1) to n1 | u1 giving the differene n2 | u2.
: 1- 1 - ;


( n -- u )
\ u is the absolute value of n. 
CODE ABS
		lda $01,X
		and #%01111111
		sta $01,X
		rts
ENDCODE

( n1 n2 -- d )
\ Modified version that uses the Y register from 6502.org
CODE M*
    lda $00,X		\ Load the first cell into W
    sta lowByteW
    lda $01,X
    sta hiByteW

    lda $02,X		\ Load the first cell into W2
    sta lowByteW2
    lda $03,X
    sta hiByteW2

    n1 = lowByteW
    n2 = lowByteW2
mult16: 
    lda #$00
    sta $02,X	 	\ clear upper bits of product
    sta $03,X 
    ldy #$10		\ set binary count to 16 
		
shift:	
    lsr n1+1 		\ divide n1 by 2 
    ror n1
    bcc rot_r 
    lda $02,X	 	\ get upper half of pproduct and add n2
    clc
    adc n2
    sta $02,X
    lda $03,X
    adc n2+1

rot_r:	
    ror			 	\ rotate partial product
    sta $03,X
    ror $02,X
    ror $01,X 
    ror $00,X
    dey
    bne shift
    rts
ENDCODE

( n1 n2 -- n3 )
: * M* SWAP DROP ;

\ ( n1 n2 -- n3 n4 )
\ n3rn4 = n2 / n1
CODE /MOD
    ldy #0			\ Clear W register
    sty lowByteW	\ Used to store quotient
    sty hiByteW
@loop:	
    jsr TWODUP		\ If R < D fall through
    jsr LESS		\ Check if lesser	
    lda $00,X		\ Load low byte of flag
    bne @end		\ Break if true
    jsr DROP		\ Drop flag
    jsr SUB			\ R = R - D
    PUT 			\ Restore stack pointer

    clc				
    lda lowByteW	\ Increment W
    adc #1
    sta lowByteW
    lda hiByteW
    adc #0
    sta hiByteW		\ Check if high byte is greater then zero
    bpl @loop		\ will be for small value
    lda lowByteW	\ but necessary if over 255
    bne @loop

@end:	
    jsr DROP		\ Drop flag
    lda lowByteW	\ Set TOS to contents of W
    sta $00,X
    lda hiByteW
    sta $01,X
    rts
ENDCODE


\ ( n1 n2 -- n3 )
: / /MOD SWAP DROP ;

\ ( n1 n2 -- n3 )
\ Divide n1 by n2, giving the single-cell remainder n3.
: MOD /MOD DROP ;

\ ( n1 n2 n3 -- n4 )
\ Multiply n1 by n2 producing the intermediate double-cell result d.
\ Divide d by n3 giving the single-cell quotient n4.
: */ M* / ;

\ ( n1 n2 n3 -- n4 n5 )
\ Multiply n1 by n2 producing the intermediate double-cell result d. 
\ Divide d by n3 producing the single-cell remainder n4 and the single-cell quotient n5.
CODE */MOD
    jsr DROP	\ Move pointer to multiply x1 and x2
    jsr M_STAR	\ (n1 n2 n3 -- d1 n3)
    PUT

    jsr DIVMOD	\ TODO: Replace with 32 x 16 division
    lda $00,X
    sta $04,X
    lda $01,X
    sta $05,X
    jmp DROP
ENDCODE

\ ( n1 n2 -- n3 )
\ n3 is the lesser value
CODE MIN
    lda $01,X \ Compare high bytes first
    cmp $03,X
    bne @end  \ If unequal skip checking low byte
    lda $00,X \ Compare low bytes
    cmp $02,X \ If highest equal
@end:	
    bpl	@drop \ If n1 is less then n2 call drop
	jsr SWAP  \ Else call swap then drop
@drop:
    jmp DROP
ENDCODE

\ ( n1 n2 -- n3 )
\ n3 is the greater value
CODE MAX
    lda $01,X \ Compare high bytes first
    cmp $03,X
    bne @end  \ If unequal skip checking low byte
    lda $00,X \ Compare low bytes
    cmp $02,X \ If high bytes are equal
@end:	
    bmi @drop  \ If n1 is greater then n2 call drop
	jsr SWAP
@drop:
    jmp DROP
ENDCODE

\ ( x1 -- x2 )
\ Invert all bits of x1, giving its logical inverse x2. 
CODE INVERT
    lda $00,X
    eor #%11111111
    sta $00,X
    lda $01,X
    eor #%11111111
    sta $01,X
    rts
ENDCODE

\ ( n1 -- n2 )
\ Negate n1, giving its arithmetic inverse n2. 
: NEGATE INVERT 1+ ;

\ ( c-addr1 -- c-addr2 )
\ Add the size in address units of a character to c-addr1, giving c-addr2.

: CHAR+ 1+ ;

\ ( u1 u2 -- ud )
\ Multiply u1 by u2, giving the unsigned double-cell p ud.
\ All values and arithmetic are unsigned. 
\
\ From The Merlin 128 Macro Assembler disk, via 'The Fridge': http://www.ffd2.com/fridge/math/mult-div.s
CODE UM*
    PUT				\ Increase stack pointer
    lda #0
    sta $01,X		\ Clear the highByte of TOS
    ldy #17
    clc
		
@loop:	
    ror $01,X		\ ext+1
    ror
    ror $03,X		\ acc+1
    ror $02,X		\ acc
    bcc @mul

    clc
    adc $04,X		\ aux
    pha
    lda $05,X		\ aux+1
    adc $01,X		\ ext+1
    sta $01,X		\ ext+1
    pla
@mul:	
    dey
    bne @loop		\ When breaks ( u1 u2 -- u1 ud )

    sta $00,X		\ Store low byte
    jsr ROT			\ Swap result and u1
    jsr DROP
    jmp SWAP
ENDCODE

\ ( ud u1 -- u2 u3 )
\ Divide ud by u1, giving the quotient u3 and the remainder u2. 
\ All values and arithmetic are unsigned. 
\ An ambiguous condition exists if u1 is zero or if the quotient lies outside the range of a single-cell unsigned integer.
\
\ From The Merlin 128 Macro Assembler disk, via 'The Fridge': http://www.ffd2.com/fridge/math/mult-div.s
CODE UM/MOD
    PUT				\ Increase stack pointer
    lda #0
    sta $01,X		\ Clear the highByte of TOS
    ldy #16

@loop:  
    asl $04,X
    rol $05,X
    rol
    rol $01,X
    pha
    cmp $02,X
    lda $01,X
    sbc $03,X
    bcc @div
    sta $01,X
    pla
    sbc $02,X
    pha
    inc $04,X
@div:	
    pla
    dey
    bne @loop

    sta $00,X		\ Store low byte
    jsr SWAP		\ Swap result and u1
    jsr DROP		\ Drop u1
    jmp SWAP		\ Swap remainder and quotient
ENDCODE

