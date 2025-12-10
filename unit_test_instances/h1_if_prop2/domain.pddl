(define (domain h1_if_prop2)

  (:predicates (p) (q))

  (:action make-p
    :parameters ()
    :precondition (and)
    :effect (p)
  )

  (:action use-p
    :parameters ()
    :precondition (p)
    :effect (q)
  )
)
