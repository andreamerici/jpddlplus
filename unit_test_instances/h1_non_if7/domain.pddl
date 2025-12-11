(define (domain h1_non_if)
  (:requirements :numeric-fluents)

  (:functions (x) (y))

  (:action set-a
    :parameters ()
    :precondition (and (>= (y) 10))
    :effect (assign (x) 6) ; Abilita inc-b
  )

  (:action inc-b
    :parameters ()
    :precondition (and (>= (x) 5))
    :effect (increase (y) 1) ; Modifica precondizione di set-a
  )
)