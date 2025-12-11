(define (domain h1_non_if)
  (:requirements :numeric-fluents)

  (:functions (z))

  (:action inc-a
    :parameters ()
    :precondition (and (>= (z) 0))
    :effect (increase (z) 1) ; Modifica la precondizione di do-b
  )

  (:action do-b
    :parameters ()
    :precondition (and (= (z) 10)) ; Richiede z=10
    :effect (assign (z) 0)
  )
)