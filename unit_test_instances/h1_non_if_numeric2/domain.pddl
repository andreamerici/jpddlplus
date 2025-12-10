(define (domain h1_non_if_numeric2)
  (:requirements :numeric-fluents)
  (:functions (x))

  (:action inc-a
    :parameters ()
    :precondition (and (>= (x) 0))
    :effect (increase (x) 3)
  )

  (:action inc-b
    :parameters ()
    :precondition (and (>= (x) 5))
    :effect (increase (x) 3)
  )
)
