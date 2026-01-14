(define (domain h1_non_if)
  (:requirements :numeric-fluents)
  (:functions (x) (y) (z))

  (:action a1
    :parameters ()
    :precondition (>= (x) 0)
    :effect (increase (x) 1))

  (:action a2
    :parameters ()
    :precondition (and (>= (y) 10) (<= (z) 5))
    :effect (increase (x) 5))
)