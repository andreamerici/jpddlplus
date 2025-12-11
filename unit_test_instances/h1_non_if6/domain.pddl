(define (domain h1_non_if)
  (:requirements :numeric-fluents)
  (:functions (y))

  (:action inc-a
    :parameters ()
    :precondition (and (>= (y) 0))
    :effect (increase (y) 2)
  )

  (:action dec-b
    :parameters ()
    :precondition (and (>= (y) 3))
    :effect (decrease (y) 1)
  )
)