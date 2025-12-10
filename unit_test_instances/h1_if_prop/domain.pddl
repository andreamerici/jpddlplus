(define (domain h1_if_prop)
  (:requirements :typing :numeric-fluents)

  (:predicates (p) (q))

  (:action set-p
    :parameters ()
    :precondition (and)
    :effect (p)
  )

  (:action use-p
    :parameters ()
    :precondition (and (p))
    :effect (q)
  )
)