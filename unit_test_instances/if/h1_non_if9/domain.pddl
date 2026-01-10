(define (domain h1_non_if)
  (:requirements :numeric-fluents)
  (:functions (x))

  (:action inc-a
    :parameters ()
    :precondition (and (>= (x) 0))
    :effect (increase (x) 1)
  )

  (:action inc-b
    :parameters ()
    :precondition (and (>= (x) 0))
    :effect (increase (x) 2)
  )
)
