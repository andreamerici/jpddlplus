(define (domain h1_non_if)
  (:requirements :numeric-fluents)
  (:functions (x) (y) (z) (w))

  (:action a1
    :parameters ()
    :precondition (>= (x) 5)
    :effect (increase (z) 2))

  (:action a2
    :parameters ()
    :precondition (and (>= (x) 10) (>= (w) 80))
    :effect (increase (z) 20))

  (:action a3
    :parameters ()
    :precondition (>= (z) 50)
    :effect (increase (w) 100))
)