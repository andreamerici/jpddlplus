(define (domain h1_if)
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
    :precondition (and (pa) (>= (x) 1))
    :effect (increase (x) 1)
  )

  (:action inc-b                    ; Azione 4: Incrementa 'x'.
    :parameters ()
    :precondition (and (pb) (>= (x) 1))
    :effect (increase (x) 1)
  )
)