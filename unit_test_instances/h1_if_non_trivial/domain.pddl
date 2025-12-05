(define (domain h1_if_non_trivial)

  (:predicates (pa) (pb))              ; Fatti proposizionali: 'pa' e 'pb'.

  (:functions (x))                     ; Variabile numerica: 'x'.

  (:action set-pa                    ; Azione 1: Abilita 'inc-a'.
    :parameters ()
    :precondition ()             ; Nessuna precondizione iniziale.
    :effect (pa)                    ; Effetto: Rende 'pa' vero.
  )

  (:action set-pb                    ; Azione 2: Abilita 'inc-b'.
    :parameters ()
    :precondition ()             ; Nessuna precondizione iniziale.
    :effect (pb)                    ; Effetto: Rende 'pb' vero.
  )

  (:action inc-a                    ; Azione 3: Incrementa 'x'.
    :parameters ()
    :precondition ()                ;
    :effect (increase (x) 1)        ; Effetto: Incrementa 'x' di 1.
  )

  (:action inc-b                    ; Azione 4: Incrementa 'x'.
    :parameters ()
    :precondition (and (pb))        ; Precondizione: Richiede 'pb'
    :effect (increase (x) 2)        ; Effetto: Incrementa 'x' di 2.
  )
  ; Le azioni 'inc-a' e 'inc-b' sono entrambe Achievers (Ach) per la condizione numerica (>= (x) 0).
  ; Tuttavia, hanno precondizioni diverse (pre(inc-a) = {pa, (>= (x) 0)} e pre(inc-b) = {pb, (>= (x) 0)}).
  ; Questo setup viola la clausola di implicazione della Definizione 6 (Interference-free Problem):
  ; Se 'set-pa' (o 'set-pb') interferisce con 'inc-b' (o 'inc-a'), non è vero che pre(a_i) implica pre(a_j),
  ; rendendo il dominio Non-IF.
)