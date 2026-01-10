(define (domain h1_if1)
  (:requirements :typing :numeric-fluents)

  (:predicates (p))

  (:functions (x))

  (:action set-p                        ; Azione 1: Abilita 'inc-x'.
    :parameters ()
    :effect (p)
  )

  (:action inc-x                        ; Azione 2: Modifica 'x'.
    :parameters ()
    :precondition (p)
    :effect (increase (x) 1)
  )
)