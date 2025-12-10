(define (domain h1_if_coachievers)
  (:requirements :numeric-fluents)
  (:functions (x))

  (:action inc1
    :parameters ()
    :precondition (and)
    :effect (increase (x) 1)
  )

  (:action inc2
    :parameters ()
    :precondition (and)
    :effect (increase (x) 2)
  )
)
