(define (domain h1_non_if13)
  (:requirements :numeric-fluents :strips)
  (:predicates (dam-permit) (reactor-safe) (water-cooling-active) (grid-connected))
  (:functions (energy-produced))

  (:action open-dam
    :parameters ()
    :precondition (dam-permit)
    :effect (and (increase (energy-produced) 50)
                 (water-cooling-active)))

  (:action run-nuclear
    :parameters ()
    :precondition (and (reactor-safe) (water-cooling-active))
    :effect (increase (energy-produced) 100))

  (:action run-solar
    :parameters ()
    :precondition (grid-connected)
    :effect (increase (energy-produced) 20))
)