(define (domain h1_non_if)
  (:requirements :numeric-fluents)

  (:functions (x) (y))

  (:action dec-x
    :parameters ()
    :precondition (and)
    :effect (decrease (x) 1)
  )

  (:action need-x-ge-1
    :parameters ()
    :precondition (and (>= (x) 1))
    :effect (increase (y) 1)
  )
)
