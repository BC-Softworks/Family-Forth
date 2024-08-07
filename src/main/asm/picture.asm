;==========================================================;
; =#= AUTHOR:  Edward Conn (@cardseller2)  			       ;
;==========================================================;

;; Registers
ppuctrl     = $2000 	; VPHB SINN 	NMI enable (V), PPU master/slave (P), sprite height (H), background tile select (B), sprite tile select (S), increment mode (I), nametable select (NN)
ppumask 	= $2001 	; BGRs bMmG 	color emphasis (BGR), sprite enable (s), background enable (b), sprite left column enable (M), background left column enable (m), greyscale (G)
ppustatus 	= $2002 	; VSO- ---- 	vblank (V), sprite 0 hit (S), sprite overflow (O); read resets write pair for $2005/$2006
oamddr   	= $2003 	; aaaa aaaa 	OAM read/write address
oamdata 	= $2004 	; dddd dddd 	OAM data read/write
ppuscroll 	= $2005 	; xxxx xxxx 	fine scroll position (two writes: X scroll, Y scroll)
ppuaddr 	= $2006 	; aaaa aaaa 	PPU read/write address (two writes: most significant byte, least significant byte)
ppudata 	= $2007 	; dddd dddd 	PPU data read/write
oamdma 	    = $4014 	; aaaa aaaa 	OAM DMA high address 



