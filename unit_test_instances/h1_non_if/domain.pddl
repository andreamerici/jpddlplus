(define (domain h1_non_if)
; Le azioni 'inc-a' e 'inc-b' sono entrambe Achievers (Ach) per la condizione numerica (>= (x) 0).
; Tuttavia, hanno precondizioni diverse (pre(inc-a) = {pa, (>= (x) 0)} e pre(inc-b) = {pb, (>= (x) 0)}).
; Questo setup viola la clausola di implicazione della Definizione 6:
; Se 'set-pa' (o 'set-pb') interferisce con 'inc-b' (o 'inc-a'), non è vero che pre(a_i) implica pre(a_j),
; rendendo il dominio Non-IF.
  (:requirements :typing :numeric-fluents)

  (:predicates (pa) (pb))

  (:functions (x))

  (:action set-pa                    ; Azione 1: Abilita 'inc-a'.
    :parameters ()
    :precondition (and)
    :effect (pa)
  )

  (:action set-pb                    ; Azione 2: Abilita 'inc-b'.
    :parameters ()
    :precondition (and)
    :effect (pb)
  )

  (:action inc-a                    ; Azione 3: Incrementa 'x'.
    :parameters ()
    :precondition (and (pa) (>= (x) 0))
    :effect (increase (x) 1)
  )

  (:action inc-b                    ; Azione 4: Incrementa 'x'.
    :parameters ()
    :precondition (and (pb) (>= (x) 0))
    :effect (increase (x) 1)
  )
)