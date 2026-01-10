(define (domain h1_if18)
  (:requirements :numeric-fluents :typing)
  (:predicates
    (pump-ready)
    (manual-override)
    (emergency-mode))
  (:functions
    (water-level)
    (energy-reserve))

  ;; Azione 1: Achiever diretto di water-level
  (:action activate-pump
    :precondition (and (pump-ready) (>= (energy-reserve) 10))
    :effect (increase (water-level) 15))

  ;; Azione 2: Achiever diretto di water-level
  (:action natural-inflow
    :precondition (manual-override)
    :effect (increase (water-level) 5))

  ;; Azione 3: Non influisce su water-level, quindi non crea interferenza
  ;; anche se è IAch per activate-pump
  (:action charge-battery
    :effect (increase (energy-reserve) 20))
)