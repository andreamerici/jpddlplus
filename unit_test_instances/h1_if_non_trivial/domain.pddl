(define (domain h1_if_non_trivial)

  (:predicates (pa) (pb))

  (:functions (x))

  (:action set-pa                    ; Azione 1: Abilita 'inc-a'.
    :parameters ()
    :precondition ()
    :effect (pa)
  )

  (:action set-pb                    ; Azione 2: Abilita 'inc-b'.
    :parameters ()
    :precondition ()
    :effect (pb)
  )

  (:action inc-a
    :parameters ()
    :precondition ()
    :effect (increase (x) 1)
  )

  (:action inc-b                    ; Azione 4: Incrementa 'x'.
    :parameters ()
    :precondition (and (pb))
    :effect (increase (x) 2)
  )
)