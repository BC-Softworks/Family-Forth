\ Default startup and rest routine

require "core.f"

segment "CODE"
 
MACRO CLEARRAM
	lda #0
	ldx #0
@ram:
	sta $00,X
	sta $0100,X
	sta $0200,X
	sta $0300,X
	sta $0400,X
	sta $0500,X
	sta $0600,X
	sta $0700,X
	inx
	bne @ram
ENDMACRO

MACRO CLEAROAM
    lda #255
	ldx #0
@oam:
    sta $0200,X
	inx
	inx
	inx
	inx
	bne @oam
ENDMACRO

MACRO SETDSP
    lda #$07
    sta lowByteDSP
    lda #$F9
    sta hiByteDSP
ENDMACRO

: WAITVBLANK ;

: RESET CLEARRAM CLEAROAM SETDSP WAITVBLANK ;
