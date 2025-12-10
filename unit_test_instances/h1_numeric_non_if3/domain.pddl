(define (domain h1_numeric_non_if3)
; Non-IF perché dec-x peggiora una precondizione numerica di need-x-nonneg
; e pre(dec-x) = {ok} NON implica pre(need-x-nonneg) = {ok, (>= (x) 0)}.
  (:requirements :typing :numeric-fluents)

  (:predicates (ok) (g))
  (:functions (x))

  (:action set-ok
    :parameters ()
    :precondition (and)
    :effect (ok)
  )

  ; Azione che peggiora una precondizione numerica altrui (v < 0)
  (:action dec-x
    :parameters ()
    :precondition (and (ok))
    :effect (decrease (x) 1)
  )

  ; Azione che richiede (>= (x) 0): soggetta a interferenza da dec-x
  (:action need-x-nonneg
    :parameters ()
    :precondition (and (ok) (>= (x) 0))
    :effect (g)
  )
)