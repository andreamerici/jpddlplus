(define (problem h1_if_p)
  (:domain h1_if)
  (:objects)                  ; Definisce gli oggetti presenti nel problema (nessuno in questo caso).

  (:init                       ; Stato Iniziale del problema.
    (= (x) 0)                 ; Inizializza 'x' al valore 0. Il fatto 'p' è implicitamente falso.
  )

  (:goal                       ; Condizione Obiettivo del problema.
    (>= (x) 2)                ; L'obiettivo è raggiungere uno stato in cui 'x' sia maggiore o uguale a 2.
  )
)
