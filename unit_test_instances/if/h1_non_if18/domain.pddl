(define (domain h1_non_if18)
  (:requirements :numeric-fluents :typing)
  (:predicates
    (parts-available)
    (machine-hot)
    (quality-check-passed))
  (:functions
    (total-items)
    (raw-material))

  ;; a_i: Achiever di total-items e IAch di pre(a_j)
  (:action prepare-parts
    :precondition (>= (raw-material) 5)
    :effect (and
        (parts-available)
        (increase (total-items) 1) ;; Contribuisce all'obiettivo
        (decrease (raw-material) 5)))

  ;; a_j: Achiever dello stesso obiettivo numerico
  (:action assemble-product
    :precondition (parts-available)
    :effect (and
        (increase (total-items) 10)
        (not (parts-available))))

  ;; Azione ausiliaria per scalabilità
  (:action calibrate-machine
    :precondition (machine-hot)
    :effect (increase (total-items) 2))
)