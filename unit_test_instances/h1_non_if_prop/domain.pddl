(define (domain h1_non_if_prop)
; Dominio proposizionale Non-IF:
; - set-p1 e keep-p sono co-achievers della stessa condizione p.
; - p è anche precondizione di keep-p.
;   La regola IF richiede che pre(set-p1) => pre(keep-p), ma {} non implica {p}.
;   Quindi il dominio deve essere rilevato come non Interference-Free.
  (:requirements :typing)

  (:predicates (p) (q))

  (:action set-p1
    :parameters ()
    :precondition ()
    :effect (and (p) (q))
  )

  (:action keep-p
    :parameters ()
    :precondition (and (q))
    :effect (p)
  )

)
