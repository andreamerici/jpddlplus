(define (domain h1_non_if)
  (:requirements :numeric-fluents)

  (:functions (x))

  (:action inc2
    :parameters ()
    :precondition (and (>= (x) 0))
    :effect (increase (x) 2)
  )

  (:action inc1_req5
    :parameters ()
    :precondition (and (>= (x) 5))
    :effect (increase (x) 1)
  )
)
