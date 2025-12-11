(define (domain h1_if)
  (:requirements :numeric-fluents)

  (:predicates (pa) (pb) (pc))
  (:functions (x))

  (:action inc
    :parameters ()
    :precondition (and (pa))
    :effect (increase (x) 1)
  )

  (:action dec1
    :parameters ()
    :precondition (and (pb))
    :effect (decrease (x) 1)
  )

  (:action dec2
    :parameters ()
    :precondition (and (pc))
    :effect (decrease (x) 2)
  )
)
