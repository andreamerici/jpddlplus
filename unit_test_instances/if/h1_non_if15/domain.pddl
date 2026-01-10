(define (domain h1_non_if15)
  (:requirements :numeric-fluents :strips)
  (:predicates (chem-bond) (safety-valve-open) (mixer-on))
  (:functions (temperature) (pressure))

  (:action add-catalyst
    :precondition (mixer-on)
    :effect (and (increase (temperature) 20) (chem-bond)))

  (:action react-b
    :precondition (and (chem-bond) (safety-valve-open))
    :effect (increase (temperature) 80))

  (:action stabilize
    :precondition (>= (temperature) 50)
    :effect (decrease (pressure) 10))
)