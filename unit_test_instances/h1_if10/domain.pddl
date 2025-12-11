(define (domain h1_if)
  (:requirements :numeric-fluents)

  (:predicates (p))
  (:functions (x))

  (:action enable-p
    :parameters ()
    :effect (p)
  )

  (:action inc1
    :parameters ()
    :precondition (and (p))
    :effect (increase (x) 1)
  )

  (:action inc2
    :parameters ()
    :precondition (and (p))
    :effect (increase (x) 1)
  )
)
