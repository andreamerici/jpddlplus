(define (domain h1_if)
  (:requirements :typing)

  (:predicates (p) (q))

  (:action set-p1
    :parameters ()
    :precondition ()
    :effect (and (p) (q))
  )

  (:action keep-p
    :parameters ()
    :precondition (and (q))
    :effect (p)
  )

)
