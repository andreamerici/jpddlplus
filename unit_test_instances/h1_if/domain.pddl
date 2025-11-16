(define (domain h1_if)
  (:requirements :typing :numeric-fluents)
  (:predicates (p))
  (:functions (x))

  (:action set-p
    :parameters ()
    :effect (p)
  )

  (:action inc-x
    :parameters ()
    :precondition (p)
    :effect (increase (x) 1)
  )
)
