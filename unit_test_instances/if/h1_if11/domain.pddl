(define (domain h1_if)
  (:requirements :numeric-fluents)

  (:predicates (p))
  (:functions (x) (y))

  (:action enable
    :parameters ()
    :effect (p)
  )

  (:action inc-x
    :parameters ()
    :precondition (and (p))
    :effect (increase (x) 1)
  )

  (:action inc-y
    :parameters ()
    :precondition (and (p))
    :effect (increase (y) 1)
  )
)
