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

