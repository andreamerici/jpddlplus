(define (domain h1_non_if)
(:requirements :numeric-fluents)
  (:functions (x) (y))

  (:action a1
    :precondition (>= (x) 0)
    :effect (increase (x) 5))

  (:action a2
    :parameters ()
    :precondition (and (>= (x) 10) (>= (y) 1))
    :effect (increase (x) 10))
)