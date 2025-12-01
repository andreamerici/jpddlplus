(define (problem h1_non_if_p)
  (:domain h1_non_if)
  (:objects)                   ; Non ci sono oggetti definiti in questa istanza.

  (:init                        ; Stato Iniziale del problema.
    (= (x) 0)                  ; Inizializza la variabile numerica 'x' al valore 0. I fatti 'pa' e 'pb' sono falsi.
  )

  (:goal                        ; Condizione Obiettivo del problema.
    (>= (x) 2)                 ; L'obiettivo è portare la variabile 'x' a un valore maggiore o uguale a 2.
  )
)
