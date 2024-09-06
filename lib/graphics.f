\ NES graphics library
\ author: Edward Conn

\ Defines the following words
\ SPRITE

require "memory.f"

CONST
    PPU_MASK	= $2001
    PPU_STATUS	= $2002
    OAM_ADDR    = $2003
    PPU_SCROLL  = $2005
    PPU_ADDR    = $2006
    PPU_DATA    = $2007 
ENDCONST

: SPRITE ;

