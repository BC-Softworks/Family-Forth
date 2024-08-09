;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

; Loads core library, subroutines, and initalize kernel

.ifndef CONTROL_GUARD
	.include "control.asm"
.endif

.ifndef MEMORY_GUARD
	.include "memory.asm"
.endif

.ifndef MATH_GUARD
	.include "math.asm"
.endif

.ifndef CORE_GUARD
	.include "core.asm"
.endif

.segment "OAM"
oam:		.res 256

.segment "CODE"

.proc RESET
		sei        ; ignore IRQs
		cld        ; disable decimal mode
		ldx #$40
		stx $4017  ; disable APU frame IRQ
		ldx #$ff
		txs        ; Set up stack
		inx        ; now X = 0
		stx $2000  ; disable NMI
		stx $2001  ; disable rendering
		stx $4010  ; disable DMC IRQs

		; The vblank flag is in an unknown state after reset,
		; so it is cleared here to make sure that @vblankwait1
		; does not exit immediately.
		bit $2002

		; First of two waits for vertical blank to make sure that the
		; PPU has stabilized
@vblankwait1:  
		bit $2002
		bpl @vblankwait1

		; We now have about 30,000 cycles to burn before the PPU stabilizes.
		; One thing we can do with this time is put RAM in a known state.
		; Here we fill it with 0, before setting X to TOS.
		txa
@clrmem:	
		sta $000,X
		sta $100,X
		sta $200,X
		sta $300,X
		sta $400,X
		sta $500,X
		sta $600,X
		sta $700,X
		inx
		bne @clrmem

; Initalize Forth Kernel
@kernel:		
		ldx #$FD    ; Set data stack pointer
		lda #$FF 
		sta lowByteDSP
		lda #$07	; Set data space pointer
		sta hiByteDSP
		lda #0		; Clear accumulator

@vblankwait2:
    bit $2002
    bpl @vblankwait2
	rts
.endproc
