(define (domain h1_if)

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
