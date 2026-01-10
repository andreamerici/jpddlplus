(define (domain h1_if17)
  (:requirements :numeric-fluents)
  (:predicates (mode-auto) (mode-manual) (sensor-on))
  (:functions (vtotal) (temp))

  (:action boost-auto
    :precondition (and (mode-auto) (sensor-on))
    :effect (increase (vtotal) 10))

  (:action boost-manual
    :precondition (mode-manual)
    :effect (increase (vtotal) 10))

  (:action boost-emergency
    :precondition (>= (temp) 100)
    :effect (increase (vtotal) 10))
)