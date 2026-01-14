(define (domain h1_if16)
  (:requirements :numeric-fluents)
  (:functions (fuel) (level))

  ; Achiever 1: Precondizione base (level >= 1)
  (:action refuel-low
    :parameters ()
    :precondition (>= (level) 1)
    :effect (increase (fuel) 50))

  ; Achiever 2: Precondizione (level >= 2) -> contiene (level >= 1)
  (:action refuel-mid
    :parameters ()
    :precondition (>= (level) 2)
    :effect (increase (fuel) 50))

  ; Achiever 3: Precondizione (level >= 3) -> contiene i precedenti
  (:action refuel-high
    :parameters ()
    :precondition (>= (level) 3)
    :effect (increase (fuel) 50))
)