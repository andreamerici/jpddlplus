(define (problem h1_non_if12)
  (:domain h1_non_if12)
  (:init
    (= (potenza-totale) 0)
    (modulo-ausiliario-attivo)
  )
  (:goal
    (>= (potenza-totale) 20)
  )
)