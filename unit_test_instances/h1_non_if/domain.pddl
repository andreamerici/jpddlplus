(define (domain h1_non_if)
  (:requirements :typing :numeric-fluents) ; Requisiti: supporto per tipi e variabili numeriche.

  (:predicates (pa) (pb))              ; Fatti proposizionali: 'pa' e 'pb'.

  (:functions (x))                     ; Variabile numerica: 'x'.

  (:action set-pa                    ; Azione 1: Abilita 'inc-a'.
    :parameters ()
    :precondition (and)             ; Nessuna precondizione iniziale.
    :effect (pa)                    ; Effetto: Rende 'pa' vero.
  )

  (:action set-pb                    ; Azione 2: Abilita 'inc-b'.
    :parameters ()
    :precondition (and)             ; Nessuna precondizione iniziale.
    :effect (pb)                    ; Effetto: Rende 'pb' vero.
  )

  (:action inc-a                    ; Azione 3: Incrementa 'x'.
    :parameters ()
    :precondition (and (pa) (>= (x) 0)) ; Precondizione: Richiede 'pa' e la condizione numerica (>= (x) 0).
    :effect (increase (x) 1)        ; Effetto: Incrementa 'x' di 1.
  )

  (:action inc-b                    ; Azione 4: Incrementa 'x'.
    :parameters ()
    :precondition (and (pb) (>= (x) 0)) ; Precondizione: Richiede 'pb' e la condizione numerica (>= (x) 0).
    :effect (increase (x) 1)        ; Effetto: Incrementa 'x' di 1.
  )
)

; Le azioni 'inc-a' e 'inc-b' sono entrambe Achievers (Ach) per la condizione numerica (>= (x) 0).
; Tuttavia, hanno precondizioni diverse (pre(inc-a) = {pa, (>= (x) 0)} e pre(inc-b) = {pb, (>= (x) 0)}).
; Questo setup viola la clausola di implicazione della Definizione 6 (Interference-free Problem):
; Se 'set-pa' (o 'set-pb') interferisce con 'inc-b' (o 'inc-a'), non è vero che pre(a_i) implica pre(a_j),
; rendendo il dominio Non-IF.