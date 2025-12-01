(define (domain h1_if)
  (:requirements :typing :numeric-fluents) ; Requisiti: supporto per tipi e variabili numeriche.

  (:predicates (p))                      ; Fatto proposizionale: 'p'.

  (:functions (x))                       ; Variabile numerica: 'x'.

  (:action set-p                        ; Azione 1: Abilita 'inc-x'.
    :parameters ()
    :effect (p)                         ; Effetto: Rende 'p' vero.
  )

  (:action inc-x                        ; Azione 2: Modifica 'x'.
    :parameters ()
    :precondition (p)                   ; Precondizione: Richiede che 'p' sia vero.
    :effect (increase (x) 1)            ; Effetto: Incrementa il valore di 'x' di 1.
  )
)

; Questo è un dominio sequenziale minimale utilizzato per testare scenari Interference-Free
; 'set-p' abilita 'inc-x'.