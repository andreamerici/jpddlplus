(define (domain h1_non_if)
  (:requirements :numeric-fluents)
  (:functions (x))

  (:action dec2
    :parameters ()
    :precondition (and)
    :effect (decrease (x) 2)
  )

  (:action need-gt3
    :parameters ()
    :precondition (and (> (x) 3))
    :effect (increase (x) 0)
  )
)
