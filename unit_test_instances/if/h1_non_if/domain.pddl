(define (domain h1_non_if)
  (:requirements :numeric-fluents :typing)
  (:types generatore batteria)

  (:functions
    (accumulo ?b - batteria)
    (gen ?g - generatore))

  (:action carica_standard
    :parameters (?g - generatore ?b - batteria)
    :precondition (>= (accumulo ?b) 0)
    :effect (increase (accumulo ?b) 10)
  )

  (:action carica_acc
    :parameters (?g - generatore ?b - batteria)
    :precondition (>= (gen ?g) 50)
    :effect (increase (accumulo ?b) 25)
  )
)