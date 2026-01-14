(define (domain h1_non_if)
  (:requirements :numeric-fluents)
  (:functions (x) (y) (z))

  (:action a1
    :parameters ()
    :precondition (>= (x) 0)
    :effect (and (increase (x) 1) (increase (y) 1)))

  (:action a2
    :parameters ()
    :precondition (>= (z) 100)
    :effect (and (increase (x) 10) (increase (y) 10)))
)