(define (domain h1_if)
  (:requirements :numeric-fluents)

  (:predicates (pa) (pb))
  (:functions (x))

  (:action set-pa
    :parameters ()
    :effect (pa)
  )

  (:action set-pb
    :parameters ()
    :effect (pb)
  )

  (:action inc-a
    :parameters ()
    :precondition (and (pa) (>= (x) 0))
    :effect (increase (x) 1)
  )

  (:action inc-b
    :parameters ()
    :precondition (and (pb) (>= (x) 0))
    :effect (increase (x) 1)
  )
)
