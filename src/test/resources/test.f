( -- )
: TWOOVER 3 PICK ( -- ) 3 PICK ;
3 4 1 2 TWOOVER
0= IF 255 ELSE 0 THEN
CODE
lda #0
ldy #255
ENDCODE