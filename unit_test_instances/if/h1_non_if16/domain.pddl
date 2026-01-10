(define (domain h1_non_if16)
  (:requirements :numeric-fluents :strips)
  (:predicates (fuel-at-airport) (truck-ready) (plane-ready) (weather-clear))
  (:functions (packages-delivered))

  (:action truck-deliver
    :precondition (truck-ready)
    :effect (and (increase (packages-delivered) 5) (fuel-at-airport)))

  (:action plane-deliver
    :precondition (and (plane-ready) (fuel-at-airport) (weather-clear))
    :effect (increase (packages-delivered) 20))
)