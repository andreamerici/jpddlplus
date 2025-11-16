(define (domain h1_non_if)
  (:requirements :typing :numeric-fluents)
  (:predicates (pa) (pb))
  (:functions (x))

  (:action set-pa
    :parameters ()
    :precondition (and)
    :effect (pa)
  )

  (:action set-pb
    :parameters ()
    :precondition (and)
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
