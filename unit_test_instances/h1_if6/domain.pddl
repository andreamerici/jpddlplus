(define (domain h1_if)
  (:requirements :numeric-fluents)

  (:predicates (p))
  (:functions (x))

  (:action enable-p
    :parameters ()
    :effect (p)
  )

  (:action inc-x
    :parameters ()
    :precondition (and (p))
    :effect (increase (x) 1)
  )

  (:action dec-x
    :parameters ()
    :precondition (and (p))
    :effect (decrease (x) 1)
  )
)
