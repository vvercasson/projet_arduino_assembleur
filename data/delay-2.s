;; Lionel Clément
;; Quelques routines de délai

;__SP_H__ = 0x3e
;__SP_L__ = 0x3d
;__SREG__ = 0x3f
;__tmp_reg__ = 0
;__zero_reg__ = 1

;; void delay_ms(uint16_t milliseconds)
;; arguments in r25, r24
;; r24 * 0xFF * r25 * (16 000) cycles
delay_ms:
;; 1ms--
; Delay 16 000 cycles
; 1ms at 16.0 MHz
; Delay 8 000 cycles
; 1ms at 8 MHz
    ldi  r18, 11
    ldi  r19, 99
Ldelay_ms1: dec  r19
    brne Ldelay_ms1
    dec  r18
    brne Ldelay_ms1
;; --
	sbiw r24, 1 ;; Subtract 1 from r25:r24
 	brcc delay_ms
 	ret
	
	
;; void delay_s(uint16_t seconds)
;; arguments in r25, r24
;; r24 * 0xFF * r25 * (16 000 000) cycles
delay_s:
;; 1s--
; Delay 8 000 000 cycles
; 1s at 8 MHz

    ldi  r18, 41
    ldi  r19, 150
    ldi  r20, 128
Ldelay_s1: dec  r20
    brne Ldelay_s1
    dec  r19
    brne Ldelay_s1
    dec  r18
    brne Ldelay_s1
;; --
	sbiw r24, 1 ;; Subtract 1 from r25:r24
 	brcc delay_s
 	ret
	