;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

;; Channel Registers
pulse_1     = $4000
pulse_2     = $4004
triangle    = $4008
noise       = $400C
dmc         = $4010
status      = $4015
frame       = $4017

.proc init_apu
        ; Init $4000-4013
        ldy #$13
@loop:  lda @regs,y
        sta pulse_1,y
        dey
        bpl @loop
 
        ; We have to skip over $4014 (OAMDMA)
        lda #$0f
        sta $4015
        lda #$40
        sta $4017
   
        rts
@regs:
        .byte $30,$08,$00,$00
        .byte $30,$08,$00,$00
        .byte $80,$00,$00,$00
        .byte $30,$00,$00,$00
        .byte $00,$00,$00,$00

.endproc