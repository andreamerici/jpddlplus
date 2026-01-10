(define (problem h1_non_if18)
  (:domain h1_non_if18)
  (:init
    (= (total-items) 0)
    (= (raw-material) 100)
    (machine-hot)
  )
  (:goal (>= (total-items) 50))
)