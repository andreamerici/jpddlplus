(define (domain h1_non_if12)
  (:requirements :numeric-fluents :strips)
  (:predicates
    (energia-accumulata)
    (sistema-pronto)
    (modulo-ausiliario-attivo)
  )
  (:functions
    (potenza-totale)
  )

  ;; Azione a_i: Achiever diretto del goal E achiever indiretto per a_j
  (:action carica-batteria-principale
    :parameters ()
    :precondition ()
    :effect (and
        (increase (potenza-totale) 10)
        (energia-accumulata)
    )
  )

  ;; Azione ponte: Necessaria per rendere a_i un achiever indiretto di a_j
  (:action preparazione-sistema
    :parameters ()
    :precondition (energia-accumulata)
    :effect (sistema-pronto)
  )

  ;; Azione a_j: Altro achiever diretto del goal, dipende dalla catena di a_i
  (:action attivazione-generatore-secondario
    :parameters ()
    :precondition (sistema-pronto)
    :effect (increase (potenza-totale) 10)
  )

  ;; Azione extra per aumentare la complessità (non interferente con le precedenti)
  (:action attiva-pannelli-solari
    :parameters ()
    :precondition (modulo-ausiliario-attivo)
    :effect (increase (potenza-totale) 5)
  )
)